import { BarChart, LineChart, PieChart, RadarChart } from 'echarts/charts'
import { DataZoomComponent, GraphicComponent, GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { App } from 'vue'
import Breadcrumb from './components/breadcrumb/breadcrumb.vue'
import Chart from './components/chart/chart.vue'
import ProductTable from './components/product-table/product-table.vue'
import SimpleProductTable from './components/simple-product-table/simple-product-table.vue'
import ProductTableFromInvoice from './components/product-table-from-invoice/product-table-from-invoice.vue'
import TimelineDonHang from './components/timeline-don-hang/timeline-don-hang.vue'

// Manually introduce ECharts modules to reduce packing size

use([
  CanvasRenderer,
  BarChart,
  LineChart,
  PieChart,
  RadarChart,
  GridComponent,
  TooltipComponent,
  LegendComponent,
  DataZoomComponent,
  GraphicComponent,
])

export default {
  install(Vue: App) {
    Vue.component('Chart', Chart)
    Vue.component('Breadcrumb', Breadcrumb)
    Vue.component('ProductTable', ProductTable)
    Vue.component('SimpleProductTable', SimpleProductTable)
    Vue.component('ProductTableFromInvoice', ProductTableFromInvoice)
    Vue.component('TimelineDonHang', TimelineDonHang)
  },
}
