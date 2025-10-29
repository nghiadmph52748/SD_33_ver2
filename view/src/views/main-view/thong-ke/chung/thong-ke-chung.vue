<template>
  <div class="thong-ke-chung">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- AI Assistant trigger -->
    <div style="display: flex; justify-content: flex-end; margin: 8px 0 16px">
      <a-button type="primary" @click="openAIAssistant">
        <template #icon>🤖</template>
        Hỏi AI về thống kê
      </a-button>
    </div>

    <!-- Bộ lọc thống kê -->
    <BoLocThongKe
      v-model:khoang-thoi-gian="khoangThoiGian"
      v-model:loai-bieu-do="loaiBieuDo"
      v-model:khoang-ngay-tuy-chon="khoangNgayTuyChon"
      :tong-don-hang="tongDonHang"
      :tong-doanh-thu="tongDoanhThu"
      @dat-lai="datLaiBoLoc"
      @xuat-excel="xuatBaoCaoExcel"
    />

    <!-- Thẻ thống kê 4 cột -->
    <div class="stats-grid">
      <TheThongKe
        :tieu-de="duLieuHienThi.title"
        :doanh-thu="duLieuHienThi.revenue"
        :san-pham-da-ban="duLieuHienThi.productsSold"
        :so-don-hang="duLieuHienThi.orders"
        :loai-mau="khoangThoiGian"
      />
      <TheThongKe
        :tieu-de="$t('thongKe.card.thisWeek')"
        :doanh-thu="duLieuTuan.revenue"
        :san-pham-da-ban="duLieuTuan.productsSold"
        :so-don-hang="duLieuTuan.orders"
        loai-mau="week"
      />
      <TheThongKe
        :tieu-de="$t('thongKe.card.thisMonth')"
        :doanh-thu="duLieuThang.revenue"
        :san-pham-da-ban="duLieuThang.productsSold"
        :so-don-hang="duLieuThang.orders"
        loai-mau="month"
      />
      <TheThongKe
        :tieu-de="$t('thongKe.card.thisYear')"
        :doanh-thu="duLieuNam.revenue"
        :san-pham-da-ban="duLieuNam.productsSold"
        :so-don-hang="duLieuNam.orders"
        loai-mau="year"
      />
    </div>

    <!-- Biểu đồ Row 1: Doanh thu + Top sản phẩm -->
    <a-row :gutter="16" class="charts-section">
      <a-col :span="17">
        <BieuDoDoanhThu
          v-model:ky-doanh-thu="kyDoanhThu"
          :du-lieu-doanh-thu="duLieuDoanhThu"
          :loai-bieu-do="loaiBieuDo"
        />
      </a-col>
      <a-col :span="7">
        <DanhSachSanPhamBanChay v-model:ky-thong-ke="kyTopProducts" :du-lieu-san-pham="duLieuSanPhamBanChay" />
      </a-col>
    </a-row>

    <!-- Biểu đồ Row 2: 3 pie charts -->
    <a-row :gutter="16" style="margin-top: 16px">
      <a-col :span="10">
        <BieuDoTrangThaiDonHang v-model:ky-thong-ke="kyTrangThaiDonHang" :du-lieu="duLieuTrangThaiDonHang" />
      </a-col>
      <a-col :span="7">
        <BieuDoPhanPhoiKenh :du-lieu="duLieuKenhPhanPhoi" />
      </a-col>
      <a-col :span="7">
        <BieuDoDanhMuc :du-lieu="duLieuDanhMuc" />
      </a-col>
    </a-row>

    <!-- Bảng thống kê chi tiết -->
    <a-row :gutter="16" style="margin-top: 16px">
      <a-col :span="24">
        <BangThongKeChiTiet :du-lieu="duLieuBangChiTiet" :phan-trang="phanTrangBangChiTiet" />
      </a-col>
    </a-row>

    <!-- Bảng sản phẩm bán chạy nhất -->
    <a-row :gutter="16" style="margin-top: 16px">
      <a-col :span="24">
        <BangSanPhamBanChayNhat :du-lieu="sanPhamBanChayNhat" :phan-trang="phanTrangTopSelling" />
      </a-col>
    </a-row>

    <!-- Bảng sản phẩm sắp hết hàng -->
    <a-row :gutter="16" style="margin-top: 16px">
      <a-col :span="24">
        <BangSanPhamSapHetHang :du-lieu="sanPhamSapHetHang" :phan-trang="phanTrangLowStock" />
      </a-col>
    </a-row>

    <!-- AI Assistant Drawer -->
    <a-drawer :visible="showAI" :width="520" unmountOnClose @cancel="showAI = false" :footer="false" title="Trợ lý AI">
      <AIChatbot ref="aiChatbotRef" :suppress-connection-notice="true" />
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import { useThongKeData } from '../composables/useThongKeData'
import { useTinhToanThongKe } from '../composables/useTinhToanThongKe'
import { useXuatExcel } from '../composables/useXuatExcel'
import type { KhoangThoiGian, LoaiBieuDo } from '../types/thong-ke.types'
import AIChatbot from '@/components/ai/AIChatbot.vue'

