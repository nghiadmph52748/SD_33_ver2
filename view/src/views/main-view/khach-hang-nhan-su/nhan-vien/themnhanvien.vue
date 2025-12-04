<template>
  <div class="add-employee-page">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />
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
          <a-col :span="12">
            <a-form-item name="cccd">
              <template #label>
                CCCD
                <span style="color: red">*</span>
              </template>

             <div style="display: flex; gap: 8px; align-items: center">
                <!-- CCCD: only accept via QR scan or image upload. Manual typing disabled. -->
                <a-input v-model="formData.cccd" placeholder="Vui lòng quét QR hoặc tải ảnh CCCD (không cho phép nhập tay)" style="flex: 1" readonly @keydown.prevent />

                <!-- Nút quét CCCD -->
                <a-button type="outline" @click="openQRModal">
                  <template #icon>
                    <icon-scan />
                  </template>
                  Quét QR
                </a-button>

                <!-- Nút tải ảnh -->
                <a-button type="outline" @click="() => cccdFileInputRef?.click()">
                  <template #icon>
                    <icon-upload />
                  </template>
                  Tải ảnh
                </a-button>

                <input ref="cccdFileInputRef" type="file" accept="image/*" style="display: none" @change="handleCCCDImageUpload" />
              </div>
            </a-form-item>
          </a-col>
        </a-row>

        <!-- Hàng 3: Số điện thoại - CCCD -->
        <a-row :gutter="24">
          <a-col :span="12">
            <a-form-item name="soDienThoai">
              <template #label>
                Số điện thoại
                <span style="color: red">*</span>
              </template>
              <a-input v-model="formData.soDienThoai" placeholder="Nhập số điện thoại" />
            </a-form-item>
          </a-col>

          <a-col :span="12">
            <a-form-item name="gioiTinh">
              <template #label>
                Giới tính
                <span style="color: red">*</span>
              </template>
              <a-radio-group v-model="formData.gioiTinh" type="button">
                <a-radio :value="true">Nam</a-radio>
                <a-radio :value="false">Nữ</a-radio>
              </a-radio-group>
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
              <a-select
                v-model="formData.idQuyenHan"
                placeholder="-- Chọn quyền hạn --"
                :options="[
                  { value: 1, label: 'Admin' },
                  { value: 2, label: 'Nhân Viên' },
                ]"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <!-- Hàng 5: Ảnh nhân viên -->
        <a-row :gutter="24">
          <a-col :span="12">
            <a-form-item label="Ảnh nhân viên" name="anhNhanVien">
              <div class="upload-container">
                <!-- Native Input File Upload - Chỉ hiển thị khi chưa có ảnh -->
                <div v-if="!previewUrl">
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

                <!-- Preview ảnh đã chọn -->
                <div v-if="previewUrl" class="image-preview">
                  <div class="preview-container">
                    <img :src="previewUrl" alt="Ảnh nhân viên" class="preview-image" />
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

                <!-- Fallback nếu không có preview -->
                <div
                  v-else-if="selectedFiles.length > 0"
                  style="margin-top: 16px; padding: 16px; border: 1px dashed #ccc; text-align: center; color: #666"
                >
                  <div>⚠️ Không thể hiển thị preview</div>
                  <div style="font-size: 12px; margin-top: 4px">File: {{ selectedFiles[0].name }}</div>
                </div>
              </div>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-card>

    <!-- Card 2: Thông tin địa chỉ -->
    <a-card title="Thông tin địa chỉ" style="margin-bottom: 16px">
      <a-form :model="formData" layout="vertical">
        <!-- Hàng 1: Thành phố và Quận/Huyện -->
        <a-row :gutter="24">
          <a-col :span="12">
            <a-form-item label="Thành phố" name="thanhPho">
              <a-select
                v-model="formData.thanhPho"
                placeholder="-- Chọn tỉnh/thành phố --"
                :options="provinces"
                allow-search
                allow-clear
                @change="onProvinceChange"
                option-label-prop="name"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="Quận/Huyện" name="quan">
              <a-select
                v-model="formData.quan"
                placeholder="-- Chọn quận/huyện --"
                :options="districts"
                allow-search
                allow-clear
                @change="onDistrictChange"
                option-label-prop="name"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <!-- Hàng 2: Phường/Xã và Địa chỉ cụ thể -->
        <a-row :gutter="24">
          <a-col :span="12">
            <a-form-item label="Phường/Xã" name="phuong">
              <a-select
                v-model="formData.phuong"
                placeholder="-- Chọn phường/xã --"
                :options="wards"
                allow-search
                allow-clear
                option-label-prop="name"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="Địa chỉ cụ thể" name="diaChiCuThe">
              <a-input v-model="formData.diaChiCuThe" placeholder="Nhập địa chỉ cụ thể (số nhà, đường...)" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-card>

    <!-- Card 3: Thao tác -->
    <a-card title="Thao tác">
      <a-row justify="center">
        <a-col>
          <a-space>
            <a-button @click="handleCancel">
              <template #icon>
                <icon-close />
              </template>
              Hủy
            </a-button>
            <a-button type="primary" :loading="loading" @click="showConfirm">
              <template #icon>
                <icon-save />
              </template>
              Lưu
            </a-button>
          </a-space>
        </a-col>
      </a-row>
    </a-card>

    <!-- Save Employee Confirm Modal -->
    <a-modal
      v-model:visible="showSaveConfirm"
      title="Xác nhận thêm nhân viên"
      ok-text="Xác nhận"
      cancel-text="Huỷ"
      @ok="confirmSaveEmployee"
      @cancel="cancelSaveEmployee"
    >
      <template #default>
        <div>
          <div>Bạn có chắc chắn muốn thêm nhân viên này?</div>
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
    <!-- Modal quét QR -->
    <a-modal
      v-model:visible="showQRModal"
      title="Quét mã QR CCCD"
      ok-text="Đóng"
      cancel-text="Hủy"
      hide-cancel
      width="400px"
      @ok="closeQRModal"
      @cancel="closeQRModal"
    >
      <div style="text-align: center">
        <video ref="videoRef" autoplay playsinline style="width: 100%; border-radius: 8px" />
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import { themNhanVien, type NhanVienRequest } from '@/api/nhan-vien'
import { Message, Modal } from '@arco-design/web-vue'
import { IconUpload, IconClose, IconSave, IconScan } from '@arco-design/web-vue/es/icon'
import QrScanner, { ScanResult } from 'qr-scanner'

