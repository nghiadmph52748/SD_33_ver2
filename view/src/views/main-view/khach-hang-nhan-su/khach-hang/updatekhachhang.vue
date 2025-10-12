<template>
  <div class="customer-update-page">
    <Breadcrumb :items="breadcrumbItems" />

    <a-card title="Cập nhật khách hàng" class="update-card" :loading="dangTai">
      <a-form :model="form" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="Tên khách hàng">
              <a-input v-model="form.name" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="Giới tính">
              <a-select v-model="form.gender">
                <a-option value="Nam">Nam</a-option>
                <a-option value="Nữ">Nữ</a-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <!-- ✅ THÊM EMAIL & SỐ ĐIỆN THOẠI -->
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="Email">
              <a-input v-model="form.email" placeholder="Nhập email khách hàng" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="Số điện thoại">
              <a-input v-model="form.soDienThoai" placeholder="Nhập số điện thoại khách hàng" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="Thành phố">
              <a-select
                v-model="form.thanhPho"
                placeholder="-- Chọn tỉnh/thành phố --"
                :options="provinces"
                @change="onProvinceChange"
                option-label-prop="label"
                option-value-prop="value"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="Quận">
              <a-select
                v-model="form.quan"
                placeholder="-- Chọn quận/huyện --"
                :options="districts"
                @change="onDistrictChange"
                option-label-prop="label"
                option-value-prop="value"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="Phường">
              <a-select
                v-model="form.phuong"
                placeholder="-- Chọn phường/xã --"
                :options="wards"
                option-label-prop="label"
                option-value-prop="value"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="Địa chỉ cụ thể">
              <a-input v-model="form.diaChiCuThe" placeholder="Nhập địa chỉ cụ thể (số nhà, đường...)" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="Tên tài khoản">
              <a-input v-model="form.tenTaiKhoan" placeholder="Nhập tên tài khoản" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="Mật khẩu">
              <a-input type="password" v-model="form.matKhau" placeholder="Nhập mật khẩu" autocomplete="new-password" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="Ngày sinh">
              <a-date-picker v-model="form.birthday" style="width: 100%" />
            </a-form-item>
          </a-col>
          <!-- <a-col :span="12">
            <a-form-item label="Trạng thái">
              <a-select v-model="form.status">
                <a-option value="active">Hoạt động</a-option>
                <a-option value="inactive">Không hoạt động</a-option>
              </a-select>
            </a-form-item>
          </a-col> -->
        </a-row>

        <div class="actions-row">
          <a-space>
            <a-button @click="quayLai">
              <template #icon>
                <icon-arrow-left />
              </template>
              Quay lại
            </a-button>
            <a-button type="primary" @click="luuKhach">
              <template #icon>
                <icon-save />
              </template>
              Lưu
            </a-button>
          </a-space>
        </div>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import { IconArrowLeft, IconSave } from '@arco-design/web-vue/es/icon'
import { Modal, Message } from '@arco-design/web-vue'

const route = useRoute()
const router = useRouter()
const provinces = ref<{ value: string; label: string; code: number }[]>([])
const districts = ref<{ value: string; label: string; code: number }[]>([])
const wards = ref<{ value: string; label: string }[]>([])
const { breadcrumbItems } = useBreadcrumb()

const dangTai = ref(false)
const form = ref<any>({
  tenKhachHang: '',
  gioiTinh: '',
  ngaySinh: '',
  trangThai: '',
  thanhPho: '',
  quan: '',
  phuong: '',
  diaChiCuThe: '',
  email: '',
  soDienThoai: '',
  tenTaiKhoan: '', // thêm
  matKhau: '',
})

