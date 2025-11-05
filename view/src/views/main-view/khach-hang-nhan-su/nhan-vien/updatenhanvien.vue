<template>
  <div class="edit-employee-page">
    <!-- Card 1: Thông tin nhân viên -->
    <a-card title="Thông tin nhân viên" :loading="loading">
      <a-form ref="formRef" :model="formData" :rules="formRules" layout="vertical">
        <!-- Hàng 1: Tên nhân viên - Ngày sinh -->
        <a-row :gutter="24">
          <a-col :span="12">
            <a-form-item name="tenNhanVien">
              <template #label>
                Tên nhân viên
                <span style="color: red">*</span>
              </template>
              <a-input v-model="formData.tenNhanVien" placeholder="Nhập tên nhân viên" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item name="ngaySinh">
              <template #label>
                Ngày sinh
                <span style="color: red">*</span>
              </template>
              <a-date-picker v-model="formData.ngaySinh" format="YYYY-MM-DD" placeholder="Chọn ngày sinh" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>

        <!-- Hàng 2: Email - Mật khẩu -->
        <a-row :gutter="24">
          <a-col :span="12">
            <a-form-item name="email">
              <template #label>
                Email
                <span style="color: red">*</span>
              </template>
              <a-input v-model="formData.email" placeholder="Nhập email" />
            </a-form-item>
          </a-col>
          <!-- <a-col :span="12">
            <a-form-item name="matKhau">
              <template #label>
                Mật khẩu
                <span style="color: red">*</span>
              </template>
              <a-input-password v-model="formData.matKhau" placeholder="Nhập mật khẩu" />
            </a-form-item>
          </a-col> -->
          <a-col :span="12">
            <a-form-item name="gioiTinh">
              <template #label>
                Giới tính
                <span style="color: red">*</span>
              </template>
              <a-radio-group v-model="formData.gioiTinh">
                <a-radio :value="true">Nam</a-radio>
                <a-radio :value="false">Nữ</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
        </a-row>

        <!-- Hàng 3: CCCD - Số điện thoại -->
        <a-row :gutter="24">
          <a-col :span="12">
            <a-form-item name="cccd">
              <template #label>
                CCCD
                <span style="color: red">*</span>
              </template>
              <a-input v-model="formData.cccd" placeholder="Nhập số CCCD" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item name="soDienThoai">
              <template #label>
                Số điện thoại
                <span style="color: red">*</span>
              </template>
              <a-input v-model="formData.soDienThoai" placeholder="Nhập số điện thoại" />
            </a-form-item>
          </a-col>
        </a-row>

        <!-- Hàng 4: Quyền hạn - Giới tính -->
        <a-row :gutter="24">
          <a-col :span="12">
            <a-form-item name="idQuyenHan">
              <template #label>
                Quyền hạn
                <span style="color: red">*</span>
              </template>
              <a-select v-model="formData.idQuyenHan" placeholder="Chọn quyền hạn">
                <a-option :value="1">Admin</a-option>
                <a-option :value="2">Nhân viên</a-option>
              </a-select>
            </a-form-item>
          </a-col>
          <!-- <a-col :span="12">
            <a-form-item name="gioiTinh">
              <template #label>
                Giới tính
                <span style="color: red">*</span>
              </template>
              <a-radio-group v-model="formData.gioiTinh">
                <a-radio :value="true">Nam</a-radio>
                <a-radio :value="false">Nữ</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col> -->
        </a-row>

        <!-- Hàng 5: Ảnh nhân viên -->
        <a-row :gutter="24">
          <a-col :span="12">
            <a-form-item label="Ảnh nhân viên" name="anhNhanVien">
              <div class="upload-container">
                <!-- Native Input File Upload - Chỉ hiển thị khi chưa có ảnh -->
                <div v-if="!previewUrl && !formData.anhNhanVien">
                  <input ref="fileInputRef" type="file" accept="image/*" @change="handleNativeFileChange" style="display: none" />
                  <a-button
                    :loading="loading"
                    type="dashed"
                    @click="() => fileInputRef?.click()"
                    style="width: 100%; height: 100px; display: flex; flex-direction: column; align-items: center; justify-content: center"
                  >
                    <template #icon>
                      <icon-upload style="font-size: 24px; margin-bottom: 8px" />
                    </template>
                    <div>Chọn ảnh nhân viên</div>
                    <div style="font-size: 12px; color: #999; margin-top: 4px">Hỗ trợ JPG, PNG. Tối đa 2MB</div>
                  </a-button>
                </div>

                <!-- Preview ảnh đã chọn hoặc ảnh hiện tại -->
                <div v-if="previewUrl || formData.anhNhanVien" class="image-preview">
                  <div class="preview-container">
                    <img
                      :src="previewUrl || formData.anhNhanVien"
                      alt="Ảnh nhân viên"
                      class="preview-image"
                      @load="console.log('✅ Ảnh đã load thành công')"
                      @error="console.error('❌ Lỗi load ảnh:', $event)"
                    />
                    <div class="image-overlay">
                      <a-button type="text" size="small" @click="removeImage" class="remove-button">
                        <template #icon>
                          <icon-close />
                        </template>
                      </a-button>
                    </div>
                  </div>
                  <div class="image-info" v-if="selectedFiles.length > 0">
                    <div class="file-details">
                      <div class="file-name">{{ selectedFiles[0].name }}</div>
                      <div class="file-size">{{ formatFileSize(selectedFiles[0].size) }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-card>

    <!-- Card 2: Địa chỉ -->
    <a-card title="Thông tin địa chỉ" style="margin-top: 16px">
      <a-form :model="formData" layout="vertical">
        <!-- Hàng 1: Tỉnh/Thành phố - Quận/Huyện -->
        <a-row :gutter="24">
          <a-col :span="12">
            <a-form-item name="thanhPho">
              <template #label>
                Tỉnh/Thành phố
                <span style="color: red">*</span>
              </template>
              <a-select v-model="formData.thanhPho" placeholder="Chọn tỉnh/thành phố" @change="onProvinceChange">
                <a-option v-for="province in provinces" :key="province.code" :value="province.value">
                  {{ province.label }}
                </a-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item name="quan">
              <template #label>
                Quận/Huyện
                <span style="color: red">*</span>
              </template>
              <a-select v-model="formData.quan" placeholder="Chọn quận/huyện" @change="onDistrictChange">
                <a-option v-for="district in districts" :key="district.code" :value="district.value">
                  {{ district.label }}
                </a-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <!-- Hàng 2: Phường/Xã - Địa chỉ cụ thể -->
        <a-row :gutter="24">
          <a-col :span="12">
            <a-form-item name="phuong">
              <template #label>
                Phường/Xã
                <span style="color: red">*</span>
              </template>
              <a-select v-model="formData.phuong" placeholder="Chọn phường/xã">
                <a-option v-for="ward in wards" :key="ward.value" :value="ward.value">
                  {{ ward.label }}
                </a-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item name="diaChiCuThe">
              <template #label>
                Địa chỉ cụ thể
                <span style="color: red">*</span>
              </template>
              <a-input v-model="formData.diaChiCuThe" placeholder="Nhập địa chỉ cụ thể" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-card>

    <!-- Card 3: Actions -->
    <a-card style="margin-top: 16px">
      <a-form-item>
        <a-space>
          <a-button type="primary" :loading="loading" @click="showConfirm">
            <template #icon>
              <icon-save />
            </template>
            Lưu thay đổi
          </a-button>
          <a-button @click="handleCancel">
            <template #icon>
              <icon-close />
            </template>
            Hủy
          </a-button>
        </a-space>
      </a-form-item>
    </a-card>

    <!-- Update Employee Confirm Modal -->
    <a-modal
      v-model:visible="showUpdateConfirm"
      title="Xác nhận cập nhật nhân viên"
      ok-text="Xác nhận"
      cancel-text="Huỷ"
      @ok="confirmUpdateEmployee"
      @cancel="cancelUpdateEmployee"
    >
      <template #default>
        <div>
          <div>Bạn có chắc chắn muốn cập nhật thông tin nhân viên này?</div>
          <div>
            Tên nhân viên:
            <strong>{{ formData.tenNhanVien }}</strong>
          </div>
          <div>
            Email:
            <strong>{{ formData.email }}</strong>
          </div>
          <div>
            Số điện thoại:
            <strong>{{ formData.soDienThoai }}</strong>
          </div>
          <div>
            Quyền hạn:
            <strong>{{ formData.idQuyenHan === 1 ? 'Admin' : 'Nhân viên' }}</strong>
          </div>
        </div>
      </template>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { layChiTietNhanVien, capNhatNhanVien, type NhanVienRequest } from '@/api/nhan-vien'
import { Modal, Message } from '@arco-design/web-vue'
import { IconUpload, IconClose, IconSave } from '@arco-design/web-vue/es/icon'
import { useUserStore } from '@/store'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const formRef = ref(null)
const selectedFiles = ref<File[]>([])
const previewUrl = ref<string>('')
const fileInputRef = ref<HTMLInputElement>()
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

const formatFileSize = (bytes: number) => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return `${parseFloat((bytes / k ** i).toFixed(2))} ${sizes[i]}`
}

