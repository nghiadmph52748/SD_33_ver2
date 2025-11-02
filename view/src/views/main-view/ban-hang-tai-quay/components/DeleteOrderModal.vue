<template>
  <a-modal :visible="visible" title="Xoá Đơn Hàng" width="400px" @ok="$emit('ok')" @update:visible="$emit('update:visible', $event)" ok-text="Xoá" cancel-text="Hủy">
    <div style="text-align: center; padding: 0">
      <a-result status="warning" title="Xác Nhận Xoá Đơn Hàng?" style="margin: 0; padding: 0" />
      <p style="margin: 4px 0 0 0; color: #666; font-size: 13px">Bạn có chắc muốn xoá đơn hàng này? Hành động này không thể hoàn tác.</p>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
defineProps<{ visible: boolean }>()

defineEmits<{ (e: 'update:visible', value: boolean): void; (e: 'ok'): void }>()
</script>
