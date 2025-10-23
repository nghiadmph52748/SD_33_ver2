<template>
  <a-card class="chart-card" :bordered="false">
    <template #title>
      <div class="chart-title">
        <div class="title-icon">🔥</div>
        <span>{{ $t('thongKe.topSellingTable.title') }}</span>
      </div>
    </template>
    <div class="table-container">
      <div v-if="duLieu.length === 0" class="no-data-container">
        <div class="no-data-icon">📊</div>
        <div class="no-data-text">{{ $t('thongKe.topSellingTable.noData') }}</div>
        <div class="no-data-subtext">{{ $t('thongKe.topSellingTable.noDataSub') }}</div>
      </div>
      <a-table v-else :columns="cot" :data="duLieu" :pagination="phanTrang" :scroll="{ x: 800 }" class="top-selling-table" :bordered="false">
        <template #stt="{ record, rowIndex }">
          <div :class="['rank-badge', getRankClass(rowIndex)]">
            <span class="rank-number">{{ rowIndex + 1 }}</span>
          </div>
        </template>
        <template #anh="{ record }">
          <div class="product-image-cell">
            <div class="image-wrapper">
              <img :src="record.anh || '/default-product.png'" :alt="record.tenSanPham" class="product-img" />
              <div class="image-overlay"></div>
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
        <template #soLuongDaBan="{ record, rowIndex }">
          <div class="quantity-cell">
            <div class="quantity-container">
              <span class="quantity-number">{{ record.soLuongDaBan }}</span>
              <div class="quantity-bar-wrapper">
                <div class="quantity-bar" :style="{ width: getBarWidth(record.soLuongDaBan, rowIndex) + '%' }"></div>
              </div>
            </div>
          </div>
        </template>
      </a-table>
    </div>
  </a-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { SanPhamBanChayNhat } from '../types/thong-ke.types'

interface Props {
  duLieu: SanPhamBanChayNhat[]
  phanTrang: any
}

const props = defineProps<Props>()

const { t } = useI18n()

const dinhDangTien = (soTien: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(soTien)
}

const getRankClass = (index: number): string => {
  if (index === 0) return 'rank-gold'
  if (index === 1) return 'rank-silver'
  if (index === 2) return 'rank-bronze'
  return 'rank-default'
}

const maxQuantity = computed(() => {
  if (props.duLieu.length === 0) return 0
  return Math.max(...props.duLieu.map(item => item.soLuongDaBan))
})

const getBarWidth = (quantity: number, index: number): number => {
  if (maxQuantity.value === 0) return 0
  return (quantity / maxQuantity.value) * 100
}

const cot = [
  {
    title: t('thongKe.topSellingTable.no'),
    dataIndex: 'stt',
    slotName: 'stt',
    width: 80,
    align: 'center' as const,
  },
  {
    title: t('thongKe.topSellingTable.image'),
    dataIndex: 'anh',
    slotName: 'anh',
    width: 100,
    align: 'center' as const,
  },
  {
    title: t('thongKe.topSellingTable.productName'),
    dataIndex: 'tenSanPham',
    slotName: 'tenSanPham',
    width: 200,
    align: 'left' as const,
  },
  {
    title: t('thongKe.topSellingTable.price'),
    dataIndex: 'giaBan',
    slotName: 'giaBan',
    width: 150,
    align: 'right' as const,
  },
  {
    title: t('thongKe.topSellingTable.soldQuantity'),
    dataIndex: 'soLuongDaBan',
    slotName: 'soLuongDaBan',
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
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
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
  background: linear-gradient(135deg, #fafafa 0%, #f5f5f5 100%);
  border-radius: 12px;
  border: 1px dashed #d9d9d9;
}

.no-data-icon {
  font-size: 64px;
  margin-bottom: 20px;
  opacity: 0.5;
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

.top-selling-table {
  border-radius: 8px;
  overflow: hidden;
}

.top-selling-table :deep(.arco-table) {
  border-radius: 8px;
}

.top-selling-table :deep(.arco-table-thead) {
  background: linear-gradient(to right, #f8f9fa, #f5f6f7);
}

.top-selling-table :deep(.arco-table-thead .arco-table-th) {
  background: transparent;
  font-weight: 700;
  font-size: 13px;
  color: #4e5969;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border-bottom: 2px solid #e5e6eb;
  padding: 18px 16px;
}

.top-selling-table :deep(.arco-table-tbody .arco-table-tr) {
  transition: all 0.3s ease;
}

.top-selling-table :deep(.arco-table-tbody .arco-table-tr:hover) {
  background: linear-gradient(to right, #f8fafc, #ffffff);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transform: translateY(-1px);
}

.top-selling-table :deep(.arco-table-td) {
  padding: 20px 16px;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
  color: #1d2129;
  vertical-align: middle;
}

/* Rank Badge Styles */
.rank-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  font-weight: 700;
  font-size: 16px;
  position: relative;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
  transition: all 0.3s ease;
}

.rank-badge:hover {
  transform: scale(1.15) rotate(5deg);
}

.rank-gold {
  background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%);
  color: #b8860b;
  box-shadow: 0 4px 15px rgba(255, 215, 0, 0.4);
}

.rank-silver {
  background: linear-gradient(135deg, #c0c0c0 0%, #e8e8e8 100%);
  color: #6b6b6b;
  box-shadow: 0 4px 15px rgba(192, 192, 192, 0.4);
}

.rank-bronze {
  background: linear-gradient(135deg, #cd7f32 0%, #e8a87c 100%);
  color: #8b4513;
  box-shadow: 0 4px 15px rgba(205, 127, 50, 0.4);
}

.rank-default {
  background: linear-gradient(135deg, #e8e8e8 0%, #f5f5f5 100%);
  color: #8c8c8c;
}

.rank-number {
  position: relative;
  z-index: 1;
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
  overflow: hidden;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.image-wrapper:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.product-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.image-wrapper:hover .product-img {
  transform: scale(1.05);
}

.image-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to bottom, transparent 0%, rgba(0, 0, 0, 0.1) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.image-wrapper:hover .image-overlay {
  opacity: 1;
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

.top-selling-table :deep(.arco-table-tbody .arco-table-tr:hover) .product-name {
  color: #165dff;
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

/* Quantity Styles */
.quantity-cell {
  display: flex;
  align-items: center;
  justify-content: center;
}

.quantity-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.quantity-number {
  font-weight: 700;
  font-size: 16px;
  color: #1d2129;
}

.quantity-bar-wrapper {
  width: 100%;
  height: 6px;
  background: #f0f0f0;
  border-radius: 3px;
  overflow: hidden;
  position: relative;
}

.quantity-bar {
  height: 100%;
  background: linear-gradient(90deg, #165dff 0%, #4080ff 100%);
  border-radius: 3px;
  transition: width 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.quantity-bar::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, transparent 0%, rgba(255, 255, 255, 0.3) 50%, transparent 100%);
  animation: shimmer 2s infinite;
}

@keyframes shimmer {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(100%);
  }
}
</style>
