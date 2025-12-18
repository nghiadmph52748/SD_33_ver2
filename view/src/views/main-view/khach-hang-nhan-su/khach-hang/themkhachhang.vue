<template>
  <div class="add-customer-page">
    <!-- Card 1: Thông tin khách hàng -->
    <a-card title="Thông tin khách hàng" style="margin-bottom: 16px">
      <a-form ref="formRef" :model="formData" :rules="formRules" layout="vertical">
        <a-row :gutter="24">
          <a-col :span="12">
            <a-form-item name="tenKhachHang">
              <template #label>
                Tên khách hàng
                <span style="color: red">*</span>
              </template>
              <a-input v-model="formData.tenKhachHang" placeholder="Nhập tên khách hàng" />
            </a-form-item>
            <a-form-item name="email">
              <template #label>
                Email
                <span style="color: red">*</span>
              </template>
              <a-input v-model="formData.email" placeholder="Nhập email" />
            </a-form-item>
            <a-form-item name="matKhau">
              <template #label>
                Mật khẩu
                <span style="color: red">*</span>
              </template>
              <a-input-password v-model="formData.matKhau" placeholder="Nhập mật khẩu" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="Ngày sinh" name="ngaySinh">
              <a-date-picker v-model="formData.ngaySinh" format="YYYY-MM-DD" placeholder="Chọn ngày sinh"
                style="width: 100%" :disabled-date="(current: Date) => current && current > new Date()" />
            </a-form-item>
            <a-form-item name="soDienThoai">
              <template #label>
                Số điện thoại
                <span style="color: red">*</span>
              </template>
              <a-input v-model="formData.soDienThoai" placeholder="Nhập số điện thoại" />
            </a-form-item>
            <a-form-item name="gioiTinh">
              <template #label>
                Giới tính
                <span style="color: red">*</span>
              </template>
              <a-radio-group v-model="formData.gioiTinh" type="button">
                <a-radio value="Nam">Nam</a-radio>
                <a-radio value="Nữ">Nữ</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-card>

    <!-- Card 2: Thông tin địa chỉ -->
    <a-card title="Thông tin địa chỉ" style="margin-bottom: 16px">
      <template #extra>
        <a-button type="primary" size="small" @click="themFormDiaChi">
          <template #icon>
            <icon-plus />
          </template>
          Thêm địa chỉ
        </a-button>
      </template>
      <!-- Hiển thị các form địa chỉ -->
      <div v-for="(diaChi, index) in danhSachFormDiaChi" :key="index" style="margin-bottom: 24px">
        <a-card size="small" :title="`${diaChi.tenDiaChi}`">
          <template #extra>
            <a-space size="small">
              <a-button v-if="danhSachFormDiaChi.length > 1 && !diaChi.macDinh" @click="setDefaultAddress(index)"
                type="text" size="small">
                <template #icon>
                  <icon-star />
                </template>
                Đặt mặc định
              </a-button>
              <a-tag v-if="diaChi.macDinh" color="green" size="small">Mặc định</a-tag>
              <a-button @click="xoaFormDiaChi(index)" type="text" status="danger" size="small">
                <template #icon>
                  <icon-delete />
                </template>
                Xóa
              </a-button>
            </a-space>
          </template>

          <a-form layout="vertical">
            <a-row :gutter="16">
              <a-col :span="6">
                <a-form-item>
                  <template #label>
                    Tên địa chỉ
                    <span style="color: red">*</span>
                  </template>
                  <a-input v-model="diaChi.tenDiaChi" placeholder="Nhập tên địa chỉ" />
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item label="Thành phố">
                  <a-select v-model="diaChi.thanhPho" placeholder="-- Chọn tỉnh/thành phố --" :options="provinces"
                    @change="onProvinceChange(index, $event)" option-label-prop="label" allow-search allow-clear />
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item label="Quận">
                  <a-select v-model="diaChi.quan" placeholder="-- Chọn quận/huyện --" :options="diaChi.districts"
                    @change="onDistrictChange(index, $event)" option-label-prop="label" allow-search allow-clear />
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item label="Phường">
                  <a-select v-model="diaChi.phuong" placeholder="-- Chọn phường/xã --" :options="diaChi.wards"
                    option-label-prop="label" allow-search allow-clear />
                </a-form-item>
              </a-col>
            </a-row>
            <a-row :gutter="16">
              <a-col :span="24">
                <a-form-item label="Địa chỉ cụ thể">
                  <a-input v-model="diaChi.diaChiCuThe" placeholder="Nhập địa chỉ cụ thể (số nhà, đường...)" />
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </a-card>
      </div>

      <!-- Thông báo khi chưa có địa chỉ -->
      <a-empty v-if="danhSachFormDiaChi.length === 0"
        description="Chưa có địa chỉ nào. Nhấn 'Thêm địa chỉ' để bắt đầu." />
    </a-card>

    <!-- Card 3: Thao tác -->
    <a-card>
      <a-form-item style="margin-bottom: 0">
        <a-button type="primary" @click="showConfirm" size="large">
          <template #icon>
            <icon-save />
          </template>
          Lưu khách hàng
        </a-button>
        <a-button style="margin-left: 8px" @click="handleCancel" size="large">
          <template #icon>
            <icon-close />
          </template>
          Hủy
        </a-button>
      </a-form-item>
    </a-card>

    <!-- Delete Address Confirm Modal -->
    <a-modal v-model:visible="showDeleteConfirm" title="Xác nhận xóa địa chỉ" ok-text="Xác nhận" cancel-text="Huỷ"
      @ok="confirmDeleteAddress" @cancel="cancelDeleteAddress">
      <template #default>
        <div v-if="addressToDelete !== null">
          <div>Bạn có chắc chắn muốn xóa địa chỉ này?</div>
          <div>
            Tên địa chỉ:
            <strong>{{ danhSachFormDiaChi[addressToDelete]?.tenDiaChi }}</strong>
          </div>
          <div>
            Địa chỉ:
            <strong>
              {{ danhSachFormDiaChi[addressToDelete]?.diaChiCuThe }}, {{ danhSachFormDiaChi[addressToDelete]?.phuong }},
              {{ danhSachFormDiaChi[addressToDelete]?.quan }}, {{ danhSachFormDiaChi[addressToDelete]?.thanhPho }}
            </strong>
          </div>
        </div>
      </template>
    </a-modal>

    <!-- Set Default Address Confirm Modal -->
    <a-modal v-model:visible="showDefaultConfirm" title="Xác nhận đặt địa chỉ mặc định" ok-text="Xác nhận"
      cancel-text="Huỷ" @ok="confirmSetDefaultAddress" @cancel="cancelSetDefaultAddress">
      <template #default>
        <div v-if="addressToSetDefault !== null">
          <div>Bạn có chắc chắn muốn đặt địa chỉ này làm mặc định?</div>
          <div>
            Tên địa chỉ:
            <strong>{{ danhSachFormDiaChi[addressToSetDefault]?.tenDiaChi }}</strong>
          </div>
          <div>
            Địa chỉ:
            <strong>
              {{ danhSachFormDiaChi[addressToSetDefault]?.diaChiCuThe }}, {{
                danhSachFormDiaChi[addressToSetDefault]?.phuong }},
              {{ danhSachFormDiaChi[addressToSetDefault]?.quan }}, {{ danhSachFormDiaChi[addressToSetDefault]?.thanhPho
              }}
            </strong>
          </div>
        </div>
      </template>
    </a-modal>

    <!-- Save Customer Confirm Modal -->
    <a-modal v-model:visible="showSaveConfirm" title="Xác nhận thêm khách hàng" ok-text="Xác nhận" cancel-text="Huỷ"
      @ok="confirmSaveCustomer" @cancel="cancelSaveCustomer">
      <template #default>
        <div>
          <div>Bạn có chắc chắn muốn thêm khách hàng này?</div>
          <div>
            Tên khách hàng:
            <strong>{{ formData.tenKhachHang }}</strong>
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
            Số địa chỉ:
            <strong>{{ danhSachFormDiaChi.length }} địa chỉ</strong>
          </div>
        </div>
      </template>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { themKhachHang, type KhachHangRequest } from '@/api/khach-hang'
