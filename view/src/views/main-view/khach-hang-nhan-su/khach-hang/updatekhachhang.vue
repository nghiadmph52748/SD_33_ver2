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

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="Ngày sinh">
              <a-date-picker v-model="form.birthday" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="Trạng thái">
              <a-select v-model="form.status">
                <a-option value="active">Hoạt động</a-option>
                <a-option value="inactive">Không hoạt động</a-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="Phân loại">
              <a-select v-model="form.customer_type">
                <a-option value="vip">VIP</a-option>
                <a-option value="regular">Thường xuyên</a-option>
                <a-option value="new">Mới</a-option>
              </a-select>
            </a-form-item>
          </a-col>
          
        </a-row>

        <div class="actions-row">
          <a-space>
            <a-button @click="quayLai">
              <template #icon>
                <icon-arrow-left />
              </template>
              Quay lại
            </a-button>
            <a-button type="primary" @click="capNhatKhach">
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

const route = useRoute()
const router = useRouter()
const { breadcrumbItems } = useBreadcrumb()

const dangTai = ref(false)
const form = ref<any>({
  code: '',
  name: '',
  gender: '',
  birthday: '',
  customer_type: '',
  status: '',
})

const layChiTietKhach = async () => {
  try {
    dangTai.value = true
    const { id } = route.params
    const res = await axios.get(`/api/khach-hang-management/detail/${id}`)
    form.value = {
      code: res.data.maKhachHang,
      name: res.data.tenKhachHang,
      gender: res.data.gioiTinh ? 'Nam' : 'Nữ',
      birthday: res.data.ngaySinh,
      customer_type: res.data.phanLoaiText,
      status: res.data.trangThaiText === 'Hoạt động' ? 'active' : 'inactive',
    }
  } catch (err) {
    console.error('Lỗi lấy chi tiết khách:', err)
  } finally {
    dangTai.value = false
  }
}

const capNhatKhach = async () => {
  try {
    dangTai.value = true
    await axios.put(`/api/khach-hang-management/${form.value.code}`, form.value)
    router.push('/khachhang')
  } catch (err) {
    console.error('Lỗi cập nhật:', err)
  } finally {
    dangTai.value = false
  }
}

const quayLai = () => {
  router.push('/khach-hang-nhan-su/khach-hang')
}

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
