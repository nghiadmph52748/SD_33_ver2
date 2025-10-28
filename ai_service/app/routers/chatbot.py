from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from app.utils.lm_studio import lm_client
from app.utils.database import db_client
import logging
from datetime import datetime

logger = logging.getLogger(__name__)
router = APIRouter(prefix="/chatbot", tags=["Chatbot"])

class ChatRequest(BaseModel):
    message: str
    context: str = ""

class ChatResponse(BaseModel):
    message: str
    sources: str = ""
    query_type: str = ""

# OPTIMIZED SYSTEM PROMPT cho tiếng Việt
SYSTEM_PROMPT = """
Bạn là trợ lý AI thông minh cho hệ thống quản lý bán hàng GearUp - Cửa hàng giày thể thao.

**Nhiệm vụ của bạn:**
- Trả lời câu hỏi dựa trên dữ liệu được cung cấp từ hệ thống
- Giải thích rõ ràng, chuyên nghiệp, dễ hiểu
- Sử dụng tiếng Việt tự nhiên
- Định dạng số rõ ràng (ví dụ: "142 đôi", "15,500,000 VNĐ")

**Phong cách:**
- Ngắn gọn nhưng đầy đủ thông tin
- Sử dụng emoji phù hợp (📊 💰 ⭐ ⚠️ 👥 🎉 🛒 📋 🎨 📏)
- Không bịa đặt thông tin không có trong dữ liệu
- Nếu không có dữ liệu, nói rõ "Không có dữ liệu"

**Chức năng bạn có thể thực hiện:**
- Tra cứu sản phẩm bán chạy nhất
- Thống kê doanh thu và đơn hàng
- Cảnh báo tồn kho thấp
- Phân tích trạng thái đơn hàng
- Top khách hàng chi tiêu nhiều nhất
- Đợt giảm giá đang hoạt động
- Hiệu suất nhân viên bán hàng
- Phân tích màu sắc và kích thước phổ biến
- So sánh bán hàng online vs tại quầy

**Định dạng trả lời:**
- Dùng tiêu đề ngắn gọn cho phần chính
- Nếu có danh sách/so sánh → trình bày dưới dạng bảng Markdown có header
- Đơn vị: VNĐ, đơn, đôi; định dạng số có dấu phẩy phần nghìn
- Kết phần trả lời bằng 1-2 dòng kết luận/ý nghĩa

**QUAN TRỌNG:** Chỉ trả lời bằng tiếng Việt!
"""

def detect_intent(message: str) -> str:
    """Detect user intent from message"""
    message_lower = message.lower()
    
    # Check low_stock first (higher priority)
    if any(word in message_lower for word in ["hết hàng", "sắp hết", "tồn kho thấp", "tồn kho", "stock", "còn lại", "số lượng còn"]):
        return "low_stock"
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
            
            context = "⚠️ **Sản phẩm sắp hết hàng (≤10 đôi):**\n\n"
            for i, p in enumerate(products, 1):
                context += f"{i}. **{p['product_name']}**"
                if p.get('color') or p.get('size'):
                    context += f" ({p.get('color', '')}"
                    if p.get('color') and p.get('size'):
                        context += f", "
                    context += f"{p.get('size', '')})"
                context += f"\n   - Còn lại: **{p['stock_quantity']} đôi**\n\n"
            
            return context
        
        elif intent == "order_status":
            statuses = db_client.get_order_status_distribution()
            
            if not statuses:
                return "Không có dữ liệu trạng thái đơn hàng."
            
            context = "📋 **Phân bố trạng thái đơn hàng:**\n\n"
            for status in statuses:
                context += f"- **{status['status_name']}**: {status['order_count']} đơn\n"
            
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
            
            context = "🛒 **Phân bố kênh bán hàng:**\n\n"
            context += f"**Bán tại quầy (POS):**\n"
            context += f"- Số đơn: {pos_orders} đơn\n"
            context += f"- Doanh thu: {pos_revenue:,.0f} VNĐ\n\n"
            context += f"**Bán hàng online:**\n"
            context += f"- Số đơn: {online_orders} đơn\n"
            context += f"- Doanh thu: {online_revenue:,.0f} VNĐ\n"
            
            return context
        
        else:
            return "Tôi có thể giúp bạn tra cứu:\n- Top sản phẩm bán chạy\n- Doanh thu\n- Tồn kho\n- Trạng thái đơn hàng\n- Top khách hàng\n- Đợt giảm giá\n- Nhân viên bán hàng\n- Màu sắc/size phổ biến\n- Phân bố kênh bán hàng\nBạn muốn biết điều gì?"
    
    except Exception as e:
        logger.error(f"Database query error: {e}")
        return f"Lỗi truy vấn dữ liệu: {str(e)}"

@router.post("/chat", response_model=ChatResponse)
async def chat(request: ChatRequest):
    """Chat with AI assistant"""
    try:
        # 1. Detect intent
        intent = detect_intent(request.message)
        logger.info(f"Detected intent: {intent}")
        
        # 2. Query database
        db_context = query_database(intent, request.message)
        
        # 3. Build messages for LLM
        messages = [
            {"role": "system", "content": SYSTEM_PROMPT},
            {"role": "user", "content": f"{request.message}\n\n**Dữ liệu từ hệ thống:**\n{db_context}"}
        ]
        
        # 4. Call LM Studio
        response = lm_client.chat(
            messages=messages,
            temperature=0.3,
            max_tokens=500  # Tăng lên để response đầy đủ
        )
        
        ai_message = response.choices[0].message.content
        
        return ChatResponse(
            message=ai_message,
            sources='',
            query_type=intent
        )
    
    except Exception as e:
        logger.error(f"Chat error: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/health")
async def health_check():
    """Check LM Studio connection"""
    is_connected, message = lm_client.test_connection()
    
    return {
        "lm_studio": "connected" if is_connected else "disconnected",
        "message": message,
        "model": lm_client.model,
        "base_url": lm_client.client.base_url
    }
