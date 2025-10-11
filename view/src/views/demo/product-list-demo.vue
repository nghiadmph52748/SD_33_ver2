<template>
  <div class="product-list-demo">
    <Breadcrumb />
    
    <div class="demo-container">
      <a-card :bordered="false" class="demo-card">
        <template #title>
          <div class="demo-title">
            <icon-apps />
            <span>Demo - Bảng danh sách sản phẩm</span>
          </div>
        </template>
        
        <template #extra>
          <a-space>
            <a-button type="primary" @click="refreshProducts">
              <template #icon>
                <icon-refresh />
              </template>
              Làm mới dữ liệu
            </a-button>
            <a-button @click="toggleViewMode">
              <template #icon>
                <icon-settings />
              </template>
              Cài đặt
            </a-button>
          </a-space>
        </template>

        <div class="demo-content">
          <!-- Product Table Component -->
          <ProductTable
            :show-actions="showActions"
            :selectable="selectable"
            :max-height="maxHeight"
            @product-select="handleProductSelect"
            @refresh="handleRefresh"
          />
        </div>
      </a-card>

      <!-- Settings Panel -->
      <a-drawer
        v-model:visible="showSettings"
        title="Cài đặt hiển thị"
        width="400px"
        placement="right"
      >
        <div class="settings-panel">
          <a-form :model="settings" layout="vertical">
            <a-form-item label="Hiển thị cột thao tác">
              <a-switch v-model="showActions" />
            </a-form-item>
            
            <a-form-item label="Cho phép chọn sản phẩm">
              <a-switch v-model="selectable" />
            </a-form-item>
            
            <a-form-item label="Chiều cao tối đa">
              <a-input v-model="maxHeight" placeholder="Ví dụ: calc(100vh - 400px)" />
            </a-form-item>
            
            <a-form-item label="Số sản phẩm mỗi trang">
              <a-select v-model="pageSize" @change="handlePageSizeChange">
                <a-option :value="10">10 sản phẩm</a-option>
                <a-option :value="20">20 sản phẩm</a-option>
                <a-option :value="50">50 sản phẩm</a-option>
                <a-option :value="100">100 sản phẩm</a-option>
              </a-select>
            </a-form-item>
          </a-form>
          
          <a-divider />
          
          <div class="settings-info">
            <h4>Thông tin component:</h4>
            <ul>
              <li><strong>STT:</strong> Số thứ tự tự động</li>
              <li><strong>Tên sản phẩm:</strong> Hiển thị tên, mã và thuộc tính</li>
              <li><strong>Ảnh sản phẩm:</strong> Ảnh thumbnail 60x60px</li>
              <li><strong>Số lượng:</strong> Hiển thị số lượng và trạng thái tồn kho</li>
              <li><strong>Giá:</strong> Giá hiện tại và giá gốc (nếu có giảm giá)</li>
            </ul>
          </div>
        </div>
      </a-drawer>

      <!-- Selected Product Info -->
      <a-card v-if="selectedProduct" class="selected-product-card" :bordered="false">
        <template #title>
          <div class="card-header">
            <icon-info-circle />
            <span>Thông tin sản phẩm được chọn</span>
          </div>
        </template>
        
        <div class="selected-product-info">
          <div class="product-detail">
            <img :src="getProductImage(selectedProduct)" :alt="selectedProduct.tenSanPham" class="detail-image" />
            <div class="detail-content">
              <h3>{{ selectedProduct.tenSanPham }}</h3>
              <p><strong>Mã sản phẩm:</strong> {{ selectedProduct.maChiTietSanPham }}</p>
              <p><strong>Số lượng:</strong> {{ selectedProduct.soLuong }}</p>
              <p><strong>Giá:</strong> {{ formatCurrency(getProductPrice(selectedProduct)) }}</p>
              <div v-if="selectedProduct.tenMauSac || selectedProduct.tenKichThuoc" class="attributes">
                <a-tag v-if="selectedProduct.tenMauSac" color="blue">{{ selectedProduct.tenMauSac }}</a-tag>
                <a-tag v-if="selectedProduct.tenKichThuoc" color="green">{{ selectedProduct.tenKichThuoc }}</a-tag>
              </div>
            </div>
          </div>
        </div>
      </a-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import ProductTable from '@/components/product-table/product-table.vue'
