<template>
  <div class="bo-loc-section">
    <a-card class="bo-loc-card" :bordered="false">
      <div class="bo-loc-header">
        <div class="bo-loc-title">
          <icon-filter class="filter-icon" />
          <span>Bộ Lọc Thống Kê</span>
        </div>
      </div>

      <div class="bo-loc-content">
        <!-- Dòng 1: Labels -->
        <div class="bo-loc-row-1">
          <div class="bo-loc-label">Khoảng thời gian thống kê</div>
          <div class="bo-loc-label">Loại biểu đồ</div>
        </div>

        <!-- Dòng 2: Controls -->
        <div class="bo-loc-row-2">
          <div class="time-range-container">
            <a-select :model-value="khoangThoiGian" class="filter-select" @change="thayDoiKhoangThoiGian">
              <a-option value="today">Hôm nay</a-option>
              <a-option value="week">Tuần này</a-option>
              <a-option value="month">Tháng này</a-option>
              <a-option value="year">Năm này</a-option>
              <a-option value="custom">Tùy chọn</a-option>
            </a-select>

            <!-- DatePicker cho tùy chọn -->
            <div v-if="khoangThoiGian === 'custom'" class="custom-date-picker">
              <a-range-picker
                :model-value="khoangNgayTuyChon"
                @change="thayDoiKhoangNgayTuyChon"
                format="DD/MM/YYYY"
                :placeholder="['Từ ngày', 'Đến ngày']"
                style="width: 300px"
              />
            </div>
          </div>

          <div class="chart-type-buttons">
            <button :class="['chart-btn', { active: loaiBieuDo === 'line' }]" @click="thayDoiLoaiBieuDo('line')">
              <icon-line class="chart-icon" />
              Đường
            </button>
            <button :class="['chart-btn', { active: loaiBieuDo === 'bar' }]" @click="thayDoiLoaiBieuDo('bar')">
              <icon-bar class="chart-icon" />
              Cột
            </button>
          </div>
        </div>

        <!-- Dòng 3: Summary và Actions -->
        <div class="bo-loc-row-3">
          <span class="summary-text">Số đơn hàng: {{ tongDonHang }} | Tổng doanh thu: {{ dinhDangTien(tongDoanhThu) }}</span>

          <div class="filter-actions">
            <a-button class="reset-btn" @click="$emit('datLai')">
              <icon-refresh class="action-icon" />
              Đặt lại bộ lọc
            </a-button>
            <a-button type="primary" class="export-btn" @click="$emit('xuatExcel')">
              <icon-download class="action-icon" />
              Xuất Excel
            </a-button>
          </div>
        </div>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { IconFilter, IconRefresh, IconDownload } from '@arco-design/web-vue/es/icon'
import type { KhoangThoiGian, LoaiBieuDo } from '../types/thong-ke.types'

interface Props {
  khoangThoiGian: KhoangThoiGian
  loaiBieuDo: LoaiBieuDo
  khoangNgayTuyChon?: any[]
  tongDonHang: number
  tongDoanhThu: number
}

const props = defineProps<Props>()

interface Emits {
  (e: 'update:khoangThoiGian', value: KhoangThoiGian): void
  (e: 'update:loaiBieuDo', value: LoaiBieuDo): void
  (e: 'update:khoangNgayTuyChon', value: any[]): void
  (e: 'datLai'): void
  (e: 'xuatExcel'): void
}

const emit = defineEmits<Emits>()

const dinhDangTien = (soTien: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(soTien)
}

const thayDoiKhoangThoiGian = (value: any) => {
  emit('update:khoangThoiGian', value)
}

const thayDoiLoaiBieuDo = (value: LoaiBieuDo) => {
  emit('update:loaiBieuDo', value)
}

const thayDoiKhoangNgayTuyChon = (dates: any[]) => {
  emit('update:khoangNgayTuyChon', dates)
}
</script>

<style scoped>
.bo-loc-section {
  margin-bottom: 24px;
}

.bo-loc-card {
  background: white;
  border: 1px solid #f0f0f0;
}

.bo-loc-header {
  padding: 20px 24px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.bo-loc-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 18px;
  font-weight: 700;
  color: #1a1a1a;
}

.filter-icon {
  font-size: 20px;
  color: #1890ff;
}

.bo-loc-content {
  padding: 24px;
}

.bo-loc-row-1 {
  display: flex;
  gap: 40px;
  margin-bottom: 16px;
}

.bo-loc-label {
  font-weight: 600;
  font-size: 14px;
  color: #333333;
  margin: 0;
  flex: 1;
}

.bo-loc-row-2 {
  display: flex;
  gap: 40px;
  margin-bottom: 20px;
  align-items: flex-start;
}

.time-range-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}

.filter-select {
  width: 200px;
  height: 40px;
  border-radius: 8px;
  border: 1px solid #d9d9d9;
  font-size: 14px;
}

.custom-date-picker {
  margin-top: 8px;
}

.chart-type-buttons {
  display: flex;
  gap: 12px;
  flex: 1;
  justify-content: flex-start;
}

.chart-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: 1px solid #d9d9d9;
  background: white;
  color: #666;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  height: 40px;
  min-width: 80px;
  justify-content: center;
}

.chart-btn:hover {
  border-color: #1890ff;
  color: #1890ff;
  background: #f0f8ff;
}

.chart-btn.active {
  background: #1890ff;
  border-color: #1890ff;
  color: white;
}

.chart-btn.active:hover {
  background: #40a9ff;
  border-color: #40a9ff;
}

.chart-icon {
  font-size: 18px;
  font-weight: 600;
}

.bo-loc-row-3 {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
}

.summary-text {
  font-size: 14px;
  color: #666666;
  font-weight: 500;
  line-height: 1.4;
}

.filter-actions {
  display: flex;
  gap: 12px;
  flex-shrink: 0;
}

.reset-btn {
  background: #8c8c8c;
  border-color: #8c8c8c;
  color: white;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  height: 40px;
  min-width: 140px;
  justify-content: center;
}

.reset-btn:hover {
  background: #737373;
  border-color: #737373;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.export-btn {
  background: #1890ff;
  border-color: #1890ff;
  color: white;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  height: 40px;
  min-width: 140px;
  justify-content: center;
}

.export-btn:hover {
  background: #40a9ff;
  border-color: #40a9ff;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(24, 144, 255, 0.3);
}

.action-icon {
  font-size: 16px;
}
</style>
