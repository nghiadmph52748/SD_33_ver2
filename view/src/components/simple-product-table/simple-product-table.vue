<template>
  <div class="simple-product-table">
    <a-table :columns="columns" :data="products" :pagination="pagination" :loading="loading" row-key="id" :scroll="{ x: 600 }">
      <!-- STT Column -->
      <template #stt="{ rowIndex }">
        <span class="stt">{{ rowIndex + 1 }}</span>
      </template>

      <!-- Product Image -->
      <template #image="{ record }">
        <img :src="getImageUrl(record)" :alt="record.tenSanPham" class="product-img" @error="handleImageError" />
      </template>

      <!-- Product Name -->
      <template #name="{ record }">
        <div class="product-name">
          <div class="name">{{ record.tenSanPham }}</div>
          <div class="code">{{ record.maChiTietSanPham || `SP${String(record.id).padStart(5, '0')}` }}</div>
        </div>
      </template>

      <!-- Attributes -->
      <template #attributes="{ record }">
        <div class="attributes">
          <a-tag v-if="record.tenMauSac" size="small" color="blue">{{ record.tenMauSac }}</a-tag>
          <a-tag v-if="record.tenKichThuoc" size="small" color="green">{{ record.tenKichThuoc }}</a-tag>
          <a-tag v-if="record.tenChatLieu" size="small" color="purple">{{ record.tenChatLieu }}</a-tag>
        </div>
      </template>

      <!-- Manufacturer -->
      <template #manufacturer="{ record }">
        <span v-if="record.tenNhaSanXuat" class="manufacturer">{{ record.tenNhaSanXuat }}</span>
        <span v-else class="no-data">-</span>
      </template>

      <!-- Quantity -->
      <template #quantity="{ record }">
        <a-tag :color="getStockColor(record.soLuong)">
          {{ record.soLuong || 0 }}
        </a-tag>
      </template>

      <!-- Price -->
      <template #price="{ record }">
        <div class="price-info">
          <div class="current-price">{{ formatPrice(getProductPrice(record)) }}</div>
          <div v-if="record.giaTriGiamGia && record.giaTriGiamGia > 0" class="discount">
            <span class="original-price">{{ formatPrice(record.giaBan) }}</span>
            <a-tag color="red" size="small">-{{ record.giaTriGiamGia }}%</a-tag>
          </div>
        </div>
      </template>

      <!-- Status -->
      <template #status="{ record }">
        <a-tag :color="record.trangThai ? 'green' : 'red'" size="small">
          {{ record.trangThai ? 'Hoạt động' : 'Ngừng bán' }}
        </a-tag>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type { BienTheSanPham } from '@/api/san-pham/bien-the'

// Props
interface Props {
  products: BienTheSanPham[]
  loading?: boolean
  showPagination?: boolean
  pageSize?: number
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  showPagination: true,
  pageSize: 10,
})

// Table columns
const columns = [
  {
    title: 'STT',
    dataIndex: 'stt',
    key: 'stt',
    width: 50,
    align: 'center' as const,
    slotName: 'stt',
  },
  {
    title: 'Ảnh',
    dataIndex: 'image',
    key: 'image',
    width: 70,
    align: 'center' as const,
    slotName: 'image',
  },
  {
    title: 'Tên sản phẩm',
    dataIndex: 'tenSanPham',
    key: 'name',
    width: 200,
    slotName: 'name',
  },
  {
    title: 'Thuộc tính',
    key: 'attributes',
    width: 150,
    slotName: 'attributes',
  },
  {
    title: 'Nhà SX',
    dataIndex: 'tenNhaSanXuat',
    key: 'manufacturer',
    width: 100,
    slotName: 'manufacturer',
  },
  {
    title: 'Số lượng',
    dataIndex: 'soLuong',
    key: 'quantity',
    width: 80,
    align: 'center' as const,
    slotName: 'quantity',
  },
  {
    title: 'Giá',
    dataIndex: 'giaBan',
    key: 'price',
    width: 120,
    align: 'right' as const,
    slotName: 'price',
  },
  {
    title: 'Trạng thái',
    key: 'status',
    width: 80,
    align: 'center' as const,
    slotName: 'status',
  },
]

// Pagination
const pagination = computed(() => {
  if (!props.showPagination) return false

  return {
    current: 1,
    pageSize: props.pageSize,
    total: props.products.length,
    showSizeChanger: true,
    showQuickJumper: true,
    showTotal: (total: number) => `Tổng ${total} sản phẩm`,
  }
})

// Methods
const getImageUrl = (product: BienTheSanPham) => {
  if (product.anhSanPham && product.anhSanPham.length > 0) {
    return product.anhSanPham[0]
  }
  return `https://via.placeholder.com/60x60/1890ff/ffffff?text=${encodeURIComponent(product.tenSanPham?.charAt(0) || 'SP')}`
}

const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement
  img.src = 'https://via.placeholder.com/60x60/e5e5e5/999999?text=No+Image'
}

const getProductPrice = (product: BienTheSanPham) => {
  if (product.giaTriGiamGia && product.giaTriGiamGia > 0) {
    return product.giaBan * (1 - product.giaTriGiamGia / 100)
  }
  return product.giaBan || 0
}

const getStockColor = (quantity: number) => {
  if (quantity > 10) return 'green'
  if (quantity > 0) return 'orange'
  return 'red'
}

const formatPrice = (amount: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount)
}
</script>

<style scoped lang="less">
.simple-product-table {
  .stt {
    font-weight: 600;
    color: var(--color-text-2);
  }

  .product-img {
    width: 50px;
    height: 50px;
    object-fit: cover;
    border-radius: 6px;
    border: 1px solid var(--color-border-2);
  }

  .product-name {
    .name {
      font-weight: 600;
      color: var(--color-text-1);
      margin-bottom: 4px;
    }

    .code {
      font-size: 12px;
      color: var(--color-text-3);
    }
  }

  .attributes {
    display: flex;
    gap: 4px;
    flex-wrap: wrap;
  }

  .manufacturer {
    font-weight: 500;
    color: var(--color-text-1);
  }

  .no-data {
    color: var(--color-text-3);
    font-style: italic;
  }

  .price-info {
    text-align: right;

    .current-price {
      font-weight: 600;
      color: var(--color-text-1);
      font-size: 13px;
    }

    .discount {
      display: flex;
      flex-direction: column;
      align-items: flex-end;
      gap: 2px;
      margin-top: 2px;

      .original-price {
        font-size: 11px;
        color: var(--color-text-3);
        text-decoration: line-through;
      }
    }
  }
}
</style>
