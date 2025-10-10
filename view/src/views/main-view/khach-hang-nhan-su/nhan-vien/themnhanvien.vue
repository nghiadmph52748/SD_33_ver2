<template>
  <a-card title="Thêm Nhân Viên Mới">
    <a-form ref="formRef" :model="formData" :rules="formRules" layout="vertical" @finish="handleSubmit">
      <a-row :gutter="24">
        <a-col :span="12">
          <a-form-item label="Tên nhân viên" name="tenNhanVien">
            <a-input v-model="formData.tenNhanVien" placeholder="Nhập tên nhân viên" />
          </a-form-item>

          <a-form-item label="Ngày sinh" name="ngaySinh">
            <a-date-picker v-model="formData.ngaySinh" format="YYYY-MM-DD" placeholder="Chọn ngày sinh" style="width: 100%" />
          </a-form-item>

          <a-form-item label="CCCD" name="cccd">
            <a-input v-model="formData.cccd" placeholder="Nhập CCCD" />
          </a-form-item>

          <a-form-item label="Số điện thoại" name="soDienThoai">
            <a-input v-model="formData.soDienThoai" placeholder="Nhập số điện thoại" />
          </a-form-item>
          <a-form-item label="Quyền hạn" name="idQuyenHan">
            <a-select
              v-model="formData.idQuyenHan"
              placeholder="-- Chọn quyền hạn --"
              :options="[
                { value: 1, label: 'Admin' },
                { value: 2, label: 'Nhân viên' },
              ]"
            />
          </a-form-item>
          <a-form-item label="Ảnh nhân viên" name="anhNhanVien">
            <a-upload name="file" :action="uploadUrl" :show-upload-list="false" :before-upload="beforeUpload" @change="handleUploadChange">
              <a-button icon="upload">Chọn ảnh</a-button>
            </a-upload>
            <div v-if="formData.anhNhanVien">
              <img :src="formData.anhNhanVien" alt="Ảnh nhân viên" style="max-width: 100px; margin-top: 10px" />
            </div>
          </a-form-item>
        </a-col>

        <a-col :span="12">
          <a-form-item label="Email" name="email">
            <a-input v-model="formData.email" placeholder="Nhập email" />
          </a-form-item>

          <a-form-item label="Thành phố" name="thanhPho">
            <a-select
              v-model="formData.thanhPho"
              placeholder="-- Chọn tỉnh/thành phố --"
              :options="provinces"
              @change="onProvinceChange"
              option-label-prop="name"
            />
          </a-form-item>

          <a-form-item label="Quận" name="quan">
            <a-select
              v-model="formData.quan"
              placeholder="-- Chọn quận/huyện --"
              :options="districts"
              @change="onDistrictChange"
              option-label-prop="name"
            />
          </a-form-item>

          <a-form-item label="Phường" name="phuong">
            <a-select v-model="formData.phuong" placeholder="-- Chọn phường/xã --" :options="wards" option-label-prop="name" />
          </a-form-item>

          <a-form-item label="Địa chỉ cụ thể" name="diaChiCuThe">
            <a-input v-model="formData.diaChiCuThe" placeholder="Nhập địa chỉ cụ thể (số nhà, đường...)" />
          </a-form-item>
          <a-form-item label="Giới tính" name="gioiTinh">
            <a-switch v-model="formData.gioiTinh" checkedChildren="Nam" unCheckedChildren="Nữ" />
          </a-form-item>
        </a-col>
      </a-row>
      <a-form-item>
        <a-button type="primary" @click="showConfirm">Lưu</a-button>
        <a-button @click="handleCancel">Hủy</a-button>
      </a-form-item>
    </a-form>
  </a-card>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { Modal, Message } from '@arco-design/web-vue'

// Router
const router = useRouter()
const provinces = ref<{ value: string; label: string; code: number }[]>([])
const districts = ref<{ value: string; label: string; code: number }[]>([])
const wards = ref<{ value: string; label: string }[]>([])

const uploadUrl = 'http://localhost:8080/api/file/upload'

// Form data
const formRef = ref()

const formData = ref({
  tenNhanVien: '',
  cccd: '',
  ngaySinh: '',
  gioiTinh: true,
  email: '',
  soDienThoai: '',
  thanhPho: '',
  quan: '',
  phuong: '',
  diaChiCuThe: '',
  tenQuyenHan: '',
  idQuyenHan: null,
  trangThai: true,
  delete: false,
  tenTaiKhoan: '',
  matKhau: '',
  anhNhanVien: null,
})

