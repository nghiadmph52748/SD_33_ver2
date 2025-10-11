<template>
  <div class="product-list-example">
    <Breadcrumb />

    <div class="page-container">
      <a-card :bordered="false" class="main-card">
        <template #title>
          <div class="card-header">
            <icon-apps />
            <span>Ví dụ sử dụng bảng danh sách sản phẩm</span>
          </div>
        </template>

        <div class="example-section">
          <h3>Component đơn giản (SimpleProductTable)</h3>
          <p>Component này phù hợp để sử dụng trong các trang khác với ít tùy chọn hơn.</p>

          <div class="table-container">
            <SimpleProductTable :products="sampleProducts" :loading="loading" :show-pagination="true" :page-size="5" />
          </div>
        </div>

        <a-divider />

        <div class="example-section">
          <h3>Component đầy đủ (ProductTable)</h3>
          <p>Component này có nhiều tính năng hơn như tìm kiếm, lọc, cài đặt hiển thị.</p>

          <div class="table-container">
            <ProductTable :show-actions="false" :selectable="false" :max-height="'400px'" />
          </div>
        </div>

        <a-divider />

        <div class="usage-examples">
          <h3>Cách sử dụng</h3>

          <div class="code-examples">
            <h4>1. Component đơn giản:</h4>
            <pre><code>&lt;SimpleProductTable
  :products="productList"
  :loading="isLoading"
  :show-pagination="true"
  :page-size="10"
/&gt;</code></pre>

            <h4>2. Component đầy đủ:</h4>
            <pre><code>&lt;ProductTable
  :show-actions="true"
  :selectable="false"
  :max-height="'calc(100vh - 400px)'"
  @product-select="handleProductSelect"
  @refresh="handleRefresh"
/&gt;</code></pre>
          </div>
        </div>
      </a-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import SimpleProductTable from '@/components/simple-product-table/simple-product-table.vue'
import ProductTable from '@/components/product-table/product-table.vue'
import { IconApps } from '@arco-design/web-vue/es/icon'
import { getBienTheSanPhamPage, type BienTheSanPham } from '@/api/san-pham/bien-the'

// Reactive data
const loading = ref(false)
const sampleProducts = ref<BienTheSanPham[]>([])

// Sample data for demonstration
const mockProducts: BienTheSanPham[] = [
  {
    id: 1,
    tenSanPham: 'Giày thể thao Nike Air Max',
    maChiTietSanPham: 'SP001',
    anhSanPham: ['https://via.placeholder.com/200x150/1890ff/ffffff?text=Nike+Air+Max'],
    soLuong: 25,
    giaBan: 2500000,
    giaTriGiamGia: 0,
    trangThai: true,
    tenMauSac: 'Đen',
    tenKichThuoc: '42',
    tenNhaSanXuat: 'Nike',
    tenXuatXu: 'Việt Nam',
    tenChatLieu: 'Da tổng hợp',
    tenDeGiay: 'Đế cao su',
    tenTrongLuong: '350g',
  },
  {
    id: 2,
    tenSanPham: 'Giày chạy bộ Adidas Ultraboost',
    maChiTietSanPham: 'SP002',
    anhSanPham: ['https://via.placeholder.com/200x150/52c41a/ffffff?text=Adidas+Ultraboost'],
    soLuong: 15,
    giaBan: 2500000,
    giaTriGiamGia: 0,
    trangThai: true,
    tenMauSac: 'Trắng',
    tenKichThuoc: '41',
    tenNhaSanXuat: 'Adidas',
    tenXuatXu: 'Đức',
    tenChatLieu: 'Vải mesh',
    tenDeGiay: 'Đế Boost',
    tenTrongLuong: '320g',
  },
  {
    id: 3,
    tenSanPham: 'Giày cao gót Jimmy Choo',
    maChiTietSanPham: 'SP003',
    anhSanPham: ['https://via.placeholder.com/200x150/ff4d4f/ffffff?text=Jimmy+Choo'],
    soLuong: 5,
    giaBan: 2500000,
    giaTriGiamGia: 0,
    trangThai: true,
    tenMauSac: 'Đỏ',
    tenKichThuoc: '38',
    tenNhaSanXuat: 'Jimmy Choo',
    tenXuatXu: 'Anh',
    tenChatLieu: 'Da thật',
    tenDeGiay: 'Đế cao gót',
    tenTrongLuong: '280g',
  },
  {
    id: 4,
    tenSanPham: 'Giày lười Gucci',
    maChiTietSanPham: 'SP004',
    anhSanPham: ['https://via.placeholder.com/200x150/faad14/ffffff?text=Gucci'],
    soLuong: 0,
    giaBan: 2500000,
    giaTriGiamGia: 0,
    trangThai: false,
    tenMauSac: 'Nâu',
    tenKichThuoc: '40',
    tenNhaSanXuat: 'Gucci',
    tenXuatXu: 'Ý',
    tenChatLieu: 'Da cao cấp',
    tenDeGiay: 'Đế phẳng',
    tenTrongLuong: '400g',
    deleted: true,
  },
  {
    id: 5,
    tenSanPham: 'Giày boot Timberland',
    maChiTietSanPham: 'SP005',
    anhSanPham: ['https://via.placeholder.com/200x150/722ed1/ffffff?text=Timberland'],
    soLuong: 30,
    giaBan: 2500000,
    giaTriGiamGia: 0,
    trangThai: true,
    tenMauSac: 'Vàng',
    tenKichThuoc: '43',
    tenNhaSanXuat: 'Timberland',
    tenXuatXu: 'Mỹ',
    tenChatLieu: 'Da bò',
    tenDeGiay: 'Đế cao su',
    tenTrongLuong: '500g',
  },
]

