<template>
  <div v-if="loading" class="loading-container">
    <div class="loading-spinner">
      <div class="spinner"></div>
      <p>Đang tải dữ liệu hóa đơn...</p>
    </div>
  </div>

  <div v-else class="invoice-print-page">
    <!-- Nút quay lại -->
    <div class="back-button-container">
      <button @click="goBack" class="back-button">← Quay lại</button>
    </div>

    <!-- Header -->
    <div class="invoice-header">
      <div class="company-info">
        <img src="/src/assets/logo-datn.png" alt="GearUp" class="company-logo" />
        <p class="company-subtitle">Cửa hàng giày thể thao</p>
        <div class="company-details">
          <p>Địa chỉ: FPT polytechnic, Hà Nội</p>
          <p>Hotline: 0332 050 542</p>
          <p>Email: truongtqph50260@gearup.com</p>
        </div>
      </div>
      <div class="invoice-title">
        <h2>BIÊN LAI BÁN HÀNG</h2>
        <div class="invoice-number">
          <p>
            <strong>Mã hóa đơn:</strong>
            {{ invoice?.maHoaDon || 'HD00001' }}
          </p>
          <p>
            <strong>Ngày tạo:</strong>
            {{ formatDate(invoice?.ngayTao) }}
          </p>
        </div>
      </div>
    </div>

    <!-- Thông tin đơn hàng -->
    <div class="invoice-section">
      <h3 class="section-title">THÔNG TIN ĐƠN HÀNG</h3>
      <div class="info-grid">
        <div class="info-column">
          <h4>Thông tin khách hàng</h4>
          <div class="info-item">
            <span class="label">Tên khách hàng:</span>
            <span class="value">{{ invoice?.tenKhachHang || 'Khách lẻ' }}</span>
          </div>
          <div class="info-item">
            <span class="label">Số điện thoại:</span>
            <span class="value">{{ invoice?.soDienThoaiKhachHang || invoice?.soDienThoai || 'N/A' }}</span>
          </div>
          <div class="info-item">
            <span class="label">Email:</span>
            <span class="value">{{ invoice?.emailKhachHang || invoice?.email || 'N/A' }}</span>
          </div>
          <div class="info-item">
            <span class="label">Địa chỉ:</span>
            <span class="value">{{ invoice?.diaChiKhachHang || 'N/A' }}</span>
          </div>
        </div>
        <div class="info-column">
          <h4>Thông tin nhân viên</h4>
          <div class="info-item">
            <span class="label">Tên nhân viên:</span>
            <span class="value">{{ invoice?.tenNhanVien || 'Admin' }}</span>
          </div>
          <div class="info-item">
            <span class="label">Ngày thanh toán:</span>
            <span class="value">{{ formatDate(invoice?.ngayThanhToan) }}</span>
          </div>
          <div class="info-item">
            <span class="label">Trạng thái:</span>
            <span class="value status-paid">Đã thanh toán</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Danh sách sản phẩm -->
    <div class="invoice-section">
      <h3 class="section-title">DANH SÁCH SẢN PHẨM ĐÃ BÁN</h3>
      <div class="products-table">
        <table>
          <thead>
            <tr>
              <th>STT</th>
              <th>Tên sản phẩm</th>
              <th>Thuộc tính</th>
              <th>Số lượng</th>
              <th>Đơn giá</th>
              <th>Thành tiền</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(product, index) in products" :key="product.id">
              <td class="center">{{ index + 1 }}</td>
              <td class="product-name">{{ product.tenSanPhamChiTiet || product.tenSanPham }}</td>
              <td class="product-attributes">
                <div v-if="product.tenMauSac" class="attribute">Màu: {{ product.tenMauSac }}</div>
                <div v-if="product.tenKichThuoc" class="attribute">Size: {{ product.tenKichThuoc }}</div>
                <div v-if="product.tenChatLieu" class="attribute">Chất liệu: {{ product.tenChatLieu }}</div>
                <div v-if="product.tenDeGiay" class="attribute">Đế: {{ product.tenDeGiay }}</div>
              </td>
              <td class="center">{{ product.soLuong }}</td>
              <td class="right">{{ formatPrice(calculateUnitPrice(product)) }}</td>
              <td class="right total">{{ formatPrice(product.thanhTien) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Tổng kết đơn hàng -->
    <div class="invoice-section">
      <h3 class="section-title">TỔNG KẾT ĐƠN HÀNG</h3>
      <div class="summary-table">
        <table>
          <tbody>
            <tr>
              <td class="label">Tổng tiền hàng:</td>
              <td class="value">{{ formatPrice(invoice?.tongTienHang || invoice?.tongTien || 0) }}</td>
            </tr>
            <tr v-if="invoice?.phiVanChuyen && invoice.phiVanChuyen > 0">
              <td class="label">Phí vận chuyển:</td>
              <td class="value">{{ formatPrice(invoice.phiVanChuyen) }}</td>
            </tr>
            <tr v-if="invoice?.giaTriGiamGia && invoice.giaTriGiamGia > 0">
              <td class="label">Giảm giá:</td>
              <td class="value discount">-{{ formatPrice(invoice.giaTriGiamGia) }}</td>
            </tr>
            <tr v-if="invoice?.phuPhi && invoice.phuPhi > 0">
              <td class="label">Phụ phí:</td>
              <td class="value">{{ formatPrice(invoice.phuPhi) }}</td>
            </tr>
            <tr v-if="invoice?.hoanPhi && invoice.hoanPhi > 0">
              <td class="label">Hoàn phí:</td>
              <td class="value discount">-{{ formatPrice(invoice.hoanPhi) }}</td>
            </tr>
            <tr class="total-row">
              <td class="label"><strong>THÀNH TIỀN:</strong></td>
              <td class="value">
                <strong>{{ formatPrice(calculateFinalTotal(invoice)) }}</strong>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Footer -->
    <div class="invoice-footer">
      <div class="thank-you">
        <p><strong>Cảm ơn quý khách đã mua hàng!</strong></p>
        <p>Hẹn gặp lại quý khách lần sau</p>
      </div>
      <div class="signature">
        <p>Người bán</p>
        <div class="signature-line"></div>
        <p class="signature-name">{{ invoice?.tenNhanVien || 'Admin' }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const invoice = ref<any>(null)
const products = ref<any[]>([])
const loading = ref(true)

// Methods
const goBack = () => {
  router.push('/quan-ly-hoa-don/index').then(() => {
    // Reload trang sau khi chuyển route
    window.location.reload()
  })
}

// Methods
const formatPrice = (price: number) => {
  if (!price) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(price)
}

// Calculate unit price: prefer thanhTien / soLuong if available, otherwise use giaBan
const calculateUnitPrice = (product: any): number => {
  // If we have thanhTien and soLuong, calculate unit price from them (most accurate)
  if (product.thanhTien && product.soLuong && product.soLuong > 0) {
    const calculatedPrice = Number(product.thanhTien) / Number(product.soLuong)
    // Round to nearest integer to avoid decimal issues
    return Math.round(calculatedPrice)
  }

  // Fallback to giaBan if available
  if (product.giaBan) {
    return Number(product.giaBan)
  }

  return 0
}

const formatDate = (date: string) => {
  if (!date) return 'N/A'
  return new Date(date).toLocaleDateString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const calculateFinalTotal = (invoice: any) => {
  if (!invoice) return 0

  const backendTotal = invoice.tongTienSauGiam
  if (backendTotal !== undefined && backendTotal !== null) {
    const numericTotal = Number(backendTotal)
    return Number.isFinite(numericTotal) ? Math.max(0, numericTotal) : 0
  }

  const base = Number(invoice.tongTien || 0)
  const shipping = Number(invoice.phiVanChuyen || 0)
  const surcharge = Number(invoice.phuPhi || 0)
  const refund = Number(invoice.hoanPhi || 0)
  const manualTotal = base + shipping + surcharge - refund
  return Math.max(0, manualTotal)
}

const printStyles = `
  @page {
    size: A4;
    margin: 8mm 10mm 10mm;
  }

  html, body {
    margin: 0;
    padding: 0;
    background: #fff;
    -webkit-print-color-adjust: exact;
    print-color-adjust: exact;
    font-family: 'Arial', sans-serif;
  }

  .invoice-print-page {
    width: 190mm;
    margin: 0 auto;
    padding: 20px 10mm 10mm 10mm;
    background: #fff;
    color: #333;
    line-height: 1.6;
  }

  .invoice-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 30px;
    padding-bottom: 20px;
    border-bottom: 3px solid #2c3e50;
  }

  .company-info { flex: 1; }
  .company-logo { display: block; max-width: 180px; height: auto; margin: 0 0 5px 0; }
  .company-subtitle { font-size: 16px; color: #7f8c8d; margin: 0 0 15px 0; }
  .company-details p { margin: 5px 0; font-size: 14px; color: #555; }
  .invoice-title { text-align: right; flex: 1; }
  .invoice-title h2 { font-size: 24px; font-weight: bold; color: #2c3e50; margin: 0 0 15px 0; text-transform: uppercase; }
  .invoice-number p { margin: 5px 0; font-size: 14px; }

  .invoice-section { margin-bottom: 30px; }
  .section-title { font-size: 18px; font-weight: bold; color: #2c3e50; margin: 0 0 15px 0; padding: 10px 0; border-bottom: 2px solid #3498db; }

  .info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 30px; }
  .info-column h4 { font-size: 16px; font-weight: bold; color: #34495e; margin: 0 0 15px 0; padding: 8px 12px; background: #ecf0f1; border-left: 4px solid #3498db; }
  .info-item { display: flex; justify-content: space-between; margin-bottom: 8px; padding: 5px 0; }
  .label { font-weight: 500; color: #555; min-width: 120px; }
  .value { font-weight: 600; color: #2c3e50; }
  .status-paid { color: #27ae60; font-weight: bold; }

  .products-table { width: 100%; }
  .products-table table { width: 100%; border-collapse: collapse; margin-top: 10px; }
  .products-table th, .products-table td { border: 1px solid #bdc3c7; padding: 10px 6px; text-align: left; }
  .products-table th { background: #34495e; color: #fff; font-weight: bold; text-align: center; }
  .products-table tr:nth-child(even) { background: #f8f9fa; }
  .center { text-align: center; }
  .right { text-align: right; }
  .product-name { font-weight: 600; color: #2c3e50; }
  .product-attributes { font-size: 12px; }
  .attribute { margin: 2px 0; color: #555; }
  .total { font-weight: bold; color: #e74c3c; }

  .summary-table { max-width: 400px; margin-left: auto; }
  .summary-table table { width: 100%; border-collapse: collapse; }
  .summary-table td { padding: 10px 15px; border-bottom: 1px solid #bdc3c7; }
  .summary-table .label { font-weight: 500; color: #555; }
  .summary-table .value { font-weight: 600; color: #2c3e50; text-align: right; }
  .summary-table .discount { color: #e74c3c; }
  .summary-table .total-row { background: #ecf0f1; border-top: 2px solid #2c3e50; border-bottom: 2px solid #2c3e50; }
  .summary-table .total-row .label, .summary-table .total-row .value { font-size: 16px; color: #2c3e50; }

  .invoice-footer { display: flex; justify-content: space-between; align-items: flex-end; margin-top: 40px; padding-top: 20px; border-top: 2px solid #bdc3c7; }
  .thank-you { flex: 1; }
  .thank-you p { margin: 5px 0; color: #555; }
  .signature { text-align: center; min-width: 200px; }
  .signature-line { width: 150px; height: 1px; background: #333; margin: 20px auto 10px; }
  .signature-name { font-weight: bold; color: #2c3e50; }

  @media print {
    .invoice-header,
    .invoice-section,
    .products-table,
    .summary-table,
    .invoice-footer {
      page-break-inside: avoid;
    }

    .products-table table,
    .products-table tr,
    .products-table td,
    .summary-table table,
    .summary-table tr,
    .summary-table td {
      page-break-inside: avoid;
      page-break-after: auto;
    }
  }
`

const openPrintWindow = () => {
  const invoiceEl = document.querySelector('.invoice-print-page') as HTMLElement | null

  if (!invoiceEl) {
    return
  }

  const html = `<!doctype html>
  <html>
    <head>
      <meta charset="utf-8" />
      <title>${invoice.value?.maHoaDon || 'Invoice'}</title>
      <style>${printStyles}</style>
    </head>
    <body>${invoiceEl.outerHTML}</body>
  </html>`

  const printWindow = window.open('', '_blank', 'noopener,noreferrer,width=900,height=1200')

  if (!printWindow) {
    return
  }

  printWindow.document.open()
  printWindow.document.write(html)
  printWindow.document.close()

  printWindow.onload = () => {
    printWindow.focus()
    printWindow.print()
    setTimeout(() => {
      printWindow.close()
    }, 300)
  }
}

const loadInvoiceData = async () => {
  try {
    const invoiceId = route.params.id as string

    if (!invoiceId) {
      return
    }

    // Load invoice details
    const invoiceResponse = await axios.get(`/api/invoice-management/${invoiceId}`)

    if (invoiceResponse && invoiceResponse.data) {
      invoice.value = invoiceResponse.data
    }

    // Load products
    const productsResponse = await axios.get(`/api/thong-tin-hoa-don-management/san-pham-da-ban/${invoiceId}`)

    if (productsResponse && productsResponse.data) {
      products.value = productsResponse.data
    }
  } catch (error) {
    console.error('Error loading invoice data:', error)
    // Fallback data
    invoice.value = {
      id: route.params.id,
      maHoaDon: 'HD00001',
      ngayTao: new Date().toISOString(),
      tenKhachHang: 'Nguyễn Văn A',
      soDienThoaiKhachHang: '0123 456 789',
      emailKhachHang: 'nguyenvana@email.com',
      diaChiKhachHang: '123 Đường ABC, Quận XYZ, TP.HCM',
      tenNhanVien: 'Trần Thị B',
      maNhanVien: 'NV001',
      ngayThanhToan: new Date().toISOString(),
      tongTienHang: 2500000,
      tongTienSauGiam: 2500000,
    }
    products.value = [
      {
        id: 1,
        tenSanPhamChiTiet: 'Giày Nike Air Max 270 - Đen - Size 39',
        tenMauSac: 'Đen',
        tenKichThuoc: '39',
        tenChatLieu: 'Da tổng hợp',
        tenDeGiay: 'Đế cao su',
        soLuong: 1,
        giaBan: 2500000,
        thanhTien: 2500000,
      },
    ]
  } finally {
    loading.value = false
    await nextTick()
    openPrintWindow()
  }
}

onMounted(() => {
  loadInvoiceData()
})
</script>

<style scoped>
/* Loading Styles */
.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: #f5f5f5;
}

.loading-spinner {
  text-align: center;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.loading-spinner p {
  color: #666;
  font-size: 16px;
}

.invoice-print-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  background: white;
  font-family: 'Arial', sans-serif;
  line-height: 1.6;
  color: #333;
}

/* Back Button */
.back-button-container {
  position: fixed;
  top: 20px;
  left: 20px;
  z-index: 10000;
}

.back-button {
  background: #3498db;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 5px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
}

.back-button:hover {
  background: #2980b9;
}

/* Header */
.invoice-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 3px solid #2c3e50;
}

.company-info {
  flex: 1;
}

.company-logo {
  display: block;
  max-width: 180px;
  height: auto;
  margin: 0 0 5px 0;
}

.company-subtitle {
  font-size: 16px;
  color: #7f8c8d;
  margin: 0 0 15px 0;
}

.company-details p {
  margin: 5px 0;
  font-size: 14px;
  color: #555;
}

.invoice-title {
  text-align: right;
  flex: 1;
}

.invoice-title h2 {
  font-size: 24px;
  font-weight: bold;
  color: #2c3e50;
  margin: 0 0 15px 0;
  text-transform: uppercase;
}

.invoice-number p {
  margin: 5px 0;
  font-size: 14px;
}

/* Sections */
.invoice-section {
  margin-bottom: 30px;
}

.section-title {
  font-size: 18px;
  font-weight: bold;
  color: #2c3e50;
  margin: 0 0 15px 0;
  padding: 10px 0;
  border-bottom: 2px solid #3498db;
}

/* Info Grid */
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
}

.info-column h4 {
  font-size: 16px;
  font-weight: bold;
  color: #34495e;
  margin: 0 0 15px 0;
  padding: 8px 12px;
  background: #ecf0f1;
  border-left: 4px solid #3498db;
}

.info-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  padding: 5px 0;
}