const handleNativeFileChange = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]

  if (!file) {
    return
  }

  // Validate file type
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png'
  if (!isJpgOrPng) {
    Message.error('Chỉ hỗ trợ file JPG/PNG!')
    target.value = ''
    return
  }

  // Validate file size (2MB)
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    Message.error('Ảnh phải nhỏ hơn 2MB!')
    target.value = ''
    return
  }

  // Store file
  selectedFiles.value = [file]

  try {
    // Create preview using FileReader
    const reader = new FileReader()

    reader.onload = async (e) => {
      const result = e.target?.result as string
      previewUrl.value = result
      await nextTick()
    }

    reader.onerror = (error) => {
      console.error('❌ FileReader error:', error)
      Message.error('Không thể đọc file ảnh!')
      selectedFiles.value = []
      previewUrl.value = ''
      target.value = ''
    }

    reader.readAsDataURL(file)
  } catch (error) {
    console.error('❌ Lỗi khi xử lý file:', error)
    Message.error('Lỗi khi xử lý file ảnh!')
    selectedFiles.value = []
    previewUrl.value = ''
    target.value = ''
  }
}

const changeImage = () => {
  // Trigger file input để chọn ảnh mới
  if (fileInputRef.value) {
    fileInputRef.value.click()
  }
}

const removeImage = () => {
  selectedFiles.value = []
  previewUrl.value = ''
  formData.value.anhNhanVien = null
}

