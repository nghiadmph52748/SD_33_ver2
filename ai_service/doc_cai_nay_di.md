1. Di chuyển vào thư mục ai_service:
```bash
cd ai_service
```

2. Tạo môi trường ảo:
```bash
python3 -m venv venv
source venv/bin/activate  # Mac/Linux
# venv\Scripts\activate  # Windows
```

3. Cài đặt thư viện:
```bash
pip install -r requirements.txt
```

4. Cấu hình môi trường:
```bash
cp .env.example .env
```

Chỉnh sửa file `.env` với thông tin database:
```env
LM_STUDIO_BASE_URL=http://localhost:3147/v1
LM_STUDIO_MODEL=openai/gpt-oss-20b

DB_HOST=localhost
DB_PORT=1433
DB_NAME=GearUp
DB_USER=sa
DB_PASSWORD=mat_khau

REDIS_HOST=localhost
REDIS_PORT=6379

API_PORT=8001
```

5. Khởi động dịch vụ:
```bash
uvicorn app.main:app --reload --port 8001
```

Dịch vụ sẽ chạy tại http://localhost:8001 

## API Endpoints

### Kiểm tra

```bash
# Kiểm tra trạng thái dịch vụ
curl http://localhost:8001/health

# Kiểm tra kết nối LM Studio
curl http://localhost:8001/api/ai/chatbot/health
```

### Chat

```bash
# Gửi tin nhắn chat
curl -X POST http://localhost:8001/api/ai/chatbot/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Sản phẩm nào bán chạy nhất?"}'
```

**Phản hồi:**
```json
{
  "message": "Dựa trên dữ liệu từ hệ thống, đây là top 5 sản phẩm bán chạy nhất trong 30 ngày qua:\n\n1. Nike Air Force 1 - 142 đôi\n2. Adidas Ultra Boost - 98 đôi\n3. Vans Old Skool - 76 đôi\n...",
  "sources": "**Top 5 sản phẩm bán chạy nhất (30 ngày qua):**\n\n1. **Nike Air Force 1**\n   - Đã bán: 142 đôi\n   - Doanh thu: 15,500,000 VNĐ\n...",
  "queryType": "top_products"
}
```