.label {
  font-weight: 500;
  color: #555;
  min-width: 120px;
}

.value {
  font-weight: 600;
  color: #2c3e50;
}

.status-paid {
  color: #27ae60;
  font-weight: bold;
}

/* Products Table */
.products-table {
  overflow-x: auto;
}

.products-table table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 10px;
}

.products-table th,
.products-table td {
  border: 1px solid #bdc3c7;
  padding: 10px 6px;
  text-align: left;
}

.products-table th {
  background: #34495e;
  color: white;
  font-weight: bold;
  text-align: center;
}

.products-table tr:nth-child(even) {
  background: #f8f9fa;
}

.center {
  text-align: center;
}

.right {
  text-align: right;
}

.product-name {
  font-weight: 600;
  color: #2c3e50;
}

.product-attributes {
  font-size: 12px;
}

.attribute {
  margin: 2px 0;
  color: #555;
}

.total {
  font-weight: bold;
  color: #e74c3c;
}

/* Summary Table */
.summary-table {
  max-width: 400px;
  margin-left: auto;
}

.summary-table table {
  width: 100%;
  border-collapse: collapse;
}

.summary-table td {
  padding: 10px 15px;
  border-bottom: 1px solid #bdc3c7;
}

.summary-table .label {
  font-weight: 500;
  color: #555;
}

