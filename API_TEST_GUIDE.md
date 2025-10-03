# Hướng dẫn Test API Backend

## 1. Khởi động Backend
```bash
cd SD_33_ver2/BE_SP
mvn spring-boot:run
```

## 2. Khởi động Frontend
```bash
cd SD_33_ver2/view
npm run dev
```

## 3. Test API Endpoints

### Health Check
```bash
curl http://localhost:8080/api/public/health
```

### Database Test
```bash
curl http://localhost:8080/api/test/database
```

### Test Invoices
```bash
curl http://localhost:8080/api/test/invoices
```

### Test Products
```bash
curl http://localhost:8080/api/test/products
```

### Test Customers
```bash
curl http://localhost:8080/api/test/customers
```

## 4. Test từ Frontend

1. Mở trang Quản lý hóa đơn
2. Click nút "Test API"
3. Mở Developer Console (F12) để xem kết quả
4. Kiểm tra dữ liệu hiển thị trong bảng

## 5. Kiểm tra Database

Đảm bảo SQL Server đang chạy với:
- Server: localhost:1433
- Database: GearUp
- Username: sa
- Password: 1

## 6. Troubleshooting

### Lỗi CORS
- Kiểm tra cấu hình CORS trong SecurityConfig.java
- Đảm bảo frontend chạy trên port 5173 hoặc 5174

### Lỗi Database
- Kiểm tra SQL Server đang chạy
- Kiểm tra connection string trong application.properties
- Kiểm tra database GearUp có tồn tại

### Lỗi API
- Kiểm tra backend đang chạy trên port 8080
- Kiểm tra logs trong console backend
- Kiểm tra network tab trong Developer Tools
