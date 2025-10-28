# Module Thống Kê - Component Decomposition

## 📊 Tổng Quan

Module thống kê đã được tái cấu trúc từ 1 file monolith (3000+ dòng) thành kiến trúc phân tách rõ ràng với composables và components tái sử dụng.

## 📁 Cấu Trúc Thư Mục

```
thong-ke/
├── composables/                    # Logic nghiệp vụ tái sử dụng
│   ├── useThongKeData.ts          (78 lines)   - Fetch data từ API
│   ├── useTinhToanThongKe.ts      (569 lines)  - Xử lý tính toán thống kê
│   └── useXuatExcel.ts            (92 lines)   - Xuất báo cáo Excel
│
├── types/                          # Type definitions
│   └── thong-ke.types.ts          (123 lines)  - TypeScript interfaces
│
└── chung/                          # Components
    ├── thong-ke-chung.vue         (3000+ lines) - [TBD] Main orchestrator
    └── the-thong-ke.vue           (198 lines)   - [✓] Stat card component
```

## ✅ Đã Hoàn Thành

### 1. Types (`thong-ke.types.ts`)
- ✅ Định nghĩa tất cả interfaces cho data models
- ✅ Type-safe cho toàn bộ module
- ✅ Union types cho filters và options

### 2. Composables

#### `useThongKeData.ts`
- ✅ API calls cho hóa đơn, sản phẩm, chi tiết sản phẩm
- ✅ Loading states và error handling
- ✅ Batch loading với `taiToanBoDuLieu()`

#### `useTinhToanThongKe.ts` ⭐ (Core logic)
- ✅ Tính toán thống kê theo thời gian (ngày/tuần/tháng/năm)
- ✅ Xây dựng dữ liệu cho biểu đồ doanh thu
- ✅ Top sản phẩm bán chạy
- ✅ Phân tích trạng thái đơn hàng
- ✅ Phân phối kênh (online/offline)
- ✅ Phân loại danh mục sản phẩm
- ✅ Sản phẩm sắp hết hàng
- ✅ Bảng thống kê chi tiết với tính năng tăng trưởng
- ✅ Auto-update khi data thay đổi (watch)

#### `useXuatExcel.ts`
- ✅ Xuất báo cáo thống kê ra Excel
- ✅ Multi-sheet export
- ✅ Format tiền tệ VNĐ
- ✅ Error handling

### 3. Components

#### `the-thong-ke.vue` ✅
- ✅ Reusable stat card
- ✅ Props-driven với TypeScript
- ✅ Dynamic color themes (today/week/month/year)
- ✅ Animations included
- ✅ Vietnamese naming convention

## 🚧 Cần Làm Tiếp

### Components Cần Tạo (Vietnamese filenames)

1. **`bo-loc-thong-ke.vue`** (Filter section)
   - Time range selector
   - Chart type toggle
   - Date range picker
   - Summary display
   - Actions (Reset, Export)

2. **`bieu-do-doanh-thu.vue`** (Revenue chart)
   - Line/Bar chart với ECharts
   - Period selector (6/12 months)
   - Responsive design

3. **`danh-sach-san-pham-ban-chay.vue`** (Top products list)
   - Top 5 products display
   - Period filter
   - Product rank badges

4. **`bieu-do-trang-thai-don-hang.vue`** (Order status pie chart)
   - Day/Month/Year selector
   - Dynamic data update

5. **`bieu-do-phan-phoi-kenh.vue`** (Channel distribution chart)
   - Online vs Offline pie chart

6. **`bieu-do-danh-muc.vue`** (Category chart)
   - Category distribution pie chart

7. **`bang-thong-ke-chi-tiet.vue`** (Detail statistics table)
   - Paginated table
   - Growth indicators
   - Status tags

8. **`bang-san-pham-ban-chay-nhat.vue`** (Top selling products table)
   - Product images
   - Sortable columns
   - Pagination

9. **`bang-san-pham-sap-het-hang.vue`** (Low stock products table)
   - Stock warning indicators
   - Color-coded status

### Refactor Main File
- **`thong-ke-chung.vue`** → Orchestrator component
  - Import all child components
  - Use composables for logic
  - Grid layout implementation
  - Remove all business logic

## 🎯 Component Pattern

```vue
<template>
  <div class="component-name">
    <!-- UI only, no logic -->
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { TypeName } from '../types/thong-ke.types'

interface Props {
  // Props definition
}

const props = defineProps<Props>()

// Use composables for logic
// const { data, method } = useSomeComposable()
</script>

<style scoped>
/* Component styles */
</style>
```

## 📈 Lợi Ích Của Decomposition

### Performance
- ✅ Smaller bundle sizes per component
- ✅ Better tree-shaking
- ✅ Easier code splitting
- ✅ Optimized re-renders

### Maintainability
- ✅ Single Responsibility Principle
- ✅ Easy to locate bugs
- ✅ Clear separation of concerns
- ✅ Testable units

### Reusability
- ✅ Composables can be shared
- ✅ Components are modular
- ✅ Type-safe interfaces
- ✅ Consistent patterns

### Developer Experience
- ✅ Faster IDE performance
- ✅ Better IntelliSense
- ✅ Easier code review
- ✅ Clear file structure

## 🚀 Next Steps

1. **Create filter component** (`bo-loc-thong-ke.vue`)
2. **Create chart components** (3 pie charts + 1 line/bar chart)
3. **Create table components** (3 tables)
4. **Refactor main orchestrator** (`thong-ke-chung.vue`)
5. **Test integration**
6. **Implement grid design**

## 📝 Notes

- Tất cả components sử dụng Vietnamese naming
- TypeScript strict mode
- Arco Design components
- ECharts for visualizations
- Composition API with `<script setup>`
- Props-driven architecture

## 🔧 Usage Example

```vue
<!-- In parent component -->
<the-thong-ke
  tieu-de="Hôm nay"
  :doanh-thu="duLieuHomNay.revenue"
  :san-pham-da-ban="duLieuHomNay.productsSold"
  :so-don-hang="duLieuHomNay.orders"
  :tang-truong="0"
  loai-mau="today"
/>
```

---

**Status**: 🟡 In Progress (Composables Complete, Components 10% Done)
