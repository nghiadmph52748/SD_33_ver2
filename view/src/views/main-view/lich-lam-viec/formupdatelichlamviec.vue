<template>
  <div class="schedule-update-page">
    <a-card class="form-card" title="Cập nhật lịch làm việc">
      <a-form :model="form" layout="vertical" :rules="rules" ref="updateFormRef">
        <a-row :gutter="16">
          <!-- Nhân viên -->
          <a-col :span="12">
            <a-form-item label="Nhân viên" name="nhanVien" required>
              <a-select
                v-model="form.nhanVien"
                placeholder="Chọn nhân viên"
                allow-clear
              >
                <a-option v-for="nv in nhanViens" :key="nv.id" :value="nv.id">
                  {{ nv.tenNhanVien }}
                </a-option>
              </a-select>
            </a-form-item>
          </a-col>

          <!-- Ca làm việc -->
          <a-col :span="12">
            <a-form-item label="Ca làm việc" name="caLamViec" required>
              <a-select
                v-model="form.caLamViec"
                placeholder="Chọn ca làm việc"
                allow-clear
              >
                <a-option v-for="ca in caLamViecs" :key="ca.id" :value="ca.id">
                  {{ ca.tenCa }}
                </a-option>
              </a-select>
            </a-form-item>
          </a-col>

          <!-- Ngày làm việc -->
          <a-col :span="12">
            <a-form-item label="Ngày làm việc" name="ngayLamViec" required>
              <a-date-picker
                v-model="form.ngayLamViec"
                placeholder="Chọn ngày làm việc"
                style="width: 100%"
                format="DD/MM/YYYY"
              />
            </a-form-item>
          </a-col>

          <!-- Ghi chú -->
          <a-col :span="24">
            <a-form-item label="Ghi chú" name="ghiChu">
              <a-textarea
                v-model="form.ghiChu"
                placeholder="Nhập ghi chú nếu có"
                rows="3"
              />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>

      <div class="actions-row">
        <a-space>
          <a-button @click="handleCancel">Hủy</a-button>
          <a-button type="primary" @click="handleUpdate" :loading="loading">
            Cập nhật
          </a-button>
        </a-space>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { Message } from '@arco-design/web-vue'

import { layDanhSachNhanVien } from '@/api/nhan-vien'
import { getCaLamViec } from '@/api/ca-lam-viec'
import { getLichLamViecById, suaLichLamViec } from '@/api/lich-lam-viec'

// 🔹 Router + route
const router = useRouter()
const route = useRoute()

// 🔹 Loading
const loading = ref(false)

// 🔹 Form
const updateFormRef = ref()
const form = ref({
  nhanVien: null as number | null,
  caLamViec: null as number | null,
  ngayLamViec: null as any,
  ghiChu: ''
})

// 🔹 Dữ liệu select
const nhanViens = ref<{id:number, tenNhanVien:string}[]>([])
const caLamViecs = ref<{id:number, tenCa:string}[]>([])

// 🔹 Quy tắc validate
const rules = {
  nhanVien: [{ required: true, message: 'Vui lòng chọn nhân viên' }],
  caLamViec: [{ required: true, message: 'Vui lòng chọn ca làm việc' }],
  ngayLamViec: [{ required: true, message: 'Vui lòng chọn ngày làm việc' }]
}

// ===========================
// 🔹 Hàm load danh sách nhân viên
// ===========================
const fetchNhanViens = async () => {
  try {
    const res = await layDanhSachNhanVien()
    const list = res.data?.data ?? res.data ?? []

    nhanViens.value = list.map((nv: any) => ({
      id: nv.id,
      tenNhanVien: nv.tenNhanVien || nv.tennhanvien || 'Không rõ'
    }))
    console.log('📦 Nhân viên list:', nhanViens.value)
  } catch (err) {
    console.error('❌ Lỗi tải nhân viên:', err)
    Message.error('Không thể tải danh sách nhân viên')
  }
}

