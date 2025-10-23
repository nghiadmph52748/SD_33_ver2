<template>
  <a-card class="chart-card" :bordered="false">
    <template #title>
      <div class="chart-title">
        <div class="title-icon">⚠️</div>
        <span>{{ $t('thongKe.lowStockTable.title') }}</span>
      </div>
    </template>
    <div class="table-container">
      <div v-if="duLieu.length === 0" class="no-data-container">
        <div class="no-data-icon">🎉</div>
        <div class="no-data-text">{{ $t('thongKe.lowStockTable.noData') }}</div>
        <div class="no-data-subtext">{{ $t('thongKe.lowStockTable.noDataSub') }}</div>
      </div>
      <a-table v-else :columns="cot" :data="duLieu" :pagination="phanTrang" :scroll="{ x: 800 }" class="low-stock-table" :bordered="false">
        <template #stt="{ record, rowIndex }">
          <div class="rank-cell">
            <span class="rank-number">{{ rowIndex + 1 }}</span>
          </div>
        </template>
        <template #anh="{ record }">
          <div class="product-image-cell">
            <div class="image-wrapper">
              <img :src="record.anh || '/default-product.png'" :alt="record.tenSanPham" class="product-img" />
              <div :class="['stock-badge', getStockBadgeClass(record.soLuongTon)]">
                <icon-exclamation-circle-fill v-if="record.soLuongTon === 0" />
                <icon-exclamation v-else />
              </div>
            </div>
          </div>
        </template>
        <template #tenSanPham="{ record }">
          <div class="product-name-cell">
            <span class="product-name">{{ record.tenSanPham }}</span>
          </div>
        </template>
        <template #giaBan="{ record }">
          <div class="price-cell">
            <span class="price-value">{{ dinhDangTien(record.giaBan) }}</span>
          </div>
        </template>
        <template #soLuongTon="{ record }">
          <div :class="['stock-indicator', getStockClass(record.soLuongTon)]">
            <div class="stock-number">{{ record.soLuongTon }}</div>
            <div class="stock-level-bar">
              <div class="stock-level-fill" :style="{ width: getStockPercentage(record.soLuongTon) + '%' }"></div>
            </div>
          </div>
        </template>
        <template #trangThai="{ record }">
          <div class="status-cell">
            <a-tag :color="layMauTrangThaiKho(record.soLuongTon)" class="status-tag">
              <template #icon>
                <icon-exclamation-circle-fill v-if="record.soLuongTon === 0" />
                <icon-clock-circle v-else />
              </template>
              {{ record.trangThai }}
            </a-tag>
          </div>
        </template>
      </a-table>
    </div>
  </a-card>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import {
  IconExclamationCircleFill,
  IconExclamation,
  IconClockCircle,
} from '@arco-design/web-vue/es/icon'
import type { SanPhamSapHetHang } from '../types/thong-ke.types'

interface Props {
  duLieu: SanPhamSapHetHang[]
  phanTrang: any
}

defineProps<Props>()

const { t } = useI18n()

const dinhDangTien = (soTien: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(soTien)
}

const layMauTrangThaiKho = (soLuongTon: number): string => {
  if (soLuongTon === 0) return 'red'
  if (soLuongTon <= 2) return 'orangered'
  return 'orange'
}

const getStockClass = (soLuongTon: number): string => {
  if (soLuongTon === 0) return 'stock-critical'
  if (soLuongTon <= 2) return 'stock-danger'
  return 'stock-warning'
}

const getStockBadgeClass = (soLuongTon: number): string => {
  if (soLuongTon === 0) return 'badge-critical'
  if (soLuongTon <= 2) return 'badge-danger'
  return 'badge-warning'
}

const getStockPercentage = (soLuongTon: number): number => {
  const maxStock = 10
  return Math.min((soLuongTon / maxStock) * 100, 100)
}

const cot = [
  {
    title: t('thongKe.lowStockTable.no'),
    dataIndex: 'stt',
    slotName: 'stt',
    width: 80,
    align: 'center' as const,
  },
  {
    title: t('thongKe.lowStockTable.image'),
    dataIndex: 'anh',
    slotName: 'anh',
    width: 100,
    align: 'center' as const,
  },
  {
    title: t('thongKe.lowStockTable.variantName'),
    dataIndex: 'tenSanPham',
    slotName: 'tenSanPham',
    width: 300,
    align: 'left' as const,
  },
  {
    title: t('thongKe.lowStockTable.price'),
    dataIndex: 'giaBan',
    slotName: 'giaBan',
    width: 150,
    align: 'right' as const,
  },
  {
    title: t('thongKe.lowStockTable.stock'),
    dataIndex: 'soLuongTon',
    slotName: 'soLuongTon',
    width: 150,
    align: 'center' as const,
  },
  {
    title: t('thongKe.lowStockTable.status'),
    dataIndex: 'trangThai',
    slotName: 'trangThai',
    width: 150,
    align: 'center' as const,
  },
]
</script>

<style scoped>
.chart-card {
  height: 100%;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border-radius: 12px;
  overflow: hidden;
}

.chart-card :deep(.arco-card-body) {
  padding: 24px;
}

.chart-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 20px;
  font-weight: 700;
  color: #1a1a1a;
}

.title-icon {
  font-size: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: shake 3s infinite;
}

@keyframes shake {
  0%, 90%, 100% {
    transform: translateX(0);
  }
  92%, 96% {
    transform: translateX(-3px);
  }
  94%, 98% {
    transform: translateX(3px);
  }
}

.table-container {
  margin-top: 20px;
}

.no-data-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
  background: linear-gradient(135deg, #e8f5e9 0%, #f1f8e9 100%);
  border-radius: 12px;
  border: 1px dashed #81c784;
}

