<template>
  <a-card class="the-thong-ke" :bordered="false">
    <div class="the-header">
      <div class="the-title-section">
        <div class="the-icon-wrapper" :class="mauIconClass">
          <icon-calendar class="the-icon" />
        </div>
        <div class="the-title">{{ tieuDe }}</div>
      </div>
      <div class="the-revenue-section">
        <div class="the-revenue-value">{{ dinhDangTien(doanhThu) }}</div>
        <div class="the-revenue-change">
          <icon-arrow-down class="change-icon" />
          <span>{{ tangTruong }}%</span>
        </div>
      </div>
    </div>
    <div class="the-details">
      <span class="the-detail-text">
        {{ $t('thongKe.card.productsSold') }}: {{ sanPhamDaBan }} | {{ $t('thongKe.card.ordersCount') }}: {{ soDonHang }}
      </span>
    </div>
  </a-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { IconArrowDown, IconCalendar } from '@arco-design/web-vue/es/icon'

interface Props {
  tieuDe: string
  doanhThu: number
  sanPhamDaBan: number
  soDonHang: number
  tangTruong?: number
  loaiMau?: 'today' | 'week' | 'month' | 'year'
}

const props = withDefaults(defineProps<Props>(), {
  tangTruong: 0,
  loaiMau: 'today',
})

const dinhDangTien = (soTien: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(soTien)
}

const mauIconClass = computed(() => {
  const classMap = {
    today: 'today-icon-wrapper',
    week: 'week-icon-wrapper',
    month: 'month-icon-wrapper',
    year: 'year-icon-wrapper',
  }
  return classMap[props.loaiMau]
})
</script>

<style scoped>
.the-thong-ke {
  animation: fadeInUp 0.6s ease-out;
}

.the-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  animation: fadeInUp 0.8s ease-out 0.4s both;
}

.the-title-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.the-icon-wrapper {
  width: 40px;
  height: 40px;
  background: #1890ff;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: scaleIn 0.5s ease-out 0.5s both;
}

.today-icon-wrapper {
  background: #1890ff;
}

.week-icon-wrapper {
  background: #52c41a;
}

.month-icon-wrapper {
  background: #fa8c16;
}

.year-icon-wrapper {
  background: #722ed1;
}

.the-icon {
  font-size: 20px;
  color: white;
}

.the-title {
  font-size: 16px;
  font-weight: 600;
  color: #1d2129;
  animation: fadeInLeft 0.6s ease-out 0.5s both;
}

.the-revenue-section {
  text-align: right;
}

.the-revenue-value {
  font-size: 24px;
  font-weight: 700;
  color: #1d2129;
  margin-bottom: 4px;
  animation: fadeInRight 0.6s ease-out 0.7s both;
}

.the-revenue-change {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
  font-size: 14px;
  color: #86909c;
  animation: fadeInRight 0.6s ease-out 0.8s both;
}

.change-icon {
  font-size: 12px;
}

.the-details {
  margin-top: 8px;
  animation: fadeInUp 0.8s ease-out 0.6s both;
}

.the-detail-text {
  font-size: 14px;
  color: #86909c;
  font-weight: 500;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeInLeft {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes fadeInRight {
  from {
    opacity: 0;
    transform: translateX(30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.9);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}
</style>
