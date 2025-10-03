<template>
  <div class="payment-result-page">
    <div class="result-container">
      <a-card :bordered="false" class="result-card">
        <a-result :status="resultStatus" :title="resultTitle" :subtitle="resultSubtitle">
          <template #icon>
            <div class="status-icon" :class="resultStatus">
              <icon-check-circle v-if="isSuccess" />
              <icon-close-circle v-else />
            </div>
          </template>

          <template #extra>
            <a-space wrap>
              <router-link to="/ban-hang-tai-quay/index">
                <a-button type="primary">Quay lại POS</a-button>
              </router-link>
              <router-link to="/quan-ly-hoa-don/danh-sach">
                <a-button>Danh sách hóa đơn</a-button>
              </router-link>
            </a-space>
          </template>
        </a-result>

        <a-divider class="divider" />

        <a-descriptions class="result-descriptions" :column="descriptionsColumn" layout="horizontal" size="large" bordered>
          <a-descriptions-item label="Mã giao dịch">{{ transactionNo || '-' }}</a-descriptions-item>
          <a-descriptions-item label="Mã đơn hàng">{{ txnRef || '-' }}</a-descriptions-item>
          <a-descriptions-item label="Số tiền">{{ formattedAmount }}</a-descriptions-item>
          <a-descriptions-item label="Phương thức">{{ bankLabel }}</a-descriptions-item>
          <a-descriptions-item label="Trạng thái">
            <a-tag :color="isSuccess ? 'green' : 'red'">{{ statusLabel }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="Thời gian">{{ formattedDate }}</a-descriptions-item>
          <a-descriptions-item label="Nội dung" :span="descriptionsColumn">
            <span class="order-info">{{ orderInfo || '-' }}</span>
          </a-descriptions-item>
        </a-descriptions>
      </a-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { IconCheckCircle, IconCloseCircle } from '@arco-design/web-vue/es/icon'

const route = useRoute()

const code = computed(() => String(route.query.code ?? ''))
const txnRef = computed(() => route.query.txnRef)
const amount = computed(() => Number(route.query.amount || 0) / 100)
const bankCode = computed(() => route.query.bankCode)
const orderInfo = computed(() => route.query.orderInfo)
const transactionNo = computed(() => route.query.transactionNo)
const payDate = computed(() => route.query.payDate)

const isSuccess = computed(() => code.value === '00')

const resultStatus = computed(() => (isSuccess.value ? 'success' : 'error'))
const resultTitle = computed(() => (isSuccess.value ? 'Thanh toán thành công' : 'Thanh toán thất bại'))
const resultSubtitle = computed(() =>
  isSuccess.value
    ? 'Đơn hàng đã được thanh toán qua VNPAY (sandbox). Vui lòng kiểm tra lại tình trạng hóa đơn trong hệ thống.'
    : 'Hệ thống chưa ghi nhận thanh toán hợp lệ. Kiểm tra lại mã giao dịch hoặc thử thanh toán lại.'
)

const formattedAmount = computed(() => {
  const value = Number.isFinite(amount.value) ? amount.value : 0
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', minimumFractionDigits: 0 }).format(value).replace('₫', ' VND')
})

const formattedDate = computed(() => {
  const raw = String(payDate.value || '')
  if (raw.length === 14) {
    const y = raw.slice(0, 4)
    const M = raw.slice(4, 6)
    const d = raw.slice(6, 8)
    const h = raw.slice(8, 10)
    const m = raw.slice(10, 12)
    const s = raw.slice(12, 14)
    return `${d}/${M}/${y} ${h}:${m}:${s}`
  }
  return raw || '-'
})

const bankLabel = computed(() => {
  const value = String(bankCode.value || '').toUpperCase()
  if (!value) return '-'
  const map: Record<string, string> = {
    VNPAYQR: 'VNPAY QR',
    VNBANK: 'Ngân hàng nội địa',
    INTCARD: 'Thẻ quốc tế (Visa/Master/JCB)',
  }
  return map[value] || value
})

const columns = ref(3)

const handleResize = () => {
  if (typeof window === 'undefined') {
    columns.value = 3
    return
  }
  columns.value = window.innerWidth <= 640 ? 1 : 3
}

onMounted(() => {
  handleResize()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
})

const descriptionsColumn = computed(() => columns.value)

const statusLabel = computed(() => {
  if (!code.value) return 'Không xác định'
  if (code.value === '00') return 'Thành công'
  return `Mã lỗi: ${code.value}`
})
</script>

<style scoped>
.payment-result-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
  background: radial-gradient(circle at top, #eff6ff 0%, #f8fafc 35%, #f1f5f9 100%);
}

.result-container {
  width: 100%;
  max-width: 960px;
}

.result-card {
  box-shadow: 0 20px 65px rgba(15, 23, 42, 0.12);
  border-radius: 18px;
  overflow: hidden;
}

.status-icon {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 42px;
}

.status-icon.success {
  background: transparent;
  color: #16a34a;
}

.status-icon.error {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.divider {
  margin: 0;
}

.result-descriptions {
  margin-top: 16px;
}

.order-info {
  display: inline-block;
  max-width: 100%;
  word-break: break-word;
  color: #0f172a;
}

@media (max-width: 768px) {
  .payment-result-page {
    padding: 24px 16px;
  }

  .status-icon {
    width: 60px;
    height: 60px;
    font-size: 32px;
  }
}
</style>
