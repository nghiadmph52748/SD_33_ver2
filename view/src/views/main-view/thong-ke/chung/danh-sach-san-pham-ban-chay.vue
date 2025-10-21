<template>
  <a-card class="chart-card">
    <template #title>
      <div class="chart-title">
        <span>{{ $t('thongKe.topProducts.title') }}</span>
      </div>
    </template>
    <template #extra>
      <a-select :model-value="kyThongKe" style="width: 120px" @change="$emit('update:kyThongKe', $event)">
        <a-option value="week">{{ $t('thongKe.topProducts.week') }}</a-option>
        <a-option value="month">{{ $t('thongKe.topProducts.month') }}</a-option>
      </a-select>
    </template>
    <div class="top-products-grid">
      <div v-for="(sanPham, index) in duLieuSanPham" :key="sanPham.name" :class="['product-card', `rank-${index + 1}`]">
        <div class="card-header">
          <div class="rank-badge">
            <span v-if="index === 0" class="trophy">🥇</span>
            <span v-else-if="index === 1" class="trophy">🥈</span>
            <span v-else-if="index === 2" class="trophy">🥉</span>
            <span v-else class="rank-text">#{{ index + 1 }}</span>
          </div>
          <div v-if="index < 3" class="hot-badge">{{ $t('thongKe.topProducts.hot') }}</div>
        </div>
        <div class="card-body">
          <div class="product-name">{{ sanPham.name }}</div>
          <div class="product-metrics">
            <div class="metric">
              <div class="metric-label">{{ $t('thongKe.topProducts.sold') }}</div>
              <div class="metric-value">{{ sanPham.value }}</div>
            </div>
            <div class="metric-divider"></div>
            <div class="metric">
              <div class="metric-label">{{ $t('thongKe.topProducts.revenue') }}</div>
              <div class="metric-value">{{ dinhDangTienNgan(sanPham.revenue) }}</div>
            </div>
          </div>
        </div>
        <div class="card-footer">
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: tinhPhanTram(sanPham.value) + '%' }"></div>
          </div>
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

const props = defineProps<Props>()

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

const dinhDangTienNgan = (soTien: number) => {
  if (soTien >= 1000000) {
    return `${(soTien / 1000000).toFixed(1)}M₫`
  }
  if (soTien >= 1000) {
    return `${(soTien / 1000).toFixed(0)}K₫`
  }
  return `${soTien}₫`
}

const tinhPhanTram = (giaTri: number) => {
  if (props.duLieuSanPham.length === 0) return 0
  const max = Math.max(...props.duLieuSanPham.map((sp) => sp.value))
  return max > 0 ? (giaTri / max) * 100 : 0
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

.top-products-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 320px;
  overflow-y: auto;
  padding: 4px 0;
}

.product-card {
  position: relative;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
  transition: all 0.3s ease;
  overflow: hidden;
}

.product-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.1) 0%, rgba(255, 255, 255, 0) 100%);
  pointer-events: none;
}

.product-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.3);
}

.product-card.rank-1 {
  background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%);
  box-shadow: 0 4px 12px rgba(255, 215, 0, 0.3);
}

.product-card.rank-2 {
  background: linear-gradient(135deg, #c0c0c0 0%, #e8e8e8 100%);
  box-shadow: 0 4px 12px rgba(192, 192, 192, 0.3);
}

.product-card.rank-3 {
  background: linear-gradient(135deg, #cd7f32 0%, #daa520 100%);
  box-shadow: 0 4px 12px rgba(205, 127, 50, 0.3);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.rank-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  backdrop-filter: blur(10px);
}

.trophy {
  font-size: 24px;
}

.rank-text {
  font-size: 16px;
  font-weight: 700;
  color: white;
}

.hot-badge {
  padding: 4px 10px;
  background: rgba(255, 77, 79, 0.9);
  color: white;
  border-radius: 12px;
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  box-shadow: 0 2px 8px rgba(255, 77, 79, 0.3);
}

.card-body {
  position: relative;
  z-index: 1;
}

.product-name {
  font-size: 15px;
  font-weight: 600;
  color: white;
  margin-bottom: 12px;
  line-height: 1.3;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-metrics {
  display: flex;
  align-items: center;
  gap: 16px;
}

.metric {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.metric-label {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.8);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  font-weight: 500;
}

.metric-value {
  font-size: 16px;
  font-weight: 700;
  color: white;
}

.metric-divider {
  width: 1px;
  height: 32px;
  background: rgba(255, 255, 255, 0.3);
}

.card-footer {
  margin-top: 12px;
}

.progress-bar {
  width: 100%;
  height: 6px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: white;
  border-radius: 3px;
  transition: width 0.3s ease;
  box-shadow: 0 0 8px rgba(255, 255, 255, 0.5);
}

/* Scrollbar styling */
.top-products-grid::-webkit-scrollbar {
  width: 6px;
}

.top-products-grid::-webkit-scrollbar-track {
  background: #f0f0f0;
  border-radius: 3px;
}

.top-products-grid::-webkit-scrollbar-thumb {
  background: #d9d9d9;
  border-radius: 3px;
}

.top-products-grid::-webkit-scrollbar-thumb:hover {
  background: #bfbfbf;
}
</style>
