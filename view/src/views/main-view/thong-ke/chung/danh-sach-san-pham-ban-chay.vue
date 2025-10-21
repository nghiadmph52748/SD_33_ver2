<template>
  <a-card class="chart-card">
    <template #title>
      <div class="chart-title">
        <span>Sản Phẩm Bán Chạy</span>
      </div>
    </template>
    <template #extra>
      <a-select :model-value="kyThongKe" style="width: 120px" @change="$emit('update:kyThongKe', $event)">
        <a-option value="week">Tuần này</a-option>
        <a-option value="month">Tháng này</a-option>
      </a-select>
    </template>
    <div class="top-products-list">
      <div v-for="(sanPham, index) in duLieuSanPham" :key="sanPham.name" class="product-item">
        <div class="product-rank">
          <span class="rank-number">{{ index + 1 }}</span>
        </div>
        <div class="product-info">
          <div class="product-name">{{ sanPham.name }}</div>
          <div class="product-stats">
            <span class="quantity">số lượng: {{ sanPham.value }}</span>
            <span class="revenue">doanh thu: {{ dinhDangTien(sanPham.revenue) }}</span>
          </div>
        </div>
        <div class="product-badge">
          <span class="badge">Hot</span>
        </div>
      </div>
    </div>
  </a-card>
</template>

<script setup lang="ts">
import type { SanPhamBanChay } from '../types/thong-ke.types'

interface Props {
  duLieuSanPham: SanPhamBanChay[]
  kyThongKe: string
}

defineProps<Props>()

interface Emits {
  (e: 'update:kyThongKe', value: string): void
}

defineEmits<Emits>()

const dinhDangTien = (soTien: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(soTien)
}
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

.top-products-list {
  max-height: 300px;
  overflow-y: auto;
  padding: 8px 0;
}

.product-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  margin-bottom: 8px;
  background: #fafafa;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
  transition: all 0.3s ease;
  font-weight: 400;
}

.product-item:hover {
  background: #f0f8ff;
  border-color: #1890ff;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.15);
}

.product-item:last-child {
  margin-bottom: 0;
}

.product-rank {
  margin-right: 16px;
  flex-shrink: 0;
}

.rank-number {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: #1890ff;
  color: white;
  border-radius: 50%;
  font-weight: 700;
  font-size: 14px;
}

.product-item:nth-child(1) .rank-number {
  background: #ff4d4f;
}

.product-item:nth-child(2) .rank-number {
  background: #fa8c16;
}

.product-item:nth-child(3) .rank-number {
  background: #52c41a;
}

.product-info {
  flex: 1;
  min-width: 0;
}

.product-name {
  font-size: 14px !important;
  font-weight: 500 !important;
  color: #1d2129 !important;
  line-height: 1.4 !important;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-stats {
  display: flex;
  gap: 16px;
  font-size: 14px !important;
  color: #1d2129 !important;
  font-weight: 400 !important;
  line-height: 1.4 !important;
}

.quantity {
  color: #1d2129 !important;
  font-weight: 400 !important;
}

.revenue {
  color: #1d2129 !important;
  font-weight: 400 !important;
}

.product-badge {
  margin-left: 12px;
  flex-shrink: 0;
}

.badge {
  display: inline-block;
  padding: 4px 8px;
  background: #ff4d4f;
  color: white;
  border-radius: 12px;
  font-size: 10px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
</style>
