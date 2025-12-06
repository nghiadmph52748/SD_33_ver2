from fastapi import APIRouter, HTTPException
from fastapi.responses import StreamingResponse
from pydantic import BaseModel
from app.utils.llm_client import llm_client
from app.utils.database import db_client
import logging
from datetime import datetime, timedelta
import json
import re

logger = logging.getLogger(__name__)
router = APIRouter(prefix="/admin", tags=["Admin Chatbot"])

class ChatRequest(BaseModel):
    message: str
    context: str = ""

class ChatResponse(BaseModel):
    message: str
    sources: str = ""
    query_type: str = ""
    data_source: str = ""
    follow_up_suggestions: list[str] = []
    data_context: dict = {}

# Admin Manager System Prompt
ADMIN_SYSTEM_PROMPT = """
Bạn là Trợ lý Quản trị (Admin Manager) của GearUp – nền tảng quản lý vận hành chuỗi cửa hàng giày.

**Quy tắc ngôn ngữ tối quan trọng**
- Trả lời 100% bằng TIẾNG VIỆT chuẩn, không dùng ký tự Trung Quốc.
- Giữ ngắn gọn, chính xác; nếu thiếu dữ liệu hãy nêu rõ "Không có dữ liệu trong hệ thống".
- Nếu người dùng đặt câu hỏi bằng ngôn ngữ khác, hãy hiểu ý và trả lời lại hoàn toàn bằng tiếng Việt.
- Trước khi hoàn tất, tự kiểm tra câu trả lời: nếu còn ký tự/đoạn không phải tiếng Việt thì phải chuyển sang tiếng Việt.

**Vai trò & Mục tiêu**
- Giám sát hoạt động bán hàng, tồn kho, khách hàng, chiến dịch và hiệu suất nhân viên.
- Hỗ trợ quản lý đưa ra quyết định hành động nhanh (nhập hàng, khuyến mãi, phân công nhân sự…).
- Luôn dựa trên dữ liệu được cung cấp, không suy diễn ngoài phạm vi.

**Cấu trúc câu trả lời**
1. **Tiêu đề:** Emoji + cụm tối đa 8 từ.
2. **Tóm tắt nhanh:** 1 câu nêu insight chính (không quá 20 từ).
3. **Khối dữ liệu:** 
   - Nếu ≥ 2 dòng, trình bày bằng bảng Markdown.
   - Định dạng số: 12,500,000 VNĐ · 1,234 đơn · 45.6%.
4. **Rủi ro / Cảnh báo:** Chỉ có khi phát hiện vấn đề (dùng "⚠️ …").
5. **Hành động quản trị:** 1 câu bắt đầu bằng "→" đưa ra đề xuất cụ thể.

**Phong cách**
- Chuyên nghiệp như quản lý vận hành: rõ ràng, quyết đoán, ưu tiên KPI, không kể lể quy trình phân tích.
- Sử dụng emoji hợp lý (📊💰⚠️👥🛒🎯📦🧾).
- Không dùng các cụm mơ hồ như "có vẻ", "có thể"; hãy khẳng định khi dữ liệu rõ ràng.

**Giới hạn**
- Tối đa 250 từ.
- Danh sách tối đa 5 mục.
- Nếu câu hỏi ngoài phạm vi bán lẻ, hãy từ chối lịch sự bằng tiếng Việt.

**Ví dụ rút gọn**
📊 **Top sản phẩm 30 ngày**

| # | Sản phẩm | Đã bán | Doanh thu |
|---|---|---:|---:|
| 1 | Nike Air Max 2024 | 156 đôi | 45,600,000 VNĐ |
| 2 | Adidas Ultra Boost | 134 đôi | 38,900,000 VNĐ |

→ Bổ sung thêm 80 đôi Nike Air Max cho tuần tới để không hụt doanh thu.
"""