// ===========================
// 🔹 Hàm load danh sách ca làm việc
// ===========================
const fetchCaLamViecs = async () => {
  try {
    const res = await getCaLamViec()
    // Kiểm tra nếu backend trả array trực tiếp hoặc res.data
    let list: any[] = []
    if (Array.isArray(res)) {
      list = res
    } else if (Array.isArray(res.data?.data)) {
      list = res.data.data
    } else if (Array.isArray(res.data)) {
      list = res.data
    }

    if (!list.length) {
      Message.warning('Danh sách ca làm việc hiện đang trống. Vui lòng thêm ca làm việc trước.')
    }

    caLamViecs.value = list.map((ca: any) => ({
      id: ca.id,
      tenCa: ca.tenCa || ca.tenca || 'Không rõ'
    }))

    console.log('📦 Ca làm việc list:', caLamViecs.value)
  } catch (err) {
    console.error('❌ Lỗi tải ca làm việc:', err)
    Message.error('Không thể tải danh sách ca làm việc')
  }
}

// ===========================
// 🔹 Hàm load dữ liệu form theo ID
// ===========================
const loadData = async () => {
  const id = Number(route.params.id)
  if (!id) {
    Message.error('Không tìm thấy ID lịch làm việc')
    return
  }

  try {
    // Load nhân viên + ca làm việc cùng lúc
    await Promise.all([fetchNhanViens(), fetchCaLamViecs()])

    // Load dữ liệu lịch theo id
    const lichRes = await getLichLamViecById(id)
    const lich = lichRes.data ?? lichRes; // lichRes.data mới chứa object từ backend


    console.log('📦 Dữ liệu lịch:', lich)

    if (!lich) {
      Message.error('Lịch làm việc với ID này không tồn tại!')
      return
    }

    form.value.nhanVien = lich.nhanVien?.id ?? null
    form.value.caLamViec = lich.caLamViec?.id ?? null
    form.value.ngayLamViec = lich.ngayLamViec ? dayjs(lich.ngayLamViec) : null
    form.value.ghiChu = lich.ghiChu ?? ''
  } catch (err) {
    console.error('❌ Lỗi khi load dữ liệu:', err)
    Message.error('Không thể tải dữ liệu lịch làm việc. Kiểm tra console để biết chi tiết.')
  }
}

// ===========================
// 🔹 Hàm cập nhật lịch
// ===========================
const handleUpdate = async () => {
  try {
    await updateFormRef.value.validate()
    loading.value = true

    const id = Number(route.params.id)
    const payload = {
      nhanVienId: form.value.nhanVien,
      caLamViecId: form.value.caLamViec,
      ngayLamViec: dayjs(form.value.ngayLamViec).format('YYYY-MM-DD'),
      ghiChu: form.value.ghiChu || null,
      trangThai: true
    }

    await suaLichLamViec(id, payload)
    Message.success('Cập nhật lịch làm việc thành công')
    router.push('/lich-lam-viec/danh-sach')
  } catch (err) {
    console.error('❌ Lỗi khi cập nhật:', err)
    Message.error('Không thể cập nhật lịch làm việc')
  } finally {
    loading.value = false
  }
}

// ===========================
// 🔹 Hủy chỉnh sửa
// ===========================
const handleCancel = () => {
  updateFormRef.value.resetFields()
  router.push('/lich-lam-viec/danh-sach')
}

// ===========================
// 🔄 Khi mount, load dữ liệu
// ===========================
onMounted(loadData)
</script>




<style scoped>
.schedule-update-page {
  padding: 0 20px 20px 20px;
}
.form-card {
  border-radius: 12px;
  padding: 20px;
}
.actions-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
:deep(.arco-input),
:deep(.arco-select),
:deep(.arco-picker) {
  border-radius: 6px;
}
:deep(.arco-btn) {
  border-radius: 6px;
}
</style>
