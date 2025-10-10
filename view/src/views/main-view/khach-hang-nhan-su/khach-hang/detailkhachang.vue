<template>
  <div class="customer-detail-page">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- Card thông tin khách hàng -->
    <a-card title="Chi tiết khách hàng" class="detail-card" :loading="dangTai">
      <a-descriptions bordered column="2">
        <a-descriptions-item label="Mã khách hàng">
          {{ khachHang?.code }}
        </a-descriptions-item>
        <a-descriptions-item label="Tên khách hàng">
          {{ khachHang?.name }}
        </a-descriptions-item>
        <a-descriptions-item label="Giới tính">
          {{ khachHang?.gender }}
        </a-descriptions-item>
        <a-descriptions-item label="Ngày sinh">
          {{ khachHang?.birthday }}
        </a-descriptions-item>
        <a-descriptions-item label="Phân loại">
          <a-tag :color="mauPhanLoai(khachHang?.customer_type)">
            {{ tenPhanLoai(khachHang?.customer_type) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="Trạng thái">
          <a-tag :color="khachHang?.status === 'active' ? 'green' : 'orange'">
            {{ khachHang?.status === 'active' ? 'Hoạt động' : 'Không hoạt động' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="Tổng đơn">
          {{ khachHang?.total_orders }}
        </a-descriptions-item>
        <a-descriptions-item label="Tổng chi tiêu">
          {{ dinhDangTien(khachHang?.total_spent) }}
        </a-descriptions-item>
      </a-descriptions>

      <div class="actions-row">
        <a-space>
          <a-button @click="quayLai">
            <template #icon>
              <icon-arrow-left />
            </template>
            Quay lại
          </a-button>
        </a-space>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import axios from 'axios'
import { IconArrowLeft, IconEdit } from '@arco-design/web-vue/es/icon'

const route = useRoute()
const router = useRouter()
const { breadcrumbItems } = useBreadcrumb()

const dangTai = ref(false)
const khachHang = ref<any>(null)

const dinhDangTien = (soTien: number) => {
  if (!soTien) return '0 VND'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(soTien)
}

const mauPhanLoai = (loai: string) => {
  switch (loai) {
    case 'vip':
      return 'gold'
    case 'regular':
      return 'blue'
    case 'new':
      return 'green'
    case 'inactive':
      return 'red'
    default:
      return 'default'
  }
}

const tenPhanLoai = (loai: string) => {
  switch (loai) {
    case 'vip':
      return 'VIP'
    case 'regular':
      return 'Thường xuyên'
    case 'new':
      return 'Mới'
    case 'inactive':
      return 'Không hoạt động'
    default:
      return loai
  }
}

const layChiTietKhachHang = async () => {
  try {
    dangTai.value = true
    const idKhach = route.params.id
    console.log('ID Khách Hàng:', idKhach) // In ra ID để kiểm tra

    const res = await axios.get(`/api/khach-hang-management/detail/${idKhach}`)
    console.log('Dữ liệu từ API:', res.data) // Kiểm tra dữ liệu trả về từ API

    khachHang.value = {
      code: res.data.maKhachHang || '',
      name: res.data.tenKhachHang || '',
      gender: res.data.gioiTinh ? 'Nam' : 'Nữ',
      birthday: res.data.ngaySinh || '',
      total_orders: res.data.tongDon || 0,
      total_spent: res.data.tongChiTieu || 0,
      customer_type: res.data.phanLoaiText || '',
      status: res.data.trangThaiText === 'Hoạt động' ? 'active' : 'inactive',
    }
  } catch (err) {
    console.error('Lỗi lấy chi tiết khách:', err)
  } finally {
    dangTai.value = false
  }
}

const quayLai = () => {
  router.push('/khach-hang-nhan-su/khach-hang') // quay lại danh sách
}

const chinhSua = () => {
  router.push(`/updatekhachhang/${khachHang.value.code}`)
}

onMounted(() => {
  layChiTietKhachHang()
})
</script>

<style scoped>
.detail-card {
  margin-top: 16px;
}
.actions-row {
  margin-top: 16px;
  text-align: right;
}
</style>