import { Message, Modal } from '@arco-design/web-vue'
import { IconDelete, IconSave, IconClose, IconStar, IconPlus } from '@arco-design/web-vue/es/icon'

const router = useRouter()

const formRef = ref()

const provinces = ref<{ value: string; label: string; code: string }[]>([])

const formData = ref({
  tenKhachHang: '',
  email: '',
  matKhau: '',
  soDienThoai: '',
  ngaySinh: '',
  phanLoai: '',
  gioiTinh: '',
})

// Danh sách form địa chỉ động
const danhSachFormDiaChi = ref<
  {
    tenDiaChi: string
    thanhPho: string
    quan: string
    phuong: string
    diaChiCuThe: string
    macDinh: boolean
    districts: { value: string; label: string; code: string }[]
    wards: { value: string; label: string }[]
  }[]
>([])

// Confirm modals state
const showDeleteConfirm = ref(false)
const addressToDelete = ref<number | null>(null)

const showDefaultConfirm = ref(false)
const addressToSetDefault = ref<number | null>(null)

const showSaveConfirm = ref(false)

// Thêm form địa chỉ mới
const themFormDiaChi = () => {
  const soDiaChi = danhSachFormDiaChi.value.length + 1
  const diaChiMoi = {
    tenDiaChi: `Địa chỉ ${soDiaChi}`,
    thanhPho: '',
    quan: '',
    phuong: '',
    diaChiCuThe: '',
    macDinh: danhSachFormDiaChi.value.length === 0, // Địa chỉ đầu tiên là mặc định
    districts: [],
    wards: [],
  }
  danhSachFormDiaChi.value.push(diaChiMoi)
}

