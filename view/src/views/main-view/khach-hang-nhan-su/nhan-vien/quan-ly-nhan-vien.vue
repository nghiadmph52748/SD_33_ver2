<template>
  <div class="staff-management-page">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- Filters and Search -->
    <a-card class="filters-card">
      <a-form :model="filters" layout="vertical">
        <a-row :gutter="12">
          <a-col :span="8">
            <a-form-item label="Tìm kiếm">
              <a-input v-model="filters.search" placeholder="Mã, tên, email, SĐT..." allow-clear @change="searchStaff" />
            </a-form-item>
          </a-col>

          <a-col :span="8">
            <a-form-item label="Chức vụ">
              <a-select v-model="filters.position" placeholder="Chọn chức vụ" allow-clear @change="searchStaff">
                <a-option value="">Tất cả</a-option>
                <a-option value="manager">Quản lý</a-option>
                <a-option value="staff">Nhân viên</a-option>
                <a-option value="intern">Thực tập</a-option>
              </a-select>
            </a-form-item>
          </a-col>

          <a-col :span="8">
            <a-form-item label="Giới tính">
              <a-radio-group v-model="filters.gender" type="button" @change="searchStaff">
                <a-radio value="">Tất cả</a-radio>
                <a-radio value="Nam">Nam</a-radio>
                <a-radio value="Nữ">Nữ</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="12">
          <a-col :span="24">
            <a-form-item label="Trạng thái">
              <a-radio-group v-model="filters.status" type="button" @change="searchStaff">
                <a-radio value="">Tất cả</a-radio>
                <a-radio value="active">Đang làm việc</a-radio>
                <a-radio value="inactive">Nghỉ việc</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>

      <div class="actions-row">
        <a-space>
          <a-button @click="resetFilters">
            <template #icon>
              <icon-refresh />
            </template>
            Đặt lại
          </a-button>
          <a-button @click="exportExcel">
            <template #icon>
              <icon-download />
            </template>
            Xuất Excel
          </a-button>
          <a-button type="primary" @click="showCreateModal">
            <template #icon>
              <icon-plus />
            </template>
            Thêm nhân viên
          </a-button>
        </a-space>
      </div>
    </a-card>

    <!-- Staff Table -->
    <a-card title="Danh sách nhân viên" class="table-card">
      <a-table
        :columns="columns"
        :data="staffWithIndex"
        :pagination="pagination"
        :loading="loading"
        :scroll="{ x: 1200 }"
        @change="handleTableChange"
      >
        <template #position="{ record }">
          <a-tag :color="getPositionColor(record.position)">
            {{ getPositionText(record.position) }}
          </a-tag>
        </template>

        <template #salary="{ record }">
          {{ formatCurrency(record.salary) }}
        </template>

        <template #status="{ record }">
          <a-tag :color="record.status === 'active' ? 'green' : 'red'">
            {{ record.status === 'active' ? 'Đang làm việc' : 'Nghỉ việc' }}
          </a-tag>
        </template>

        <template #action="{ record }">
          <a-space>
            <a-button type="text" @click="viewStaff(record)">
              <template #icon>
                <icon-eye />
              </template>
            </a-button>
            <a-button type="text" @click="editStaff(record)">
              <template #icon>
                <icon-edit />
              </template>
            </a-button>
            <a-button type="text" danger @click="deleteStaff(record)">
              <template #icon>
                <icon-delete />
              </template>
            </a-button>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <!-- Create/Edit Modal -->
    <a-modal
      v-model:open="modalVisible"
      :title="isEdit ? 'Chỉnh sửa nhân viên' : 'Thêm nhân viên mới'"
      width="600px"
      @ok="handleSubmit"
      @cancel="handleCancel"
    >
      <a-form ref="formRef" :model="formData" :rules="formRules" layout="vertical">
        <a-form-item label="Mã nhân viên" name="code">
          <a-input v-model="formData.code" placeholder="Nhập mã nhân viên" allow-clear />
        </a-form-item>

        <a-form-item label="Họ và tên" name="name">
          <a-input v-model="formData.name" placeholder="Nhập họ và tên" allow-clear />
        </a-form-item>

        <a-form-item label="Email" name="email">
          <a-input v-model="formData.email" placeholder="Nhập email" allow-clear />
        </a-form-item>

        <a-form-item label="Số điện thoại" name="phone">
          <a-input v-model="formData.phone" placeholder="Nhập số điện thoại" allow-clear />
        </a-form-item>

        <a-form-item label="Ngày sinh" name="birthday">
          <a-date-picker v-model="formData.birthday" placeholder="Chọn ngày sinh" style="width: 100%" />
        </a-form-item>

        <a-form-item label="Giới tính" name="gender">
          <a-radio-group v-model="formData.gender">
            <a-radio value="Nam">Nam</a-radio>
            <a-radio value="Nữ">Nữ</a-radio>
          </a-radio-group>
        </a-form-item>

        <a-form-item label="Chức vụ" name="position">
          <a-select v-model="formData.position" placeholder="Chọn chức vụ">
            <a-option value="manager">Quản lý</a-option>
            <a-option value="staff">Nhân viên</a-option>
            <a-option value="intern">Thực tập</a-option>
          </a-select>
        </a-form-item>

        <a-form-item label="Lương cơ bản" name="salary">
          <a-input-number v-model="formData.salary" :min="0" :precision="0" style="width: 100%" placeholder="Nhập lương cơ bản" />
        </a-form-item>

        <a-form-item label="Trạng thái" name="status">
          <a-radio-group v-model="formData.status">
            <a-radio value="active">Đang làm việc</a-radio>
            <a-radio value="inactive">Nghỉ việc</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import {
  IconPlus,
  IconSearch,
  IconRefresh,
  IconDownload,
  IconEye,
  IconEdit,
  IconDelete,
  IconLock,
  IconUserGroup,
  IconUser,
  IconStar,
} from '@arco-design/web-vue/es/icon'

