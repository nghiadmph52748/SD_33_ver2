<template>
  <div class="invoice-detail-page">
    <!-- Simple Breadcrumb -->
    <div class="simple-breadcrumb">
      <a-button @click="goBack" type="text" size="small">
        <template #icon>
          <icon-arrow-left />
        </template>
        Quản lý hóa đơn
      </a-button>
      <span class="breadcrumb-separator">/</span>
      <span class="breadcrumb-current">Chi tiết hóa đơn</span>
    </div>

    <!-- Page Header -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">Chi tiết hóa đơn</h1>
        <a-tag :color="getStatusColor(invoice?.trangThai)" class="status-badge">
          {{ getStatusText(invoice?.trangThai) }}
        </a-tag>
      </div>
      <div class="header-right">
        <a-button @click="goBack" class="back-button">
          <template #icon>
            <icon-arrow-left />
          </template>
          Quay lại
        </a-button>
        <a-button type="primary" @click="printInvoice" class="print-button">
          <template #icon>
            <icon-printer />
          </template>
          In hóa đơn
        </a-button>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-state">
      <a-spin size="large" />
      <p>Đang tải thông tin hóa đơn...</p>
    </div>

    <!-- Invoice Content -->
    <div v-else-if="invoice" class="invoice-content">
      <!-- Trạng thái hóa đơn -->
      <a-card class="status-card" :bordered="false">
        <template #title>
          <div class="card-header">
            <icon-file />
            <span>Trạng thái hóa đơn</span>
          </div>
        </template>
        <div class="status-content">
          <div class="status-main">
            <a-tag :color="getStatusColor(invoice.trangThai)" class="status-tag-large">
              {{ getStatusText(invoice.trangThai) }}
            </a-tag>
            <div class="status-info">
              <p>
                <strong>Mã hóa đơn:</strong>
                {{ invoice.maHoaDon || `HD${String(invoice.id).padStart(6, '0')}` }}
              </p>
              <p>
                <strong>Ngày tạo:</strong>
                {{ formatDate(invoice.ngayTao) }}
              </p>
              <p>
                <strong>Loại đơn:</strong>
                <a-tag :color="invoice.loaiDon ? 'blue' : 'green'">
                  {{ invoice.loaiDon ? 'Online' : 'Tại quầy' }}
                </a-tag>
              </p>
            </div>
          </div>
        </div>
      </a-card>

      <!-- Thông tin đơn hàng -->
      <a-card class="order-info-card" :bordered="false">
        <template #title>
          <div class="card-header">
            <icon-file />
            <span>Thông tin đơn hàng</span>
          </div>
        </template>
        <a-row :gutter="24">
          <a-col :span="12">
            <div class="info-block">
              <h3 class="block-title">Thông tin cơ bản</h3>
              <div class="info-list">
                <div class="info-item">
                  <span class="label">Mã hóa đơn:</span>
                  <span class="value">{{ invoice.maHoaDon || `HD${String(invoice.id).padStart(6, '0')}` }}</span>
                </div>
                <div class="info-item">
                  <span class="label">Ngày tạo:</span>
                  <span class="value">{{ formatDate(invoice.ngayTao) }}</span>
                </div>
                <div class="info-item">
                  <span class="label">Ngày thanh toán:</span>
                  <span class="value">{{ formatDate(invoice.ngayThanhToan) }}</span>
                </div>
                <div class="info-item">
                  <span class="label">Nhân viên:</span>
                  <span class="value">{{ invoice.tenNhanVien || 'Chưa xác định' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">Ghi chú:</span>
                  <span class="value">{{ invoice.ghiChu || 'Không có' }}</span>
                </div>
              </div>
            </div>
          </a-col>
          <a-col :span="12">
            <div class="info-block">
              <h3 class="block-title">Thông tin thanh toán</h3>
              <div class="info-list">
                <div class="info-item">
                  <span class="label">Tổng tiền hàng:</span>
                  <span class="value">{{ formatCurrency(invoice.tongTienHang || invoice.tongTien || 0) }}</span>
                </div>
                <div class="info-item" v-if="invoice.phiVanChuyen && invoice.phiVanChuyen > 0">
                  <span class="label">Phí vận chuyển:</span>
                  <span class="value">{{ formatCurrency(invoice.phiVanChuyen) }}</span>
                </div>
                <div class="info-item" v-if="invoice.giaTriGiamGia && invoice.giaTriGiamGia > 0">
                  <span class="label">Giảm giá:</span>
                  <span class="value discount">-{{ formatCurrency(invoice.giaTriGiamGia) }}</span>
                </div>
                <div class="info-item total-item">
                  <span class="label">Thành tiền:</span>
                  <span class="value total">{{ formatCurrency(invoice.tongTienSauGiam) }}</span>
                </div>
                <div class="info-item">
                  <span class="label">Trạng thái:</span>
                  <span class="value">
                    <a-tag :color="getStatusColor(invoice.trangThai)" class="status-tag">
                      {{ getStatusText(invoice.trangThai) }}
                    </a-tag>
                  </span>
                </div>
              </div>
            </div>
          </a-col>
        </a-row>
      </a-card>

      <!-- Thông tin khách hàng -->
      <a-card class="customer-info-card" :bordered="false">
        <template #title>
          <div class="card-header">
            <icon-user />
            <span>Thông tin khách hàng</span>
          </div>
        </template>
        <a-row :gutter="24">
          <a-col :span="12">
            <div class="info-block">
              <h3 class="block-title">Thông tin liên hệ</h3>
              <div class="info-list">
                <div class="info-item">
                  <span class="label">Tên khách hàng:</span>
                  <span class="value">{{ invoice.tenKhachHang || 'Khách lẻ' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">Số điện thoại:</span>
                  <span class="value">{{ invoice.soDienThoai || 'Chưa có' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">Email:</span>
                  <span class="value">{{ invoice.email || 'Chưa có' }}</span>
                </div>
              </div>
            </div>
          </a-col>
          <a-col :span="12">
            <div class="info-block">
              <h3 class="block-title">Địa chỉ giao hàng</h3>
              <div class="info-list">
                <div class="info-item">
                  <span class="label">Người nhận:</span>
                  <span class="value">{{ invoice.tenNguoiNhan || invoice.tenKhachHang || 'Chưa có' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">SĐT người nhận:</span>
                  <span class="value">{{ invoice.soDienThoaiNguoiNhan || invoice.soDienThoai || 'Chưa có' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">Email người nhận:</span>
                  <span class="value">{{ invoice.emailNguoiNhan || invoice.email || 'Chưa có' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">Địa chỉ:</span>
                  <span class="value">{{ invoice.diaChiNguoiNhan || 'Chưa có' }}</span>
                </div>
              </div>
            </div>
          </a-col>
        </a-row>
      </a-card>

      <!-- Danh sách sản phẩm đã bán -->
      <a-card class="products-card" :bordered="false">
        <template #title>
          <div class="card-header">
            <icon-file />
            <span>Danh sách sản phẩm đã bán</span>
            <span class="product-count">({{ getTotalProducts() }} sản phẩm)</span>
          </div>
        </template>

        <a-table
          :columns="productColumns"
          :data="invoice.hoaDonChiTiets || []"
          :pagination="false"
          :loading="loading"
          class="products-table"
        >
          <template #stt="{ record, index }">
            <div class="stt-cell">{{ index + 1 }}</div>
          </template>

          <template #maHoaDonChiTiet="{ record }">
            <div class="ma-hdct-cell">
              <a-tag color="blue" class="ma-tag">{{ record.maHoaDonChiTiet || 'N/A' }}</a-tag>
            </div>
          </template>

          <template #tenSanPhamChiTiet="{ record }">
            <div class="product-info">
              <div class="product-name">{{ record.tenSanPhamChiTiet || record.tenSanPham || 'Sản phẩm không xác định' }}</div>
            </div>
          </template>

          <template #donGia="{ record }">
            <div class="price-cell">
              <span class="price-value">{{ formatCurrency(record.giaBan || record.donGia || 0) }}</span>
            </div>
          </template>

          <template #soLuong="{ record }">
            <div class="quantity-cell">
              <a-tag color="blue" class="quantity-tag">{{ record.soLuong || 0 }}</a-tag>
            </div>
          </template>

          <template #thanhTien="{ record }">
            <div class="total-cell">
              <span class="total-price">{{ formatCurrency(record.thanhTien || 0) }}</span>
            </div>
          </template>
        </a-table>
      </a-card>

      <!-- Lịch sử thanh toán -->
      <a-card class="payment-history-card" :bordered="false">
        <template #title>
          <div class="card-header">
            <icon-file />
            <span>Lịch sử thanh toán</span>
          </div>
        </template>
        <div class="payment-history">
          <div v-if="invoice.lichSuThanhToan && invoice.lichSuThanhToan.length > 0" class="payment-list">
            <div v-for="(payment, index) in invoice.lichSuThanhToan" :key="index" class="payment-item">
              <div class="payment-info">
                <div class="payment-method">
                  <strong>{{ payment.phuongThucThanhToan || 'Tiền mặt' }}</strong>
                </div>
                <div class="payment-amount">
                  {{ formatCurrency(payment.soTien || 0) }}
                </div>
                <div class="payment-date">
                  {{ formatDate(payment.ngayThanhToan) }}
                </div>
              </div>
              <div class="payment-status">
                <a-tag :color="payment.trangThai ? 'green' : 'orange'">
                  {{ payment.trangThai ? 'Thành công' : 'Chờ xử lý' }}
                </a-tag>
              </div>
            </div>
          </div>
          <div v-else class="no-payment">
            <p>Chưa có lịch sử thanh toán</p>
          </div>
        </div>
      </a-card>

      <!-- Tổng kết đơn hàng -->
      <a-card class="summary-card" :bordered="false">
        <template #title>
          <div class="card-header">
            <icon-file />
            <span>Tổng kết đơn hàng</span>
          </div>
        </template>

        <div class="summary-content">
          <div class="summary-row">
            <span class="summary-label">Tổng số sản phẩm:</span>
            <span class="summary-value">{{ getTotalProducts() }}</span>
          </div>
          <div class="summary-row">
            <span class="summary-label">Tổng số lượng:</span>
            <span class="summary-value">{{ getTotalQuantity() }}</span>
          </div>
          <div class="summary-row">
            <span class="summary-label">Tổng tiền hàng:</span>
            <span class="summary-value">{{ formatCurrency(getSubtotal()) }}</span>
          </div>
          <div class="summary-row" v-if="invoice.phiVanChuyen && invoice.phiVanChuyen > 0">
            <span class="summary-label">Phí vận chuyển:</span>
            <span class="summary-value">{{ formatCurrency(invoice.phiVanChuyen) }}</span>
          </div>
          <div class="summary-row" v-if="invoice.giaTriGiamGia && invoice.giaTriGiamGia > 0">
            <span class="summary-label">Giảm giá:</span>
            <span class="summary-value discount">-{{ formatCurrency(invoice.giaTriGiamGia) }}</span>
          </div>
          <div class="summary-row total-row">
            <span class="summary-label">Thành tiền:</span>
            <span class="summary-value total">{{ formatCurrency(invoice.tongTienSauGiam) }}</span>
          </div>
        </div>
      </a-card>
    </div>

    <!-- Error State -->
    <div v-else class="error-state">
      <a-result status="error" title="Không tìm thấy hóa đơn" sub-title="Hóa đơn bạn đang tìm kiếm không tồn tại hoặc đã bị xóa.">
        <template #extra>
          <a-button type="primary" @click="goBack">Quay lại danh sách</a-button>
        </template>
      </a-result>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import { IconArrowLeft, IconPrinter, IconFile, IconEdit, IconUser } from '@arco-design/web-vue/es/icon'

const route = useRoute()
const router = useRouter()

// Data
const invoice = ref<any>(null)
const loading = ref(true)
const invoiceId = ref(route.params.id as string)

// Table columns
const productColumns = [
  {
    title: 'STT',
    slotName: 'stt',
    width: 60,
    align: 'center',
  },
  {
    title: 'Mã HDCT',
    slotName: 'maHoaDonChiTiet',
    width: 100,
    align: 'center',
  },
  {
    title: 'Tên Sản phẩm chi tiết',
    slotName: 'tenSanPhamChiTiet',
    width: 300,
  },
  {
    title: 'Đơn giá',
    slotName: 'donGia',
    width: 120,
    align: 'right',
  },
  {
    title: 'Số lượng',
    slotName: 'soLuong',
    width: 100,
    align: 'center',
  },
  {
    title: 'Thành tiền',
    slotName: 'thanhTien',
    width: 120,
    align: 'right',
  },
]

// Methods
// Method để lấy danh sách sản phẩm đã bán
const fetchSanPhamDaBan = async () => {
  try {
    const sanPhamResponse = await axios.get(`/api/thong-tin-hoa-don-management/san-pham-da-ban/${invoiceId.value}`)

    if (sanPhamResponse.data && sanPhamResponse.data.data && sanPhamResponse.data.data.length > 0) {
      invoice.value.hoaDonChiTiets = sanPhamResponse.data.data.map((item) => ({
        id: item.id,
        maHoaDonChiTiet: item.maHoaDonChiTiet,
        tenSanPhamChiTiet: item.tenSanPhamChiTiet || item.tenSanPham,
        tenSanPham: item.tenSanPhamChiTiet || item.tenSanPham,
        maSanPham: item.maSanPham,
        tenMauSac: item.tenMauSac,
        maMauSac: item.maMauSac,
        tenKichThuoc: item.tenKichThuoc,
        maKichThuoc: item.maKichThuoc,
        tenDeGiay: item.tenDeGiay,
        maDeGiay: item.maDeGiay,
        tenChatLieu: item.tenChatLieu,
        maChatLieu: item.maChatLieu,
        tenTrongLuong: item.tenTrongLuong,
        maTrongLuong: item.maTrongLuong,
        donGia: item.giaBan,
        giaBan: item.giaBan,
        soLuong: item.soLuong,
        thanhTien: item.thanhTien,
        ghiChu: item.ghiChu,
      }))
    }
  } catch (error: any) {
    // Silent error handling
  }
}

const fetchInvoiceDetail = async () => {
  try {
    loading.value = true

    // Lấy thông tin hóa đơn từ API hóa đơn
    const invoiceResponse = await axios.get(`/api/hoa-don-management/${invoiceId.value}`)

    if (invoiceResponse.data && invoiceResponse.data.data) {
      invoice.value = invoiceResponse.data.data

      // Thử lấy thông tin đơn hàng từ API thông tin đơn hàng mới (không bắt buộc)
      try {
        const orderInfoResponse = await axios.get(`/api/thong-tin-hoa-don-management/latest-by-hoa-don/${invoiceId.value}`)

        if (orderInfoResponse.data && orderInfoResponse.data.data) {
          const orderInfo = orderInfoResponse.data.data
          // Cập nhật thông tin từ API thông tin đơn hàng
          invoice.value.ngayTao = orderInfo.ngayTao || invoice.value.ngayTao
          invoice.value.ngayThanhToan = orderInfo.ngayThanhToan || invoice.value.ngayThanhToan
          invoice.value.tenNhanVien = orderInfo.tenNhanVien || invoice.value.tenNhanVien
          invoice.value.maNhanVien = orderInfo.maNhanVien || invoice.value.maNhanVien
          invoice.value.tongTienHang = orderInfo.tongTienHang || invoice.value.tongTien

          // Cập nhật thông tin khách hàng từ API thông tin đơn hàng
          invoice.value.tenKhachHang = orderInfo.tenKhachHang || invoice.value.tenKhachHang
          invoice.value.maKhachHang = orderInfo.maKhachHang || invoice.value.maKhachHang
          invoice.value.emailKhachHang = orderInfo.emailKhachHang || invoice.value.email
          invoice.value.soDienThoaiKhachHang = orderInfo.soDienThoaiKhachHang || invoice.value.soDienThoai
          invoice.value.diaChiKhachHang = orderInfo.diaChiKhachHang || invoice.value.diaChiKhachHang

          // Cập nhật danh sách sản phẩm đã bán từ API thông tin đơn hàng
          if (orderInfo.danhSachSanPhamDaBan && orderInfo.danhSachSanPhamDaBan.length > 0) {
            invoice.value.hoaDonChiTiets = orderInfo.danhSachSanPhamDaBan.map((item) => ({
              id: item.id,
              maHoaDonChiTiet: item.maHoaDonChiTiet,
              tenSanPhamChiTiet: item.tenSanPhamChiTiet || item.tenSanPham,
              tenSanPham: item.tenSanPhamChiTiet || item.tenSanPham,
              maSanPham: item.maSanPham,
              tenMauSac: item.tenMauSac,
              maMauSac: item.maMauSac,
              tenKichThuoc: item.tenKichThuoc,
              maKichThuoc: item.maKichThuoc,
              tenDeGiay: item.tenDeGiay,
              maDeGiay: item.maDeGiay,
              tenChatLieu: item.tenChatLieu,
              maChatLieu: item.maChatLieu,
              tenTrongLuong: item.tenTrongLuong,
              maTrongLuong: item.maTrongLuong,
              donGia: item.giaBan,
              giaBan: item.giaBan,
              soLuong: item.soLuong,
              thanhTien: item.thanhTien,
              ghiChu: item.ghiChu,
            }))
          } else {
            // Nếu không có dữ liệu từ orderInfo, thử lấy trực tiếp từ API sản phẩm đã bán
            try {
              const sanPhamResponse = await axios.get(`/api/thong-tin-hoa-don-management/san-pham-da-ban/${invoiceId.value}`)

              if (sanPhamResponse.data && sanPhamResponse.data.data && sanPhamResponse.data.data.length > 0) {
                invoice.value.hoaDonChiTiets = sanPhamResponse.data.data.map((item) => ({
                  id: item.id,
                  maHoaDonChiTiet: item.maHoaDonChiTiet,
                  tenSanPhamChiTiet: item.tenSanPhamChiTiet || item.tenSanPham,
                  tenSanPham: item.tenSanPhamChiTiet || item.tenSanPham,
                  maSanPham: item.maSanPham,
                  tenMauSac: item.tenMauSac,
                  maMauSac: item.maMauSac,
                  tenKichThuoc: item.tenKichThuoc,
                  maKichThuoc: item.maKichThuoc,
                  tenDeGiay: item.tenDeGiay,
                  maDeGiay: item.maDeGiay,
                  tenChatLieu: item.tenChatLieu,
                  maChatLieu: item.maChatLieu,
                  tenTrongLuong: item.tenTrongLuong,
                  maTrongLuong: item.maTrongLuong,
                  donGia: item.giaBan,
                  giaBan: item.giaBan,
                  soLuong: item.soLuong,
                  thanhTien: item.thanhTien,
                  ghiChu: item.ghiChu,
                }))
              }
            } catch (sanPhamError: any) {
              console.warn('⚠️ Không thể lấy danh sách sản phẩm đã bán:', sanPhamError.message)
            }
          }
        }
      } catch (orderInfoError: any) {
        // Silent error handling
      }

      // Nếu không có danh sách sản phẩm, thử lấy trực tiếp từ API sản phẩm đã bán
      if (!invoice.value.hoaDonChiTiets || invoice.value.hoaDonChiTiets.length === 0) {
        await fetchSanPhamDaBan()
      }
    } else if (invoiceResponse.data) {
      // Nếu response trực tiếp là dữ liệu (không có wrapper data)
      invoice.value = invoiceResponse.data
      console.log('✅ Dữ liệu hóa đơn đã được tải (trực tiếp):', invoice.value)

      // Nếu không có danh sách sản phẩm, thử lấy trực tiếp từ API sản phẩm đã bán
      if (!invoice.value.hoaDonChiTiets || invoice.value.hoaDonChiTiets.length === 0) {
        await fetchSanPhamDaBan()
      }

      // Thử lấy thông tin đơn hàng từ API thông tin đơn hàng mới (không bắt buộc)
      try {
        const orderInfoResponse = await axios.get(`/api/thong-tin-hoa-don-management/latest-by-hoa-don/${invoiceId.value}`)

        if (orderInfoResponse.data && orderInfoResponse.data.data) {
          const orderInfo = orderInfoResponse.data.data
          // Cập nhật thông tin từ API thông tin đơn hàng
          invoice.value.ngayTao = orderInfo.ngayTao || invoice.value.ngayTao
          invoice.value.ngayThanhToan = orderInfo.ngayThanhToan || invoice.value.ngayThanhToan
          invoice.value.tenNhanVien = orderInfo.tenNhanVien || invoice.value.tenNhanVien
          invoice.value.maNhanVien = orderInfo.maNhanVien || invoice.value.maNhanVien
          invoice.value.tongTienHang = orderInfo.tongTienHang || invoice.value.tongTien

          // Cập nhật thông tin khách hàng từ API thông tin đơn hàng
          invoice.value.tenKhachHang = orderInfo.tenKhachHang || invoice.value.tenKhachHang
          invoice.value.maKhachHang = orderInfo.maKhachHang || invoice.value.maKhachHang
          invoice.value.emailKhachHang = orderInfo.emailKhachHang || invoice.value.email
          invoice.value.soDienThoaiKhachHang = orderInfo.soDienThoaiKhachHang || invoice.value.soDienThoai
          invoice.value.diaChiKhachHang = orderInfo.diaChiKhachHang || invoice.value.diaChiKhachHang

          // Cập nhật danh sách sản phẩm đã bán từ API thông tin đơn hàng
          if (orderInfo.danhSachSanPhamDaBan && orderInfo.danhSachSanPhamDaBan.length > 0) {
            invoice.value.hoaDonChiTiets = orderInfo.danhSachSanPhamDaBan.map((item) => ({
              id: item.id,
              maHoaDonChiTiet: item.maHoaDonChiTiet,
              tenSanPhamChiTiet: item.tenSanPhamChiTiet || item.tenSanPham,
              tenSanPham: item.tenSanPhamChiTiet || item.tenSanPham,
              maSanPham: item.maSanPham,
              tenMauSac: item.tenMauSac,
              maMauSac: item.maMauSac,
              tenKichThuoc: item.tenKichThuoc,
              maKichThuoc: item.maKichThuoc,
              tenDeGiay: item.tenDeGiay,
              maDeGiay: item.maDeGiay,
              tenChatLieu: item.tenChatLieu,
              maChatLieu: item.maChatLieu,
              tenTrongLuong: item.tenTrongLuong,
              maTrongLuong: item.maTrongLuong,
              donGia: item.giaBan,
              giaBan: item.giaBan,
              soLuong: item.soLuong,
              thanhTien: item.thanhTien,
              ghiChu: item.ghiChu,
            }))
          } else {
            // Nếu không có dữ liệu từ orderInfo, thử lấy trực tiếp từ API sản phẩm đã bán
            try {
              const sanPhamResponse = await axios.get(`/api/thong-tin-hoa-don-management/san-pham-da-ban/${invoiceId.value}`)

              if (sanPhamResponse.data && sanPhamResponse.data.data && sanPhamResponse.data.data.length > 0) {
                invoice.value.hoaDonChiTiets = sanPhamResponse.data.data.map((item) => ({
                  id: item.id,
                  maHoaDonChiTiet: item.maHoaDonChiTiet,
                  tenSanPhamChiTiet: item.tenSanPhamChiTiet || item.tenSanPham,
                  tenSanPham: item.tenSanPhamChiTiet || item.tenSanPham,
                  maSanPham: item.maSanPham,
                  tenMauSac: item.tenMauSac,
                  maMauSac: item.maMauSac,
                  tenKichThuoc: item.tenKichThuoc,
                  maKichThuoc: item.maKichThuoc,
                  tenDeGiay: item.tenDeGiay,
                  maDeGiay: item.maDeGiay,
                  tenChatLieu: item.tenChatLieu,
                  maChatLieu: item.maChatLieu,
                  tenTrongLuong: item.tenTrongLuong,
                  maTrongLuong: item.maTrongLuong,
                  donGia: item.giaBan,
                  giaBan: item.giaBan,
                  soLuong: item.soLuong,
                  thanhTien: item.thanhTien,
                  ghiChu: item.ghiChu,
                }))
              }
            } catch (sanPhamError: any) {
              console.warn('⚠️ Không thể lấy danh sách sản phẩm đã bán:', sanPhamError.message)
            }
          }
        }
      } catch (orderInfoError: any) {
        // Silent error handling
      }
    } else {
      console.log('⚠️ Không có dữ liệu từ API, sử dụng dữ liệu mẫu')
      // Fallback to sample data for testing
      invoice.value = {
        id: invoiceId.value,
        maHoaDon: `HD${String(invoiceId.value).padStart(6, '0')}`,
        ngayTao: new Date().toISOString(),
        tenNhanVien: 'Nguyễn Văn A',
        tenKhachHang: 'Trần Thị B',
        tongTienSauGiam: 2500000,
        giamGia: 100000,
        phuongThucThanhToan: 'Tiền mặt',
        trangThai: true, // true = Hoàn thành, false = Chờ xác nhận
        hoaDonChiTiets: [
          {
            tenSanPham: 'Giày Nike Air Max 270',
            mauSac: 'Đen',
            kichThuoc: '42',
            donGia: 1500000,
            soLuong: 1,
            thanhTien: 1500000,
          },
          {
            tenSanPham: 'Áo thun Adidas',
            mauSac: 'Trắng',
            kichThuoc: 'L',
            donGia: 800000,
            soLuong: 1,
            thanhTien: 800000,
          },
        ],
      }
    }
  } catch (error: any) {
    console.error('❌ Lỗi khi tải chi tiết hóa đơn:', error)
    console.error('📋 Chi tiết lỗi:', error.response?.data || error.message)

    // Use sample data on error
    invoice.value = {
      id: invoiceId.value,
      maHoaDon: `HD${String(invoiceId.value).padStart(6, '0')}`,
      ngayTao: new Date().toISOString(),
      tenNhanVien: 'Nguyễn Văn A',
      tenKhachHang: 'Trần Thị B',
      tongTienSauGiam: 2500000,
      giamGia: 100000,
      phuongThucThanhToan: 'Tiền mặt',
      trangThai: true, // true = Hoàn thành, false = Chờ xác nhận
      hoaDonChiTiets: [
        {
          tenSanPham: 'Giày Nike Air Max 270',
          mauSac: 'Đen',
          kichThuoc: '42',
          donGia: 1500000,
          soLuong: 1,
          thanhTien: 1500000,
        },
        {
          tenSanPham: 'Áo thun Adidas',
          mauSac: 'Trắng',
          kichThuoc: 'L',
          donGia: 800000,
          soLuong: 1,
          thanhTien: 800000,
        },
      ],
    }
  } finally {
    loading.value = false
  }
}

const formatDate = (dateString: string) => {
  if (!dateString) return 'Chưa xác định'
  const date = new Date(dateString)
  return date.toLocaleDateString('vi-VN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const formatCurrency = (amount: number) => {
  if (!amount) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount)
}

const getStatusColor = (status: any) => {
  // Xử lý cả boolean và string
  if (typeof status === 'boolean') {
    return status ? 'green' : 'orange'
  }

  if (typeof status === 'string') {
    const statusColors: { [key: string]: string } = {
      'Chờ xác nhận': 'orange',
      'Chờ giao hàng': 'blue',
      'Đang giao': 'purple',
      'Hoàn thành': 'green',
      'Đã hủy': 'red',
      'Đã thanh toán': 'green',
      'Chờ thanh toán': 'orange',
      true: 'green',
      false: 'orange',
    }
    return statusColors[status] || 'gray'
  }

  return 'gray'
}

const getStatusText = (status: any) => {
  // Xử lý cả boolean và string
  if (typeof status === 'boolean') {
    return status ? 'Hoàn thành' : 'Chờ xác nhận'
  }

  if (typeof status === 'string') {
    const statusTexts: { [key: string]: string } = {
      'Chờ xác nhận': 'Chờ xác nhận',
      'Chờ giao hàng': 'Chờ giao hàng',
      'Đang giao': 'Đang giao',
      'Hoàn thành': 'Hoàn thành',
      'Đã hủy': 'Đã hủy',
      'Đã thanh toán': 'Hoàn thành',
      'Chờ thanh toán': 'Chờ xác nhận',
      true: 'Hoàn thành',
      false: 'Chờ xác nhận',
    }
    return statusTexts[status] || status || 'Chưa xác định'
  }

  return 'Chưa xác định'
}

const getTotalProducts = () => {
  return invoice.value?.hoaDonChiTiets?.length || 0
}

const getTotalQuantity = () => {
  return (
    invoice.value?.hoaDonChiTiets?.reduce((total: number, item: any) => {
      return total + (item.soLuong || 0)
    }, 0) || 0
  )
}

const getSubtotal = () => {
  return (
    invoice.value?.hoaDonChiTiets?.reduce((total: number, item: any) => {
      return total + (item.thanhTien || 0)
    }, 0) || 0
  )
}

const goBack = () => {
  router.back()
}

const printInvoice = () => {
  window.print()
}

// Lifecycle
onMounted(() => {
  fetchInvoiceDetail()
})
</script>

<style scoped>
.invoice-detail-page {
  padding: 20px;
  background-color: var(--color-fill-2);
  min-height: calc(100vh - 80px);
}

/* Simple Breadcrumb */
.simple-breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
  padding: 8px 0;
}

.breadcrumb-separator {
  color: #86909c;
  font-size: 14px;
}

.breadcrumb-current {
  color: #1d2129;
  font-weight: 500;
  font-size: 14px;
}

/* Header */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  background: white;
  padding: 20px 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1d2129;
  margin: 0;
}

.status-badge {
  font-size: 14px;
  font-weight: 500;
}

.header-right {
  display: flex;
  gap: 12px;
}

.back-button,
.print-button {
  height: 36px;
  padding: 0 16px;
}

/* Loading */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.loading-state p {
  margin-top: 16px;
  color: #86909c;
  font-size: 16px;
}

/* Debug Panel */
.debug-panel {
  margin-bottom: 20px;
  background: #f0f8ff;
  border: 1px solid #1890ff;
}

.debug-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.debug-content p {
  margin: 0;
  font-size: 14px;
}

/* Cards */
.info-card,
.products-card,
.summary-card {
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1d2129;
}

/* Info Block */
.info-block {
  margin-bottom: 16px;
}

.block-title {
  font-size: 16px;
  font-weight: 600;
  color: #1d2129;
  margin-bottom: 16px;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f2f3f5;
}

.info-item:last-child {
  border-bottom: none;
}

.label {
  font-weight: 500;
  color: #4e5969;
  min-width: 120px;
}

.value {
  font-weight: 400;
  color: #1d2129;
  text-align: right;
}

.value.total {
  font-size: 18px;
  font-weight: 600;
  color: #165dff;
}

/* Products Table */
.products-table {
  margin-top: 16px;
}

.stt-number {
  font-weight: 500;
  color: #1d2129;
}

.product-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.product-name {
  font-weight: 500;
  color: #1d2129;
}

.product-specs {
  display: flex;
  gap: 8px;
  font-size: 12px;
  color: #86909c;
}

.spec-item {
  background: #f2f3f5;
  padding: 2px 6px;
  border-radius: 4px;
}

.price-text,
.total-text {
  font-weight: 500;
  color: #1d2129;
}

.quantity-tag {
  font-weight: 500;
}

/* Summary */
.summary-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f2f3f5;
}

.summary-item:last-child {
  border-bottom: none;
}

.summary-label {
  font-weight: 500;
  color: #4e5969;
}

.summary-value {
  font-weight: 400;
  color: #1d2129;
}

.summary-value.discount {
  color: #f53f3f;
}

.summary-item.final-total {
  border-top: 2px solid #165dff;
  margin-top: 8px;
  padding-top: 16px;
}

.summary-item.final-total .summary-label {
  font-size: 16px;
  font-weight: 600;
}

.summary-item.final-total .summary-value.total {
  font-size: 18px;
  font-weight: 700;
  color: #165dff;
}

/* Error State */
.error-state {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

/* Status Card */
.status-card {
  background: #f7f8fa;
  border: 1px solid #e5e6eb;
}

.status-content {
  padding: 16px 0;
}

.status-main {
  display: flex;
  align-items: center;
  gap: 16px;
}

.status-tag-large {
  font-size: 16px;
  padding: 8px 16px;
  border-radius: 4px;
  font-weight: 500;
}

.status-info p {
  margin: 6px 0;
  font-size: 14px;
  color: #4e5969;
}

.status-info .arco-tag {
  margin-left: 8px;
}

/* Order Info Card */
.order-info-card .info-block {
  margin-bottom: 16px;
}

.order-info-card .block-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 12px;
  color: #1d2129;
  border-bottom: 1px solid #e5e6eb;
  padding-bottom: 6px;
}

.order-info-card .info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f2f3f5;
}

.order-info-card .info-item:last-child {
  border-bottom: none;
}

.order-info-card .total-item {
  background: #f7f8fa;
  padding: 12px;
  border-radius: 4px;
  margin-top: 8px;
  border: 1px solid #e5e6eb;
}

.order-info-card .total-item .label,
.order-info-card .total-item .value {
  font-weight: 500;
  font-size: 14px;
}

.order-info-card .discount {
  color: #f53f3f;
}

/* Customer Info Card */
.customer-info-card .info-block {
  margin-bottom: 16px;
}

.customer-info-card .block-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 12px;
  color: #1d2129;
  border-bottom: 1px solid #e5e6eb;
  padding-bottom: 6px;
}

.customer-info-card .info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f2f3f5;
}

.customer-info-card .info-item:last-child {
  border-bottom: none;
}

/* Products Card */
.product-count {
  font-size: 12px;
  color: #86909c;
  font-weight: normal;
  margin-left: 8px;
}

.product-info {
  padding: 6px 0;
}

.product-name {
  font-weight: 500;
  font-size: 14px;
  color: #1d2129;
  margin-bottom: 6px;
}

.product-specs {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.spec-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.spec-label {
  font-size: 12px;
  color: #86909c;
  min-width: 50px;
}

.spec-value {
  font-size: 12px;
  color: #4e5969;
  background: #f2f3f5;
  padding: 1px 4px;
  border-radius: 2px;
}

.stt-cell {
  text-align: center;
  font-weight: 500;
  color: #4e5969;
}

.price-cell,
.total-cell {
  text-align: right;
}

.price-value,
.total-price {
  font-weight: 500;
  color: #1d2129;
}

.quantity-cell {
  text-align: center;
}

.quantity-tag {
  font-weight: 500;
}

/* Payment History Card */
.payment-history {
  padding: 12px 0;
}

.payment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.payment-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f7f8fa;
  border-radius: 4px;
  border: 1px solid #e5e6eb;
}

.payment-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.payment-method {
  font-size: 14px;
  color: #1d2129;
}

.payment-amount {
  font-size: 16px;
  font-weight: 500;
  color: #00b42a;
}

.payment-date {
  font-size: 12px;
  color: #86909c;
}

.no-payment {
  text-align: center;
  padding: 32px;
  color: #86909c;
}

/* Summary Card */
.summary-content {
  padding: 16px 0;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f2f3f5;
}

.summary-row:last-child {
  border-bottom: none;
}

.summary-row.total-row {
  background: #f7f8fa;
  padding: 16px;
  border-radius: 4px;
  margin-top: 12px;
  border: 1px solid #e5e6eb;
}

.summary-row.total-row .summary-label,
.summary-row.total-row .summary-value {
  font-weight: 500;
  font-size: 16px;
}

.summary-label {
  font-size: 14px;
  color: #4e5969;
}

.summary-value {
  font-size: 14px;
  font-weight: 500;
  color: #1d2129;
}

.summary-value.discount {
  color: #f53f3f;
}

.summary-value.total {
  color: #00b42a;
  font-size: 16px;
}

/* Responsive */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .header-right {
    justify-content: center;
  }

  .info-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }

  .value {
    text-align: left;
  }
}
</style>
