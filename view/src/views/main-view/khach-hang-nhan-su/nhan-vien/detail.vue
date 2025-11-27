<template>
  <a-card title="Chi Tiết Nhân Viên" style="padding: 16px 20px">
    <a-descriptions bordered column="2">
      <a-descriptions-item label="Tên nhân viên">{{ formData.tenNhanVien }}</a-descriptions-item>
      <a-descriptions-item label="Tên tài khoản">{{ formData.tenTaiKhoan }}</a-descriptions-item>
      <a-descriptions-item label="Ngày sinh">{{ formData.ngaySinh }}</a-descriptions-item>
      <a-descriptions-item label="Email">{{ formData.email }}</a-descriptions-item>
      <a-descriptions-item label="CCCD">{{ formData.cccd }}</a-descriptions-item>
      <a-descriptions-item label="Số điện thoại">{{ formData.soDienThoai }}</a-descriptions-item>
      <a-descriptions-item label="Thành phố">{{ formData.thanhPho }}</a-descriptions-item>
      <a-descriptions-item label="Phường">{{ formData.phuong }}</a-descriptions-item>
      <a-descriptions-item label="Quận">{{ formData.quan }}</a-descriptions-item>
      <a-descriptions-item label="Quyền hạn">
        {{ formData.idQuyenHan === 1 ? 'Admin' : formData.idQuyenHan === 2 ? 'Nhân viên' : 'Không xác định' }}
      </a-descriptions-item>
      <a-descriptions-item label="Giới tính">
        {{ formData.gioiTinh ? 'Nam' : 'Nữ' }}
      </a-descriptions-item>
      <a-descriptions-item label="Trạng thái">
        {{ formData.trangThai ? 'Đang làm việc' : 'Nghỉ việc' }}
      </a-descriptions-item>
    </a-descriptions>

    <a-button style="margin-top: 16px" @click="router.back()">Quay lại</a-button>
  </a-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { layChiTietNhanVien, type NhanVienResponse } from '@/api/nhan-vien'

const route = useRoute()
const router = useRouter()

const formData = ref({
  tenNhanVien: '',
  ngaySinh: '',
  cccd: '',
  soDienThoai: '',
  quan: '',
  gioiTinh: true,
  tenTaiKhoan: '',
  email: '',
  thanhPho: '',
  phuong: '',
  trangThai: true,
  idQuyenHan: null,
})

onMounted(async () => {
  try {
    const { id } = route.params
    if (!id) {
      return
    }

    const res = await layChiTietNhanVien(id)

    const { data } = res
    formData.value = {
      tenNhanVien: data.tenNhanVien || '',
      ngaySinh: data.ngaySinh || '',
      cccd: data.cccd || '',
      soDienThoai: data.soDienThoai || '',
      quan: data.quan || '',
      gioiTinh: Boolean(data.gioiTinh),
      tenTaiKhoan: data.tenTaiKhoan || '',
      email: data.email || '',
      thanhPho: data.thanhPho || '',
      phuong: data.phuong || '',
      trangThai: Boolean(data.trangThai),
      idQuyenHan: data.idQuyenHan ?? null,
    }
  } catch (err) {}
})
</script>