// Breadcrumb setup
const { breadcrumbItems } = useBreadcrumb()

// Modal and form
const modalVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const selectedRowKeys = ref([])

// Form data
const formData = reactive({
  code: '',
  name: '',
  email: '',
  phone: '',
  birthday: null,
  gender: 'Nam',
  position: 'staff',
  salary: 0,
  status: 'active',
})

// Filters
const filters = ref({
  search: '',
  position: '',
  gender: '',
  status: '',
})

// Mock data
const staff = ref([
  {
    id: 1,
    code: 'NV001',
    name: 'Nguyễn Thị A',
    email: 'nguyenthia@company.com',
    phone: '0987654321',
    birthday: '1990-05-15',
    gender: 'Nữ',
    avatar: 'https://via.placeholder.com/80x80/1890ff/ffffff?text=A',
    position: 'manager',
    salary: 15000000,
    status: 'active',
    hire_date: '2022-01-15',
  },
  {
    id: 2,
    name: 'Trần Văn B',
    code: 'NV002',
    email: 'tranvanb@company.com',
    phone: '0978123456',
    birthday: '1988-12-03',
    gender: 'Nam',
    avatar: 'https://via.placeholder.com/80x80/52c41a/ffffff?text=B',
    position: 'staff',
    salary: 8000000,
    status: 'active',
    hire_date: '2022-03-20',
  },
  {
    id: 3,
    name: 'Lê Thị C',
    code: 'NV003',
    email: 'lethic@company.com',
    phone: '0967234567',
    birthday: '1995-08-20',
    gender: 'Nữ',
    avatar: 'https://via.placeholder.com/80x80/fa8c16/ffffff?text=C',
    position: 'staff',
    salary: 7000000,
    status: 'active',
    hire_date: '2023-01-10',
  },
])

// Computed staff with filtering and index for STT
const staffWithIndex = computed(() => {
  let filteredStaff = staff.value

  // Filter by search term (code, name, email, phone)
  if (filters.value.search) {
    const searchTerm = filters.value.search.toLowerCase()
    filteredStaff = filteredStaff.filter(
      (member) =>
        member.code.toLowerCase().includes(searchTerm) ||
        member.name.toLowerCase().includes(searchTerm) ||
        member.email.toLowerCase().includes(searchTerm) ||
        member.phone.toLowerCase().includes(searchTerm)
    )
  }

  // Filter by position
  if (filters.value.position && filters.value.position !== '') {
    filteredStaff = filteredStaff.filter((member) => member.position === filters.value.position)
  }

  // Filter by gender
  if (filters.value.gender && filters.value.gender !== '') {
    filteredStaff = filteredStaff.filter((member) => member.gender === filters.value.gender)
  }

  // Filter by status
  if (filters.value.status && filters.value.status !== '') {
    filteredStaff = filteredStaff.filter((member) => member.status === filters.value.status)
  }

  // Add index for STT
  return filteredStaff.map((member, index) => ({
    ...member,
    index: index + 1,
  }))
})

// Table
const loading = ref(false)
const columns = [
  {
    title: 'STT',
    dataIndex: 'index',
    width: 50,
    align: 'center',
  },
  {
    title: 'Mã',
    dataIndex: 'code',
    width: 100,
  },
  {
    title: 'Tên',
    dataIndex: 'name',
    width: 150,
  },
  {
    title: 'Email',
    dataIndex: 'email',
    width: 200,
  },
  {
    title: 'Số điện thoại',
    dataIndex: 'phone',
    width: 150,
  },
  {
    title: 'Ngày sinh',
    dataIndex: 'birthday',
    width: 120,
    align: 'center',
  },
  {
    title: 'Giới tính',
    dataIndex: 'gender',
    width: 100,
    align: 'center',
  },
  {
    title: 'Chức vụ',
    dataIndex: 'position',
    slotName: 'position',
    width: 120,
    align: 'center',
  },
  {
    title: 'Trạng thái',
    dataIndex: 'status',
    slotName: 'status',
    width: 120,
    align: 'center',
  },
  {
    title: 'Thao tác',
    slotName: 'action',
    width: 150,
    fixed: 'right',
  },
]

