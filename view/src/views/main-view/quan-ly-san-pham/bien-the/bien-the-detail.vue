<template>
  <div class="variant-detail-page">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- Loading State -->
    <div v-if="loading" class="loading-container">
      <a-spin :size="40" />
    </div>

    <!-- Detail Content -->
    <div v-else-if="variant" class="detail-content">
      <!-- Header Card -->
      <a-card class="header-card">
        <template #extra>
          <a-space>
            <a-button @click="goBack">
              <template #icon>
                <icon-arrow-left />
              </template>
              Quay lại
            </a-button>
            <a-button type="primary" @click="goToEdit">
              <template #icon>
                <icon-edit />
              </template>
              Chỉnh sửa
            </a-button>
          </a-space>
        </template>
        <div class="variant-header">
          <div class="variant-image">
            <a-avatar
              :image-url="
                Array.isArray(variant.anhSanPham) && variant.anhSanPham.length > 0 ? variant.anhSanPham[0] : '/default-product.png'
              "
              :size="100"
              shape="square"
            />
          </div>
          <div class="variant-info">
            <h2>{{ variant.tenSanPham }}</h2>
            <p class="variant-code">
              Mã biến thể:
              <strong>{{ variant.maChiTietSanPham || variant.id }}</strong>
            </p>
            <a-tag :color="variant.trangThai ? 'green' : 'red'" size="large">
              {{ variant.trangThai ? 'Đang bán' : 'Tạm ngưng bán' }}
            </a-tag>
          </div>
        </div>
      </a-card>

      <!-- Detail Information -->
      <a-row :gutter="16">
        <!-- Basic Information -->
        <a-col :span="12">
          <a-card title="Thông tin cơ bản" class="info-card">
            <a-descriptions :column="1" bordered>
              <a-descriptions-item label="Tên sản phẩm">
                {{ variant.tenSanPham }}
              </a-descriptions-item>
              <a-descriptions-item label="Mã biến thể">
                {{ variant.maChiTietSanPham }}
              </a-descriptions-item>
              <a-descriptions-item label="Giá bán">
                <span class="price-text">{{ formatCurrency(variant.giaBan) }}</span>
              </a-descriptions-item>
              <a-descriptions-item label="Giá trị giảm giá">{{ variant.giaTriGiamGia || 0 }}%</a-descriptions-item>
              <a-descriptions-item label="Số lượng tồn kho">
                <span :class="{ 'low-stock': variant.soLuong < 10 }">
                  {{ variant.soLuong }}
                </span>
              </a-descriptions-item>
              <a-descriptions-item label="Trạng thái">
                <a-tag :color="variant.trangThai ? 'green' : 'red'">
                  {{ variant.trangThai ? 'Đang bán' : 'Tạm ngưng bán' }}
                </a-tag>
              </a-descriptions-item>
            </a-descriptions>
          </a-card>
        </a-col>

        <!-- Attributes -->
        <a-col :span="12">
          <a-card title="Thuộc tính sản phẩm" class="info-card">
            <a-descriptions :column="1" bordered>
              <a-descriptions-item label="Nhà sản xuất">
                {{ variant.tenNhaSanXuat }}
              </a-descriptions-item>
              <a-descriptions-item label="Xuất xứ">
                {{ variant.tenXuatXu }}
              </a-descriptions-item>
              <a-descriptions-item label="Màu sắc">
                {{ variant.tenMauSac }}
              </a-descriptions-item>
              <a-descriptions-item label="Kích thước">
                {{ variant.tenKichThuoc }}
              </a-descriptions-item>
              <a-descriptions-item label="Chất liệu">
                {{ variant.tenChatLieu }}
              </a-descriptions-item>
              <a-descriptions-item label="Đế giày">
                {{ variant.tenDeGiay }}
              </a-descriptions-item>
              <a-descriptions-item label="Trọng lượng">
                {{ variant.tenTrongLuong }}
              </a-descriptions-item>
            </a-descriptions>
          </a-card>
        </a-col>
      </a-row>

      <!-- Images Gallery -->
      <a-card v-if="variant.anhSanPham && variant.anhSanPham.length > 0" title="Hình ảnh sản phẩm" class="images-card">
        <div class="images-gallery">
          <div v-for="(image, index) in variant.anhSanPham" :key="index" class="image-item">
            <a-image :src="image" :width="150" :height="150" fit="cover" />
          </div>
        </div>
      </a-card>
    </div>

    <!-- Error State -->
    <div v-else class="error-container">
      <a-result status="404" title="Không tìm thấy biến thể" sub-title="Biến thể bạn đang tìm kiếm không tồn tại hoặc đã bị xóa.">
        <template #extra>
          <a-button type="primary" @click="goBack">Quay lại</a-button>
        </template>
      </a-result>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import { IconArrowLeft, IconEdit } from '@arco-design/web-vue/es/icon'
import { getBienTheById, type BienTheSanPham } from '@/api/san-pham/bien-the'

// Router
const route = useRoute()
const router = useRouter()

// Breadcrumb
const { breadcrumbItems } = useBreadcrumb()

// State
const loading = ref(true)
const variant = ref<BienTheSanPham | null>(null)

// Get variant ID from route
const variantId = Number(route.params.id)

// Dynamic page title cho tab-bar
const pageTitle = computed(() => {
  if (variant.value) {
    return `Chi tiết - ${variant.value.tenSanPham || 'Biến thể'}`
  }
  return 'Chi tiết biến thể'
})

// Cập nhật document title
watch(
  pageTitle,
  (newTitle) => {
    if (typeof document !== 'undefined') {
      document.title = newTitle
    }
  },
  { immediate: true }
)

// Methods
const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount)
}

const goBack = () => {
  router.go(-1)
}

const goToEdit = () => {
  router.push(`/quan-ly-san-pham/bien-the/update/${variantId}`)
}

// Load variant data
const loadVariantDetail = async () => {
  try {
    loading.value = true
    const response = await getBienTheById(variantId)
    if (response.data && response.success) {
      variant.value = response.data
    } else {
      Message.error('Không thể tải thông tin biến thể')
    }
  } catch (error) {
    console.error('Error loading variant detail:', error)
    Message.error('Có lỗi xảy ra khi tải thông tin biến thể')
  } finally {
    loading.value = false
  }
}

// Lifecycle
onMounted(() => {
  if (variantId) {
    loadVariantDetail()
  } else {
    Message.error('ID biến thể không hợp lệ')
    goBack()
  }
})
</script>

<style scoped>
.variant-detail-page {
  padding: 0 20px 20px 20px;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.error-container {
  margin-top: 40px;
}

.header-card {
  margin-bottom: 16px;
}

.variant-header {
  display: flex;
  align-items: center;
  gap: 20px;
}

.variant-image {
  flex-shrink: 0;
}

.variant-info h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
}

.variant-code {
  margin: 0 0 12px 0;
  color: #666;
}

.info-card {
  margin-bottom: 16px;
}

.price-text {
  font-size: 18px;
  font-weight: 600;
  color: #f53f3f;
}

.low-stock {
  color: #f53f3f;
  font-weight: 600;
}

.images-card {
  margin-bottom: 16px;
}

.images-gallery {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.image-item {
  border-radius: 6px;
  overflow: hidden;
}

:deep(.arco-descriptions-item-label) {
  font-weight: 600;
  background-color: #f7f8fa;
}

:deep(.arco-descriptions-item-value) {
  background-color: white;
}
</style>
