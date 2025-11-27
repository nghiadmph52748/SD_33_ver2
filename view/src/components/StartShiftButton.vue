<template>
  <a-tooltip content="Bắt đầu ca làm việc">
    <a-button class="nav-btn" type="outline" :shape="'circle'" @click="openStartModal">
      <template #icon>
        <icon-play-circle />
      </template>
    </a-button>
  </a-tooltip>

  <a-modal
    v-model:visible="showModal"
    title="Bắt Đầu Ca Làm Việc"
    width="420"
    :ok-loading="isLoading"
    @before-ok="onModalOk"
    @cancel="closeModal"
  >
    <div style="display: flex; flex-direction: column; gap: 12px">
      <div>Vui lòng nhập số tiền mặt ban đầu để bắt đầu ca làm việc.</div>
      <a-input v-model="startCashInput" style="width: 100%" placeholder="Số tiền mặt (VND)" type="number" />
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useUserStore } from '@/store'
import { themGiaoCa } from '@/api/giao-ca'
import { Message } from '@arco-design/web-vue'
import { useRouter } from 'vue-router'

const emit = defineEmits<{
  (e: 'shift-started'): void
}>()

const router = useRouter()
const userStore = useUserStore()
const showModal = ref(false)
const startCash = ref<number | null>(null)
const startCashInput = ref<string>('')
const isLoading = ref(false)

function openStartModal() {
  startCashInput.value = ''
  startCash.value = null
  showModal.value = true
}

function closeModal() {
  showModal.value = false
  startCashInput.value = ''
  startCash.value = null
}

function onModalOk() {
  return confirmStart()
}

async function confirmStart() {
  console.log('startCashInput.value:', startCashInput.value, 'type:', typeof startCashInput.value)

  if (!startCashInput.value || startCashInput.value.trim() === '') {
    Message.error('Vui lòng nhập số tiền mặt')
    return false
  }

  const cash = Number(startCashInput.value)
  if (isNaN(cash) || cash < 0) {
    Message.error('Số tiền phải là số dương')
    return false
  }

  isLoading.value = true
  try {
    const payload = {
      nguoiGiaoId: userStore.id,
      nguoiNhanId: userStore.id,
      nguoiNhan: {
        id: userStore.id,
        tenNhanVien: userStore.name || 'Nhân viên',
      },
      caLamViecId: 1, // mặc định
      thoiGianGiaoCa: new Date().toISOString().replace('T', ' ').substring(0, 19),
      tongTienBanDau: cash,
      trangThai: 'Đang hoạt động',
    }
    await themGiaoCa(payload as any)
    Message.success('Bắt đầu ca thành công')
    closeModal()
    // Navigate to giao-ca page to show the newly started shift
    await router.push('/lich-lam-viec/giao-ca')
    emit('shift-started')
    return true
  } catch (err) {
    console.error('Lỗi khi bắt đầu ca', err)
    Message.error('Lỗi khi bắt đầu ca')
    return false
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.nav-btn {
  color: var(--color-text-2);
  border-color: transparent;
  background-color: transparent;
  border-radius: 6px;

  &:hover {
    color: rgb(var(--primary-6));
    background-color: rgb(var(--primary-1));
  }
}
</style>
