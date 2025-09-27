# API Quản Lý Sản Phẩm

## 📁 Cấu trúc thư mục

```
view/src/api/san-pham/
├── index.ts          # Export tất cả API
├── danh-muc.ts       # API cho danh mục sản phẩm
├── bien-the.ts       # API cho biến thể sản phẩm
└── thuoc-tinh/       # Folder chứa các API thuộc tính
    ├── nha-san-xuat.ts
    ├── chat-lieu.ts
    ├── mau-sac.ts
    ├── kich-thuoc.ts
    ├── xuat-xu.ts
    ├── trong-luong.ts
    └── de-giay.ts
```

## 🔧 Cách sử dụng

### Import API

```typescript
import {
  // Thuộc tính sản phẩm
  getNhaSanXuatList,
  getChatLieuList,
  getMauSacList,
  getKichThuocList,
  getXuatXuList,
  getTrongLuongList,
  getDeGiayList,

  // Danh mục sản phẩm
  getDanhMucSanPhamList,
  getNhaSanXuatOptions,
  getXuatXuOptions,

  // Biến thể sản phẩm
  getBienTheSanPhamList,
  uploadAnhBienThe,
  getSanPhamOptions,
  getMauSacOptions,
  getKichThuocOptions,
  getChatLieuOptions,
  getDeGiayOptions,
  getTrongLuongOptions,
} from '@/api/san-pham'

// Hoặc import từ từng file riêng lẻ (bao gồm cả interface)
import { getNhaSanXuatList, NhaSanXuat, NhaSanXuatParams } from '@/api/san-pham/thuoc-tinh/nha-san-xuat'

import { getChatLieuList, ChatLieu, ChatLieuParams } from '@/api/san-pham/thuoc-tinh/chat-lieu'
// ...
```

## 📋 API Endpoints

### 🎨 Thuộc tính sản phẩm (`thuoc-tinh/`)

#### Nhà sản xuất (`nha-san-xuat.ts`)

- `GET /api/san-pham/thuoc-tinh/nha-san-xuat` - Lấy danh sách nhà sản xuất
- `GET /api/san-pham/thuoc-tinh/nha-san-xuat/:id` - Lấy chi tiết nhà sản xuất
- `POST /api/san-pham/thuoc-tinh/nha-san-xuat` - Tạo nhà sản xuất mới
- `PUT /api/san-pham/thuoc-tinh/nha-san-xuat/:id` - Cập nhật nhà sản xuất
- `DELETE /api/san-pham/thuoc-tinh/nha-san-xuat/:id` - Xóa nhà sản xuất

#### Chất liệu (`chat-lieu.ts`)

- `GET /api/san-pham/thuoc-tinh/chat-lieu` - Lấy danh sách chất liệu
- `GET /api/san-pham/thuoc-tinh/chat-lieu/:id` - Lấy chi tiết chất liệu
- `POST /api/san-pham/thuoc-tinh/chat-lieu` - Tạo chất liệu mới
- `PUT /api/san-pham/thuoc-tinh/chat-lieu/:id` - Cập nhật chất liệu
- `DELETE /api/san-pham/thuoc-tinh/chat-lieu/:id` - Xóa chất liệu

#### Màu sắc (`mau-sac.ts`)

- `GET /api/san-pham/thuoc-tinh/mau-sac` - Lấy danh sách màu sắc
- `GET /api/san-pham/thuoc-tinh/mau-sac/:id` - Lấy chi tiết màu sắc
- `POST /api/san-pham/thuoc-tinh/mau-sac` - Tạo màu sắc mới
- `PUT /api/san-pham/thuoc-tinh/mau-sac/:id` - Cập nhật màu sắc
- `DELETE /api/san-pham/thuoc-tinh/mau-sac/:id` - Xóa màu sắc

#### Kích thước (`kich-thuoc.ts`)