def detect_admin_intent(message: str) -> str:
    """Detect admin intent from message"""
    message_lower = message.lower()
    
    # Check low_stock first (higher priority)
    if any(word in message_lower for word in ["hết hàng", "sắp hết", "tồn kho thấp", "tồn kho", "stock", "còn lại", "số lượng còn"]):
        return "low_stock"
    elif any(word in message_lower for word in ["tạo mã giảm", "tạo voucher", "tạo phiếu giảm", "create voucher", "coupon", "mã khuyến mãi"]):
        return "create_voucher"
    elif any(word in message_lower for word in ["bán chạy", "top", "phổ biến", "best seller"]):
        return "top_products"
    elif any(word in message_lower for word in ["doanh thu", "revenue", "tổng tiền", "thu nhập"]):
        return "revenue"
    elif any(word in message_lower for word in ["trạng thái đơn", "status", "đơn hàng", "orders"]):
        return "order_status"
    elif any(word in message_lower for word in ["khách hàng vip", "top khách hàng", "chi tiêu"]):
        return "customers_detail"
    elif any(word in message_lower for word in ["giảm giá", "đợt giảm", "campaign", "voucher", "phiếu giảm"]):
        return "discounts"
    elif any(word in message_lower for word in ["nhân viên", "employee", "nhan vien"]):
        return "employees"
    elif any(word in message_lower for word in ["màu sắc", "color", "màu", "mau"]):
        return "products_color"
    elif any(word in message_lower for word in ["kích thước", "size", "kich thuoc"]):
        return "products_size"
    elif any(word in message_lower for word in ["online", "tại quầy", "pos", "kênh"]):
        return "channel_dist"
    elif any(word in message_lower for word in ["khách hàng", "customer", "vip", "người mua"]):
        return "customers"
    else:
        return "general"

def get_data_source_info(intent: str) -> str:
    """Get data source information for each intent"""
    sources = {
        "top_products": "Dữ liệu bán hàng 30 ngày gần nhất",
        "revenue": "Dữ liệu doanh thu và đơn hàng 30 ngày gần nhất",
        "low_stock": "Dữ liệu tồn kho thời gian thực",
        "order_status": "Dữ liệu đơn hàng hiện tại",
        "customers_detail": "Dữ liệu khách hàng toàn hệ thống",
        "discounts": "Dữ liệu campaign đang hoạt động",
        "employees": "Dữ liệu hiệu suất nhân viên",
        "products_color": "Dữ liệu bán hàng theo màu sắc",
        "products_size": "Dữ liệu bán hàng theo size",
        "channel_dist": "Dữ liệu kênh bán hàng (Online vs POS)",
        "create_voucher": "Hệ thống quản lý voucher",
    }
    return sources.get(intent, "Dữ liệu hệ thống GearUp")

def get_follow_up_suggestions_llm(user_question: str, ai_response: str, intent: str) -> list[str]:
    """Generate contextual follow-up suggestions using LLM"""
    try:
        prompt = f"""Dựa trên cuộc trò chuyện sau, hãy đề xuất 3 câu hỏi tiếp theo ngắn gọn, liên quan và hữu ích cho quản trị viên cửa hàng GearUp.

Câu hỏi của admin: {user_question}
Phản hồi của AI: {ai_response[:300]}...
Chủ đề: {intent}

Yêu cầu:
- 3 câu hỏi ngắn gọn, súc tích (tối đa 50 ký tự mỗi câu)
- Liên quan trực tiếp đến dữ liệu vừa được hiển thị
- Tập trung vào phân tích sâu hơn, so sánh, hoặc hành động tiếp theo
- Sử dụng ngôn ngữ quản trị (doanh thu, tồn kho, nhân viên, khách hàng)
- Chỉ trả lời 3 câu hỏi, mỗi câu trên 1 dòng, không có số thứ tự hoặc ký tự đầu dòng

Ví dụ format trả lời:
So sánh với tháng trước?
Phân tích theo kênh bán?
Tạo mã giảm giá?"""

        messages = [{"role": "user", "content": prompt}]
        response = llm_client.chat(messages, temperature=0.7, max_tokens=150, stream=False)
        
        # Parse response into list of suggestions
        suggestions_text = response.strip()
        suggestions = [s.strip() for s in suggestions_text.split('\n') if s.strip()]
        
        # Filter out numbering, bullets, and ensure max 3 suggestions
        cleaned_suggestions = []
        for s in suggestions[:5]:  # Take max 5 in case of extras
            # Remove common prefixes like "1.", "-", "*", etc.
            cleaned = re.sub(r'^[\d\.\-\*\+]+\s*', '', s).strip()
            if cleaned and len(cleaned) <= 100:  # Reasonable length
                cleaned_suggestions.append(cleaned)
        
        # Return exactly 3 suggestions
        return cleaned_suggestions[:3] if len(cleaned_suggestions) >= 3 else cleaned_suggestions + get_fallback_suggestions(intent)[:3-len(cleaned_suggestions)]
    
    except Exception as e:
        logger.error(f"Failed to generate LLM suggestions: {e}")
        return get_fallback_suggestions(intent)

