<template>
  <a-card class="chart-card">
    <template #title>
      <div class="chart-title">
        <span>{{ $t('thongKe.orderStatus.title') }}</span>
      </div>
    </template>
    <template #extra>
      <div class="order-status-buttons">
        <button :class="['status-btn', { active: kyThongKe === 'day' }]" @click="$emit('update:kyThongKe', 'day')">{{ $t('thongKe.orderStatus.day') }}</button>
        <button :class="['status-btn', { active: kyThongKe === 'month' }]" @click="$emit('update:kyThongKe', 'month')">{{ $t('thongKe.orderStatus.month') }}</button>
        <button :class="['status-btn', { active: kyThongKe === 'year' }]" @click="$emit('update:kyThongKe', 'year')">{{ $t('thongKe.orderStatus.year') }}</button>
      </div>
    </template>
    <div class="chart-container">
      <v-chart class="chart" :option="cauHinhBieuDo" autoresize />
    </div>
  </a-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart } from 'echarts/charts'
import { TooltipComponent, LegendComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import type { DuLieuTrangThaiDonHang } from '../types/thong-ke.types'

use([CanvasRenderer, PieChart, TooltipComponent, LegendComponent])

interface Props {
  duLieu: DuLieuTrangThaiDonHang[]
  kyThongKe: 'day' | 'month' | 'year'
}

const props = defineProps<Props>()

interface Emits {
  (e: 'update:kyThongKe', value: 'day' | 'month' | 'year'): void
}

defineEmits<Emits>()

const cauHinhBieuDo = computed(() => ({
  tooltip: {
    trigger: 'item',
    formatter: '{a} <br/>{b}: {c} ({d}%)',
  },
  legend: {
    orient: 'horizontal',
    left: 'center',
    bottom: 0,
    itemGap: 15,
  },
  series: [
    {
      name: 'Trạng thái đơn hàng',
      type: 'pie',
      radius: ['40%', '65%'],
      center: ['50%', '45%'],
      data: props.duLieu,
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)',
        },
      },
      label: {
        formatter: '{b}: {d}%',
        fontSize: 11,
      },
      labelLine: {
        show: true,
        length: 8,
        length2: 5,
      },
    },
  ],
}))
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

.order-status-buttons {
  display: flex;
  gap: 8px;
}

.status-btn {
  padding: 6px 12px;
  border: 1px solid #d9d9d9;
  background: white;
  color: #666;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 50px;
  text-align: center;
}

.status-btn:hover {
  border-color: #1890ff;
  color: #1890ff;
  background: #f0f8ff;
}

.status-btn.active {
  background: #1890ff;
  border-color: #1890ff;
  color: white;
}

.chart-container {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart {
  width: 100%;
  height: 100%;
}
</style>
