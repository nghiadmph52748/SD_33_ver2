<template>
  <a-card class="chart-card">
    <template #title>
      <div class="chart-title">
        <span>{{ $t('thongKe.revenue.title') }}</span>
      </div>
    </template>
    <template #extra>
      <a-select :model-value="kyDoanhThu" style="width: 120px" @change="$emit('update:kyDoanhThu', $event)">
        <a-option value="6months">{{ $t('thongKe.revenue.6months') }}</a-option>
        <a-option value="12months">{{ $t('thongKe.revenue.12months') }}</a-option>
      </a-select>
    </template>
    <div class="chart-container">
      <v-chart class="chart" :option="cauHinhBieuDo" autoresize />
    </div>
  </a-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, BarChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, GridComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import type { DuLieuDoanhThu, LoaiBieuDo } from '../types/thong-ke.types'

use([CanvasRenderer, LineChart, BarChart, TitleComponent, TooltipComponent, GridComponent])

interface Props {
  duLieuDoanhThu: DuLieuDoanhThu[]
  loaiBieuDo: LoaiBieuDo
  kyDoanhThu: string
}

const props = defineProps<Props>()

interface Emits {
  (e: 'update:kyDoanhThu', value: string): void
}

defineEmits<Emits>()

const { t } = useI18n()

const dinhDangTien = (soTien: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(soTien)
}

const cauHinhBieuDo = computed(() => ({
  tooltip: {
    trigger: 'axis',
    formatter: (params: any) => {
      const data = params[0]
      return `${data.axisValue}<br/>${data.seriesName}: ${dinhDangTien(data.value)}`
    },
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true,
  },
  xAxis: {
    type: 'category',
    data: props.duLieuDoanhThu.map((item) => item.month),
    axisLabel: {
      rotate: 45,
    },
  },
  yAxis: {
    type: 'value',
    axisLabel: {
      formatter: (value: number) => `${(value / 1000000).toFixed(0)}M`,
    },
  },
  series: [
    {
      name: t('thongKe.revenue.name'),
      type: props.loaiBieuDo,
      data: props.duLieuDoanhThu.map((item) => item.revenue),
      smooth: props.loaiBieuDo === 'line',
      lineStyle:
        props.loaiBieuDo === 'line'
          ? {
              color: '#1890ff',
              width: 3,
            }
          : undefined,
      itemStyle:
        props.loaiBieuDo === 'line'
          ? {
              color: '#1890ff',
            }
          : {
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [
                  { offset: 0, color: '#52c41a' },
                  { offset: 1, color: '#73d13d' },
                ],
              },
            },
      areaStyle:
        props.loaiBieuDo === 'line'
          ? {
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [
                  { offset: 0, color: 'rgba(24, 144, 255, 0.3)' },
                  { offset: 1, color: 'rgba(24, 144, 255, 0.1)' },
                ],
              },
            }
          : undefined,
      emphasis:
        props.loaiBieuDo === 'bar'
          ? {
              itemStyle: {
                color: {
                  type: 'linear',
                  x: 0,
                  y: 0,
                  x2: 0,
                  y2: 1,
                  colorStops: [
                    { offset: 0, color: '#389e0d' },
                    { offset: 1, color: '#52c41a' },
                  ],
                },
              },
            }
          : undefined,
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
