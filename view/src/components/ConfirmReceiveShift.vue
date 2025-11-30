<template>
  <a-modal v-model:visible="visible" title="Xác nhận nhận ca" :width="820" :closable="!loading" @cancel="onCancel">
    <div class="confirm-receive">
      <div class="summary">
        <div class="row"><strong>Người giao ca:</strong> {{ shift?.nguoiGiao?.tenNhanVien || shift?.nguoiGiao?.name || '-' }}</div>
        <div class="row"><strong>Kết thúc lúc:</strong> {{ formatDate(shift?.thoiGianKetThuc || endTime) }}</div>
        <div class="row"><strong>Tiền mặt dự kiến bàn giao:</strong> {{ formatCurrency(expectedCash) }}</div>
        <div class="row">
          <strong>Tiền mặt thực tế:</strong>
          <a-input-number v-model="actualCash" :min="0" :precision="0" :max="100000000000" />
        </div>
        <div class="row"><strong>Tiền lệch ca trước:</strong> {{ formatCurrency(shift?.chenhLech || 0) }}</div>
        <div class="row"><strong>Ghi chú kết ca:</strong> {{ shift?.ghiChu || '-' }}</div>
      </div>

      <div class="invoices">
        <h4>Danh sách hóa đơn trong ca ({{ invoiceList?.length || 0 }})</h4>
        <a-table :data="invoiceList || []" :pagination="false" size="small">
          <a-table-column title="ID" data-index="id" :width="60" />
          <a-table-column title="Số tiền" :width="140">
            <template #cell="{ record }">{{ formatCurrency(record.tongTienSauGiam ?? record.tongTien ?? 0) }}</template>
          </a-table-column>
          <a-table-column title="Thời gian" :width="180">
            <template #cell="{ record }">{{ formatDate(record.ngayTao || record.createAt) }}</template>
          </a-table-column>
          <a-table-column title="Mô tả" data-index="mota" />
        </a-table>
      </div>

      <div class="notes">
        <a-form-item label="Ghi chú nhận ca">
          <a-textarea v-model="receiveNote" rows="3" placeholder="Ghi chú nhận ca" />
        </a-form-item>
      </div>
    </div>

    <template #footer>
      <a-button @click="onCancel" :disabled="loading">Hủy</a-button>
      <a-button type="primary" :loading="loading" @click="onConfirm">XÁC NHẬN NHẬN CA</a-button>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { xacNhanGiaoCa } from '@/api/giao-ca'
import { Message } from '@arco-design/web-vue'

const visible = ref(false)
const loading = ref(false)
const shift = ref(null as any)
const invoiceList = ref([] as any[])
const endTime = ref(new Date().toISOString())
const actualCash = ref(0)
const receiveNote = ref('')

let resolver: ((v: any) => void) | null = null

function formatDate(d: any) {
  if (!d) return '-'
  try {
    let s = d
    if (typeof s === 'string' && s.length >= 19 && s[10] === ' ') s = s.replace(' ', 'T')
    return new Date(s).toLocaleString()
  } catch {
    return d
  }
}

function formatCurrency(n: any) {
  if (n == null) return '-'
  return new Intl.NumberFormat('vi-VN').format(n) + ' ₫'
}

const expectedCash = computed(() => {
  const base = (shift.value?.tongTienBanDau || 0)
  const invoicesTotal = (invoiceList.value || []).reduce((acc, cur) => acc + Number(cur.tongTienSauGiam ?? cur.tongTien ?? 0), 0)
  return base + invoicesTotal
})

function show(s: any, invoices: any[] = [], opts: { endTime?: string } = {}) {
  shift.value = s
  invoiceList.value = invoices
  endTime.value = opts.endTime || new Date().toISOString()
  actualCash.value = Number(s?.tongTienCuoiCa ?? expectedCash.value) || 0
  receiveNote.value = ''
  visible.value = true
  return new Promise((resolve) => {
    resolver = resolve
  })
}

async function onConfirm() {
  if (!shift.value || !shift.value.id) return
  loading.value = true
  try {
    const payload = {
      soTienNhanThucTe: Number(actualCash.value),
      trangThaiXacNhan: 'Đã xác nhận',
      ghiChuXacNhan: receiveNote.value || undefined,
    }
    await xacNhanGiaoCa(shift.value.id, payload)
    Message.success('Xác nhận nhận ca thành công')
    visible.value = false
    if (resolver) resolver(true)
  } catch (e) {
    console.error('Lỗi xác nhận nhận ca', e)
    Message.error('Xác nhận thất bại')
    if (resolver) resolver(false)
  } finally {
    loading.value = false
    resolver = null
  }
}

function onCancel() {
  visible.value = false
  if (resolver) resolver(false)
  resolver = null
}

// Export hàm show để sử dụng từ bên ngoài
defineExpose({
  show
})
</script>

<style scoped>
.confirm-receive .summary { display:flex; flex-direction:column; gap:8px; margin-bottom:16px }
.confirm-receive .row { font-size:14px }
.confirm-receive .invoices { margin-top:12px }
</style>
