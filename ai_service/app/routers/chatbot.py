from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from app.utils.lm_studio import lm_client
from app.utils.database import db_client
import logging

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
- Sử dụng emoji phù hợp (📊 💰 ⭐ ⚠️)
- Không bịa đặt thông tin không có trong dữ liệu
- Nếu không có dữ liệu, nói rõ "Không có dữ liệu"

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
    elif any(word in message_lower for word in ["khách hàng", "customer", "vip", "người mua"]):
        return "customers"
    else:
        return "general"

def query_database(intent: str, message: str) -> str:
    """Query database based on intent"""
    try:
        if intent == "top_products":
            products = db_client.get_top_selling_products(limit=5, days=30)
            
            if not products:
                return "Không có dữ liệu sản phẩm trong 30 ngày qua."
            
            context = "📊 **Top 5 sản phẩm bán chạy nhất (30 ngày qua):**\n\n"
            for i, p in enumerate(products, 1):
                total_sold = p.get('total_sold') or 0
                total_revenue = p.get('total_revenue') or 0
                context += f"{i}. **{p['product_name']}**\n"
                context += f"   - Đã bán: {total_sold} đôi\n"
                context += f"   - Doanh thu: {total_revenue:,.0f} VNĐ\n\n"
            
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
        
        else:
            return "Tôi có thể giúp bạn tra cứu:\n- Top sản phẩm bán chạy\n- Doanh thu\n- Tồn kho\nBạn muốn biết điều gì?"
    
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
            sources=db_context,
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