def get_fallback_suggestions(intent: str) -> list[str]:
    """Fallback suggestions when LLM fails"""
    suggestions = {
        "top_products": [
            "So sánh với tháng trước?",
            "Phân tích theo kênh bán?",
            "Tồn kho còn bao nhiêu?"
        ],
        "revenue": [
            "Khách VIP chi tiêu nhiều?",
            "Phân tích theo kênh?",
            "Nhân viên bán tốt nhất?"
        ],
        "low_stock": [
            "Sản phẩm nào cần nhập?",
            "Doanh thu các sản phẩm?",
            "Tạo mã xả hàng?"
        ],
        "order_status": [
            "Đơn chờ xác nhận?",
            "Doanh thu đơn hoàn thành?",
            "Nhân viên xử lý nhiều?"
        ],
    }
    return suggestions.get(intent, [
        "Top sản phẩm bán chạy?",
        "Doanh thu tháng này?",
        "Tồn kho sắp hết?"
    ])

def query_database(intent: str, message: str) -> str:
    """Query database based on intent"""
    try:
        # ===== Helpers =====
        def fmt_number(n):
            try:
                return f"{(0 if n is None else float(n)) :,.0f}"
            except Exception:
                return str(n)

        def fmt_datetime(dt):
            if isinstance(dt, datetime):
                return dt.strftime("%Y-%m-%d %H:%M:%S")
            return str(dt) if dt is not None else ""

        if intent == "top_products":
            products = db_client.get_top_selling_products(limit=5, days=30)
            
            if not products:
                return "Không có dữ liệu sản phẩm trong 30 ngày qua."
            
            context = "📊 **Top 5 sản phẩm bán chạy nhất (30 ngày qua)**\n\n"
            context += "| # | Sản phẩm | Đã bán | Doanh thu (VNĐ) |\n|---:|---|---:|---:|\n"
            for i, p in enumerate(products, 1):
                total_sold = p.get('total_sold') or 0
                total_revenue = p.get('total_revenue') or 0
                context += f"| {i} | {p['product_name']} | {fmt_number(total_sold)} | {fmt_number(total_revenue)} |\n"
            
            return context
        
        elif intent == "revenue":
            summary = db_client.get_revenue_summary(days=30)
            
            if not summary:
                return "Không có dữ liệu doanh thu."
            
            # Handle None values from database
            total_orders = summary.get('total_orders') or 0
            total_revenue = summary.get('total_revenue') or 0
            avg_order_value = summary.get('avg_order_value') or 0
            unique_customers = summary.get('unique_customers') or 0
            
            context = "💰 **Tổng quan doanh thu (30 ngày qua):**\n\n"
            context += f"- Tổng đơn hàng: {total_orders} đơn\n"
            context += f"- Tổng doanh thu: {total_revenue:,.0f} VNĐ\n"
            context += f"- Giá trị đơn trung bình: {avg_order_value:,.0f} VNĐ\n"
            context += f"- Số khách hàng: {unique_customers} người\n"
            
            return context
        
        elif intent == "low_stock":
            products = db_client.get_low_stock_products(threshold=10)
            
            if not products:
                return "✅ Tất cả sản phẩm đều còn đủ hàng."
            
            context = "⚠️ **Sản phẩm sắp hết hàng (≤10 đôi)**\n\n"
            context += "| # | Sản phẩm | Biến thể | Màu | Size | Tồn kho |\n|---:|---|---|---|---:|---:|\n"
            for i, p in enumerate(products, 1):
                variant = p.get('variant_id') or '-'
                color = p.get('color') or ''
                size = p.get('size') or ''
                context += (
                    f"| {i} | {p['product_name']} | {variant} | {color} | {size} | {p['stock_quantity']} |\n"
                )
            
            return context
        
        elif intent == "order_status":
            statuses = db_client.get_order_status_distribution()
            
            if not statuses:
                return "Không có dữ liệu trạng thái đơn hàng."
            
            context = "📋 **Phân bố trạng thái đơn hàng**\n\n"
            context += "| Trạng thái | Số đơn |\n|---|---:|\n"
            for status in statuses:
                context += f"| {status['status_name']} | {fmt_number(status['order_count'])} |\n"
            
            return context
        
        elif intent == "customers_detail":
            customers = db_client.get_top_customers_by_spending(limit=5)
            
            if not customers:
                return "Không có dữ liệu khách hàng."
            
            context = "👥 **Top 5 khách hàng chi tiêu nhiều nhất**\n\n"
            context += "| # | Khách hàng | Mã | Số đơn | Tổng chi tiêu (VNĐ) |\n|---:|---|---|---:|---:|\n"
            for i, c in enumerate(customers, 1):
                total_spent = c.get('total_spent') or 0
                total_orders = c.get('total_orders') or 0
                context += f"| {i} | {c['customer_name']} | {c['customer_code']} | {fmt_number(total_orders)} | {fmt_number(total_spent)} |\n"
            
            return context
        
        elif intent == "discounts":
            campaigns = db_client.get_active_discount_campaigns()
            
            if not campaigns:
                return "📌 Hiện tại không có đợt giảm giá nào đang hoạt động."
            
            context = "🎉 **Đợt giảm giá đang hoạt động**\n\n"
            context += "| Mã | Tên đợt | Giảm | Bắt đầu | Kết thúc |\n|---|---|---:|---|---|\n"
            for campaign in campaigns:
                discount_value = campaign.get('discount_value') or 0
                context += (
                    f"| {campaign['campaign_code']} | {campaign['campaign_name']} | {fmt_number(discount_value)}% | "
                    f"{fmt_datetime(campaign['start_date'])} | {fmt_datetime(campaign['end_date'])} |\n"
                )
            
            return context
        
        elif intent == "create_voucher":
            # naive parsing: look for % or đ numbers and optional date range
            name = "Voucher tự động"
            percent = True
            value = 10.0
            min_order = 0.0
            usage_limit = 1
            description = "Tạo bởi AI"
            # value percent like 10% or 15 %
            m = re.search(r"(\d{1,2})\s*%", message)
            if m:
                percent = True
                value = float(m.group(1))
            else:
                m2 = re.search(r"(\d{5,})\s*(vnđ|vnd|đ)?", message, re.IGNORECASE)
                if m2:
                    percent = False
                    value = float(m2.group(1))
            # min order
            mo = re.search(r"tối thiểu\s*(\d{5,})", message)
            if mo:
                min_order = float(mo.group(1))
            # usage limit
            ul = re.search(r"(\d+)\s*(lần|uses)", message)
            if ul:
                usage_limit = int(ul.group(1))
            # dates (fallback today..+30d)
            start_dt = datetime.now()
            end_dt = start_dt + timedelta(days=30)
            sd = re.search(r"(\d{4}-\d{2}-\d{2})", message)
            ed = re.findall(r"(\d{4}-\d{2}-\d{2})", message)
            if ed and len(ed) >= 2:
                try:
                    start_dt = datetime.fromisoformat(ed[0])
                    end_dt = datetime.fromisoformat(ed[1]) + timedelta(hours=23, minutes=59, seconds=59)
                except Exception:
                    pass
            start_iso = start_dt.strftime("%Y-%m-%d %H:%M:%S")
            end_iso = end_dt.strftime("%Y-%m-%d %H:%M:%S")
            # create
            try:
                created = db_client.create_voucher(
                    name=name,
                    percent_type=percent,
                    value=value,
                    start_datetime=start_iso,
                    end_datetime=end_iso,
                    min_order=min_order,
                    usage_limit=usage_limit,
                    description=description,
                )
            except Exception as e:
                return f"❌ Không thể tạo voucher: {str(e)}"
            if not created:
                return "❌ Không thể tạo voucher."
            context = "🎉 **Đã tạo mã giảm giá mới**\n\n"
            context += "| Mã | Tên | Loại | Giá trị | Bắt đầu | Kết thúc | Tối thiểu | Số lượt |\n|---|---|---|---:|---|---|---:|---:|\n"
            loai = "%" if created.get("loai_phieu_giam_gia") == 0 else "VNĐ"
            context += (
                f"| {created.get('ma_phieu_giam_gia')} | {created.get('ten_phieu_giam_gia')} | {loai} | "
                f"{created.get('gia_tri_giam_gia')} | {fmt_datetime(created.get('ngay_bat_dau'))} | "
                f"{fmt_datetime(created.get('ngay_ket_thuc'))} | {fmt_number(created.get('hoa_don_toi_thieu'))} | "
                f"{fmt_number(created.get('so_luong_dung'))} |\n"
            )
            return context
        
        elif intent == "employees":
            employees = db_client.get_employee_sales_performance(limit=5)
            
            if not employees:
                return "Không có dữ liệu nhân viên."
            
            context = "👨‍💼 **Top 5 nhân viên theo doanh thu**\n\n"
            context += "| # | Nhân viên | Mã | Số đơn | Doanh thu (VNĐ) |\n|---:|---|---|---:|---:|\n"
            for i, emp in enumerate(employees, 1):
                total_revenue = emp.get('total_revenue') or 0
                total_orders = emp.get('total_orders') or 0
                context += f"| {i} | {emp['employee_name']} | {emp['employee_code']} | {fmt_number(total_orders)} | {fmt_number(total_revenue)} |\n"
            
            return context
        
        elif intent == "products_color":
            colors = db_client.get_top_product_colors(limit=5)
            
            if not colors:
                return "Không có dữ liệu màu sắc."
            
            context = "🎨 **Top 5 màu sắc bán chạy nhất**\n\n"
            context += "| # | Màu sắc | Đã bán (đôi) |\n|---:|---|---:|\n"
            for i, color in enumerate(colors, 1):
                total_sold = color.get('total_sold') or 0
                context += f"| {i} | {color['color_name']} | {fmt_number(total_sold)} |\n"
            
            return context
        
        elif intent == "products_size":
            sizes = db_client.get_top_product_sizes(limit=5)
            
            if not sizes:
                return "Không có dữ liệu kích thước."
            
            context = "📏 **Top 5 kích thước bán chạy nhất**\n\n"
            context += "| # | Size | Đã bán (đôi) |\n|---:|---|---:|\n"
            for i, size in enumerate(sizes, 1):
                total_sold = size.get('total_sold') or 0
                context += f"| {i} | {size['size_name']} | {fmt_number(total_sold)} |\n"
            
            return context
        
        elif intent == "channel_dist":
            stats = db_client.get_online_vs_pos_stats()
            
            if not stats:
                return "Không có dữ liệu kênh bán hàng."
            
            pos_revenue = stats.get('pos_revenue') or 0
            online_revenue = stats.get('online_revenue') or 0
            pos_orders = stats.get('pos_orders') or 0
            online_orders = stats.get('online_orders') or 0
            
            context = "🛒 **Phân bố kênh bán hàng**\n\n"
            context += "| Kênh | Số đơn | Doanh thu (VNĐ) |\n|---|---:|---:|\n"
            context += f"| Tại quầy (POS) | {fmt_number(pos_orders)} | {fmt_number(pos_revenue)} |\n"
            context += f"| Online | {fmt_number(online_orders)} | {fmt_number(online_revenue)} |\n"
            
            return context
        
        else:
            return (
                "✨ Mình giúp bạn tra nhanh mấy thứ này nè:\n"
                "- Top sản phẩm bán chạy\n"
                "- Doanh thu/đơn hàng\n"
                "- Tồn kho sắp hết\n"
                "- Trạng thái đơn hàng\n"
                "- Top khách hàng chi tiêu\n"
                "- Đợt giảm giá đang chạy\n"
                "- Hiệu suất nhân viên\n"
                "- Màu sắc/size phổ biến\n"
                "- Kênh bán: online vs tại quầy\n"
                "Bạn cần gì nói mình biết, trả lời gọn lẹ ngay! 💬"
            )
    
    except Exception as e:
        logger.error(f"Database query error: {e}")
        return f"Lỗi truy vấn dữ liệu: {str(e)}"

