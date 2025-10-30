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
      <a-table
        v-else
        :columns="cot"
        :data="duLieu"
        :pagination="phanTrang"
        :scroll="{ x: 800 }"
        class="top-selling-table"
        :bordered="false"
      >
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
        <template #soLuongDaBan="{ record }">
          <div class="quantity-badge">
            <span class="quantity-value">{{ record.soLuongDaBan }}</span>
            <span class="quantity-label">đã bán</span>
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

.top-selling-table :deep(.arco-table-cell) {
  padding: 12px 16px;
}

.top-selling-table :deep(.arco-table-th) {
  font-weight: 600;
  color: var(--color-text-2);
  background-color: var(--color-fill-2);
}

.top-selling-table :deep(.arco-table-tr:hover) {
  background-color: var(--color-fill-1);
}

/* Rank Badge Styles */
.rank-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 4px;
  font-weight: 600;
  font-size: 14px;
}

.rank-gold {
  background: rgb(var(--gold-1));
  color: rgb(var(--gold-6));
}

.rank-silver {
  background: rgb(var(--gray-2));
  color: rgb(var(--gray-7));
}

.rank-bronze {
  background: rgb(var(--orange-1));
  color: rgb(var(--orange-6));
}

.rank-default {
  background: var(--color-fill-2);
  color: var(--color-text-3);
}

/* Product Image Styles */
.image-wrapper {
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

/* Quantity Styles */
.quantity-badge {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 8px 16px;
  background: rgb(var(--blue-1));
  border-radius: 4px;
  border: 1px solid rgb(var(--blue-3));
}

.quantity-value {
  font-weight: 700;
  font-size: 16px;
  color: rgb(var(--blue-6));
  line-height: 1;
}

.quantity-label {
  font-size: 11px;
  font-weight: 500;
  color: rgb(var(--blue-5));
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
</style>
