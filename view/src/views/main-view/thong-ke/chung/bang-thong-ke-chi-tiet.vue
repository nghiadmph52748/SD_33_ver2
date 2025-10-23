<template>
  <a-card class="chart-card" :bordered="false">
    <template #title>
      <div class="chart-title">
        <div class="title-icon">📊</div>
        <span>{{ $t('thongKe.detailTable.title') }}</span>
      </div>
    </template>
    <div class="table-container">
      <a-table :columns="cot" :data="duLieu" :pagination="phanTrang" :scroll="{ x: 800 }" class="detail-table" :bordered="false">
        <template #thoiGian="{ record }">
          <div class="time-cell">
            <icon-calendar class="time-icon" />
            <span class="time-text">{{ record.thoiGian }}</span>
          </div>
        </template>
        <template #doanhThu="{ record }">
          <div class="revenue-cell">
            <div class="revenue-amount">{{ dinhDangTien(record.doanhThu) }}</div>
          </div>
        </template>
        <template #soDonHang="{ record }">
          <div class="order-count-cell">
            <div class="count-badge">
              <icon-file class="count-icon" />
              <span class="count-number">{{ record.soDonHang }}</span>
            </div>
          </div>
        </template>
        <template #giaTriTB="{ record }">
          <div class="avg-value-cell">
            <span class="avg-amount">{{ dinhDangTien(record.giaTriTB) }}</span>
          </div>
        </template>
        <template #tangTruong="{ record }">
          <div class="growth-container">
            <div :class="['growth-badge', record.tangTruong >= 0 ? 'positive' : 'negative']">
              <icon-arrow-up v-if="record.tangTruong >= 0" class="growth-icon" />
              <icon-arrow-down v-else class="growth-icon" />
              <span class="growth-value">{{ Math.abs(record.tangTruong) }}%</span>
            </div>
          </div>
        </template>
      </a-table>
    </div>
  </a-card>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import {
  IconCalendar,
  IconFile,
  IconArrowUp,
  IconArrowDown,
} from '@arco-design/web-vue/es/icon'
import type { DuLieuBangChiTiet } from '../types/thong-ke.types'

interface Props {
  duLieu: DuLieuBangChiTiet[]
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

const cot = [
  {
    title: t('thongKe.detailTable.time'),
    dataIndex: 'thoiGian',
    slotName: 'thoiGian',
    width: 120,
    align: 'center' as const,
  },
  {
    title: t('thongKe.detailTable.revenue'),
    dataIndex: 'doanhThu',
    slotName: 'doanhThu',
    width: 150,
    align: 'right' as const,
  },
  {
    title: t('thongKe.detailTable.orders'),
    dataIndex: 'soDonHang',
    slotName: 'soDonHang',
    width: 120,
    align: 'center' as const,
  },
  {
    title: t('thongKe.detailTable.avgValue'),
    dataIndex: 'giaTriTB',
    slotName: 'giaTriTB',
    width: 150,
    align: 'right' as const,
  },
  {
    title: t('thongKe.detailTable.growth'),
    dataIndex: 'tangTruong',
    slotName: 'tangTruong',
    width: 120,
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

.detail-table :deep(.arco-table-cell) {
  padding: 12px 16px;
}

.detail-table :deep(.arco-table-th) {
  font-weight: 600;
  color: var(--color-text-2);
  background-color: var(--color-fill-2);
}

.detail-table :deep(.arco-table-tr:hover) {
  background-color: var(--color-fill-1);
}

/* Time Cell Styles */
.time-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  justify-content: center;
}

.time-icon {
  font-size: 14px;
  color: rgb(var(--blue-6));
}

.time-text {
  font-weight: 500;
  color: var(--color-text-1);
}

/* Revenue Cell Styles */
.revenue-amount {
  font-weight: 600;
  color: rgb(var(--green-6));
}

/* Order Count Cell Styles */
.count-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.count-icon {
  font-size: 14px;
  color: rgb(var(--blue-6));
}

.count-number {
  font-weight: 600;
  color: var(--color-text-1);
}

/* Average Value Cell Styles */
.avg-amount {
  font-weight: 500;
  color: var(--color-text-2);
}

/* Growth Cell Styles */
.growth-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-weight: 600;
  font-size: 13px;
}

.growth-badge.positive {
  color: rgb(var(--green-6));
}

.growth-badge.negative {
  color: rgb(var(--red-6));
}

.growth-icon {
  font-size: 12px;
}
</style>