// Pagination
const pagination = computed(() => ({
  current: 1,
  pageSize: 10,
  total: staffWithIndex.value.length,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: true,
}))

// Form validation rules
const formRules = {
  code: [
    { required: true, message: 'Vui lòng nhập mã nhân viên' },
    { pattern: /^NV\d{3}$/, message: 'Mã nhân viên phải có format NVxxx' },
  ],
  name: [
    { required: true, message: 'Vui lòng nhập họ và tên' },
    { min: 2, max: 50, message: 'Tên phải từ 2-50 ký tự' },
  ],
  email: [
    { required: true, message: 'Vui lòng nhập email' },
    { type: 'email', message: 'Email không hợp lệ' },
  ],
  phone: [
    { required: true, message: 'Vui lòng nhập số điện thoại' },
    { pattern: /^0\d{9}$/, message: 'Số điện thoại phải có 10 chữ số và bắt đầu bằng 0' },
  ],
  position: [{ required: true, message: 'Vui lòng chọn chức vụ' }],
  salary: [
    { required: true, message: 'Vui lòng nhập lương' },
    { type: 'number', min: 1000000, message: 'Lương phải ít nhất 1.000.000 VNĐ' },
  ],
}

// Methods
const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount)
}

const getPositionColor = (position: string) => {
  switch (position) {
    case 'manager':
      return 'gold'
    case 'staff':
      return 'blue'
    case 'intern':
      return 'green'
    default:
      return 'default'
  }
}

const getPositionText = (position: string) => {
  switch (position) {
    case 'manager':
      return 'Quản lý'
    case 'staff':
      return 'Nhân viên'
    case 'intern':
      return 'Thực tập'
    default:
      return position
  }
}

const searchStaff = () => {
  // Filtering is handled by computed property staffWithIndex
  // This method is called when filters change (@change event)
}

const resetFilters = () => {
  filters.value = {
    search: '',
    position: '',
    gender: '',
    status: '',
  }
  formData.value = {
    code: '',
    name: '',
    email: '',
    phone: '',
    birthday: null,
    gender: 'Nam',
    position: 'staff',
    salary: 0,
    status: 'active',
  }
}

const handleTableChange = (paginationData: any, filtersData: any, sorter: any) => {
  // Removed console.log
}

const showCreateModal = () => {
  isEdit.value = false
  formData.code = ''
  formData.name = ''
  formData.email = ''
  formData.phone = ''
  formData.birthday = null
  formData.gender = 'Nam'
  formData.position = 'staff'
  formData.salary = 0
  formData.status = 'active'
  modalVisible.value = true
}

const editStaff = (staffMember: any) => {
  isEdit.value = true
  formData.code = staffMember.code
  formData.name = staffMember.name
  formData.email = staffMember.email
  formData.phone = staffMember.phone
  formData.birthday = staffMember.birthday
  formData.gender = staffMember.gender
  formData.position = staffMember.position
  formData.salary = staffMember.salary
  formData.status = staffMember.status
  modalVisible.value = true
}

const viewStaff = (staffMember: any) => {
  // Removed console.log
}

const deleteStaff = (staffMember: any) => {
  // TODO: Implement delete staff functionality
  // Show confirmation modal and remove staff from list
}

const managePermissions = (staffMember: any) => {
  // Removed console.log
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    // Removed console.log
    modalVisible.value = false
  } catch (error) {
    // Handle validation error silently
  }
}

const handleCancel = () => {
  modalVisible.value = false
}

const exportExcel = () => {
  // Removed console.log
}

onMounted(() => {
  // Removed console.log
})
</script>

<style scoped>
.staff-management-page {
  padding: 0 20px 20px 20px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.stat-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-icon {
  font-size: 20px;
  color: #1890ff;
}

.active-icon {
  color: #52c41a;
}

.manager-icon {
  color: #faad14;
}

.salary-icon {
  color: #722ed1;
}

.stat-content {
  margin-top: 12px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1d2129;
  margin-bottom: 4px;
}

.stat-change {
  font-size: 12px;
  color: #86909c;
}

.filters-card,
.table-card {
  margin-bottom: 16px;
}

.actions-row {
  display: flex;
  justify-content: flex-end;
}

/* Responsive */
@media (max-width: 768px) {
  .staff-management-page {
    padding: 16px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .header-right {
    width: 100%;
    justify-content: center;
  }
}
</style>