// Validation rules
const formRules = {
  tenNhanVien: [{ required: true, message: 'Vui lòng nhập tên nhân viên' }],
  ngaySinh: [{ required: true, message: 'Vui lòng chọn ngày sinh' }],
  cccd: [
    { required: true, message: 'Vui lòng nhập CCCD' },
    { pattern: /^\d{9,12}$/, message: 'CCCD phải là 9-12 chữ số' },
  ],
  soDienThoai: [
    { required: true, message: 'Vui lòng nhập số điện thoại' },
    { pattern: /^(0|\+84)[0-9]{9}$/, message: 'Số điện thoại không hợp lệ' },
  ],
  email: [
    { required: true, message: 'Vui lòng nhập email' },
    { type: 'email', message: 'Email không hợp lệ' },
  ],
  idQuyenHan: [{ required: true, message: 'Vui lòng chọn quyền hạn' }],
  thanhPho: [{ required: true, message: 'Vui lòng chọn tỉnh/thành phố' }],
  quan: [{ required: true, message: 'Vui lòng chọn quận/huyện' }],
  phuong: [{ required: true, message: 'Vui lòng chọn phường/xã' }],
  diaChiCuThe: [{ required: true, message: 'Vui lòng nhập địa chỉ cụ thể' }],
  anhNhanVien: [{ required: true, message: 'Vui lòng chọn ảnh nhân viên' }],
}

const loadProvinces = async () => {
  const res = await fetch('https://provinces.open-api.vn/api/p/')
  const data = await res.json()
  provinces.value = data.map((p: any) => ({
    value: p.name,
    label: p.name,
    code: p.code,
  }))
}
loadProvinces()

const onProvinceChange = async (value: string) => {
  districts.value = []
  wards.value = []
  formData.value.quan = ''
  formData.value.phuong = ''

  const province = provinces.value.find((p) => p.value === value)
  if (province) {
    const res = await fetch(`https://provinces.open-api.vn/api/p/${province.code}?depth=2`)
    const data = await res.json()
    districts.value = data.districts.map((d: any) => ({
      value: d.name,
      label: d.name,
      code: d.code,
    }))
  }
}

const onDistrictChange = async (value: string) => {
  wards.value = []
  formData.value.phuong = ''

  const district = districts.value.find((d) => d.value === value)
  if (district) {
    const res = await fetch(`https://provinces.open-api.vn/api/d/${district.code}?depth=2`)
    const data = await res.json()
    wards.value = data.wards.map((w: any) => ({
      value: w.name,
      label: w.name,
    }))
  }
}
// Handle submit
const handleSubmit = async () => {
  try {
    await formRef.value.validate()

    // ⚠️ Kiểm tra email có hợp lệ để tạo tên tài khoản
    if (!formData.value.email || !formData.value.email.includes('@')) {
      Message.error('Email không hợp lệ để tạo tài khoản.')
      return
    }

    // ✅ Tự động tạo tên tài khoản từ email
    const emailUsername = formData.value.email.split('@')[0] // "abc@gmail.com" => "abc"
    formData.value.tenTaiKhoan = emailUsername

    // ✅ Đặt mật khẩu mặc định
    formData.value.matKhau = '123456' // hoặc sinh random nếu muốn

    await axios.post('http://localhost:8080/api/nhan-vien-management/add', formData.value)

    // ✅ Điều hướng khi thành công
    Message.success('Thêm nhân viên thành công!')
    router.push('/khach-hang-nhan-su/nhan-vien')
  } catch (error: unknown) {
    const err = error as any

    if (err.response?.data?.message?.includes('tài khoản đã tồn tại')) {
      Message.error('Email này đã được dùng để tạo tài khoản. Vui lòng dùng email khác.')
    } else {
      Message.error('Thêm nhân viên thất bại. Vui lòng thử lại.')
    }
  }
}
const showConfirm = () => {
  Modal.confirm({
    title: 'Xác nhận thêm nhân viên',
    content: 'Bạn có chắc chắn muốn lưu thông tin nhân viên này không?',
    centered: true,
    okText: 'Xác nhận',
    cancelText: 'Hủy',
    onOk() {
      handleSubmit()
    },
    onCancel() {
      // User cancelled
    },
  })
}

const beforeUpload = (file: File) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png'
  if (!isJpgOrPng) {
    Message.error('Chỉ hỗ trợ upload file JPG/PNG!')
    return false
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    Message.error('Ảnh phải nhỏ hơn 2MB!')
    return false
  }
  return true
}

// Xử lý khi upload thay đổi (upload thành công, thất bại...)
const handleUploadChange = (info: any) => {
  if (info.file.status === 'done') {
    // Giả sử server trả về URL ảnh ở info.file.response.url
    formData.value.anhNhanVien = info.file.response.url
  } else if (info.file.status === 'error') {
    Message.error('Upload ảnh thất bại!')
  }
}

// Handle cancel
const handleCancel = () => {
  router.push('/khach-hang-nhan-su/nhan-vien')
}
</script>
