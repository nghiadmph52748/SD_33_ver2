<template>
  <div class="product-table-container">
    <a-card :bordered="false" class="product-table-card">
      <template #title>
        <div class="card-header">
          <icon-apps />
          <span>Danh sách sản phẩm</span>
        </div>
      </template>

      <!-- Search and Filter -->
      <div class="table-controls">
        <a-space>
          <a-input-search
            v-model="searchKeyword"
            placeholder="Tìm kiếm sản phẩm..."
            style="width: 300px"
            @search="handleSearch"
            @input="handleSearch"
          />
          <a-select v-model="selectedCategory" placeholder="Chọn danh mục" style="width: 200px" allow-clear @change="handleCategoryChange">
            <a-option v-for="category in categories" :key="category.id" :value="category.id" :label="category.tenDanhMuc" />
          </a-select>
          <a-button type="primary" @click="refreshData">
            <template #icon>
              <icon-refresh />
            </template>
            Làm mới
          </a-button>
        </a-space>
      </div>

      <!-- Data Source Notice -->
      <div v-if="isUsingMockData" class="data-notice">
        <a-alert
          type="warning"
          message="Đang hiển thị dữ liệu mẫu"
          description="Không thể kết nối đến API sản phẩm. Đang sử dụng dữ liệu mẫu để demo."
          show-icon
          closable
        />
      </div>

      <!-- Product Table -->
      <div class="table-wrapper">
        <a-spin :loading="loading" style="width: 100%">
          <a-table
            :columns="tableColumns"
            :data="filteredProducts"
            :pagination="paginationConfig"
            row-key="id"
            :scroll="{ x: 1200, y: 'calc(100vh - 400px)' }"
            class="product-table"
          >
            <!-- STT Column -->
            <template #stt="{ rowIndex }">
              <span class="stt-number">{{ rowIndex + 1 + (paginationConfig.current - 1) * paginationConfig.pageSize }}</span>
            </template>

            <!-- Product Image Column -->
            <template #image="{ record }">
              <div class="product-image-container">
                <img :src="getProductImage(record)" :alt="record.tenSanPham" class="product-image" @error="handleImageError" />
              </div>
            </template>

            <!-- Product Name Column -->
            <template #name="{ record }">
              <div class="product-name-container">
                <div class="product-name">{{ record.tenSanPham }}</div>
                <div class="product-code">Mã: {{ record.maChiTietSanPham || `SP${String(record.id).padStart(5, '0')}` }}</div>
              </div>
            </template>

            <!-- Attributes Column -->
            <template #attributes="{ record }">
              <div class="attributes-container">
                <div class="attribute-row">
                  <a-tag v-if="record.tenMauSac" size="small" color="blue">{{ record.tenMauSac }}</a-tag>
                  <a-tag v-if="record.tenKichThuoc" size="small" color="green">{{ record.tenKichThuoc }}</a-tag>
                </div>
                <div class="attribute-row">
                  <a-tag v-if="record.tenChatLieu" size="small" color="purple">{{ record.tenChatLieu }}</a-tag>
                  <a-tag v-if="record.tenDeGiay" size="small" color="orange">{{ record.tenDeGiay }}</a-tag>
                </div>
                <div v-if="record.tenTrongLuong" class="attribute-row">
                  <a-tag size="small" color="cyan">{{ record.tenTrongLuong }}</a-tag>
                </div>
              </div>
            </template>

            <!-- Manufacturer Column -->
            <template #manufacturer="{ record }">
              <div class="manufacturer-info">
                <span v-if="record.tenNhaSanXuat" class="manufacturer-name">{{ record.tenNhaSanXuat }}</span>
                <span v-else class="no-data">-</span>
              </div>
            </template>

            <!-- Origin Column -->
            <template #origin="{ record }">
              <div class="origin-info">
                <span v-if="record.tenXuatXu" class="origin-name">{{ record.tenXuatXu }}</span>
                <span v-else class="no-data">-</span>
              </div>
            </template>

            <!-- Quantity Column -->
            <template #quantity="{ record }">
              <div class="quantity-container">
                <a-tag :color="getStockColor(record)" class="quantity-tag">
                  {{ record.soLuong || 0 }}
                </a-tag>
                <div class="stock-status">{{ getStockStatus(record) }}</div>
              </div>
            </template>

            <!-- Price Column -->
            <template #price="{ record }">
              <div class="price-container">
                <div class="current-price">{{ formatCurrency(getProductPrice(record)) }}</div>
                <div v-if="record.giaTriGiamGia && record.giaTriGiamGia > 0" class="discount-info">
                  <span class="original-price">{{ formatCurrency(record.giaBan) }}</span>
                  <a-tag color="red" size="small">-{{ record.giaTriGiamGia }}%</a-tag>
                </div>
                <div v-if="record.tenDotGiamGia" class="promotion-name">
                  <a-tag color="gold" size="small">{{ record.tenDotGiamGia }}</a-tag>
                </div>
              </div>
            </template>

            <!-- Status Column -->
            <template #status="{ record }">
              <div class="status-container">
                <a-tag :color="record.trangThai ? 'green' : 'red'" class="status-tag">
                  {{ record.trangThai ? 'Hoạt động' : 'Ngừng bán' }}
                </a-tag>
                <div v-if="record.deleted" class="deleted-indicator">
                  <a-tag color="gray" size="small">Đã xóa</a-tag>
                </div>
              </div>
            </template>
          </a-table>
        </a-spin>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { IconApps, IconRefresh } from '@arco-design/web-vue/es/icon'
