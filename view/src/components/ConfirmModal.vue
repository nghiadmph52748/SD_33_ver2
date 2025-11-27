<template>
  <a-modal
    v-model:visible="visible"
    :title="title"
    :width="400"
    @ok="handleOk"
    @cancel="handleCancel"
    :ok-text="okText"
    :cancel-text="cancelText"
  >
    <div>{{ message }}</div>
  </a-modal>
</template>

<script setup>
import { ref } from 'vue'

const visible = ref(false)
const title = ref('Xác nhận')
const message = ref('')
const okText = ref('OK')
const cancelText = ref('Hủy')
let resolveCallback = null

function handleOk() {
  visible.value = false
  if (resolveCallback) {
    resolveCallback(true)
  }
}

function handleCancel() {
  visible.value = false
  if (resolveCallback) {
    resolveCallback(false)
  }
}

function showConfirm(titleText, messageText, okButtonText = 'Xác nhận', cancelButtonText = 'Hủy') {
  return new Promise((resolve) => {
    title.value = titleText
    message.value = messageText
    okText.value = okButtonText
    cancelText.value = cancelButtonText
    resolveCallback = resolve
    visible.value = true
  })
}

defineExpose({
  showConfirm,
})
</script>

<style scoped></style>