const showQRModal = ref(false)
const videoRef = ref<HTMLVideoElement | null>(null)
const qrScanner = ref<QrScanner | null>(null)
const cccdFileInputRef = ref<HTMLInputElement | null>(null)

// Router
const router = useRouter()
const { breadcrumbItems } = useBreadcrumb()

const provinces = ref<{ value: string; label: string; code: number }[]>([])
const districts = ref<{ value: string; label: string; code: number }[]>([])
const wards = ref<{ value: string; label: string }[]>([])

// Form data
const formRef = ref()
const selectedFiles = ref<File[]>([])
const previewUrl = ref<string>('')
const fileInputRef = ref<HTMLInputElement>()
const loading = ref(false)

const formData = ref({
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
})

// Validation rules
const formRules = {
  tenNhanVien: [{ required: true, message: 'Vui lòng nhập tên nhân viên' }],
  ngaySinh: [{ required: true, message: 'Vui lòng chọn ngày sinh' }],
  cccd: [
    { required: true, message: 'Vui lòng quét CCCD (QR hoặc ảnh)' },
  ],
  soDienThoai: [
    { required: true, message: 'Vui lòng nhập số điện thoại' },
    { pattern: /^(0|\+84)[0-9]{9}$/, message: 'Số điện thoại không hợp lệ' },
  ],
  email: [
    { required: true, message: 'Vui lòng nhập email' },
    { type: 'email', message: 'Email không hợp lệ' },
  ],
  matKhau: [
    { required: true, message: 'Vui lòng nhập mật khẩu' },
    { minLength: 6, message: 'Mật khẩu phải có ít nhất 6 ký tự' },
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

const closeQRModal = () => {
  if (qrScanner.value) {
    qrScanner.value.stop()
    qrScanner.value.destroy()
    qrScanner.value = null
  }
  showQRModal.value = false
}

const openQRModal = async () => {
  showQRModal.value = true
  await nextTick()

  if (!videoRef.value) return

  qrScanner.value = new QrScanner(
    videoRef.value,
    (result: ScanResult) => {
      const raw = result.data.trim()

      // Nếu là loại chứa dấu |
      if (raw.includes('|')) {
        const parts = raw.split('|')
        if (parts.length > 5) {
          const cccd = parts[0]
          const ten = parts[2]
          const dob = parts[3]
          const gioiTinh = parts[4]
          const diaChi = parts[5]

          formData.value.cccd = cccd || ''
          formData.value.tenNhanVien = ten || ''
          formData.value.ngaySinh = dob ? `${dob.slice(4, 8)}-${dob.slice(2, 4)}-${dob.slice(0, 2)}` : ''
          formData.value.gioiTinh = gioiTinh?.toLowerCase().includes('nam')

          const addressParts = diaChi.split(',').map((x) => x.trim())
          formData.value.diaChiCuThe = addressParts[0] || ''
          formData.value.phuong = addressParts[1] || ''
          formData.value.quan = addressParts[2] || ''
          formData.value.thanhPho = addressParts[3] || ''

          Message.success('✅ Đã tự động điền thông tin từ CCCD!')
        } else {
          Message.warning('Không thể đọc đầy đủ thông tin từ mã CCCD!')
        }
      }
      // Nếu là loại mã hóa JWT
      else if (raw.split('.').length === 3) {
        formData.value.cccd = raw
        Message.info('📦 Mã CCCD mới (JWT) – không thể giải mã thông tin chi tiết!')
      } else {
        Message.warning('Mã QR không đúng định dạng CCCD!')
      }

      closeQRModal()
    },
    { highlightScanRegion: true, returnDetailedScanResult: true }
  )

  await qrScanner.value.start()
}

// Xử lý khi người dùng tải ảnh CCCD
const handleCCCDImageUpload = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  try {
    const result = await QrScanner.scanImage(file)
    if (result) {
      const raw = result.trim()
      const parts = raw.split('|')
      if (parts.length > 5) {
        const cccd = parts[0]
        const ten = parts[2]
        const dob = parts[3]
        const gioiTinh = parts[4]
        const diaChi = parts[5]

        formData.value.cccd = cccd || ''
        formData.value.tenNhanVien = ten || ''
        formData.value.ngaySinh = dob ? `${dob.slice(4, 8)}-${dob.slice(2, 4)}-${dob.slice(0, 2)}` : ''
        formData.value.gioiTinh = gioiTinh?.toLowerCase().includes('nam')

        const addressParts = diaChi.split(',').map((x) => x.trim())
        formData.value.diaChiCuThe = addressParts[0] || ''
        formData.value.phuong = addressParts[1] || ''
        formData.value.quan = addressParts[2] || ''
        formData.value.thanhPho = addressParts[3] || ''

        Message.success('✅ Đã tự động điền thông tin từ ảnh CCCD!')
      } else {
        Message.warning('Không thể đọc được đầy đủ thông tin từ ảnh CCCD!')
      }
    }
  } catch (err) {
    Message.warning('Không phát hiện được mã QR trong ảnh này.')
  } finally {
    target.value = ''
  }
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
      Message.error('Không thể đọc file ảnh!')
      selectedFiles.value = []
      previewUrl.value = ''
      target.value = ''
    }

    reader.readAsDataURL(file)
  } catch (error) {
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

  // Reset native input
  if (fileInputRef.value) {
    fileInputRef.value.value = ''
  }
}

