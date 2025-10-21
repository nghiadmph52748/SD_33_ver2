<template>
  <a-card class="chart-card">
    <template #title>
      <div class="chart-title">
        <span>Danh Mục Sản Phẩm Bán Chạy</span>
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
import type { DuLieuDanhMuc } from '../types/thong-ke.types'

use([CanvasRenderer, PieChart, TooltipComponent, LegendComponent])

interface Props {
  duLieu: DuLieuDanhMuc[]
}

const props = defineProps<Props>()

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
      name: 'Danh mục sản phẩm bán chạy',
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