import { getBienTheSanPhamPage, type BienTheSanPham } from '@/api/san-pham/bien-the'
import { getDanhMucSanPhamList } from '@/api/san-pham/danh-muc'

// Props
interface Props {
  showActions?: boolean
  selectable?: boolean
  maxHeight?: string
}

const props = withDefaults(defineProps<Props>(), {
  showActions: false,
  selectable: false,
  maxHeight: 'calc(100vh - 400px)',
})

// Emits
const emit = defineEmits<{
  productSelect: [product: BienTheSanPham]
  refresh: []
}>()

// Reactive data
const loading = ref(false)
const products = ref<BienTheSanPham[]>([])
const categories = ref<any[]>([])
const searchKeyword = ref('')
const selectedCategory = ref<number | undefined>(undefined)
const isUsingMockData = ref(false)

// Pagination
const paginationConfig = ref({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `Tổng ${total} sản phẩm`,
  pageSizeOptions: ['10', '20', '50', '100'],
})

// Table columns configuration
const tableColumns = computed(() => {
  const baseColumns = [
    {
      title: 'STT',
      dataIndex: 'stt',
      key: 'stt',
      width: 60,
      align: 'center' as const,
      slotName: 'stt',
    },
    {
      title: 'Ảnh sản phẩm',
      dataIndex: 'image',
      key: 'image',
      width: 100,
      align: 'center' as const,
      slotName: 'image',
    },
    {
      title: 'Tên sản phẩm',
      dataIndex: 'tenSanPham',
      key: 'name',
      width: 250,
      slotName: 'name',
    },
    {
      title: 'Thuộc tính',
      key: 'attributes',
      width: 200,
      slotName: 'attributes',
    },
    {
      title: 'Nhà sản xuất',
      dataIndex: 'tenNhaSanXuat',
      key: 'manufacturer',
      width: 150,
      slotName: 'manufacturer',
    },
    {
      title: 'Xuất xứ',
      dataIndex: 'tenXuatXu',
      key: 'origin',
      width: 120,
      slotName: 'origin',
    },
    {
      title: 'Số lượng',
      dataIndex: 'soLuong',
      key: 'quantity',
      width: 100,
      align: 'center' as const,
      slotName: 'quantity',
    },
    {
      title: 'Giá',
      dataIndex: 'giaBan',
      key: 'price',
      width: 150,
      align: 'right' as const,
      slotName: 'price',
    },
    {
      title: 'Trạng thái',
      key: 'status',
      width: 100,
      align: 'center' as const,
      slotName: 'status',
    },
  ]

  // Add action column if needed
  if (props.showActions) {
    baseColumns.push({
      title: 'Thao tác',
      key: 'action',
      width: 120,
      align: 'center' as const,
      slotName: 'action',
    })
  }

  return baseColumns
})

// Computed properties
const filteredProducts = computed(() => {
  let filtered = products.value

  // Search filter
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(
      (product) =>
        product.tenSanPham?.toLowerCase().includes(keyword) ||
        product.maChiTietSanPham?.toLowerCase().includes(keyword) ||
        product.tenMauSac?.toLowerCase().includes(keyword) ||
        product.tenKichThuoc?.toLowerCase().includes(keyword)
    )
  }

  // Category filter (if implemented)
  if (selectedCategory.value) {
    // This would need to be implemented based on your category structure
    // filtered = filtered.filter(product => product.categoryId === selectedCategory.value)
  }

  return filtered
})