.no-data-icon {
  font-size: 64px;
  margin-bottom: 20px;
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.no-data-text {
  font-size: 18px;
  font-weight: 600;
  color: #2e7d32;
  margin-bottom: 8px;
}

.no-data-subtext {
  font-size: 14px;
  color: #558b2f;
  line-height: 1.5;
}

.low-stock-table {
  border-radius: 8px;
  overflow: hidden;
}

.low-stock-table :deep(.arco-table) {
  border-radius: 8px;
}

.low-stock-table :deep(.arco-table-thead) {
  background: linear-gradient(to right, #fff3e0, #ffe0b2);
}

.low-stock-table :deep(.arco-table-thead .arco-table-th) {
  background: transparent;
  font-weight: 700;
  font-size: 13px;
  color: #4e5969;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border-bottom: 2px solid #ffcc80;
  padding: 18px 16px;
}

.low-stock-table :deep(.arco-table-tbody .arco-table-tr) {
  transition: all 0.3s ease;
}

.low-stock-table :deep(.arco-table-tbody .arco-table-tr:hover) {
  background: linear-gradient(to right, #fff8f0, #ffffff);
  box-shadow: 0 2px 8px rgba(255, 152, 0, 0.1);
  transform: translateY(-1px);
}

.low-stock-table :deep(.arco-table-td) {
  padding: 20px 16px;
  border-bottom: 1px solid #ffe0b2;
  font-size: 14px;
  color: #1d2129;
  vertical-align: middle;
}

/* Rank Cell Styles */
.rank-cell {
  display: flex;
  align-items: center;
  justify-content: center;
}

.rank-number {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
  border-radius: 8px;
  font-weight: 700;
  font-size: 14px;
  color: #e65100;
  border: 2px solid #ffcc80;
}

/* Product Image Styles */
.product-image-cell {
  display: flex;
  justify-content: center;
  align-items: center;
}

.image-wrapper {
  position: relative;
  width: 64px;
  height: 64px;
  border-radius: 12px;
  overflow: visible;
  transition: all 0.3s ease;
}

.image-wrapper:hover {
  transform: scale(1.05);
}

.product-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 12px;
  border: 2px solid #ffcc80;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.image-wrapper:hover .product-img {
  box-shadow: 0 4px 16px rgba(255, 152, 0, 0.2);
}

.stock-badge {
  position: absolute;
  top: -6px;
  right: -6px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  border: 2px solid white;
  z-index: 10;
  animation: pulse-badge 2s infinite;
}

@keyframes pulse-badge {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

.badge-critical {
  background: linear-gradient(135deg, #f44336 0%, #d32f2f 100%);
  animation: pulse-critical 1s infinite;
}

@keyframes pulse-critical {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 2px 8px rgba(244, 67, 54, 0.5);
  }
  50% {
    transform: scale(1.15);
    box-shadow: 0 4px 16px rgba(244, 67, 54, 0.8);
  }
}

.badge-danger {
  background: linear-gradient(135deg, #ff5722 0%, #f4511e 100%);
}

.badge-warning {
  background: linear-gradient(135deg, #ff9800 0%, #f57c00 100%);
}

/* Product Name Styles */
.product-name-cell {
  display: flex;
  align-items: center;
}

.product-name {
  font-weight: 600;
  color: #1d2129;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.2s ease;
}

.low-stock-table :deep(.arco-table-tbody .arco-table-tr:hover) .product-name {
  color: #d84315;
}

/* Price Styles */
.price-cell {
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.price-value {
  font-weight: 700;
  font-size: 15px;
  color: #00b96b;
}

/* Stock Cell Styles */
.stock-indicator {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 8px;
  min-width: 80px;
  transition: all 0.3s ease;
}

.stock-critical {
  background: linear-gradient(135deg, #ffebee 0%, #ffcdd2 100%);
  border: 2px solid #ef5350;
  box-shadow: 0 2px 12px rgba(244, 67, 54, 0.2);
}

.stock-danger {
  background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
  border: 2px solid #ff9800;
  box-shadow: 0 2px 12px rgba(255, 152, 0, 0.2);
}

.stock-warning {
  background: linear-gradient(135deg, #fffde7 0%, #fff9c4 100%);
  border: 2px solid #fbc02d;
  box-shadow: 0 2px 12px rgba(251, 192, 45, 0.2);
}

.stock-number {
  font-weight: 700;
  font-size: 16px;
  line-height: 1;
}

.stock-critical .stock-number {
  color: #d32f2f;
}

.stock-danger .stock-number {
  color: #e65100;
}

.stock-warning .stock-number {
  color: #f57f17;
}

.stock-level-bar {
  width: 60px;
  height: 3px;
  background: rgba(0, 0, 0, 0.1);
  border-radius: 2px;
  overflow: hidden;
  position: relative;
}

.stock-level-fill {
  height: 100%;
  border-radius: 2px;
  transition: width 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.stock-critical .stock-level-fill {
  background: linear-gradient(90deg, #f44336 0%, #e53935 100%);
}

.stock-danger .stock-level-fill {
  background: linear-gradient(90deg, #ff9800 0%, #fb8c00 100%);
}

.stock-warning .stock-level-fill {
  background: linear-gradient(90deg, #fbc02d 0%, #f9a825 100%);
}

/* Status Cell Styles */
.status-cell {
  display: flex;
  align-items: center;
  justify-content: center;
}

.low-stock-table :deep(.arco-tag) {
  padding: 4px 10px;
  font-weight: 600;
  font-size: 12px;
  border-radius: 6px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  white-space: nowrap;
}

.low-stock-table :deep(.arco-tag:hover) {
  transform: translateY(-1px);
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.15);
}
</style>
