<template>
  <div class="test-page">
    <h1>Test API Hóa Đơn</h1>

    <div class="test-section">
      <h2>Test API Endpoints</h2>

      <div class="test-buttons">
        <a-button @click="testInvoiceList" type="primary">Test Danh sách Hóa đơn</a-button>
        <a-button @click="testInvoiceDetail" type="primary">Test Chi tiết Hóa đơn</a-button>
        <a-button @click="testHealth" type="primary">Test Health Check</a-button>
      </div>

      <div class="test-results">
        <h3>Kết quả:</h3>
        <pre>{{ testResult }}</pre>
      </div>
    </div>

    <div class="invoice-list" v-if="invoices.length > 0">
      <h2>Danh sách Hóa đơn</h2>
      <a-table :columns="columns" :data="invoices" :pagination="false">
        <template #actions="{ record }">
          <a-button @click="viewDetail(record.id)" size="small">Xem chi tiết</a-button>
        </template>
      </a-table>
    </div>

    <div class="invoice-detail" v-if="selectedInvoice">
      <h2>Chi tiết Hóa đơn ID: {{ selectedInvoice.id }}</h2>
      <pre>{{ JSON.stringify(selectedInvoice, null, 2) }}</pre>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import axios from 'axios'

const testResult = ref('')
const invoices = ref<any[]>([])
const selectedInvoice = ref<any>(null)

const columns = [
  { title: 'ID', dataIndex: 'id', width: 80 },
  { title: 'Mã HĐ', dataIndex: 'maHoaDon', width: 120 },
  { title: 'Khách hàng', dataIndex: 'tenKhachHang', width: 150 },
  { title: 'SDT KH', dataIndex: 'soDienThoaiKhachHang', width: 120 },
  { title: 'Loại đơn', dataIndex: 'moTaLoaiDon', width: 150 },
  { title: 'Ngày tạo', dataIndex: 'thoiGianTao', width: 150 },
  { title: 'Tổng tiền', dataIndex: 'tongTienSauGiam', width: 150 },
  { title: 'Trạng thái', dataIndex: 'trangThai', width: 100 },
  { title: 'Actions', slotName: 'actions', width: 120 },
]

const testHealth = async () => {
  try {
    testResult.value = 'Testing health check...'
    const response = await axios.get('/api/public/health')
    testResult.value = `Health Check: ${JSON.stringify(response.data, null, 2)}`
  } catch (error) {
    testResult.value = `Health Check Error: ${error.message}`
  }
}

const testInvoiceList = async () => {
  try {
    testResult.value = 'Testing invoice list...'
    const response = await axios.get('/api/hoa-don-management/playlist')
    testResult.value = `Invoice List Response: ${JSON.stringify(response.data, null, 2)}`

    if (response.data && response.data.data) {
      invoices.value = response.data.data
    }
  } catch (error) {
    testResult.value = `Invoice List Error: ${error.message}\n${error.response?.data || ''}`
  }
}

const testInvoiceDetail = async () => {
  try {
    testResult.value = 'Testing invoice detail...'
    const response = await axios.get('/api/hoa-don-management/1')
    testResult.value = `Invoice Detail Response: ${JSON.stringify(response.data, null, 2)}`

    if (response.data && response.data.data) {
      selectedInvoice.value = response.data.data
    }
  } catch (error) {
    testResult.value = `Invoice Detail Error: ${error.message}\n${error.response?.data || ''}`
  }
}

const viewDetail = async (id: number) => {
  try {
    testResult.value = `Loading invoice detail for ID: ${id}`
    const response = await axios.get(`/api/hoa-don-management/${id}`)
    testResult.value = `Invoice Detail Response: ${JSON.stringify(response.data, null, 2)}`

    if (response.data && response.data.data) {
      selectedInvoice.value = response.data.data
    }
  } catch (error) {
    testResult.value = `Invoice Detail Error: ${error.message}\n${error.response?.data || ''}`
  }
}
</script>

<style scoped>
.test-page {
  padding: 20px;
}

.test-section {
  margin-bottom: 30px;
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 8px;
}

.test-buttons {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.test-results {
  background: #f5f5f5;
  padding: 15px;
  border-radius: 4px;
}

.test-results pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  max-height: 300px;
  overflow-y: auto;
}

.invoice-list,
.invoice-detail {
  margin-top: 30px;
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 8px;
}

.invoice-detail pre {
  background: #f5f5f5;
  padding: 15px;
  border-radius: 4px;
  max-height: 400px;
  overflow-y: auto;
}
</style>