// Mock data fallback
const getMockProducts = () => {
  return [
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
    {
      id: 6,
      tenSanPham: 'Giày sneaker Converse',
      maChiTietSanPham: 'SP006',
      anhSanPham: ['https://via.placeholder.com/200x150/13c2c2/ffffff?text=Converse'],
      soLuong: 20,
      giaBan: 2500000,
      giaTriGiamGia: 0,
      trangThai: true,
      tenMauSac: 'Trắng',
      tenKichThuoc: '39',
      tenNhaSanXuat: 'Converse',
      tenXuatXu: 'Mỹ',
      tenChatLieu: 'Vải canvas',
      tenDeGiay: 'Đế cao su',
      tenTrongLuong: '300g',
    },
    {
      id: 7,
      tenSanPham: 'Giày thể thao Puma',
      maChiTietSanPham: 'SP007',
      anhSanPham: ['https://via.placeholder.com/200x150/eb2f96/ffffff?text=Puma'],
      soLuong: 12,
      giaBan: 2500000,
      giaTriGiamGia: 0,
      trangThai: true,
      tenMauSac: 'Xanh',
      tenKichThuoc: '44',
      tenNhaSanXuat: 'Puma',
      tenXuatXu: 'Đức',
      tenChatLieu: 'Da tổng hợp',
      tenDeGiay: 'Đế cao su',
      tenTrongLuong: '380g',
    },
    {
      id: 8,
      tenSanPham: 'Giày sandal Birkenstock',
      maChiTietSanPham: 'SP008',
      anhSanPham: ['https://via.placeholder.com/200x150/52c41a/ffffff?text=Birkenstock'],
      soLuong: 8,
      giaBan: 2500000,
      giaTriGiamGia: 0,
      trangThai: true,
      tenMauSac: 'Nâu',
      tenKichThuoc: '41',
      tenNhaSanXuat: 'Birkenstock',
      tenXuatXu: 'Đức',
      tenChatLieu: 'Da thật',
      tenDeGiay: 'Đế gỗ',
      tenTrongLuong: '250g',
    },
    {
      id: 9,
      tenSanPham: 'Giày Oxford Clarks',
      maChiTietSanPham: 'SP009',
      anhSanPham: ['https://via.placeholder.com/200x150/722ed1/ffffff?text=Clarks'],
      soLuong: 18,
      giaBan: 2500000,
      giaTriGiamGia: 0,
      trangThai: true,
      tenMauSac: 'Đen',
      tenKichThuoc: '42',
      tenNhaSanXuat: 'Clarks',
      tenXuatXu: 'Anh',
      tenChatLieu: 'Da thật',
      tenDeGiay: 'Đế da',
      tenTrongLuong: '450g',
    },
    {
      id: 10,
      tenSanPham: 'Giày thể thao New Balance',
      maChiTietSanPham: 'SP010',
      anhSanPham: ['https://via.placeholder.com/200x150/13c2c2/ffffff?text=New+Balance'],
      soLuong: 22,
      giaBan: 2500000,
      giaTriGiamGia: 0,
      trangThai: true,
      tenMauSac: 'Xám',
      tenKichThuoc: '40',
      tenNhaSanXuat: 'New Balance',
      tenXuatXu: 'Mỹ',
      tenChatLieu: 'Vải mesh',
      tenDeGiay: 'Đế cao su',
      tenTrongLuong: '320g',
    },
  ]
}

const loadProducts = async () => {
  try {
    loading.value = true
    const response = await getBienTheSanPhamPage(paginationConfig.value.current - 1, undefined, paginationConfig.value.pageSize)

    // Response đã được interceptor xử lý, nên response.data chính là data từ backend
    if (response.success) {
      products.value = response.data.data
      paginationConfig.value.total = response.data.totalElements
      isUsingMockData.value = false
    } else {
      // Fallback to mock data if API fails
      products.value = getMockProducts()
      paginationConfig.value.total = products.value.length
      isUsingMockData.value = true
    }
  } catch (error) {
    // Use mock data as fallback
    products.value = getMockProducts()
    paginationConfig.value.total = products.value.length
    isUsingMockData.value = true
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  try {
    const response = await getDanhMucSanPhamList()
    categories.value = response.data || []
  } catch (error) {
    // Silently handle error
    categories.value = []
  }
}

const refreshData = () => {
  loadProducts()
  emit('refresh')
}

const handleSearch = () => {
  // Search is handled by computed property
}

const handleCategoryChange = () => {
  // Category filtering is handled by computed property
}

const getProductImage = (product: BienTheSanPham) => {
  if (product.anhSanPham && product.anhSanPham.length > 0) {
    return product.anhSanPham[0]
  }
  return `https://via.placeholder.com/100x100/1890ff/ffffff?text=${encodeURIComponent(product.tenSanPham || 'SP')}`
}

const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement
  img.src = 'https://via.placeholder.com/100x100/e5e5e5/999999?text=No+Image'
}