// Show confirm modal for delete address
const xoaFormDiaChi = (index: number) => {
  addressToDelete.value = index
  showDeleteConfirm.value = true
}

// Actual delete address implementation
const performDeleteAddress = (index: number) => {
  const diaChiXoa = danhSachFormDiaChi.value[index]
  danhSachFormDiaChi.value.splice(index, 1)

  // Nếu xóa địa chỉ mặc định và còn địa chỉ khác, đặt địa chỉ đầu tiên làm mặc định
  if (diaChiXoa.macDinh && danhSachFormDiaChi.value.length > 0) {
    danhSachFormDiaChi.value[0].macDinh = true
  }
}

const confirmDeleteAddress = () => {
  if (addressToDelete.value !== null) {
    performDeleteAddress(addressToDelete.value)
  }
  showDeleteConfirm.value = false
  addressToDelete.value = null
}

const cancelDeleteAddress = () => {
  showDeleteConfirm.value = false
  addressToDelete.value = null
}

const formRules = {
  tenKhachHang: [{ required: true, message: 'Vui lòng nhập tên khách hàng', trigger: 'blur' }],
  email: [
    { required: true, message: 'Vui lòng nhập email', trigger: 'blur' },
    { type: 'email', message: 'Email không hợp lệ', trigger: ['blur', 'change'] },
  ],
  matKhau: [{ required: true, message: 'Vui lòng nhập mật khẩu', trigger: 'blur' }],
  soDienThoai: [{ required: true, message: 'Vui lòng nhập số điện thoại', trigger: 'blur' }],
  gioiTinh: [{ required: true, message: 'Vui lòng chọn giới tính', trigger: 'change' }],
  thanhPho: [{ required: true, message: 'Vui lòng chọn tỉnh/thành phố', trigger: 'change' }],
  quan: [{ required: true, message: 'Vui lòng chọn quận/huyện', trigger: 'change' }],
  phuong: [{ required: true, message: 'Vui lòng chọn phường/xã', trigger: 'change' }],
  diaChiCuThe: [{ required: true, message: 'Vui lòng nhập địa chỉ cụ thể', trigger: 'blur' }],
}
const loadProvinces = async () => {
  try {
    const res = await fetch('https://esgoo.net/api-tinhthanh/1/0.htm')
    const responseData = await res.json()
    if (responseData.error === 0) {
      provinces.value = responseData.data.map((p: any) => ({
        value: p.full_name,
        label: p.full_name,
        code: p.id,
      }))
    }
  } catch (error) {
    console.error('Lỗi tải tỉnh thành:', error)
  }
}
loadProvinces()

const onProvinceChange = async (formIndex: number, value: string) => {
  danhSachFormDiaChi.value[formIndex].districts = []
  danhSachFormDiaChi.value[formIndex].wards = []
  danhSachFormDiaChi.value[formIndex].quan = ''
  danhSachFormDiaChi.value[formIndex].phuong = ''

  const province = provinces.value.find((p) => p.value === value)
  if (province) {
    try {
      const res = await fetch(`https://esgoo.net/api-tinhthanh/2/${province.code}.htm`)
      const responseData = await res.json()
      if (responseData.error === 0) {
        danhSachFormDiaChi.value[formIndex].districts = responseData.data.map((d: any) => ({
          value: d.full_name,
          label: d.full_name,
          code: d.id,
        }))
      }
    } catch (error) {
      console.error('Lỗi tải quận huyện:', error)
    }
  }
}

const onDistrictChange = async (formIndex: number, value: string) => {
  danhSachFormDiaChi.value[formIndex].wards = []
  danhSachFormDiaChi.value[formIndex].phuong = ''

  const district = danhSachFormDiaChi.value[formIndex].districts.find((d) => d.value === value)
  if (district) {
    try {
      const res = await fetch(`https://esgoo.net/api-tinhthanh/3/${district.code}.htm`)
      const responseData = await res.json()
      if (responseData.error === 0) {
        danhSachFormDiaChi.value[formIndex].wards = responseData.data.map((w: any) => ({
          value: w.full_name,
          label: w.full_name,
        }))
      }
    } catch (error) {
      console.error('Lỗi tải phường xã:', error)
    }
  }
}

