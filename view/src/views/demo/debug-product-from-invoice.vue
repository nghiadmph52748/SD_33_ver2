<template>
  <div class="debug-page">
    <h1>Debug: Sản phẩm đã bán từ hóa đơn</h1>

    <div class="debug-section">
      <h2>1. Test API trực tiếp</h2>
      <a-button @click="testAPI" :loading="testing">Test API</a-button>
      <div v-if="apiResult" class="result">
        <h3>API Result:</h3>
        <pre>{{ JSON.stringify(apiResult, null, 2) }}</pre>
      </div>
    </div>

    <div class="debug-section">
      <h2>2. Test Component</h2>
      <a-input v-model="testInvoiceId" placeholder="Nhập Invoice ID (ví dụ: 1)" style="width: 200px; margin-right: 10px" />
      <a-button @click="loadTestData" :loading="loading">Load Data</a-button>

      <div v-if="products.length > 0" class="result">
        <h3>Products loaded:</h3>
        <div v-for="product in products" :key="product.id" class="product-item">
          <strong>{{ product.tenSanPhamChiTiet || product.tenSanPham }}</strong>
          <br />
          <small>Mã: {{ product.maSanPhamChiTiet || product.maSanPham }}</small>
          <br />
          <small>Số lượng: {{ product.soLuong }}</small>
          <br />
          <small>Giá: {{ formatPrice(product.giaBan) }}</small>
        </div>
      </div>

      <div v-else-if="!loading" class="no-data">
        <p>Không có dữ liệu</p>
      </div>
    </div>

    <div class="debug-section">
      <h2>3. Console Logs</h2>
      <p>Mở DevTools (F12) và xem tab Console để thấy logs</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import axios from 'axios'

const testing = ref(false)
const loading = ref(false)
const apiResult = ref<any>(null)
const products = ref<any[]>([])
const testInvoiceId = ref('1')

const testAPI = async () => {
  try {
    testing.value = true
    console.log('Testing API...')

    const response = await axios.get('/api/thong-tin-hoa-don-management/san-pham-da-ban/1')
    console.log('Raw response:', response)

    apiResult.value = response
  } catch (error) {
    console.error('API Error:', error)
    apiResult.value = { error: error.message }
  } finally {
    testing.value = false
  }
}

const loadTestData = async () => {
  if (!testInvoiceId.value) return

  try {
    loading.value = true
    console.log('Loading products for invoice ID:', testInvoiceId.value)

    const response = await axios.get(`/api/thong-tin-hoa-don-management/san-pham-da-ban/${testInvoiceId.value}`)
    console.log('API Response:', response)

    if (response && Array.isArray(response)) {
      products.value = response
      console.log('Products loaded successfully:', products.value)
    } else {
      console.log('Response is not an array:', response)
      products.value = []
    }
  } catch (error) {
    console.error('Error loading products:', error)
    products.value = []
  } finally {
    loading.value = false
  }
}

const formatPrice = (price: number) => {
  if (!price) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(price)
}
</script>

<style scoped>
.debug-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.debug-section {
  margin-bottom: 30px;
  padding: 20px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #f9fafb;
}

.debug-section h2 {
  margin-top: 0;
  color: #1f2937;
}

.result {
  margin-top: 15px;
  padding: 15px;
  background: white;
  border-radius: 4px;
  border: 1px solid #d1d5db;
}

.result pre {
  background: #f3f4f6;
  padding: 10px;
  border-radius: 4px;
  overflow-x: auto;
  font-size: 12px;
}

.product-item {
  margin-bottom: 15px;
  padding: 10px;
  background: white;
  border-radius: 4px;
  border: 1px solid #e5e7eb;
}

.no-data {
  color: #6b7280;
  font-style: italic;
}
</style>