const layChiTietKhach = async () => {
  try {
    dangTai.value = true
    const { id } = route.params
    const res = await axios.get(`/api/khach-hang-management/detail/${id}`)
    form.value = {
      id: res.data.id,
      code: res.data.maKhachHang,
      name: res.data.tenKhachHang,
      gender: res.data.gioiTinh ? 'Nam' : 'Nữ',
      birthday: res.data.ngaySinh,
      status: res.data.trangThai === 'Hoạt động' ? 'active' : 'inactive',
      email: res.data.email,
      soDienThoai: res.data.soDienThoai,
      thanhPho: res.data.thanhPho || '',
      quan: res.data.quan || '',
      phuong: res.data.phuong || '',
      diaChiCuThe: res.data.diaChiCuThe || '',
      tenTaiKhoan: res.data.tenTaiKhoan || '',
      matKhau: res.data.matKhau,
    }
  } catch (err) {
    // eslint-disable-next-line no-console
    console.error('Lỗi lấy chi tiết khách:', err)
  } finally {
    dangTai.value = false
  }
}
const luuKhach = () => {
  Modal.confirm({
    title: 'Xác nhận lưu thay đổi',
    content: 'Bạn có chắc chắn muốn lưu thông tin khách hàng này không?',
    okText: 'Lưu',
    cancelText: 'Hủy',
    okButtonProps: { type: 'primary', status: 'success' },
    async onOk() {
      // Validate tất cả các trường
      if (!form.value.name) {
        Message.error('Vui lòng nhập tên khách hàng.')
        return false
      }
      if (!form.value.gender) {
        Message.error('Vui lòng chọn giới tính.')
        return false
      }
      if (!form.value.birthday) {
        Message.error('Vui lòng chọn ngày sinh.')
        return false
      }
      if (!form.value.status) {
        Message.error('Vui lòng chọn trạng thái.')
        return false
      }
      if (!form.value.email || !form.value.email.includes('@')) {
        Message.error('Email không hợp lệ.')
        return false
      }
      if (!form.value.soDienThoai) {
        Message.error('Vui lòng nhập số điện thoại.')
        return false
      }
      if (!form.value.thanhPho) {
        Message.error('Vui lòng chọn tỉnh/thành phố.')
        return false
      }
      if (!form.value.quan) {
        Message.error('Vui lòng chọn quận/huyện.')
        return false
      }
      if (!form.value.phuong) {
        Message.error('Vui lòng chọn phường/xã.')
        return false
      }
      if (!form.value.diaChiCuThe) {
        Message.error('Vui lòng nhập địa chỉ cụ thể.')
        return false
      }
      if (!form.value.tenTaiKhoan) {
        Message.error('Vui lòng nhập tên tài khoản.')
        return false
      }
      if (!form.value.matKhau) {
        Message.error('Vui lòng nhập mật khẩu.')
        return false
      }

      try {
        dangTai.value = true

        const payload = {
          tenKhachHang: form.value.name,
          gioiTinh: form.value.gender === 'Nam',
          ngaySinh: form.value.birthday,
          phanLoaiText: form.value.customer_type,
          trangThai: form.value.status === 'active',
          listDiaChi: [
            {
              thanhPho: form.value.thanhPho,
              quan: form.value.quan,
              phuong: form.value.phuong,
              diaChiCuThe: form.value.diaChiCuThe,
            },
            // Nếu có nhiều địa chỉ, thêm vào đây
          ],
          email: form.value.email,
          soDienThoai: form.value.soDienThoai,
          tenTaiKhoan: form.value.tenTaiKhoan?.trim() || '',
          matkhau: form.value.matKhau,
        }

        if (form.value.id) {
          await axios.put(`/api/khach-hang-management/update/${form.value.id}`, payload)
          Message.success('Cập nhật khách hàng thành công!')
        } else {
          await axios.post('/api/khach-hang-management', payload)
          Message.success('Thêm mới khách hàng thành công!')
        }

        router.push('/khach-hang-nhan-su/khach-hang')
      } catch (err) {
        // eslint-disable-next-line no-console
        console.error('Lỗi lưu khách:', err)
        Message.error('Lưu khách hàng thất bại!')
      } finally {
        dangTai.value = false
      }
      return true
    },
  })
}

const quayLai = () => {
  router.push('/khach-hang-nhan-su/khach-hang')
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
  form.value.quan = ''
  form.value.phuong = ''

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
  form.value.phuong = ''

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

onMounted(() => {
  layChiTietKhach()
})
</script>

<style scoped>
.update-card {
  margin-top: 16px;
}
.actions-row {
  margin-top: 16px;
  text-align: right;
}
</style>
