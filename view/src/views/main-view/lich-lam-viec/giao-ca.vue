<template>
  <div class="giao-ca-page">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- Bộ lọc -->
    <a-card class="filters-card">
      <a-form :model="filterForm" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="6">
            <a-form-item label="Tìm kiếm">
              <a-input v-model="search" placeholder="Tìm theo nhân viên, ca làm..." allow-clear />
            </a-form-item>
          </a-col>

          <a-col :span="5">
            <a-form-item label="Ca làm việc">
              <a-select v-model="filterForm.caLam" placeholder="Chọn ca làm" allow-clear>
                <a-option value="Ca sáng">Ca sáng</a-option>
                <a-option value="Ca chiều">Ca chiều</a-option>
                <a-option value="Ca tối">Ca tối</a-option>
              </a-select>
            </a-form-item>
          </a-col>

          <a-col :span="5">
            <a-form-item label="Ngày giao ca">
              <a-date-picker
                v-model="filterForm.ngayGiaoCa"
                placeholder="Chọn ngày"
                format="YYYY-MM-DD"
                allow-clear
              />
            </a-form-item>
          </a-col>

          <!-- <a-col :span="8">
            <a-form-item label="Trạng thái">
              <a-radio-group v-model="filterForm.trangThai" type="button">
                <a-radio value="all">Tất cả</a-radio>
                <a-radio value="Đang làm">Đang làm</a-radio>
                <a-radio value="Đã kết thúc">Hoàn tất</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col> -->
        </a-row>
      </a-form>

      <div class="actions-row">
        <a-space>
          <a-button @click="resetFilters">
            <template #icon><icon-refresh /></template>
            Đặt lại
          </a-button>

          <a-button type="primary" @click="themGiaoCa">
            <template #icon><icon-plus /></template>
            Giao ca
          </a-button>
        </a-space>
      </div>
    </a-card>

    <!-- Danh sách giao ca -->
    <a-card title="Danh sách giao ca" class="table-card">
      <a-table
        :columns="columns"
        :data="filteredList"
        :pagination="pagination"
        :loading="loading"
        :scroll="{ x: 1200 }"
      >
        <!-- Cột trạng thái -->
        <template #trangThai="{ record }">
          <a-tag
            :class="{
              'success-tag': record.trangThai === 'Đã kết thúc',
              'info-tag': record.trangThai === 'Đang làm'
            }"
          >
            {{ record.trangThai }}
          </a-tag>
        </template>

        <!-- Cột hành động -->
        <template #action="{ record }">
          <a-space>
            <a-button type="text" @click="viewDetail(record)">
              <template #icon><icon-edit /></template>
            </a-button>

            <a-button type="text" status="danger" @click="deleteGiaoCa(record.id)">
              <template #icon><icon-delete /></template>
            </a-button>
          </a-space>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Message,Modal } from '@arco-design/web-vue'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import { getGiaoCa, xoaGiaoCa } from '@/api/giao-ca'
import useBreadcrumb from '@/hooks/breadcrumb'

// 🧭 Router & Breadcrumb
const router = useRouter()
const { breadcrumbItems } = useBreadcrumb()

// ⚙️ Data & filter
const search = ref('')
const filterForm = ref({
  caLam: '',
  ngayGiaoCa: '',
  trangThai: 'all'
})
const loading = ref(false)
const giaoCaList = ref([])

// 🧮 Table columns
const columns = [
  { title: '#', dataIndex: 'id', width: 60 },
  { title: 'Ca làm việc', dataIndex: 'caLamViec.tenCa', width: 120 },
  { title: 'Người giao', dataIndex: 'nguoiGiao.tenNhanVien', width: 160 },
  { title: 'Người nhận', dataIndex: 'nguoiNhan.tenNhanVien', width: 160 },
   { title: 'Ngày giao ca', dataIndex: 'thoiGianGiaoCa', width: 150 },
  { title: 'Tiền đầu ca', dataIndex: 'tongTienBanDau', width: 120 },
  { title: 'Tiền cuối ca', dataIndex: 'tongTienCuoiCa', width: 120 },
  { title: 'Ghi chú', dataIndex: 'ghiChu', width: 200 },
  // { title: 'Trạng thái', dataIndex: 'trangThai', slotName: 'trangThai', width: 120 },
  { title: 'Hành động', slotName: 'action', width: 120 }
]

// 🔄 Lọc dữ liệu
const filteredList = computed(() =>
  giaoCaList.value.filter(g =>
    // Tìm kiếm
    (!search.value ||
      g.nguoiGiao?.tenNhanVien.toLowerCase().includes(search.value.toLowerCase()) ||
      g.nguoiNhan?.tenNhanVien.toLowerCase().includes(search.value.toLowerCase()) ||
      g.caLamViec?.tenCa.toLowerCase().includes(search.value.toLowerCase())
    ) &&
    // Lọc theo ca
    (!filterForm.value.caLam || g.caLamViec?.tenCa === filterForm.value.caLam) &&
    // Lọc theo trạng thái
    (filterForm.value.trangThai === 'all' || g.trangThai === filterForm.value.trangThai) &&
    // Lọc theo ngày
    (!filterForm.value.ngayGiaoCa || 
      new Date(g.thoiGianGiaoCa).toISOString().slice(0,10) === filterForm.value.ngayGiaoCa)
  )
)


// 🔁 Lấy danh sách
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getGiaoCa()
    giaoCaList.value = res.data || res || []
  } catch (error) {
    Message.error('Lỗi khi tải danh sách giao ca')
    // eslint-disable-next-line no-console
    console.error(error)
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  search.value = ''
  filterForm.value = { caLam: '', ngayGiaoCa: '', trangThai: 'all' }
}

const themGiaoCa = () => {
  router.push({ name: 'GiaoCa' })
}

const viewDetail = (record: any) => {
  router.push({
    name: 'updategiaoca',
    params: { id: record.id },
  })
}


const deleteGiaoCa = async (id: number) => {
  Modal.confirm({
    title: 'Xác nhận xóa',
    content: 'Bạn có chắc chắn muốn xóa giao ca này không?',
    okText: 'Xóa',
    cancelText: 'Hủy',
    async onOk() {
      try {
        await xoaGiaoCa(id)
        Message.success('Xóa giao ca thành công')
        fetchData()
      } catch (_error) {
        Message.error('Không thể xóa giao ca')
      }
    }
  })
}

onMounted(() => fetchData())

// 📄 Pagination
const pagination = {
  pageSize: 10,
  showTotal: true
}
</script>


<style scoped>
.schedule-management-page {
  padding: 0 20px 20px 20px;
}

.filters-card,
.table-card {
  margin-bottom: 16px;
  border-radius: 12px;
}

.actions-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

/* Table cell padding */
:deep(.arco-table .arco-table-cell) {
  padding: 6px 8px;
}

/* Tag styling để hiển thị trạng thái */
:deep(.success-tag) {
  color: rgb(var(--success-6)) !important;
  background: rgb(var(--success-1)) !important;
  border-color: rgb(var(--success-1)) !important;
}

:deep(.info-tag) {
  color: rgb(var(--primary-6)) !important;
  background: rgb(var(--primary-1)) !important;
  border-color: rgb(var(--primary-1)) !important;
}

/* Button nhỏ gọn trong table */
:deep(.arco-table .arco-btn) {
  padding: 2px 6px;
}

/* Optional: căn chỉnh input, select filter */
:deep(.filters-card .arco-form-item) {
  margin-bottom: 12px;
}
</style>