@router.post("/chat", response_model=ChatResponse)
async def chat(request: ChatRequest):
    """Chat with admin AI assistant"""
    try:
        # 1. Detect intent
        intent = detect_admin_intent(request.message)
        logger.info(f"Admin chat - Detected intent: {intent}")
        
        # 2. Query database
        db_context = query_database(intent, request.message)
        
        # 3. Build messages for LLM
        messages = [
            {"role": "system", "content": ADMIN_SYSTEM_PROMPT},
            {"role": "user", "content": f"{request.message}\n\n**Dữ liệu từ hệ thống:**\n{db_context}"}
        ]
        
        # 4. Call LLM
        response = llm_client.chat(
            messages=messages,
            temperature=0.3,
            max_tokens=2000
        )
        
        ai_message = response.choices[0].message.content
        
        # Generate contextual suggestions using LLM
        suggestions = get_follow_up_suggestions_llm(request.message, ai_message, intent)
        
        return ChatResponse(
            message=ai_message,
            sources=get_data_source_info(intent),
            query_type=intent,
            data_source=get_data_source_info(intent),
            follow_up_suggestions=suggestions,
            data_context={
                "time_range": "30_days",
                "channel": "all",
                "timestamp": datetime.now().isoformat()
            }
        )
    
    except Exception as e:
        logger.error(f"Admin chat error: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/chat-stream")