// Import components
import BoLocThongKe from './bo-loc-thong-ke.vue'
import TheThongKe from './the-thong-ke.vue'
import BieuDoDoanhThu from './bieu-do-doanh-thu.vue'
import DanhSachSanPhamBanChay from './danh-sach-san-pham-ban-chay.vue'
import BieuDoTrangThaiDonHang from './bieu-do-trang-thai-don-hang.vue'
import BieuDoPhanPhoiKenh from './bieu-do-phan-phoi-kenh.vue'
import BieuDoDanhMuc from './bieu-do-danh-muc.vue'
import BangThongKeChiTiet from './bang-thong-ke-chi-tiet.vue'
import BangSanPhamBanChayNhat from './bang-san-pham-ban-chay-nhat.vue'
import BangSanPhamSapHetHang from './bang-san-pham-sap-het-hang.vue'

// ============= SETUP =============
const { t } = useI18n()
const { breadcrumbItems } = useBreadcrumb()

// ============= STATE =============
const khoangThoiGian = ref<KhoangThoiGian>('today')
const loaiBieuDo = ref<LoaiBieuDo>('line')
const khoangNgayTuyChon = ref<any[]>([])
const kyDoanhThu = ref('6months')
const kyTopProducts = ref('month')
const kyTrangThaiDonHang = ref<'day' | 'month' | 'year'>('day')
const showAI = ref(false)
const aiChatbotRef = ref()

// ============= DATA FETCHING =============
const { danhSachHoaDon, danhSachChiTietSanPham, taiToanBoDuLieu } = useThongKeData()

// ============= CALCULATIONS =============
const {
  duLieuHomNay,
  duLieuTuan,
  duLieuThang,
  duLieuNam,
  duLieuTuyChon,
  duLieuDoanhThu,
  duLieuSanPhamBanChay,
  duLieuTrangThaiDonHang,
  duLieuKenhPhanPhoi,
  duLieuDanhMuc,
  sanPhamBanChayNhat,
  sanPhamSapHetHang,
  duLieuBangChiTiet,
  capNhatToanBoDuLieu,
  capNhatDuLieuTrangThai,
} = useTinhToanThongKe(danhSachHoaDon, danhSachChiTietSanPham, kyDoanhThu, t)

// ============= EXCEL EXPORT =============
const { xuatBaoCaoThongKe } = useXuatExcel()

// ============= COMPUTED =============
const duLieuHienThi = computed(() => {
  const map = {
    today: duLieuHomNay.value,
    week: duLieuTuan.value,
    month: duLieuThang.value,
    year: duLieuNam.value,
    custom: duLieuTuyChon.value,
  }
  return map[khoangThoiGian.value] || duLieuHomNay.value
})

const tongDonHang = computed(() => duLieuHienThi.value.orders)
const tongDoanhThu = computed(() => duLieuHienThi.value.revenue)

// ============= PAGINATION =============
const phanTrangBangChiTiet = ref({
  current: 1,
  pageSize: 4,
  total: 0,
  showTotal: (total: number, range: [number, number]) => `${range[0]}-${range[1]} / ${total}`,
  showSizeChanger: true,
  showQuickJumper: false,
  pageSizeOptions: ['4', '8', '12'],
  size: 'small',
})

const phanTrangTopSelling = ref({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: (total: number, range: [number, number]) => `${range[0]}-${range[1]} / ${total}`,
  showSizeChanger: true,
  showQuickJumper: false,
  pageSizeOptions: ['10', '20', '50'],
  size: 'small',
})

const phanTrangLowStock = ref({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: (total: number, range: [number, number]) => `${range[0]}-${range[1]} / ${total}`,
  showSizeChanger: true,
  showQuickJumper: false,
  pageSizeOptions: ['10', '20', '50'],
  size: 'small',
})

// ============= METHODS =============
const datLaiBoLoc = () => {
  khoangThoiGian.value = 'today'
  loaiBieuDo.value = 'line'
  khoangNgayTuyChon.value = []
}