// load dữ liệu nhân viên
onMounted(async () => {
  try {
    console.log('🔍 Loading nhân viên với ID:', id)
    const res = await layChiTietNhanVien(id)
    console.log('📦 Response từ API:', res)
    console.log('👤 Data nhân viên:', res.data)
    console.log('🖼️ Ảnh nhân viên từ API:', res.data.anhNhanVien)

    formData.value = res.data

    console.log('✅ FormData sau khi set:', formData.value)
    console.log('🖼️ Ảnh trong formData:', formData.value.anhNhanVien)
  } catch (error) {
    console.error('❌ Lỗi load nhân viên:', error)
    Message.error('Không thể tải dữ liệu nhân viên')
  }
})

const handleSubmit = async () => {
  try {
    loading.value = true
    await formRef.value.validate()

    // Validate tất cả các trường
    if (!formData.value.tenNhanVien) {
      Message.error('Vui lòng nhập tên nhân viên.')
      return
    }
    if (!formData.value.ngaySinh) {
      Message.error('Vui lòng chọn ngày sinh.')
      return
    }
    if (!formData.value.cccd) {
      Message.error('Vui lòng nhập CCCD.')
      return
    }
    if (!/^\d{9,12}$/.test(formData.value.cccd)) {
      Message.error('CCCD phải là 9-12 chữ số.')
      return
    }
    if (!formData.value.soDienThoai) {
      Message.error('Vui lòng nhập số điện thoại.')
      return
    }
    if (!/^(0|\+84)[0-9]{9}$/.test(formData.value.soDienThoai)) {
      Message.error('Số điện thoại không hợp lệ.')
      return
    }
    if (!formData.value.email || !formData.value.email.includes('@')) {
      Message.error('Email không hợp lệ để tạo tài khoản.')
      return
    }
    if (!formData.value.idQuyenHan) {
      Message.error('Vui lòng chọn quyền hạn.')
      return
    }
    if (!formData.value.thanhPho) {
      Message.error('Vui lòng chọn tỉnh/thành phố.')
      return
    }
    if (!formData.value.quan) {
      Message.error('Vui lòng chọn quận/huyện.')
      return
    }
    if (!formData.value.phuong) {
      Message.error('Vui lòng chọn phường/xã.')
      return
    }
    if (!formData.value.diaChiCuThe) {
      Message.error('Vui lòng nhập địa chỉ cụ thể.')
      return
    }

    if (formData.value.id) {
      // ✅ Tự động tạo tên tài khoản từ email
      const emailUsername = formData.value.email.split('@')[0]
      formData.value.tenTaiKhoan = emailUsername

      // ✅ Tạo FormData để gửi file giống như component thêm
      const submitFormData = new FormData()

      console.log('🔄 Updating:', formData.value.tenNhanVien, '- Files:', selectedFiles.value.length)

      // Thêm thông tin nhân viên
      Object.keys(formData.value).forEach((key) => {
        const value = formData.value[key as keyof typeof formData.value]
        if (value !== null && value !== undefined && key !== 'id') {
          submitFormData.append(key, value.toString())
        }
      })

      // Thêm author (ID của user hiện tại)
      if (userStore.id) {
        submitFormData.append('createBy', userStore.id.toString())
      }

      // Thêm deleted và createAt
      submitFormData.append('deleted', 'false')
      submitFormData.append('createAt', new Date().toISOString().split('T')[0])

      // Thêm file ảnh nếu có
      if (selectedFiles.value.length > 0) {
        selectedFiles.value.forEach((file) => {
          submitFormData.append('file', file)
        })
      }

      // ✅ Gửi request với FormData giống như component thêm
      await capNhatNhanVien(formData.value.id, submitFormData)

      Message.success('Cập nhật nhân viên thành công!')
      router.push({ name: 'QuanLyNhanVien' }) // ✅ SPA routing với route name
    } else {
      Message.error('Không tìm thấy ID nhân viên')
    }
  } catch (error) {
    Message.error('Cập nhật nhân viên thất bại. Vui lòng kiểm tra lại thông tin.')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// Modal confirm state
const showUpdateConfirm = ref(false)

// Show confirm modal for update employee
const showConfirm = () => {
  showUpdateConfirm.value = true
}

const confirmUpdateEmployee = async () => {
  await handleSubmit()
  showUpdateConfirm.value = false
}

const cancelUpdateEmployee = () => {
  showUpdateConfirm.value = false
}

const handleCancel = () => {
  router.push({ name: 'QuanLyNhanVien' }) // ✅ SPA routing với route name
}
</script>

<style scoped>
.edit-employee-page {
  padding: 16px 20px;
}

.upload-container {
  width: 100%;
}

.image-preview {
  position: relative;
  display: inline-block;
}

.preview-container {
  position: relative;
  display: inline-block;
}

.preview-image {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  object-fit: cover;
  border: 1px solid #d9d9d9;
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
  border-radius: 8px;
}

.preview-container:hover .image-overlay {
  opacity: 1;
}

.remove-button {
  background: rgba(255, 255, 255, 0.9);
  border: none;
  border-radius: 50%;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.remove-button:hover {
  background: #ff4d4f;
  color: white;
}

.image-info {
  margin-top: 8px;
}

.file-details {
  font-size: 12px;
  color: #666;
}

.file-name {
  font-weight: 500;
  margin-bottom: 2px;
}

.file-size {
  color: #999;
}
</style>