async def chat_stream(request: ChatRequest):
    """Chat with admin AI assistant - Streaming response"""
    try:
        # 1. Detect intent
        intent = detect_admin_intent(request.message)
        logger.info(f"Admin stream - Request: {request.message[:50]}... (intent: {intent})")
        
        # 2. Query database
        db_context = query_database(intent, request.message)
        
        # 3. Build messages for LLM
        messages = [
            {"role": "system", "content": ADMIN_SYSTEM_PROMPT},
            {"role": "user", "content": f"{request.message}\n\n**Dữ liệu từ hệ thống:**\n{db_context}"}
        ]
        
        # 4. Stream generator function
        async def generate():
            full_response = ""  # Collect full response for suggestions
            try:
                # Send initial metadata without suggestions (will send after response completes)
                metadata = {
                    'type': 'start',
                    'intent': intent,
                    'data_source': get_data_source_info(intent),
                    'data_context': {
                        'time_range': '30_days',
                        'channel': 'all'
                    }
                }
                yield f"data: {json.dumps(metadata)}\n\n"
                
                # Call LLM with streaming enabled
                stream = llm_client.chat(
                    messages=messages,
                    temperature=0.3,
                    max_tokens=2000,
                    stream=True
                )
                
                # Stream each chunk (handle both OpenAI and Gemini formats)
                try:
                    for chunk in stream:
                        content = None
                        
                        # Handle OpenAI format (LM Studio)
                        if hasattr(chunk, 'choices') and chunk.choices:
                            if hasattr(chunk.choices[0], 'delta') and hasattr(chunk.choices[0].delta, 'content'):
                                content = chunk.choices[0].delta.content
                        # Handle Gemini format - check multiple possible attributes
                        elif hasattr(chunk, 'text') and chunk.text:
                            content = chunk.text
                        elif hasattr(chunk, 'parts') and chunk.parts:
                            # Gemini sometimes returns parts array
                            for part in chunk.parts:
                                if hasattr(part, 'text') and part.text:
                                    content = part.text
                                    break
                        # Fallback: try to get content directly
                        elif hasattr(chunk, 'content'):
                            content = chunk.content
                        
                        if content:
                            full_response += content  # Collect for suggestions
                            content_event = json.dumps({'type': 'content', 'content': content}, ensure_ascii=False)
                            yield f"data: {content_event}\n\n"
                except StopIteration:
                    # Gemini stream iterator ends with StopIteration - this is normal
                    logger.debug("Stream ended normally (StopIteration)")
                    pass
                except Exception as stream_error:
                    logger.error(f"Error during streaming: {stream_error}", exc_info=True)
                    error_event = json.dumps({'type': 'error', 'error': f'Streaming error: {str(stream_error)}'})
                    yield f"data: {error_event}\n\n"
                
                # Generate contextual suggestions using LLM after response is complete
                if full_response:
                    try:
                        suggestions = get_follow_up_suggestions_llm(request.message, full_response, intent)
                        suggestions_event = json.dumps({
                            'type': 'suggestions',
                            'follow_up_suggestions': suggestions
                        }, ensure_ascii=False)
                        yield f"data: {suggestions_event}\n\n"
                    except Exception as sug_error:
                        logger.error(f"Failed to generate suggestions: {sug_error}")
                        # Send fallback suggestions
                        fallback_suggestions = get_fallback_suggestions(intent)
                        suggestions_event = json.dumps({
                            'type': 'suggestions',
                            'follow_up_suggestions': fallback_suggestions
                        }, ensure_ascii=False)
                        yield f"data: {suggestions_event}\n\n"
                
                # Send end signal
                yield f"data: {json.dumps({'type': 'end'})}\n\n"
                
            except Exception as e:
                logger.error(f"Streaming error: {e}", exc_info=True)
                error_event = json.dumps({'type': 'error', 'error': str(e)})
                yield f"data: {error_event}\n\n"
        
        return StreamingResponse(
            generate(),
            media_type="text/event-stream",
            headers={
                "Cache-Control": "no-cache",
                "Connection": "keep-alive",
                "X-Accel-Buffering": "no"
            }
        )
    
    except Exception as e:
        logger.error(f"Admin chat stream error: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/health")
async def health_check():
    """Check LLM provider connection"""
    is_connected, message = llm_client.test_connection()
    
    provider_info = {
        "provider": llm_client.provider,
        "status": "connected" if is_connected else "disconnected",
        "message": message,
        "model": llm_client.model
    }
    
    # Add provider-specific info
    if llm_client.provider == "lm_studio":
        provider_info["base_url"] = llm_client.client.client.base_url
    elif llm_client.provider == "gemini":
        provider_info["api_key_configured"] = bool(llm_client.client.api_key) if hasattr(llm_client.client, 'api_key') else False
    
    return provider_info