const formatFileSize = (bytes: number) => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return `${parseFloat((bytes / k ** i).toFixed(2))} ${sizes[i]}`
}

// Handle submit
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
    if (!formData.value.cccd) {
      Message.error('Vui lòng quét CCCD bằng QR hoặc tải ảnh CCCD.')
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

    // ✅ Tự động tạo tên tài khoản từ email
    const emailUsername = formData.value.email.split('@')[0]
    formData.value.tenTaiKhoan = emailUsername

    // ✅ Upload ảnh trước nếu có
    let imageUrl: string | null = null
    if (selectedFiles.value.length > 0) {
      const uploadFormData = new FormData()
      uploadFormData.append('file', selectedFiles.value[0])
      const uploadResponse = await (await import('axios')).default.post('/api/v1/upload-image/add', uploadFormData)

      // Try multiple ways to access the URL
      let extractedUrl: string | null = null

      // Method 1: If response.data is directly an array (Axios unwrapped ResponseObject)
      if (Array.isArray(uploadResponse.data) && uploadResponse.data.length > 0) {
        const firstItem = uploadResponse.data[0]
        if (firstItem && typeof firstItem === 'object' && 'url' in firstItem) {
          extractedUrl = firstItem.url as string
        }
      }

      // Method 2: ResponseObject structure - data.data[0].url
      if (!extractedUrl && uploadResponse.data?.data && Array.isArray(uploadResponse.data.data) && uploadResponse.data.data.length > 0) {
        const firstItem = uploadResponse.data.data[0]
        if (firstItem && typeof firstItem === 'object' && 'url' in firstItem) {
          extractedUrl = firstItem.url as string
        }
      }

      // Method 3: Direct data.data.url (if it's an object, not array)
      if (
        !extractedUrl &&
        uploadResponse.data?.data &&
        typeof uploadResponse.data.data === 'object' &&
        !Array.isArray(uploadResponse.data.data) &&
        'url' in uploadResponse.data.data
      ) {
        extractedUrl = (uploadResponse.data.data as any).url
      }

      // Method 4: Top-level data.url
      if (!extractedUrl && uploadResponse.data?.url) {
        extractedUrl = uploadResponse.data.url as string
      }

      // Method 5: Direct response.data if it's a string URL
      if (!extractedUrl && typeof uploadResponse.data === 'string') {
        extractedUrl = uploadResponse.data
      }

      if (extractedUrl) {
        imageUrl = extractedUrl
      } else {
        Message.error('Không thể lấy URL ảnh từ phản hồi upload. Vui lòng thử lại.')
        return
      }
    }

    // ✅ Validate imageUrl if file was uploaded
    if (selectedFiles.value.length > 0 && !imageUrl) {
      Message.error('Không thể lấy URL ảnh sau khi upload. Vui lòng thử lại.')
      return
    }

    // ✅ Tạo payload JSON với URL ảnh
    const payload: NhanVienRequest = {
      tenNhanVien: formData.value.tenNhanVien,
      tenTaiKhoan: formData.value.tenTaiKhoan,
      matKhau: formData.value.matKhau,
      ngaySinh: formData.value.ngaySinh,
      cccd: formData.value.cccd,
      email: formData.value.email,
      soDienThoai: formData.value.soDienThoai,
      thanhPho: formData.value.thanhPho,
      quan: formData.value.quan,
      phuong: formData.value.phuong,
      gioiTinh: formData.value.gioiTinh,
      diaChiCuThe: formData.value.diaChiCuThe,
      idQuyenHan: formData.value.idQuyenHan as number,
      trangThai: formData.value.trangThai,
      anhNhanVien: imageUrl,
      deleted: false,
    }

    // ✅ Gửi request JSON (không multipart)
    const response = await themNhanVien(payload)
    const result = await response.data

    Message.success('Thêm nhân viên thành công!')
    router.push({ name: 'QuanLyNhanVien' }) // ✅ SPA routing với route name
  } catch (error: unknown) {
    const err = error as any
    if (err.message?.includes('tài khoản đã tồn tại')) {
      Message.error('Email này đã được dùng để tạo tài khoản. Vui lòng dùng email khác.')
    } else if (err.message?.includes('HTTP 413')) {
      Message.error('File ảnh quá lớn. Vui lòng chọn ảnh nhỏ hơn.')
    } else if (err.message?.includes('HTTP 415')) {
      Message.error('Định dạng file không được hỗ trợ.')
    } else if (err.message?.includes('HTTP 400')) {
      Message.error('Dữ liệu không hợp lệ. Vui lòng kiểm tra lại thông tin.')
    } else if (err.message?.includes('HTTP 500')) {
      Message.error('Lỗi server. Vui lòng thử lại sau.')
    } else {
      Message.error('Thêm nhân viên thất bại. Vui lòng thử lại.')
    }
  } finally {
    loading.value = false
  }
}
// Modal confirm state
const showSaveConfirm = ref(false)

