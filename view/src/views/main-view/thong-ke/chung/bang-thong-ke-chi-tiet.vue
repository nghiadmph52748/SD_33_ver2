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
        <template #trangThai="{ record }">
          <div class="status-cell">
            <a-tag :color="layMauTrangThai(record.trangThai)" class="status-tag">
              <template #icon>
                <icon-check-circle v-if="record.trangThai === 'Hoạt động' || record.trangThai === $t('thongKe.detailTable.active')" />
                <icon-close-circle v-else />
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
  IconCalendar,
  IconFile,
  IconArrowUp,
  IconArrowDown,
  IconCheckCircle,
  IconCloseCircle,
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

const layMauTrangThai = (trangThai: string): string => {
  const activeText = t('thongKe.detailTable.active')
  const inactiveText = t('thongKe.detailTable.inactive')
  switch (trangThai) {
    case activeText:
    case 'Hoạt động':
      return 'green'
    case inactiveText:
    case 'Không hoạt động':
      return 'red'
    default:
      return 'blue'
  }
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
  {
    title: t('thongKe.detailTable.status'),
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
}

.table-container {
  margin-top: 20px;
}

.detail-table {
  border-radius: 8px;
  overflow: hidden;
}

.detail-table :deep(.arco-table) {
  border-radius: 8px;
}

.detail-table :deep(.arco-table-thead) {
  background: linear-gradient(to right, #f0f5ff, #e8f4fd);
}

.detail-table :deep(.arco-table-thead .arco-table-th) {
  background: transparent;
  font-weight: 700;
  font-size: 13px;
  color: #4e5969;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border-bottom: 2px solid #bedaff;
  padding: 18px 16px;
}

.detail-table :deep(.arco-table-tbody .arco-table-tr) {
  transition: all 0.3s ease;
}

.detail-table :deep(.arco-table-tbody .arco-table-tr:hover) {
  background: linear-gradient(to right, #f8fbff, #ffffff);
  box-shadow: 0 2px 8px rgba(22, 93, 255, 0.08);
  transform: translateY(-1px);
}

.detail-table :deep(.arco-table-td) {
  padding: 18px 16px;
  border-bottom: 1px solid #e8f1ff;
  font-size: 14px;
  color: #1d2129;
  vertical-align: middle;
}

/* Time Cell Styles */
.time-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  justify-content: center;
}

.time-icon {
  font-size: 16px;
  color: #165dff;
}

.time-text {
  font-weight: 600;
  color: #1d2129;
  font-size: 14px;
}

/* Revenue Cell Styles */
.revenue-cell {
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.revenue-amount {
  font-weight: 700;
  font-size: 16px;
  color: #00b96b;
  padding: 4px 12px;
  background: linear-gradient(135deg, #e8f9f1 0%, #f0fdf7 100%);
  border-radius: 6px;
  border: 1px solid #b7f0d4;
}

/* Order Count Cell Styles */
.order-count-cell {
  display: flex;
  align-items: center;
  justify-content: center;
}

.count-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: linear-gradient(135deg, #f0f5ff 0%, #e8f4fd 100%);
  border: 1px solid #bedaff;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.count-badge:hover {
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(22, 93, 255, 0.15);
}

.count-icon {
  font-size: 14px;
  color: #165dff;
}

.count-number {
  font-weight: 700;
  font-size: 15px;
  color: #165dff;
}

/* Average Value Cell Styles */
.avg-value-cell {
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.avg-amount {
  font-weight: 600;
  font-size: 14px;
  color: #1d2129;
  padding: 4px 10px;
  background: #f7f8fa;
  border-radius: 6px;
}

/* Growth Cell Styles */
.growth-container {
  display: flex;
  align-items: center;
  justify-content: center;
}

.growth-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border-radius: 8px;
  font-weight: 700;
  font-size: 14px;
  transition: all 0.3s ease;
  min-width: 70px;
  justify-content: center;
}

.growth-badge:hover {
  transform: scale(1.08);
}

.growth-badge.positive {
  background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%);
  border: 2px solid #86efac;
  color: #16a34a;
  box-shadow: 0 2px 8px rgba(22, 163, 74, 0.15);
}

.growth-badge.negative {
  background: linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%);
  border: 2px solid #fca5a5;
  color: #dc2626;
  box-shadow: 0 2px 8px rgba(220, 38, 38, 0.15);
}

.growth-icon {
  font-size: 14px;
  font-weight: bold;
}

.growth-value {
  font-weight: 700;
}

/* Status Cell Styles */
.status-cell {
  display: flex;
  align-items: center;
  justify-content: center;
}

.detail-table :deep(.arco-tag) {
  padding: 5px 12px;
  font-weight: 600;
  font-size: 12px;
  border-radius: 8px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  white-space: nowrap;
}

.detail-table :deep(.arco-tag:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}
</style>
