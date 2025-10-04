<template>
  <a-card title="Thêm Khách Hàng Mới">
    <a-form ref="formRef" :model="formData" :rules="formRules" layout="vertical" @finish="handleSubmit">
      <a-row :gutter="24">
        <a-col :span="12">
          <a-form-item label="Tên khách hàng" name="tenKhachHang" required>
            <a-input v-model="formData.tenKhachHang" placeholder="Nhập tên khách hàng" />
          </a-form-item>

          <a-form-item label="Email" name="email" required>
            <a-input v-model="formData.email" placeholder="Nhập email" />
          </a-form-item>
          <a-form-item label="Ngày sinh" name="ngaySinh">
            <a-date-picker v-model="formData.ngaySinh" format="YYYY-MM-DD" placeholder="Chọn ngày sinh" style="width: 100%" />
          </a-form-item>
          <a-form-item label="Số điện thoại" name="soDienThoai" required>
            <a-input v-model="formData.soDienThoai" placeholder="Nhập số điện thoại" />
          </a-form-item>

          <!-- <a-form-item label="Phân loại" name="phanLoai">
            <a-select v-model="formData.phanLoai" placeholder="Chọn phân loại">
              <a-select-option value="vip">VIP</a-select-option>
              <a-select-option value="regular">Thường xuyên</a-select-option>
              <a-select-option value="new">Mới</a-select-option>
            </a-select>
          </a-form-item> -->

          <a-form-item label="Giới tính" name="gioiTinh" required>
            <a-switch v-model="formData.gioiTinh" checkedChildren="Nam" unCheckedChildren="Nữ" />
          </a-form-item>
        </a-col>

        <a-col :span="12">
          <a-form-item label="Thành phố" name="thanhPho" required>
            <a-select
              v-model="formData.thanhPho"
              placeholder="-- Chọn tỉnh/thành phố --"
              :options="provinces"
              @change="onProvinceChange"
              option-label-prop="label"
            />
          </a-form-item>

          <a-form-item label="Quận" name="quan" required>
            <a-select
              v-model="formData.quan"
              placeholder="-- Chọn quận/huyện --"
              :options="districts"
              @change="onDistrictChange"
              option-label-prop="label"
            />
          </a-form-item>

          <a-form-item label="Phường" name="phuong" required>
            <a-select v-model="formData.phuong" placeholder="-- Chọn phường/xã --" :options="wards" option-label-prop="label" />
          </a-form-item>

          <a-form-item label="Địa chỉ cụ thể" name="diaChiCuThe" required>
            <a-input v-model="formData.diaChiCuThe" placeholder="Nhập địa chỉ cụ thể (số nhà, đường...)" />
          </a-form-item>
        </a-col>
      </a-row>

      <a-form-item>
        <a-button type="primary" @click="showConfirm">Lưu</a-button>
        <a-button style="margin-left: 8px" @click="handleCancel">Hủy</a-button>
      </a-form-item>
    </a-form>
  </a-card>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { Modal, Message } from '@arco-design/web-vue'

const router = useRouter()

const formRef = ref()

const provinces = ref<{ value: string; label: string; code: number }[]>([])
const districts = ref<{ value: string; label: string; code: number }[]>([])
const wards = ref<{ value: string; label: string }[]>([])

const formData = ref({
  tenKhachHang: '',
  email: '',
  soDienThoai: '',
  ngaySinh: '',
  phanLoai: '',
  gioiTinh: true, // true = Nam, false = Nữ
  thanhPho: '',
  quan: '',
  phuong: '',
  diaChiCuThe: '',
})

const formRules = {
  tenKhachHang: [{ required: true, message: 'Vui lòng nhập tên khách hàng', trigger: 'blur' }],
  email: [
    { required: true, message: 'Vui lòng nhập email', trigger: 'blur' },
    { type: 'email', message: 'Email không hợp lệ', trigger: ['blur', 'change'] },
  ],
  soDienThoai: [{ required: true, message: 'Vui lòng nhập số điện thoại', trigger: 'blur' }],
  gioiTinh: [{ required: true, message: 'Vui lòng chọn giới tính', trigger: 'change' }],
  thanhPho: [{ required: true, message: 'Vui lòng chọn tỉnh/thành phố', trigger: 'change' }],
  quan: [{ required: true, message: 'Vui lòng chọn quận/huyện', trigger: 'change' }],
  phuong: [{ required: true, message: 'Vui lòng chọn phường/xã', trigger: 'change' }],
  diaChiCuThe: [{ required: true, message: 'Vui lòng nhập địa chỉ cụ thể', trigger: 'blur' }],
}

// Load provinces on mount
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

const handleSubmit = async () => {
  try {
    await formRef.value.validate()

    if (!formData.value.email || !formData.value.email.includes('@')) {
      Message.error('Email không hợp lệ để tạo tài khoản.')
      return
    }

    const emailUsername = formData.value.email.split('@')[0]
    const matKhauMacDinh = '123456'

    const payload = {
      tenKhachHang: formData.value.tenKhachHang,
      email: formData.value.email,
      ngaySinh: formData.value.ngaySinh,
      soDienThoai: formData.value.soDienThoai,
      phanLoai: formData.value.phanLoai || null,
      gioiTinh: formData.value.gioiTinh,
      trangThai: true,
      tenTaiKhoan: emailUsername,
      matKhau: matKhauMacDinh,
      listDiaChi: [
        {
          diaChiCuThe: formData.value.diaChiCuThe,
          thanhPho: formData.value.thanhPho,
          quan: formData.value.quan,
          phuong: formData.value.phuong,
        },
      ],
    }

    await axios.post('http://localhost:8080/api/khach-hang-management/add', payload)
    Message.success('Thêm khách hàng thành công!')
    router.push('/khach-hang-nhan-su/khach-hang')
  } catch (error) {
    Message.error('Thêm khách hàng thất bại. Vui lòng kiểm tra lại thông tin.')
  }
}
const showConfirm = () => {
  Modal.confirm({
    title: 'Xác nhận thêm khách hàng',
    content: 'Bạn có chắc chắn muốn lưu thông tin khách hàng này không?',
    okText: 'Xác nhận',
    cancelText: 'Hủy',
    centered: true,
    onOk() {
      return handleSubmit() // trả về promise để Modal biết khi nào kết thúc
    },
    onCancel() {
      // User cancelled
    },
  })
}

const handleCancel = () => {
  router.push('/khach-hang-nhan-su/khach-hang')
}
</script>
