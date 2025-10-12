<template>
  <a-card title="Chỉnh Sửa Nhân Viên">
    <a-form ref="formRef" :model="formData" layout="vertical">
      <a-row :gutter="24">
        <a-col :span="12">
          <a-form-item label="Tên nhân viên" name="tenNhanVien">
            <a-input v-model="formData.tenNhanVien" style="width: 100%" />
          </a-form-item>

          <a-form-item label="Ngày sinh" name="ngaySinh">
            <a-date-picker v-model="formData.ngaySinh" format="YYYY-MM-DD" style="width: 100%" />
          </a-form-item>

          <a-form-item label="CCCD" name="cccd">
            <a-input v-model="formData.cccd" style="width: 100%" />
          </a-form-item>

          <a-form-item label="Số điện thoại" name="soDienThoai">
            <a-input v-model="formData.soDienThoai" style="width: 100%" />
          </a-form-item>
          <a-form-item label="Quyền hạn" name="idQuyenHan">
            <a-select v-model="formData.idQuyenHan" placeholder="Chọn quyền hạn">
              <a-option :value="1">Admin</a-option>
              <a-option :value="2">Nhân viên</a-option>
            </a-select>
          </a-form-item>
          <a-form-item label="Tên tài khoản" name="tenTaiKhoan">
            <a-input v-model="formData.tenTaiKhoan" placeholder="Nhập tên tài khoản" />
          </a-form-item>

          <a-form-item label="Giới tính" name="gioiTinh">
            <a-switch v-model="formData.gioiTinh" checked-children="Nam" un-checked-children="Nữ" />
          </a-form-item>
          <!-- <a-form-item label="Ảnh nhân viên" name="anhNhanVien">
            <a-upload name="file" :action="uploadUrl" :show-upload-list="false" :before-upload="beforeUpload" @change="handleUploadChange">
              <a-button icon="upload">Chọn ảnh</a-button>
            </a-upload>
            <div v-if="formData.anhNhanVien">
              <img :src="formData.anhNhanVien" alt="Ảnh nhân viên" style="max-width: 100px; margin-top: 10px" />
            </div>
          </a-form-item> -->
        </a-col>

        <a-col :span="12">
          <a-form-item label="Email" name="email">
            <a-input v-model="formData.email" style="width: 100%" />
          </a-form-item>

          <a-form-item label="Thành phố" name="thanhPho">
            <a-select
              v-model="formData.thanhPho"
              placeholder="Chọn tỉnh / thành phố"
              :options="provinces"
              option-label-prop="label"
              @change="onProvinceChange"
            />
          </a-form-item>

          <a-form-item label="Quận" name="quan">
            <a-select
              v-model="formData.quan"
              placeholder="Chọn quận / huyện"
              :options="districts"
              option-label-prop="label"
              @change="onDistrictChange"
            />
          </a-form-item>

          <a-form-item label="Phường" name="phuong">
            <a-select v-model="formData.phuong" placeholder="Chọn phường / xã" :options="wards" option-label-prop="label" />
          </a-form-item>

          <a-form-item label="Địa chỉ cụ thể" name="diaChiCuThe">
            <a-input v-model="formData.diaChiCuThe" placeholder="Nhập địa chỉ cụ thể" />
          </a-form-item>
          <a-form-item label="Mật khẩu" name="matKhau">
            <a-input-password v-model="formData.matKhau" placeholder="Nhập mật khẩu mới" />
          </a-form-item>
          <!-- <a-form-item label="Giới tính" name="gioiTinh">
            <a-switch v-model="formData.gioiTinh" checked-children="Nam" un-checked-children="Nữ" />
          </a-form-item> -->
        </a-col>
      </a-row>

      <a-form-item>
        <a-space>
          <a-button type="primary" :loading="loading" html-type="submit" @click="handleSubmit">Lưu</a-button>
          <a-button @click="handleCancel">Hủy</a-button>
        </a-space>
      </a-form-item>
    </a-form>
  </a-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { Modal, message } from 'ant-design-vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const formRef = ref(null)
const provinces = ref<{ value: string; label: string; code: number }[]>([])
const districts = ref<{ value: string; label: string; code: number }[]>([])
const wards = ref<{ value: string; label: string }[]>([])
// lấy id từ params
const { id } = route.params

const formData = ref({
  id: null,
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
// load dữ liệu nhân viên
onMounted(async () => {
  try {
    const res = await axios.get(`http://localhost:8080/api/nhan-vien-management/detail/${id}`)
    formData.value = res.data
  } catch (err) {
    message.error('Không thể tải dữ liệu nhân viên')
  }
})

const handleSubmit = () => {
  Modal.confirm({
    title: 'Xác nhận lưu',
    content: 'Bạn có chắc chắn muốn lưu thay đổi không?',
    centered: true,
    okText: 'Có',
    cancelText: 'Không',
    onOk: async () => {
      loading.value = true
      try {
        if (formData.value.id) {
          const res = await axios.put(`http://localhost:8080/api/nhan-vien-management/update/${formData.value.id}`, formData.value)
          // Thành công nếu res.data.success === true
          if (res && res.data && res.data.success) {
            message.success(res.data.message && res.data.message !== 'null' ? res.data.message : '✅ Cập nhật nhân viên thành công!')
            router.push('/khach-hang-nhan-su/nhan-vien')
          } else {
            const msg =
              res?.data?.message && res?.data?.message !== 'null'
                ? res.data.message
                : 'Cập nhật thất bại'
            message.error(msg)
          }
        } else {
          message.error('Chưa có chức năng thêm mới')
        }
      } catch (error: any) {
        const msg = error?.response?.data?.message || error?.message || 'Form chưa hợp lệ hoặc có lỗi xảy ra. Vui lòng kiểm tra lại.'
        message.error(msg)
      } finally {
        loading.value = false
      }
    },
  })
}

const handleCancel = () => {
  router.push('/khach-hang-nhan-su/nhan-vien')
}
</script>