const xuatBaoCaoExcel = () => {
  xuatBaoCaoThongKe(
    duLieuBangChiTiet.value,
    sanPhamBanChayNhat.value,
    sanPhamSapHetHang.value,
    tongDoanhThu.value,
    tongDonHang.value,
    duLieuHomNay.value.revenue,
    duLieuTuan.value.revenue,
    duLieuThang.value.revenue,
    duLieuNam.value.revenue
  )
}

const openAIAssistant = async () => {
  showAI.value = true
  await nextTick()
  
  // Generate comprehensive statistics prompt
  const prompt = generateStatisticsPrompt()
  
  // Send the prompt to AI chatbot
  if (aiChatbotRef.value) {
    aiChatbotRef.value.sendMessage(prompt)
  }
}

const generateStatisticsPrompt = () => {
  const currentData = duLieuHienThi.value
  const todayData = duLieuHomNay.value
  const weekData = duLieuTuan.value
  const monthData = duLieuThang.value
  const yearData = duLieuNam.value
  
  const topProducts = sanPhamBanChayNhat.value.slice(0, 5)
  const lowStockProducts = sanPhamSapHetHang.value.slice(0, 5)
  
  return `Hãy phân tích chi tiết dữ liệu thống kê kinh doanh của tôi:

📊 **THỐNG KÊ TỔNG QUAN:**
- Khoảng thời gian hiện tại: ${khoangThoiGian.value}
- Tổng doanh thu: ${currentData.revenue.toLocaleString('vi-VN')} VNĐ
- Tổng đơn hàng: ${currentData.orders} đơn
- Sản phẩm đã bán: ${currentData.productsSold} sản phẩm

📈 **SO SÁNH THEO THỜI GIAN:**
- Hôm nay: ${todayData.revenue.toLocaleString('vi-VN')} VNĐ (${todayData.orders} đơn)
- Tuần này: ${weekData.revenue.toLocaleString('vi-VN')} VNĐ (${weekData.orders} đơn)
- Tháng này: ${monthData.revenue.toLocaleString('vi-VN')} VNĐ (${monthData.orders} đơn)
- Năm này: ${yearData.revenue.toLocaleString('vi-VN')} VNĐ (${yearData.orders} đơn)

🏆 **TOP 5 SẢN PHẨM BÁN CHẠY:**
${topProducts.map((product, index) => 
  `${index + 1}. ${product.tenSanPham} - ${product.soLuongBan} sản phẩm - ${product.doanhThu.toLocaleString('vi-VN')} VNĐ`
).join('\n')}

⚠️ **SẢN PHẨM SẮP HẾT HÀNG:**
${lowStockProducts.map((product, index) => 
  `${index + 1}. ${product.tenSanPham} - Còn ${product.soLuongTon} sản phẩm`
).join('\n')}

📊 **DỮ LIỆU BIỂU ĐỒ:**
- Doanh thu theo ${kyDoanhThu.value}: ${duLieuDoanhThu.value.length} điểm dữ liệu
- Trạng thái đơn hàng theo ${kyTrangThaiDonHang.value}: ${duLieuTrangThaiDonHang.value.length} trạng thái
- Phân phối kênh: ${duLieuKenhPhanPhoi.value.length} kênh
- Phân phối danh mục: ${duLieuDanhMuc.value.length} danh mục

Hãy đưa ra:
1. **Phân tích xu hướng** doanh thu và đơn hàng
2. **Đánh giá hiệu suất** sản phẩm bán chạy
3. **Cảnh báo** về tình trạng tồn kho
4. **Khuyến nghị** cải thiện kinh doanh
5. **Dự đoán** xu hướng trong tương lai

Tôi muốn có cái nhìn tổng quan và chi tiết về tình hình kinh doanh của mình.`
}

// ============= WATCHERS =============
watch(kyTrangThaiDonHang, (newValue) => {
  capNhatDuLieuTrangThai(newValue)
})

watch(duLieuBangChiTiet, (newValue) => {
  phanTrangBangChiTiet.value.total = newValue.length
})

watch(sanPhamBanChayNhat, (newValue) => {
  phanTrangTopSelling.value.total = newValue.length
})

watch(sanPhamSapHetHang, (newValue) => {
  phanTrangLowStock.value.total = newValue.length
})

// ============= LIFECYCLE =============
onMounted(async () => {
  await taiToanBoDuLieu()
  capNhatToanBoDuLieu('day')
})
</script>

<style scoped>
.thong-ke-chung {
  padding: 0 20px 20px 20px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.charts-section {
  margin-top: 16px;
}
</style>