import { IconApps, IconRefresh, IconSettings, IconInfoCircle } from '@arco-design/web-vue/es/icon'
import type { BienTheSanPham } from '@/api/san-pham/bien-the'

// Reactive data
const showSettings = ref(false)
const showActions = ref(false)
const selectable = ref(false)
const maxHeight = ref('calc(100vh - 400px)')
const pageSize = ref(20)
const selectedProduct = ref<BienTheSanPham | null>(null)

const settings = reactive({
  showActions: false,
  selectable: false,
  maxHeight: 'calc(100vh - 400px)',
  pageSize: 20
})

// Methods
const refreshProducts = () => {
  console.log('Refreshing products...')
}

const toggleViewMode = () => {
  showSettings.value = true
}

const handleProductSelect = (product: BienTheSanPham) => {
  selectedProduct.value = product
  console.log('Product selected:', product)
}

const handleRefresh = () => {
  console.log('Data refreshed')
}

const handlePageSizeChange = (value: number) => {
  pageSize.value = value
  console.log('Page size changed to:', value)
}

const getProductImage = (product: BienTheSanPham) => {
  if (product.anhSanPham && product.anhSanPham.length > 0) {
    return product.anhSanPham[0]
  }
  return `https://via.placeholder.com/200x150/1890ff/ffffff?text=${encodeURIComponent(product.tenSanPham || 'SP')}`
}

const getProductPrice = (product: BienTheSanPham) => {
  if (product.giaTriGiamGia && product.giaTriGiamGia > 0) {
    return product.giaBan * (1 - product.giaTriGiamGia / 100)
  }
  return product.giaBan || 0
}

const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount)
}
</script>

<style scoped lang="less">
.product-list-demo {
  padding: 20px;
  
  .demo-container {
    max-width: 1400px;
    margin: 0 auto;
    
    .demo-card {
      .demo-title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-weight: 600;
        font-size: 18px;
      }
      
      .demo-content {
        margin-top: 20px;
      }
    }
  }
}

.settings-panel {
  .settings-info {
    margin-top: 20px;
    
    h4 {
      margin-bottom: 12px;
      color: var(--color-text-1);
    }
    
    ul {
      margin: 0;
      padding-left: 20px;
      
      li {
        margin-bottom: 8px;
        line-height: 1.5;
        
        strong {
          color: var(--color-text-1);
        }
      }
    }
  }
}

.selected-product-card {
  margin-top: 20px;
  
  .card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 600;
  }
  
  .selected-product-info {
    .product-detail {
      display: flex;
      gap: 20px;
      align-items: flex-start;
      
      .detail-image {
        width: 120px;
        height: 120px;
        object-fit: cover;
        border-radius: 12px;
        border: 1px solid var(--color-border-2);
      }
      
      .detail-content {
        flex: 1;
        
        h3 {
          margin: 0 0 12px 0;
          color: var(--color-text-1);
          font-size: 18px;
        }
        
        p {
          margin: 8px 0;
          color: var(--color-text-2);
          line-height: 1.5;
          
          strong {
            color: var(--color-text-1);
          }
        }
        
        .attributes {
          margin-top: 12px;
          display: flex;
          gap: 8px;
        }
      }
    }
  }
}

// Responsive design
@media (max-width: 768px) {
  .product-list-demo {
    padding: 10px;
  }
  
  .selected-product-info .product-detail {
    flex-direction: column;
    text-align: center;
    
    .detail-image {
      width: 100px;
      height: 100px;
      margin: 0 auto;
    }
  }
}
</style>