// Show confirm modal for save employee
const showConfirm = () => {
  showSaveConfirm.value = true
}

const confirmSaveEmployee = async () => {
  await handleSubmit()
  showSaveConfirm.value = false
}

const cancelSaveEmployee = () => {
  showSaveConfirm.value = false
}

// Handle cancel
const handleCancel = () => {
  router.push({ name: 'QuanLyNhanVien' }) // ✅ SPA routing với route name
}
</script>

<style scoped>
.add-employee-page {
  padding: 0 20px 20px 20px;
}

.mt-2 {
  margin-top: 8px;
}

/* Upload Container */
.upload-container {
  position: relative;
}

/* Image Preview */
.image-preview {
  margin-top: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
  background: #f9fafb;
}

.preview-container {
  position: relative;
  width: 100%;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
}

.preview-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  border-radius: 4px;
}

.image-overlay {
  position: absolute;
  top: 8px;
  right: 8px;
  opacity: 0;
  transition: opacity 0.2s;
}

.preview-container:hover .image-overlay {
  opacity: 1;
}

.remove-button {
  background: rgba(0, 0, 0, 0.6);
  color: white;
  border-radius: 50%;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
}

.remove-button:hover {
  background: #ff4d4f;
}

/* Image Info */
.image-info {
  padding: 12px 16px;
  border-top: 1px solid #e5e7eb;
  background: #f9fafb;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.file-details {
  flex: 1;
}

.file-name {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 4px;
  word-break: break-all;
}

.file-size {
  font-size: 12px;
  color: #6b7280;
}

.image-actions {
  display: flex;
  align-items: center;
}

/* Native Upload Button Styles */
.upload-container .arco-btn-dashed {
  border-style: dashed;
  border-width: 2px;
  transition: all 0.3s;
}

.upload-container .arco-btn-dashed:hover {
  border-color: #165dff;
  color: #165dff;
}
.qr-container {
  position: relative;
  width: 100%;
  height: 100vh;
  background: rgba(0, 0, 0, 0.8); /* nền mờ xung quanh */
  display: flex;
  justify-content: center;
  align-items: center;
}

.qrcode-stream {
  width: 300px;
  height: 300px;
  border: 3px solid #00ff99;
  border-radius: 10px;
  overflow: hidden;
}
</style>