// Methods
const loadSampleData = async () => {
  try {
    loading.value = true

    // Try to load real data first
    const response = await getBienTheSanPhamPage(0, undefined, 5)
    if (response.data.success && response.data.data.data.length > 0) {
      sampleProducts.value = response.data.data.data
    } else {
      // Fallback to mock data
      sampleProducts.value = mockProducts
    }
  } catch (error) {
    console.error('Error loading products:', error)
    // Use mock data as fallback
    sampleProducts.value = mockProducts
  } finally {
    loading.value = false
  }
}

// Lifecycle
onMounted(() => {
  loadSampleData()
})
</script>

<style scoped lang="less">
.product-list-example {
  padding: 20px;

  .page-container {
    max-width: 1400px;
    margin: 0 auto;

    .main-card {
      .card-header {
        display: flex;
        align-items: center;
        gap: 8px;
        font-weight: 600;
        font-size: 18px;
      }
    }
  }
}

.example-section {
  margin-bottom: 24px;

  h3 {
    margin-bottom: 12px;
    color: var(--color-text-1);
    font-size: 16px;
  }

  p {
    margin-bottom: 16px;
    color: var(--color-text-2);
    line-height: 1.5;
  }

  .table-container {
    border: 1px solid var(--color-border-2);
    border-radius: 8px;
    padding: 16px;
    background: var(--color-bg-1);
  }
}

.usage-examples {
  h3 {
    margin-bottom: 16px;
    color: var(--color-text-1);
  }

  .code-examples {
    h4 {
      margin: 16px 0 8px 0;
      color: var(--color-text-1);
      font-size: 14px;
    }

    pre {
      background: var(--color-fill-2);
      border: 1px solid var(--color-border-2);
      border-radius: 6px;
      padding: 12px;
      margin-bottom: 16px;
      overflow-x: auto;

      code {
        font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
        font-size: 13px;
        line-height: 1.5;
        color: var(--color-text-1);
      }
    }
  }
}

// Responsive design
@media (max-width: 768px) {
  .product-list-example {
    padding: 10px;
  }

  .example-section .table-container {
    padding: 8px;
  }

  .usage-examples .code-examples pre {
    padding: 8px;
    font-size: 12px;
  }
}
</style>
