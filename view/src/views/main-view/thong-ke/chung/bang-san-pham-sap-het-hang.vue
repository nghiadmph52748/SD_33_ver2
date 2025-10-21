<template>
  <a-card class="chart-card">
    <template #title>
      <div class="chart-title">
        <span>Sản phẩm sắp hết hàng</span>
      </div>
    </template>
    <div class="table-container">
      <div v-if="duLieu.length === 0" class="no-data-container">
        <div class="no-data-icon">📦</div>
        <div class="no-data-text">Không có sản phẩm sắp hết hàng</div>
        <div class="no-data-subtext">Tất cả sản phẩm đều có đủ hàng trong kho</div>
      </div>
      <a-table v-else :columns="cot" :data="duLieu" :pagination="phanTrang" :scroll="{ x: 800 }" class="low-stock-table">
        <template #stt="{ record }">
          <span class="rank-cell">{{ record.id }}</span>
        </template>
        <template #anh="{ record }">
          <div class="product-image-cell">
            <img :src="record.anh || '/default-product.png'" :alt="record.tenSanPham" class="product-img" />
          </div>
        </template>
        <template #tenSanPham="{ record }">
          <span class="product-name-cell">{{ record.tenSanPham }}</span>
        </template>
        <template #giaBan="{ record }">
          <span class="price-cell">{{ dinhDangTien(record.giaBan) }}</span>
        </template>
        <template #soLuongTon="{ record }">
          <span class="stock-cell">{{ record.soLuongTon }}</span>
        </template>
        <template #trangThai="{ record }">
          <a-tag :color="layMauTrangThaiKho(record.soLuongTon)">
            {{ record.trangThai }}
          </a-tag>
        </template>
      </a-table>
    </div>
  </a-card>
</template>

<script setup lang="ts">
import type { SanPhamSapHetHang } from '../types/thong-ke.types'

interface Props {
  duLieu: SanPhamSapHetHang[]
  phanTrang: any
}

defineProps<Props>()

const dinhDangTien = (soTien: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(soTien)
}

const layMauTrangThaiKho = (soLuongTon: number): string => {
  if (soLuongTon === 0) return '#ff4d4f' // Đỏ đậm
  if (soLuongTon <= 2) return '#fa8c16' // Cam đậm
  return '#fadb14' // Vàng đậm
}

const cot = [
  {
    title: 'STT',
    dataIndex: 'stt',
    slotName: 'stt',
    width: 80,
    align: 'center' as const,
  },
  {
    title: 'Ảnh',
    dataIndex: 'anh',
    slotName: 'anh',
    width: 100,
    align: 'center' as const,
  },
  {
    title: 'Tên biến thể sản phẩm chi tiết',
    dataIndex: 'tenSanPham',
    slotName: 'tenSanPham',
    width: 300,
    align: 'left' as const,
  },
  {
    title: 'Giá bán',
    dataIndex: 'giaBan',
    slotName: 'giaBan',
    width: 150,
    align: 'right' as const,
  },
  {
    title: 'Số lượng tồn',
    dataIndex: 'soLuongTon',
    slotName: 'soLuongTon',
    width: 150,
    align: 'center' as const,
  },
  {
    title: 'Trạng thái',
    dataIndex: 'trangThai',
    slotName: 'trangThai',
    width: 120,
    align: 'center' as const,
  },
]
</script>

<style scoped>
.chart-card {
  height: 100%;
}

.chart-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 700;
  color: #1a1a1a;
}

.table-container {
  margin-top: 16px;
}

.no-data-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
  background: #fafafa;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
}

.no-data-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.6;
}

.no-data-text {
  font-size: 18px;
  font-weight: 600;
  color: #666;
  margin-bottom: 8px;
}

.no-data-subtext {
  font-size: 14px;
  color: #999;
  line-height: 1.5;
}

.low-stock-table {
  border-radius: 8px;
  overflow: hidden;
}

.low-stock-table :deep(.arco-table-thead) {
  background-color: #f5f5f5;
}

.low-stock-table :deep(.arco-table-thead .arco-table-th) {
  background-color: #f5f5f5;
  font-weight: 600;
  font-size: 14px;
  color: #1d2129;
  border-bottom: 2px solid #e8e8e8;
  padding: 16px 12px;
}

.low-stock-table :deep(.arco-table-tbody .arco-table-tr:hover) {
  background-color: #f8f9fa;
}

.low-stock-table :deep(.arco-table-td) {
  padding: 16px 12px;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
  color: #1d2129;
}

.rank-cell {
  font-weight: 400;
  color: #000000;
}

.product-image-cell {
  display: flex;
  justify-content: center;
  align-items: center;
}

.product-img {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #e8e8e8;
}

.product-name-cell {
  font-weight: 500;
  color: #1d2129;
}

.price-cell {
  font-weight: 500;
  color: #52c41a;
}

.stock-cell {
  font-weight: 600;
  color: #000000;
}
</style>
