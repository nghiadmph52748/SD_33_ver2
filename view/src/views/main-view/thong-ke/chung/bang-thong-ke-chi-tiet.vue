<template>
  <a-card class="chart-card">
    <template #title>
      <div class="chart-title">
        <span>Bảng Thống Kê Chi Tiết</span>
      </div>
    </template>
    <div class="table-container">
      <a-table :columns="cot" :data="duLieu" :pagination="phanTrang" :scroll="{ x: 800 }" class="detail-table">
        <template #thoiGian="{ record }">
          <span class="time-cell">{{ record.thoiGian }}</span>
        </template>
        <template #doanhThu="{ record }">
          <span class="revenue-cell">{{ dinhDangTien(record.doanhThu) }}</span>
        </template>
        <template #soDonHang="{ record }">
          <span class="order-count-cell">{{ record.soDonHang }}</span>
        </template>
        <template #giaTriTB="{ record }">
          <span class="avg-value-cell">{{ dinhDangTien(record.giaTriTB) }}</span>
        </template>
        <template #tangTruong="{ record }">
          <span :class="['growth-cell', record.tangTruong >= 0 ? 'positive' : 'negative']">
            {{ record.tangTruong >= 0 ? '+' : '' }}{{ record.tangTruong }}%
          </span>
        </template>
        <template #trangThai="{ record }">
          <a-tag :color="layMauTrangThai(record.trangThai)">
            {{ record.trangThai }}
          </a-tag>
        </template>
      </a-table>
    </div>
  </a-card>
</template>

<script setup lang="ts">
import type { DuLieuBangChiTiet } from '../types/thong-ke.types'

interface Props {
  duLieu: DuLieuBangChiTiet[]
  phanTrang: any
}

defineProps<Props>()

const dinhDangTien = (soTien: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(soTien)
}

const layMauTrangThai = (trangThai: string): string => {
  switch (trangThai) {
    case 'Hoạt động':
      return 'green'
    case 'Không hoạt động':
      return 'red'
    default:
      return 'blue'
  }
}

const cot = [
  {
    title: 'Thời gian',
    dataIndex: 'thoiGian',
    slotName: 'thoiGian',
    width: 120,
    align: 'center' as const,
  },
  {
    title: 'Doanh thu',
    dataIndex: 'doanhThu',
    slotName: 'doanhThu',
    width: 150,
    align: 'right' as const,
  },
  {
    title: 'Số đơn hàng',
    dataIndex: 'soDonHang',
    slotName: 'soDonHang',
    width: 120,
    align: 'center' as const,
  },
  {
    title: 'Giá trị TB/đơn',
    dataIndex: 'giaTriTB',
    slotName: 'giaTriTB',
    width: 150,
    align: 'right' as const,
  },
  {
    title: 'Tăng trưởng',
    dataIndex: 'tangTruong',
    slotName: 'tangTruong',
    width: 120,
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

.detail-table {
  border-radius: 8px;
  overflow: hidden;
}

.detail-table :deep(.arco-table-thead) {
  background-color: #f5f5f5;
}

.detail-table :deep(.arco-table-thead .arco-table-th) {
  background-color: #f5f5f5;
  font-weight: 600;
  font-size: 14px;
  color: #1d2129;
  border-bottom: 2px solid #e8e8e8;
  padding: 12px 16px;
}

.detail-table :deep(.arco-table-tbody .arco-table-tr:hover) {
  background-color: #f8f9fa;
}

.detail-table :deep(.arco-table-td) {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
  color: #1d2129;
}

.time-cell {
  font-weight: 500;
  color: #000000;
}

.revenue-cell {
  font-weight: 600;
  color: #000000;
}

.order-count-cell,
.avg-value-cell {
  font-weight: 500;
  color: #000000;
}

.growth-cell {
  font-weight: 600;
  font-size: 14px;
  padding: 4px 8px;
  border-radius: 4px;
  display: inline-block;
  min-width: 60px;
  text-align: center;
  color: #000000;
}

.growth-cell.positive {
  background-color: #f6ffed;
  border: 1px solid #b7eb8f;
}

.growth-cell.negative {
  background-color: #fff2f0;
  border: 1px solid #ffccc7;
}
</style>
