<template>
  <a-card title="Thêm Nhân Viên Mới">
    <a-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      layout="vertical"
     @finish="handleSubmit"
    >
      <a-row :gutter="24">
        <a-col :span="12">
          <a-form-item label="Tên nhân viên" name="tenNhanVien">
            <a-input v-model="formData.tenNhanVien" placeholder="Nhập tên nhân viên" />
          </a-form-item>

          <a-form-item label="Ngày sinh" name="ngaySinh">
            <a-date-picker
              v-model="formData.ngaySinh"
              format="YYYY-MM-DD"
              placeholder="Chọn ngày sinh"
              style="width: 100%;"
            />
          </a-form-item>

          <a-form-item label="CCCD" name="cccd">
            <a-input v-model="formData.cccd" placeholder="Nhập CCCD" />
          </a-form-item>

          <a-form-item label="Số điện thoại" name="soDienThoai">
            <a-input v-model="formData.soDienThoai" placeholder="Nhập số điện thoại" />
          </a-form-item>

          <a-form-item label="Quận" name="quan">
            <a-input v-model="formData.quan" placeholder="Nhập quận" />
          </a-form-item>

          <a-form-item label="Giới tính" name="gioiTinh">
            <a-switch
              v-model="formData.gioiTinh"
              checkedChildren="Nam"
              unCheckedChildren="Nữ"
            />
          </a-form-item>
        </a-col>

        <a-col :span="12">
          <a-form-item label="Tên tài khoản" name="tenTaiKhoan">
            <a-input v-model="formData.tenTaiKhoan" placeholder="Nhập tên tài khoản" />
          </a-form-item>

          <a-form-item label="Mật khẩu" name="matKhau">
            <a-input-password v-model="formData.matKhau" placeholder="Nhập mật khẩu" />
          </a-form-item>

          <a-form-item label="Email" name="email">
            <a-input v-model="formData.email" placeholder="Nhập email" />
          </a-form-item>

          <a-form-item label="Thành phố" name="thanhPho">
            <a-input v-model="formData.thanhPho" placeholder="Nhập thành phố" />
          </a-form-item>

          <a-form-item label="Phường" name="phuong">
            <a-input v-model="formData.phuong" placeholder="Nhập phường" />
          </a-form-item>
          <a-form-item label="Quyền hạn" name="idQuyenHan">
            <a-select
              v-model="formData.idQuyenHan"
              placeholder="-- Chọn quyền hạn --"
              :options="[
                { value: 1, label: 'Admin' },
                { value: 2, label: 'Nhân viên' }
              ]"
            />
          </a-form-item>
        </a-col>
      </a-row>
      <a-form-item>
        <a-button type="primary" html-type="submit"  @click="handleSubmit">Lưu</a-button>
        <a-button @click="handleCancel">Hủy</a-button>
      </a-form-item>
    </a-form>
  </a-card>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

// Router
const router = useRouter()

// Form data
const formRef = ref()
const listQuyenHan = ref([
  { id: 1, tenQuyenHan: "Admin" },
  { id: 2, tenQuyenHan: "Nhân viên" },
])

const formData = ref({
  tenNhanVien: "",
  cccd: "",
  ngaySinh: "",
  gioiTinh: true,
  email: "",
  soDienThoai: "",
  thanhPho: "",
  quan: "",
  phuong: "",
  diaChiCuThe: "",
  tenQuyenHan: "",
  idQuyenHan: null,
  trangThai: true,
  delete: false,
  tenTaiKhoan: "",
  matKhau: "",
})

// Validation rules
const formRules = {
  tenNhanVien: [{ required: true, message: 'Vui lòng nhập tên nhân viên' }],
  tenTaiKhoan: [{ required: true, message: 'Vui lòng nhập tên tài khoản' }],
  matKhau: [{ required: true, message: 'Vui lòng nhập mật khẩu' }],
  email: [{ required: true, type: 'email', message: 'Email không hợp lệ' }],
  soDienThoai: [{ required: true, message: 'Vui lòng nhập số điện thoại' }],
  idQuyenHan: [{ required: true, message: 'Vui lòng chọn quyền hạn' }]
}

// Handle submit
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    console.log("Dữ liệu gửi API:", formData.value)
    await axios.post('http://localhost:8080/api/nhan-vien-management/add', formData.value)
    router.push('/khach-hang-nhan-su/nhan-vien')
  } catch (error) {
    console.error('Lỗi khi thêm nhân viên:', error)
  }
}


// Handle cancel
const handleCancel = () => {
  router.push('/khach-hang-nhan-su/nhan-vien')
}
</script>