// Show confirm modal for set default address
const setDefaultAddress = (index: number) => {
  addressToSetDefault.value = index
  showDefaultConfirm.value = true
}

// Actual set default address implementation
const performSetDefaultAddress = (index: number) => {
  // Bỏ mặc định của tất cả địa chỉ
  danhSachFormDiaChi.value.forEach((addr) => {
    addr.macDinh = false
  })
  // Đặt địa chỉ được chọn làm mặc định
  danhSachFormDiaChi.value[index].macDinh = true
}

const confirmSetDefaultAddress = () => {
  if (addressToSetDefault.value !== null) {
    performSetDefaultAddress(addressToSetDefault.value)
  }
  showDefaultConfirm.value = false
  addressToSetDefault.value = null
}

const cancelSetDefaultAddress = () => {
  showDefaultConfirm.value = false
  addressToSetDefault.value = null
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()

    // Validate tất cả các trường
    if (!formData.value.tenKhachHang) {
      Message.error('Vui lòng nhập tên khách hàng.')
      return
    }
    if (!formData.value.email || !formData.value.email.includes('@')) {
      Message.error('Email không hợp lệ để tạo tài khoản.')
      return
    }
    if (!formData.value.matKhau) {
      Message.error('Vui lòng nhập mật khẩu.')
      return
    }
    if (!formData.value.ngaySinh) {
      Message.error('Vui lòng chọn ngày sinh.')
      return
    }
    if (!formData.value.soDienThoai) {
      Message.error('Vui lòng nhập số điện thoại.')
      return
    }
    if (!formData.value.gioiTinh) {
      Message.error('Vui lòng chọn giới tính.')
      return
    }

    // Validate danh sách địa chỉ
    if (danhSachFormDiaChi.value.length === 0) {
      Message.error('Vui lòng thêm ít nhất một địa chỉ.')
      return
    }

    for (let i = 0; i < danhSachFormDiaChi.value.length; i++) {
      const addr = danhSachFormDiaChi.value[i]
      const index = i + 1
      if (!addr.tenDiaChi) {
        Message.error(`Vui lòng nhập tên địa chỉ cho Địa chỉ ${index}`)
        return
      }
      if (!addr.thanhPho) {
        Message.error(`Vui lòng chọn tỉnh/thành phố cho ${addr.tenDiaChi}`)
        return
      }
      if (!addr.quan) {
        Message.error(`Vui lòng chọn quận/huyện cho ${addr.tenDiaChi}`)
        return
      }
      if (!addr.phuong) {
        Message.error(`Vui lòng chọn phường/xã cho ${addr.tenDiaChi}`)
        return
      }
      if (!addr.diaChiCuThe) {
        Message.error(`Vui lòng nhập địa chỉ cụ thể cho ${addr.tenDiaChi}`)
        return
      }
    }

    const emailUsername = formData.value.email.split('@')[0]

    const payload = {
      tenKhachHang: formData.value.tenKhachHang,
      email: formData.value.email,
      matKhau: formData.value.matKhau,
      ngaySinh: formData.value.ngaySinh,
      soDienThoai: formData.value.soDienThoai,
      phanLoai: formData.value.phanLoai || null,
      gioiTinh: formData.value.gioiTinh === 'Nam',
      trangThai: true,
      deleted: false,
      tenTaiKhoan: emailUsername,
      listDiaChi: danhSachFormDiaChi.value.map((diaChi) => ({
        tenDiaChi: diaChi.tenDiaChi,
        diaChiCuThe: diaChi.diaChiCuThe,
        thanhPho: diaChi.thanhPho,
        quan: diaChi.quan,
        phuong: diaChi.phuong,
        macDinh: diaChi.macDinh,
      })),
    }

    await themKhachHang(payload)
    Message.success('Thêm khách hàng thành công!')
    router.push({ name: 'QuanLyKhachHang' }) //  SPA routing với route name
  } catch (error) {
    Message.error('Thêm khách hàng thất bại. Vui lòng kiểm tra lại thông tin.')
    // console.error(error)
  }
}
// Show confirm modal for save customer
const showConfirm = () => {
  showSaveConfirm.value = true
}

const confirmSaveCustomer = async () => {
  await handleSubmit()
  showSaveConfirm.value = false
}

const cancelSaveCustomer = () => {
  showSaveConfirm.value = false
}

const handleCancel = () => {
  router.push('/khach-hang-nhan-su/khach-hang')
}
</script>

<style scoped>
.add-customer-page {
  padding: 16px 20px;
}
</style>
