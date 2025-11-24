<template>
  <a-card class="chart-card">
    <template #title>
      <div class="chart-title">
        <span>{{ $t('thongKe.category.title') }}</span>
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
    formatter: (params: any) => {
      const data = params.data
      if (!data.products || data.products.length === 0) {
        return `${data.name}<br/>Số lượng sản phẩm: ${data.value}`
      }
      
      // Sắp xếp sản phẩm theo số lượng bán giảm dần
      const sortedProducts = [...data.products].sort((a, b) => b.quantity - a.quantity)
      
      let tooltip = `<div style="max-width: 400px;">
        <div style="font-weight: bold; margin-bottom: 8px; color: ${data.color};">
          ${data.name}
        </div>
        <div style="margin-bottom: 8px;">
          <strong>Tổng số sản phẩm: ${data.value}</strong>
        </div>
        <div style="font-size: 12px; color: #666; margin-bottom: 4px;">
          Chi tiết sản phẩm:
        </div>`
      
      // Hiển thị tối đa 10 sản phẩm để tránh tooltip quá dài
      const productsToShow = sortedProducts.slice(0, 10)
      productsToShow.forEach((product) => {
        tooltip += `
          <div style="display: flex; justify-content: space-between; margin-bottom: 2px; font-size: 11px;">
            <span style="max-width: 250px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
              • ${product.name}
            </span>
            <span style="margin-left: 8px; color: #1890ff; font-weight: 500;">
              ${product.quantity} sp
            </span>
          </div>`
      })
      
      // Hiển thị thông báo nếu có nhiều sản phẩm hơn
      if (sortedProducts.length > 10) {
        tooltip += `
          <div style="font-size: 10px; color: #999; margin-top: 4px; font-style: italic;">
            ... và ${sortedProducts.length - 10} sản phẩm khác
          </div>`
      }
      
      tooltip += '</div>'
      return tooltip
    },
    backgroundColor: 'rgba(255, 255, 255, 0.96)',
    borderColor: '#d9d9d9',
    borderWidth: 1,
    textStyle: {
      color: '#333'
    },
    extraCssText: 'box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); border-radius: 6px; padding: 12px;'
  },
  legend: {
    orient: 'horizontal',
    left: 'center',
    bottom: 0,
    itemGap: 15,
    textStyle: {
      fontSize: 11
    }
  },
  series: [
    {
      name: 'Phân loại sản phẩm theo mức độ bán chạy',
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
        formatter: '{b}: {c} sản phẩm\n({d}%)',
        fontSize: 10,
        lineHeight: 14
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
