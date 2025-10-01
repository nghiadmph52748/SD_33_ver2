<template>
  <a-card title="Chỉnh Sửa Nhân Viên">
  <a-form
  ref="formRef"
  :model="formData"
  layout="vertical"
  :rules="rules"
  @finish="handleSubmit"
>
      <a-row :gutter="24">
        <a-col :span="12">
          <a-form-item label="Tên nhân viên" name="tenNhanVien">
            <a-input v-model="formData.tenNhanVien"  style="width: 100%" />
          </a-form-item>

          <a-form-item label="Ngày sinh" name="ngaySinh">
            <a-date-picker v-model="formData.ngaySinh" format="YYYY-MM-DD" style="width: 100%" />
          </a-form-item>

          <a-form-item label="CCCD" name="cccd">
            <a-input v-model="formData.cccd"  style="width: 100%" />
          </a-form-item>

          <a-form-item label="Số điện thoại" name="soDienThoai">
            <a-input v-model="formData.soDienThoai"  style="width: 100%" />
          </a-form-item>

          <a-form-item label="Quận" name="quan">
            <a-input v-model="formData.quan"  style="width: 100%" />
          </a-form-item>

          <a-form-item label="Giới tính" name="gioiTinh">
            <a-switch v-model="formData.gioiTinh" checked-children="Nam" un-checked-children="Nữ" />
          </a-form-item>
        </a-col>

        <a-col :span="12">
          <a-form-item label="Tên tài khoản" name="tenTaiKhoan">
            <a-input v-model="formData.tenTaiKhoan"  style="width: 100%" />
          </a-form-item>

          <a-form-item label="Mật khẩu" name="matKhau">
            <a-input-password v-model="formData.matKhau"  style="width: 100%" />
          </a-form-item>

          <a-form-item label="Email" name="email">
            <a-input v-model="formData.email"  style="width: 100%" />
          </a-form-item>

          <a-form-item label="Thành phố" name="thanhPho">
            <a-input v-model="formData.thanhPho"  style="width: 100%" />
          </a-form-item>

          <a-form-item label="Phường" name="phuong">
            <a-input v-model="formData.phuong"  style="width: 100%" />
          </a-form-item>
          <a-form-item label="Quyền hạn" name="idQuyenHan">
          <a-select v-model="formData.idQuyenHan" placeholder="Chọn quyền hạn">
            <a-option :value="1">Admin</a-option>
            <a-option :value="2">Nhân viên</a-option>
          </a-select>
        </a-form-item>
        </a-col>
      </a-row>

      <a-form-item>
        <a-space>
          <a-button type="primary" @click="handleSubmit" :loading="loading">Lưu</a-button>
          <a-button @click="handleCancel">Hủy</a-button>
        </a-space>
      </a-form-item>
    </a-form>
  </a-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const formRef = ref(null)
// lấy id từ params
const { id } = route.params
const rules = {
  tenNhanVien: [{ required: true, message: 'Vui lòng nhập tên nhân viên' }],
  email: [{ type: 'email', required: true, message: 'Email không hợp lệ' }],
  soDienThoai: [{ required: true, message: 'Vui lòng nhập số điện thoại' }],
}

// dữ liệu form (cần có id để update)
const formData = reactive({
  id: null,
  tenNhanVien: '',
  ngaySinh: '',
  cccd: '',
  soDienThoai: '',
  quan: '',
  gioiTinh: true,
  tenTaiKhoan: '',
  matKhau: '',
  email: '',
  thanhPho: '',
  phuong: '',
  trangThai: true,
  idQuyenHan: null,
})

// load dữ liệu nhân viên
onMounted(async () => {
  if (id) {
    try {
      const res = await axios.get(
        `http://localhost:8080/api/nhan-vien-management/detail/${id}`
      )
      console.log("API trả về:", res.data)

      // Nếu backend trả về object trực tiếp thì gán thẳng
      Object.assign(formData, res.data)

      console.log("FormData sau khi merge:", formData)
    } catch (err) {
      console.error('Lỗi tải nhân viên:', err)
    }
  }
})





const handleSubmit = async () => {
  formRef.value
    .validate()
    .then(async () => {
      console.log("📤 Data gửi đi:", formData)
      try {
        loading.value = true
        const res = await axios.put(
          `http://localhost:8080/api/nhan-vien-management/update/${formData.id}`,
          formData
        )
        if (res.data.success) {
          router.push('/khach-hang-nhan-su/nhan-vien')
        }
      } catch (err) {
        console.error(err)
      } finally {
        loading.value = false
      }
    })
    .catch(err => {
      console.error("Form chưa hợp lệ:", err)
    })
}


const handleCancel = () => {
  router.push('/khach-hang-nhan-su/nhan-vien')
}
</script>

