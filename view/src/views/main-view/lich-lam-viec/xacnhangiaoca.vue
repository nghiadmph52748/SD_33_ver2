<template>
  <a-modal
    v-model:open="visible"
    title="Xác nhận giao ca"
    @ok="handleSubmit"
    :okText="'Xác nhận'"
    :cancelText="'Hủy'"
    :confirmLoading="loading"
  >
    <a-form layout="vertical">
      <!-- Thông tin ca (read-only) -->
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="Người giao">
            <a-input :value="giaoCa?.nguoiGiao?.tenNhanVien || ''" disabled />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="Người nhận">
            <a-input :value="giaoCa?.nguoiNhan?.tenNhanVien || ''" disabled />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="Ca làm việc">
            <a-input :value="giaoCa?.caLamViec?.tenCa || ''" disabled />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="Tổng tiền cuối ca">
            <a-input :value="giaoCa?.tongTienCuoiCa || 0" disabled />
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item label="Ghi chú bàn giao">
            <a-input :value="giaoCa?.ghiChu || ''" disabled />
          </a-form-item>
        </a-col>
      </a-row>

      <!-- Thông tin xác nhận -->
      <a-row :gutter="16" style="margin-top: 16px;">
        <a-col :span="12">
          <a-form-item label="Số tiền thực tế nhận" required>
            <a-input-number
              v-model:value="form.soTienNhanThucTe"
              :min="0"
              style="width: 100%"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="Trạng thái xác nhận" required>
            <a-select v-model:value="form.trangThaiXacNhan" placeholder="Chọn trạng thái">
              <a-select-option value="Đã xác nhận">Đã xác nhận</a-select-option>
              <a-select-option value="Chênh lệch">Chênh lệch</a-select-option>
              <a-select-option value="Từ chối">Từ chối</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item label="Ghi chú xác nhận">
            <a-textarea
              v-model:value="form.ghiChuXacNhan"
              placeholder="Nhập lý do nếu có..."
              rows="3"
            />
          </a-form-item>
        </a-col>
      </a-row>
    </a-form>
  </a-modal>
</template>

<script setup>
import { reactive, ref, watch } from 'vue';
import { message } from 'ant-design-vue';
import { xacNhanGiaoCa } from '@/api/giao-ca';

const props = defineProps({
  giaoCa: {
    type: Object,
    default: null,
  },
});

const visible = ref(false);
const loading = ref(false);

const form = reactive({
  soTienNhanThucTe: null,
  trangThaiXacNhan: 'Đã xác nhận',
  ghiChuXacNhan: '',
});

// Watch props.giaoCa để show modal khi có dữ liệu
watch(
  () => props.giaoCa,
  (newVal) => {
    if (newVal && newVal.id) {
      visible.value = true;
      form.soTienNhanThucTe = null;
      form.trangThaiXacNhan = 'Đã xác nhận';
      form.ghiChuXacNhan = '';
    } else {
      visible.value = false;
    }
  },
  { immediate: true }
);

const handleSubmit = async () => {
  if (!form.soTienNhanThucTe) {
    message.warning('Vui lòng nhập số tiền thực tế nhận!');
    return;
  }
  if (!props.giaoCa || !props.giaoCa.id) {
    message.error('Dữ liệu giao ca không hợp lệ!');
    return;
  }

  loading.value = true;
  try {
    await xacNhanGiaoCa(props.giaoCa.id, {
      soTienNhanThucTe: form.soTienNhanThucTe,
      trangThaiXacNhan: form.trangThaiXacNhan,
      thoiGianXacNhan: new Date().toISOString(),
      ghiChuXacNhan: form.ghiChuXacNhan,
      trangThaiCa: 'Hoàn tất',
    });
    message.success('Xác nhận giao ca thành công!');
    visible.value = false;
  } catch (err) {
    console.error(err);
    message.error('Có lỗi khi xác nhận giao ca');
  } finally {
    loading.value = false;
  }
};
</script>
