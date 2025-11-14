<template>
  <a-modal
    :visible="visible"
    title="Thêm Khách Hàng Mới"
    width="500px"
    ok-text="Thêm"
    cancel-text="Hủy"
    @ok="$emit('ok')"
    @update:visible="$emit('update:visible', $event)"
  >
    <a-form :model="{}" layout="vertical">
      <a-form-item label="Tên Khách Hàng" required>
        <a-input :model-value="name" placeholder="Nhập tên khách hàng" @update:model-value="$emit('update:name', $event)" />
      </a-form-item>
      <a-form-item label="Số Điện Thoại" required>
        <a-input :model-value="phone" placeholder="Nhập số điện thoại" @update:model-value="$emit('update:phone', $event)" />
      </a-form-item>
      <a-form-item label="Email" required>
        <a-input :model-value="email" placeholder="Nhập email" type="email" @update:model-value="$emit('update:email', $event)" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
defineProps<{ visible: boolean; name: string; phone: string; email: string }>()

defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'update:name', value: string): void
  (e: 'update:phone', value: string): void
  (e: 'update:email', value: string): void
  (e: 'ok'): void
}>()
</script>