const getProductPrice = (product: BienTheSanPham) => {
  if (product.giaTriGiamGia && product.giaTriGiamGia > 0) {
    return product.giaBan * (1 - product.giaTriGiamGia / 100)
  }
  return product.giaBan || 0
}

const getStockColor = (product: BienTheSanPham) => {
  const stock = product.soLuong || 0
  if (stock > 10) return 'green'
  if (stock > 0) return 'orange'
  return 'red'
}

const getStockStatus = (product: BienTheSanPham) => {
  const stock = product.soLuong || 0
  if (stock > 10) return 'Còn hàng'
  if (stock > 0) return 'Sắp hết'
  return 'Hết hàng'
}

const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount)
}

// Watch pagination changes
watch(
  () => paginationConfig.value.current,
  () => {
    loadProducts()
  }
)

watch(
  () => paginationConfig.value.pageSize,
  () => {
    paginationConfig.value.current = 1
    loadProducts()
  }
)

// Lifecycle
onMounted(() => {
  loadProducts()
  loadCategories()
})
</script>

<style scoped lang="less">
.product-table-container {
  width: 100%;

  .product-table-card {
    .card-header {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: 600;
      font-size: 16px;
    }
  }
}

.table-controls {
  margin-bottom: 16px;
  padding: 16px 0;
  border-bottom: 1px solid var(--color-border-2);
}

.data-notice {
  margin-bottom: 16px;
}

.table-wrapper {
  .product-table {
    .stt-number {
      font-weight: 600;
      color: var(--color-text-2);
    }

    .product-image-container {
      display: flex;
      justify-content: center;
      align-items: center;

      .product-image {
        width: 60px;
        height: 60px;
        object-fit: cover;
        border-radius: 8px;
        border: 1px solid var(--color-border-2);
        transition: transform 0.2s ease;

        &:hover {
          transform: scale(1.05);
        }
      }
    }

    .product-name-container {
      .product-name {
        font-weight: 600;
        color: var(--color-text-1);
        margin-bottom: 4px;
        line-height: 1.4;
      }

      .product-code {
        font-size: 12px;
        color: var(--color-text-3);
      }
    }

    .attributes-container {
      .attribute-row {
        display: flex;
        gap: 4px;
        margin-bottom: 4px;
        flex-wrap: wrap;

        &:last-child {
          margin-bottom: 0;
        }
      }
    }

    .manufacturer-info,
    .origin-info {
      .manufacturer-name,
      .origin-name {
        font-weight: 500;
        color: var(--color-text-1);
      }

      .no-data {
        color: var(--color-text-3);
        font-style: italic;
      }
    }

    .quantity-container {
      text-align: center;

      .quantity-tag {
        font-weight: 600;
        margin-bottom: 4px;
      }

      .stock-status {
        font-size: 12px;
        color: var(--color-text-3);
      }
    }

    .price-container {
      text-align: right;

      .current-price {
        font-weight: 600;
        color: var(--color-text-1);
        font-size: 14px;
        margin-bottom: 4px;
      }

      .discount-info {
        display: flex;
        flex-direction: column;
        align-items: flex-end;
        gap: 2px;

        .original-price {
          font-size: 12px;
          color: var(--color-text-3);
          text-decoration: line-through;
        }
      }

      .promotion-name {
        margin-top: 4px;
        display: flex;
        justify-content: flex-end;
      }
    }

    .status-container {
      text-align: center;

      .status-tag {
        margin-bottom: 4px;
      }

      .deleted-indicator {
        margin-top: 2px;
      }
    }
  }
}

// Responsive design
@media (max-width: 768px) {
  .table-controls {
    .arco-space {
      flex-direction: column;
      align-items: stretch;

      .arco-space-item {
        width: 100%;
      }
    }
  }

  .product-table {
    .product-image-container .product-image {
      width: 40px;
      height: 40px;
    }

    .product-name-container {
      .product-name {
        font-size: 14px;
      }

      .product-attributes {
        .arco-tag {
          font-size: 10px;
        }
      }
    }
  }
}
</style>