- `GET /api/san-pham/thuoc-tinh/kich-thuoc` - Lấy danh sách kích thước
- `GET /api/san-pham/thuoc-tinh/kich-thuoc/:id` - Lấy chi tiết kích thước
- `POST /api/san-pham/thuoc-tinh/kich-thuoc` - Tạo kích thước mới
- `PUT /api/san-pham/thuoc-tinh/kich-thuoc/:id` - Cập nhật kích thước
- `DELETE /api/san-pham/thuoc-tinh/kich-thuoc/:id` - Xóa kích thước

#### Xuất xứ (`xuat-xu.ts`)

- `GET /api/san-pham/thuoc-tinh/xuat-xu` - Lấy danh sách xuất xứ
- `GET /api/san-pham/thuoc-tinh/xuat-xu/:id` - Lấy chi tiết xuất xứ
- `POST /api/san-pham/thuoc-tinh/xuat-xu` - Tạo xuất xứ mới
- `PUT /api/san-pham/thuoc-tinh/xuat-xu/:id` - Cập nhật xuất xứ
- `DELETE /api/san-pham/thuoc-tinh/xuat-xu/:id` - Xóa xuất xứ

#### Trọng lượng (`trong-luong.ts`)

- `GET /api/san-pham/thuoc-tinh/trong-luong` - Lấy danh sách trọng lượng
- `GET /api/san-pham/thuoc-tinh/trong-luong/:id` - Lấy chi tiết trọng lượng
- `POST /api/san-pham/thuoc-tinh/trong-luong` - Tạo trọng lượng mới
- `PUT /api/san-pham/thuoc-tinh/trong-luong/:id` - Cập nhật trọng lượng
- `DELETE /api/san-pham/thuoc-tinh/trong-luong/:id` - Xóa trọng lượng

#### Đế giày (`de-giay.ts`)

- `GET /api/san-pham/thuoc-tinh/de-giay` - Lấy danh sách đế giày
- `GET /api/san-pham/thuoc-tinh/de-giay/:id` - Lấy chi tiết đế giày
- `POST /api/san-pham/thuoc-tinh/de-giay` - Tạo đế giày mới
- `PUT /api/san-pham/thuoc-tinh/de-giay/:id` - Cập nhật đế giày
- `DELETE /api/san-pham/thuoc-tinh/de-giay/:id` - Xóa đế giày

### 📦 Danh mục sản phẩm (`danh-muc.ts`)

- `GET /api/san-pham/danh-muc` - Lấy danh sách sản phẩm
- `GET /api/san-pham/danh-muc/:id` - Lấy chi tiết sản phẩm
- `POST /api/san-pham/danh-muc` - Tạo sản phẩm mới
- `PUT /api/san-pham/danh-muc/:id` - Cập nhật sản phẩm
- `DELETE /api/san-pham/danh-muc/:id` - Xóa sản phẩm
- `GET /api/san-pham/danh-muc/nha-san-xuat/options` - Lấy danh sách nhà sản xuất cho dropdown
- `GET /api/san-pham/danh-muc/xuat-xu/options` - Lấy danh sách xuất xứ cho dropdown

### 🔄 Biến thể sản phẩm (`bien-the.ts`)

- `GET /api/san-pham/bien-the` - Lấy danh sách biến thể sản phẩm
- `GET /api/san-pham/bien-the/:id` - Lấy chi tiết biến thể
- `POST /api/san-pham/bien-the` - Tạo biến thể mới
- `PUT /api/san-pham/bien-the/:id` - Cập nhật biến thể
- `DELETE /api/san-pham/bien-the/:id` - Xóa biến thể
- `POST /api/san-pham/bien-the/upload-anh` - Upload ảnh cho biến thể
- `DELETE /api/san-pham/bien-the/:idBienThe/anh/:idAnh` - Xóa ảnh biến thể

#### Options cho dropdown:

