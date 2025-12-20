<template>
  <div class="bo-loc-section">
    <a-card class="bo-loc-card" :bordered="false">
      <div class="bo-loc-container">
        <!-- Header với title và actions -->
        <div class="bo-loc-header">
          <div class="bo-loc-header-left">
            <div class="bo-loc-title">
              <icon-filter class="filter-icon" />
              <span>{{ $t('thongKe.filter.title') }}</span>
            </div>

            <!-- Chart Type - Moved to Header -->
            <div class="control-group header-control">
              <label class="control-label">{{ $t('thongKe.filter.chartType') }}</label>
              <div class="chart-type-buttons">
                <button :class="['chart-btn', { active: loaiBieuDo === 'line' }]" @click="thayDoiLoaiBieuDo('line')">
                  <icon-line class="chart-icon" />
                  {{ $t('thongKe.filter.lineChart') }}
                </button>
                <button :class="['chart-btn', { active: loaiBieuDo === 'bar' }]" @click="thayDoiLoaiBieuDo('bar')">
                  <icon-bar class="chart-icon" />
                  {{ $t('thongKe.filter.barChart') }}
                </button>
              </div>
            </div>
          </div>

          <div class="filter-actions">
            <a-button class="reset-btn" @click="$emit('datLai')">
              <icon-refresh class="action-icon" />
              {{ $t('thongKe.filter.reset') }}
            </a-button>
            <a-button type="primary" class="export-btn" @click="$emit('xuatExcel')">
              <icon-download class="action-icon" />
              {{ $t('thongKe.filter.exportExcel') }}
            </a-button>
            <a-button type="primary" class="ai-btn" @click="$emit('hoiAI')">Hỏi AI về thống kê</a-button>
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
  (e: 'hoiAI'): void
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
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.bo-loc-container {
  padding: 20px 24px;
}

.bo-loc-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.bo-loc-header-left {
  display: flex;
  align-items: center;
  gap: 40px;
}

.header-control {
  flex-direction: row !important;
  align-items: center;
  gap: 16px !important;
}

.header-control .control-label {
  margin-bottom: 0;
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

.control-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.control-label {
  font-weight: 600;
  font-size: 13px;
  color: #666;
  text-transform: uppercase;
  letter-spacing: 0.3px;
}

.chart-type-buttons {
  display: flex;
  gap: 8px;
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
  font-size: 16px;
}

.filter-actions {
  display: flex;
  gap: 10px;
}

.reset-btn {
  background: white;
  border: 1px solid #d9d9d9;
  color: #666;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  height: 40px;
}

.reset-btn:hover {
  border-color: #faad14;
  color: #faad14;
  background: #fffbe6;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(250, 173, 20, 0.2);
}

.export-btn {
  background: #1890ff;
  border-color: #1890ff;
  color: white;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  height: 40px;
}

.export-btn:hover {
  background: #40a9ff;
  border-color: #40a9ff;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
}

.action-icon {
  font-size: 14px;
}

.ai-btn {
  background: #1890ff;
  border-color: #1890ff;
  color: white;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  height: 40px;
}

.ai-btn:hover {
  background: #40a9ff;
  border-color: #40a9ff;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
}
</style>
