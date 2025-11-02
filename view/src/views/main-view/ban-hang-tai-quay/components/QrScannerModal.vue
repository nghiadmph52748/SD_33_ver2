<template>
  <a-modal
    :visible="visible"
    title="Quét Mã QR Sản Phẩm"
    width="600px"
    :footer="null"
    @cancel="$emit('update:visible', false)"
    @open="$emit('open')"
  >
    <div style="text-align: center; padding: 20px">
      <div id="qr-reader" style="width: 100%; max-width: 550px; margin: 0 auto"></div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
defineProps<{ visible: boolean }>()

defineEmits<{ (e: 'update:visible', value: boolean): void; (e: 'open'): void }>()
</script>