.summary-table .value {
  font-weight: 600;
  color: #2c3e50;
  text-align: right;
}

.summary-table .discount {
  color: #e74c3c;
}

.summary-table .total-row {
  background: #ecf0f1;
  border-top: 2px solid #2c3e50;
  border-bottom: 2px solid #2c3e50;
}

.summary-table .total-row .label,
.summary-table .total-row .value {
  font-size: 16px;
  color: #2c3e50;
}

/* Footer */
.invoice-footer {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-top: 40px;
  padding-top: 20px;
  border-top: 2px solid #bdc3c7;
}

.thank-you {
  flex: 1;
}

.thank-you p {
  margin: 5px 0;
  color: #555;
}

.signature {
  text-align: center;
  min-width: 200px;
}

.signature-line {
  width: 150px;
  height: 1px;
  background: #333;
  margin: 20px auto 10px;
}

.signature-name {
  font-weight: bold;
  color: #2c3e50;
}

/* Print Styles */
@media print {
  @page {
    size: A4;
    margin: 8mm 10mm 10mm;
  }

  .invoice-print-page {
    width: 190mm;
    margin: 0 auto;
    padding: 0 10mm 10mm 10mm;
    box-shadow: none;
    font-size: 11px;
    line-height: 1.3;
    background: #fff;
    page-break-after: avoid;
    page-break-before: avoid;
  }

  .invoice-header,
  .invoice-section,
  .products-table,
  .summary-table,
  .invoice-footer,
 :global(.back-button-container),
 .back-button-container {
    page-break-inside: avoid;
  }

  .products-table table,
  .products-table tr,
  .products-table td,
  .summary-table table,
  .summary-table tr,
  .summary-table td {
    page-break-inside: avoid;
    page-break-after: auto;
  }
}

/* Responsive */
@media (max-width: 768px) {
  .invoice-header {
    flex-direction: column;
    gap: 20px;
  }

  .info-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .invoice-footer {
    flex-direction: column;
    gap: 20px;
    align-items: center;
  }
}
</style>
