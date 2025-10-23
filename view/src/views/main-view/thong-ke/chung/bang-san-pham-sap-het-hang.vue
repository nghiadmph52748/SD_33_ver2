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
  border-radius: 4px;
  border: none;
}

.chart-card :deep(.arco-card-header) {
  height: auto;
  padding: 20px;
  border: none;
}

.chart-card :deep(.arco-card-body) {
  padding: 0 20px 20px 20px;
}

.chart-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-1);
}

.title-icon {
  font-size: 18px;
}

.table-container {
  /* No margin needed */
}

.no-data-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
  background: var(--color-fill-1);
  border-radius: 4px;
}

.no-data-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.4;
}

.no-data-text {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-2);
  margin-bottom: 4px;
}

.no-data-subtext {
  font-size: 12px;
  color: var(--color-text-3);
}

.low-stock-table :deep(.arco-table-cell) {
  padding: 12px 16px;
}

.low-stock-table :deep(.arco-table-th) {
  font-weight: 600;
  color: var(--color-text-2);
  background-color: var(--color-fill-2);
}

.low-stock-table :deep(.arco-table-tr:hover) {
  background-color: var(--color-fill-1);
}

/* Rank Cell Styles */
.rank-number {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: var(--color-fill-2);
  border-radius: 4px;
  font-weight: 600;
  font-size: 14px;
  color: var(--color-text-2);
}

/* Product Image Styles */
.image-wrapper {
  position: relative;
  width: 56px;
  height: 56px;
  border-radius: 2px;
  overflow: hidden;
  border: 1px solid var(--color-border-2);
}

.product-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.stock-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  color: white;
  border: 2px solid white;
}

.badge-critical {
  background: rgb(var(--red-6));
}

.badge-danger {
  background: rgb(var(--orange-6));
}

.badge-warning {
  background: rgb(var(--gold-6));
}

/* Product Name Styles */
.product-name {
  font-weight: 500;
  color: var(--color-text-1);
  line-height: 1.5;
}

/* Price Styles */
.price-value {
  font-weight: 600;
  color: rgb(var(--green-6));
}

/* Stock Cell Styles */
.stock-indicator {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stock-number {
  font-weight: 600;
  color: var(--color-text-1);
}

.stock-level-bar {
  width: 60px;
  height: 4px;
  background: var(--color-fill-2);
  border-radius: 2px;
  overflow: hidden;
}

.stock-level-fill {
  height: 100%;
  border-radius: 2px;
  transition: width 0.6s ease;
}

.stock-critical .stock-level-fill {
  background: rgb(var(--red-6));
}

.stock-danger .stock-level-fill {
  background: rgb(var(--orange-6));
}

.stock-warning .stock-level-fill {
  background: rgb(var(--gold-6));
}

/* Status Cell Styles */
.low-stock-table :deep(.arco-tag) {
  font-size: 12px;
}
</style>