- `GET /api/san-pham/bien-the/san-pham/options` - Danh sách sản phẩm
- `GET /api/san-pham/bien-the/mau-sac/options` - Danh sách màu sắc
- `GET /api/san-pham/bien-the/kich-thuoc/options` - Danh sách kích thước
- `GET /api/san-pham/bien-the/chat-lieu/options` - Danh sách chất liệu
- `GET /api/san-pham/bien-the/de-giay/options` - Danh sách đế giày
- `GET /api/san-pham/bien-the/trong-luong/options` - Danh sách trọng lượng

## 📝 Tham số truy vấn (Query Parameters)

### Pagination

- `page`: Số trang (bắt đầu từ 0)
- `size`: Số bản ghi mỗi trang

### Filtering

- `search`: Từ khóa tìm kiếm
- `trangThai`: Trạng thái (true/false)
- `idNhaSanXuat`: ID nhà sản xuất
- `idXuatXu`: ID xuất xứ
- `giaTu`: Giá từ
- `giaDen`: Giá đến

## 🚀 Ví dụ sử dụng

### Load danh mục sản phẩm với filter

```typescript
import { getDanhMucSanPhamList } from '@/api/san-pham'

const loadDanhMuc = async () => {
  try {
    const response = await getDanhMucSanPhamList({
      page: 1,
      size: 10,
      search: 'Nike',
      trangThai: true,
      giaTu: 100000,
      giaDen: 1000000,
    })

    console.log('Danh mục sản phẩm:', response.data.content)
    console.log('Tổng số:', response.data.totalElements)
  } catch (error) {
    console.error('Lỗi khi tải danh mục:', error)
  }
}
```

### Load options cho dropdown

```typescript
import { getNhaSanXuatOptions, getXuatXuOptions } from '@/api/san-pham'

const loadOptions = async () => {
  const [nhaSanXuat, xuatXu] = await Promise.all([getNhaSanXuatOptions(), getXuatXuOptions()])

  console.log('Nhà sản xuất:', nhaSanXuat.data)
  console.log('Xuất xứ:', xuatXu.data)
}
```

### Upload ảnh cho biến thể

```typescript
import { uploadAnhBienThe } from '@/api/san-pham'

const uploadImage = async (idBienThe: number, files: File[]) => {
  const formData = new FormData()
  formData.append('idBienThe', idBienThe.toString())
  files.forEach((file) => formData.append('files', file))

  try {
    const response = await uploadAnhBienThe(formData)
    console.log('Upload thành công:', response.data)
  } catch (error) {
    console.error('Lỗi upload:', error)
  }
}
```

## 🔒 Bảo mật và xử lý lỗi

- Tất cả API đều sử dụng axios interceptor cho authentication
- Error handling được thực hiện ở component level
- Validation được thực hiện cả frontend và backend

## 📊 Phân trang

API sử dụng phân trang Spring Boot mặc định:

- `page`: Số trang (0-based)
- `size`: Kích thước trang
- Response bao gồm: `content`, `totalElements`, `totalPages`, `size`, `number`

## 📸 Quản lý ảnh sản phẩm (`anh-san-pham.ts`)

### API Endpoints:

#### **Lấy danh sách ảnh sản phẩm**

```typescript
getAnhSanPhamList(params?: AnhSanPhamParams)
```

- **Method**: GET
- **Endpoint**: `/api/anh-san-pham-management/paging`
- **Params**: `page`, `size`, `search`, `trangThai`

#### **Lấy chi tiết ảnh sản phẩm**

```typescript
getAnhSanPhamById(id: number)
```

- **Method**: GET
- **Endpoint**: `/api/anh-san-pham-management/detail/{id}`

#### **Upload ảnh sản phẩm**

```typescript
uploadAnhSanPham(file: File, tenAnh?: string)
```

- **Method**: POST
- **Endpoint**: `/api/anh-san-pham-management/add`
- **Body**: FormData với `file` và `tenAnh` (optional)

#### **Upload nhiều ảnh cùng lúc**

```typescript
uploadNhieuAnhSanPham(files: File[])
```

- **Method**: POST
- **Endpoint**: `/api/anh-san-pham-management/add-multi-image/cloud`
- **Body**: FormData với nhiều file
- **Return**: `number[]` - Array ID của ảnh đã upload lên Cloudinary

#### **Cập nhật ảnh sản phẩm**

```typescript
updateAnhSanPham(id: number, data: Partial<AnhSanPham>)
```

- **Method**: PUT
- **Endpoint**: `/api/anh-san-pham-management/update/{id}`

#### **Xóa ảnh sản phẩm**

```typescript
deleteAnhSanPham(id: number)
```

- **Method**: PUT
- **Endpoint**: `/api/anh-san-pham-management/update/status/{id}`

#### **Lấy tất cả ảnh sản phẩm**

```typescript
getAllAnhSanPham()
```

- **Method**: GET
- **Endpoint**: `/api/anh-san-pham-management/playlist`

### Interface `AnhSanPham`:

```typescript
interface AnhSanPham {
  id: number
  duongDanAnh: string
  tenAnh?: string
  mauAnh?: string
  trangThai: boolean
  deleted: boolean
  createAt?: string
  updateAt?: string
}
```

## 🔗 Ảnh biến thể sản phẩm (trong `bien-the.ts`)

### API Endpoints bổ sung:

#### **Lấy ảnh của biến thể**

```typescript
getAnhBienThe(idBienThe: number)
```

- **Method**: GET
- **Endpoint**: `/api/chi-tiet-san-pham-anh-management/list/{idBienThe}`

#### **Thêm ảnh cho biến thể**

```typescript
themAnhChoBienThe(data: ThemAnhBienTheRequest)
```

- **Method**: POST
- **Endpoint**: `/api/chi-tiet-san-pham-anh-management/add-multiple`
- **Body**: `{ idChiTietSanPham: number, danhSachAnh: number[] }`

#### **Xóa ảnh khỏi biến thể**

```typescript
xoaAnhKhoiBienThe(idAnhBienThe: number)
```

- **Method**: PUT
- **Endpoint**: `/api/chi-tiet-san-pham-anh-management/update/status/{idAnhBienThe}`

### Interface cập nhật:

```typescript
interface BienTheSanPham {
  // khớp với ChiTietSanPhamFullResponse
  id: number
  maChiTietSanPham?: string
  tenSanPham?: string
  anhSanPham?: string[] // List đường dẫn ảnh (từ response)
  tenNhaSanXuat?: string
  tenXuatXu?: string
  tenDeGiay?: string
  tenChatLieu?: string
  tenMauSac?: string
  tenKichThuoc?: string
  tenTrongLuong?: string
  soLuong: number
  giaBan: number
  idDotGiamGia?: number
  tenDotGiamGia?: string
  giaTriGiamGia?: number
  trangThai: boolean
  deleted?: boolean
  createAt?: string
  updateAt?: string
  // Thông tin bổ sung từ entity
  ghiChu?: string
  createBy?: number
  updateBy?: number
}

interface AnhSanPhamBienThe {
  // cho API quản lý liên kết ảnh
  id: number
  idAnhSanPham: number
  duongDanAnh: string
  tenAnh?: string
  mauAnh?: string
  trangThai: boolean
}
```

## 🔄 Cập nhật và bảo trì

- API được tổ chức theo module riêng biệt để dễ bảo trì
- Mỗi thuộc tính sản phẩm có file riêng biệt để quản lý tốt hơn
- TypeScript interfaces được định nghĩa rõ ràng và khớp với backend models
- Error handling nhất quán
- Code được comment đầy đủ
- Có thể import từ từng file cụ thể hoặc từ index.ts
- **Mới**: Hỗ trợ đầy đủ quản lý ảnh sản phẩm và ảnh biến thể
- **Cập nhật**: Interface khớp với `/src/model/response` và `/src/model/request` từ backend
- **Cloud Upload**: Upload ảnh sử dụng Cloudinary cho lưu trữ đám mây
