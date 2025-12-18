<template>
  <div class="update-customer-page">
    <!-- Card 1: Thông tin khách hàng -->
    <a-card title="Thông tin khách hàng" style="margin-bottom: 16px" :loading="dangTai">
      <a-form :model="form" layout="vertical">
        <!-- Hàng 1: Tên khách hàng và Ngày sinh -->
        <a-row :gutter="24">
          <a-col :span="12">
            <a-form-item>
              <template #label>
                Tên khách hàng
                <span style="color: red">*</span>
              </template>
              <a-input v-model="form.name" placeholder="Nhập tên khách hàng" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="Ngày sinh">
              <a-date-picker v-model="form.birthday" format="YYYY-MM-DD" placeholder="Chọn ngày sinh"
                style="width: 100%" :disabled-date="(current: Date) => current && current > new Date()" />
            </a-form-item>
          </a-col>
        </a-row>

        <!-- Hàng 2: Email và Mật khẩu -->
        <a-row :gutter="24">
          <a-col :span="12">
            <a-form-item>
              <template #label>
                Email
                <span style="color: red">*</span>
              </template>
              <a-input v-model="form.email" placeholder="Nhập email" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item>
              <template #label>
                Mật khẩu
                <span style="color: red">*</span>
              </template>
              <a-input-password v-model="form.matKhau" placeholder="Nhập mật khẩu" autocomplete="new-password" />
            </a-form-item>
          </a-col>
        </a-row>

        <!-- Hàng 3: Số điện thoại và Giới tính -->
        <a-row :gutter="24">
          <a-col :span="12">
            <a-form-item>
              <template #label>
                Số điện thoại
                <span style="color: red">*</span>
              </template>
              <a-input v-model="form.soDienThoai" placeholder="Nhập số điện thoại" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item>
              <template #label>
                Giới tính
                <span style="color: red">*</span>
              </template>
              <a-radio-group v-model="form.gender" type="button">
                <a-radio value="Nam">Nam</a-radio>
                <a-radio value="Nữ">Nữ</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
        </a-row>

        <!-- Hàng 4: Trạng thái -->
        <a-row :gutter="24">
          <a-col :span="12">
            <a-form-item>
              <template #label>
                Trạng thái
                <span style="color: red">*</span>
              </template>
              <a-radio-group v-model="form.status" type="button">
                <a-radio value="active">Hoạt động</a-radio>
                <a-radio value="inactive">Không hoạt động</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-card>

    <!-- Card 2: Thông tin địa chỉ -->
    <a-card title="Thông tin địa chỉ" style="margin-bottom: 16px">
      <template #extra>
        <a-button type="primary" size="small" @click="themDiaChi">
          <template #icon>
            <icon-plus />
          </template>
          Thêm địa chỉ
        </a-button>
      </template>

      <!-- Hiển thị các địa chỉ -->
      <div v-for="(diaChi, index) in danhSachDiaChi" :key="index" style="margin-bottom: 24px">
        <a-card size="small" :title="`${diaChi.tenDiaChi || `Địa chỉ ${index + 1}`}`">
          <template #extra>
            <a-space size="small">
              <a-button v-if="danhSachDiaChi.length > 1 && !diaChi.macDinh" @click="setDefaultAddress(index)"
                type="text" size="small">
                <template #icon>
                  <icon-star />
                </template>
                Đặt mặc định
              </a-button>
              <a-tag v-if="diaChi.macDinh" color="green" size="small">Mặc định</a-tag>
              <a-button v-if="danhSachDiaChi.length > 1" @click="xoaDiaChi(index)" type="text" status="danger"
                size="small">
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
      <a-empty v-if="danhSachDiaChi.length === 0" description="Chưa có địa chỉ nào. Nhấn 'Thêm địa chỉ' để bắt đầu." />
    </a-card>

    <!-- Card 3: Thao tác -->
    <a-card>
      <a-form-item style="margin-bottom: 0">
        <a-space>
          <a-button @click="quayLai" size="large">
            <template #icon>
              <icon-close />
            </template>
            Hủy
          </a-button>
          <a-button type="primary" @click="showConfirm" size="large">
            <template #icon>
              <icon-save />
            </template>
            Cập nhật
          </a-button>
        </a-space>
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
            <strong>{{ danhSachDiaChi[addressToDelete]?.tenDiaChi }}</strong>
          </div>
          <div>
            Địa chỉ:
            <strong>{{ getFullAddress(danhSachDiaChi[addressToDelete]) }}</strong>
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
            <strong>{{ danhSachDiaChi[addressToSetDefault]?.tenDiaChi }}</strong>
          </div>
          <div>
            Địa chỉ:
            <strong>{{ getFullAddress(danhSachDiaChi[addressToSetDefault]) }}</strong>
          </div>
        </div>
      </template>
    </a-modal>

    <!-- Update Customer Confirm Modal -->
    <a-modal v-model:visible="showUpdateConfirm" title="Xác nhận cập nhật khách hàng" ok-text="Xác nhận"
      cancel-text="Huỷ" @ok="confirmUpdateCustomer" @cancel="cancelUpdateCustomer">
      <template #default>
        <div>
          <div>Bạn có chắc chắn muốn cập nhật thông tin khách hàng này?</div>
          <div>
            Tên khách hàng:
            <strong>{{ form.name }}</strong>
          </div>
          <div>
            Email:
            <strong>{{ form.email }}</strong>
          </div>
          <div>
            Số điện thoại:
            <strong>{{ form.soDienThoai }}</strong>
          </div>
          <div>
            Số địa chỉ:
            <strong>{{ danhSachDiaChi.length }} địa chỉ</strong>
          </div>
        </div>
      </template>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { layChiTietKhachHang, capNhatKhachHang, themKhachHang, type KhachHangRequest } from '@/api/khach-hang'
import { IconClose, IconSave, IconPlus, IconStar, IconDelete } from '@arco-design/web-vue/es/icon'
import { Message } from '@arco-design/web-vue'

const route = useRoute()
const router = useRouter()
const provinces = ref<{ value: string; label: string; code: string }[]>([])

const dangTai = ref(false)
const form = ref<any>({
  tenKhachHang: '',
  gioiTinh: '',
  ngaySinh: '',
  trangThai: '',
  email: '',
  soDienThoai: '',
  tenTaiKhoan: '', // thêm
  matKhau: '',
})

// Danh sách địa chỉ động
const danhSachDiaChi = ref<
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

const showUpdateConfirm = ref(false)

// Helper function to get full address
const getFullAddress = (diaChi: any) => {
  if (!diaChi) return ''
  const parts = [diaChi.diaChiCuThe, diaChi.phuong, diaChi.quan, diaChi.thanhPho].filter(Boolean)
  return parts.join(', ')
}

// Helper functions for address loading
// Using esgoo.net API which is more stable than provinces.open-api.vn
const loadDistrictsForAddress = async (provinceName: string) => {
  const province = provinces.value.find((p) => p.value === provinceName)
  if (province) {
    try {
      const res = await fetch(`https://esgoo.net/api-tinhthanh/2/${province.code}.htm`)
      const responseData = await res.json()
      if (responseData.error === 0) {
        return responseData.data.map((d: any) => ({
          value: d.full_name,
          label: d.full_name,
          code: d.id,
        }))
      }
    } catch (error) {
      console.error('Lỗi load quận huyện:', error)
    }
  }
  return []
}

const loadWardsForAddress = async (districtName: string, districtsList: any[]) => {
  const district = districtsList.find((d) => d.value === districtName)
  if (district) {
    try {
      const res = await fetch(`https://esgoo.net/api-tinhthanh/3/${district.code}.htm`)
      const responseData = await res.json()
      if (responseData.error === 0) {
        return responseData.data.map((w: any) => ({
          value: w.full_name,
          label: w.full_name,
        }))
      }
    } catch (error) {
      console.error('Lỗi load phường xã:', error)
    }
  }
  return []
}

const layChiTietKhach = async () => {
  try {
    dangTai.value = true
    const { id } = route.params
    const res = await layChiTietKhachHang(Number(id))

    form.value = {
      id: res.data.id,
      code: res.data.maKhachHang,
      name: res.data.tenKhachHang,
      gender: res.data.gioiTinh ? 'Nam' : 'Nữ',
      birthday: res.data.ngaySinh,
      status: res.data.trangThai ? 'active' : 'inactive',
      email: res.data.email,
      soDienThoai: res.data.soDienThoai,
      tenTaiKhoan: res.data.tenTaiKhoan || '',
      matKhau: res.data.matKhau,
    }

    // Load tất cả địa chỉ
    if (res.data.listDiaChi && res.data.listDiaChi.length > 0) {
      danhSachDiaChi.value = await Promise.all(
        res.data.listDiaChi.map(async (addr: any, index: number) => {
          const diaChiItem = {
            tenDiaChi: addr.tenDiaChi || `Địa chỉ ${index + 1}`,
            thanhPho: addr.thanhPho || '',
            quan: addr.quan || '',
            phuong: addr.phuong || '',
            diaChiCuThe: addr.diaChiCuThe || '',
            macDinh: addr.macDinh || false,
            districts: [] as { value: string; label: string; code: string }[],
            wards: [] as { value: string; label: string }[],
          }

          // Load dropdown cho quận và phường nếu có thành phố
          if (diaChiItem.thanhPho) {
            diaChiItem.districts = await loadDistrictsForAddress(diaChiItem.thanhPho)

            // Load dropdown cho phường nếu có quận
            if (diaChiItem.quan) {
              diaChiItem.wards = await loadWardsForAddress(diaChiItem.quan, diaChiItem.districts)
            }
          }

          return diaChiItem
        })
      )
    } else {
      // Nếu không có địa chỉ, tạo 1 địa chỉ mặc định
      danhSachDiaChi.value = [
        {
          tenDiaChi: 'Địa chỉ 1',
          thanhPho: '',
          quan: '',
          phuong: '',
          diaChiCuThe: '',
          macDinh: true,
          districts: [],
          wards: [],
        },
      ]
    }
  } catch (err) {
    // eslint-disable-next-line no-console
    console.error('Lỗi lấy chi tiết khách:', err)
  } finally {
    dangTai.value = false
  }
}
const luuKhach = async () => {
  try {
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
    if (!form.value.tenTaiKhoan) {
      Message.error('Vui lòng nhập tên tài khoản.')
      return false
    }
    // Mật khẩu là bắt buộc khi tạo mới, nhưng khi update có thể rỗng (nếu không đổi)
    // Tuy nhiên ở đây form.value.matKhau đang được bind và validate
    if (!form.value.id && !form.value.matKhau) {
      // Nếu là thêm mới thì cần check, update thì backend có thể xử lý việc giữ nguyên pass cũ
      // Tuy nhiên logic hiện tại của UI update đang require pass.
      // Giữ nguyên logic cũ cho an toàn với backend hiện tại
      Message.error('Vui lòng nhập mật khẩu.')
      return false
    } else if (form.value.id && !form.value.matKhau) {
      // Check if backend allows empty password for update? 
      // Assuming current requirement needs password or it's fetched and pre-filled.
      // Assuming pre-filled based on 'layChiTietKhach'.
      Message.error('Vui lòng nhập mật khẩu.')
      return false
    }

    // Validate danh sách địa chỉ
    if (danhSachDiaChi.value.length === 0) {
      Message.error('Vui lòng thêm ít nhất một địa chỉ.')
      return false
    }

    for (let i = 0; i < danhSachDiaChi.value.length; i++) {
      const addr = danhSachDiaChi.value[i]
      const index = i + 1
      if (!addr.tenDiaChi) {
        Message.error(`Vui lòng nhập tên địa chỉ cho Địa chỉ ${index}`)
        return false
      }
      if (!addr.thanhPho) {
        Message.error(`Vui lòng chọn tỉnh/thành phố cho ${addr.tenDiaChi}`)
        return false
      }
      if (!addr.quan) {
        Message.error(`Vui lòng chọn quận/huyện cho ${addr.tenDiaChi}`)
        return false
      }
      if (!addr.phuong) {
        Message.error(`Vui lòng chọn phường/xã cho ${addr.tenDiaChi}`)
        return false
      }
      if (!addr.diaChiCuThe) {
        Message.error(`Vui lòng nhập địa chỉ cụ thể cho ${addr.tenDiaChi}`)
        return false
      }
    }

    try {
      dangTai.value = true

      const payload = {
        tenKhachHang: form.value.name,
        gioiTinh: form.value.gender === 'Nam',
        ngaySinh: form.value.birthday,
        phanLoaiText: form.value.customer_type,
        trangThai: form.value.status === 'active',
        listDiaChi: danhSachDiaChi.value.map((diaChi) => ({
          tenDiaChi: diaChi.tenDiaChi,
          thanhPho: diaChi.thanhPho,
          quan: diaChi.quan,
          phuong: diaChi.phuong,
          diaChiCuThe: diaChi.diaChiCuThe,
          macDinh: diaChi.macDinh,
        })),
        email: form.value.email,
        soDienThoai: form.value.soDienThoai,
        tenTaiKhoan: form.value.tenTaiKhoan?.trim() || '',
        matkhau: form.value.matKhau,
        deleted: false,
      }

      if (form.value.id) {
        await capNhatKhachHang(form.value.id, payload)
        Message.success('Cập nhật khách hàng thành công!')
      } else {
        await themKhachHang(payload)
        Message.success('Thêm mới khách hàng thành công!')
      }

      router.push({ name: 'QuanLyKhachHang' }) //  SPA routing với route name
      return true
    } catch (err) {
      // eslint-disable-next-line no-console
      console.error('Lỗi lưu khách:', err)
      Message.error('Lưu khách hàng thất bại!')
      return false
    } finally {
      dangTai.value = false
    }
  } catch (error) {
    // console.error('Lỗi validation:', error)
    Message.error('Vui lòng kiểm tra lại thông tin!')
    return false
  }
}

const quayLai = () => {
  router.push({ name: 'QuanLyKhachHang' }) //  SPA routing với route name
}
const loadProvinces = async () => {
  try {
    const res = await fetch('https://esgoo.net/api-tinhthanh/1/0.htm')
    const responseData = await res.json()
    if (responseData.error === 0) {
      provinces.value = responseData.data.map((p: any) => ({
        value: p.full_name, // Using full_name makes it easier to match with existing string data
        label: p.full_name,
        code: p.id,
      }))
    }
  } catch (error) {
    console.error('Lỗi load tỉnh thành:', error)
    Message.error('Không thể tải danh sách tỉnh thành')
  }
}
loadProvinces()

const onProvinceChange = async (formIndex: number, value: string) => {
  danhSachDiaChi.value[formIndex].districts = []
  danhSachDiaChi.value[formIndex].wards = []
  danhSachDiaChi.value[formIndex].quan = ''
  danhSachDiaChi.value[formIndex].phuong = ''

  danhSachDiaChi.value[formIndex].districts = await loadDistrictsForAddress(value)
}

const onDistrictChange = async (formIndex: number, value: string) => {
  danhSachDiaChi.value[formIndex].wards = []
  danhSachDiaChi.value[formIndex].phuong = ''

  danhSachDiaChi.value[formIndex].wards = await loadWardsForAddress(value, danhSachDiaChi.value[formIndex].districts)
}

// Thêm địa chỉ mới
const themDiaChi = () => {
  const soDiaChi = danhSachDiaChi.value.length + 1
  const diaChiMoi = {
    tenDiaChi: `Địa chỉ ${soDiaChi}`,
    thanhPho: '',
    quan: '',
    phuong: '',
    diaChiCuThe: '',
    macDinh: danhSachDiaChi.value.length === 0, // Địa chỉ đầu tiên là mặc định
    districts: [],
    wards: [],
  }
  danhSachDiaChi.value.push(diaChiMoi)
}

// Show confirm modal for delete address
const xoaDiaChi = (index: number) => {
  addressToDelete.value = index
  showDeleteConfirm.value = true
}

// Actual delete address implementation
const performDeleteAddress = (index: number) => {
  const diaChiXoa = danhSachDiaChi.value[index]
  danhSachDiaChi.value.splice(index, 1)

  // Nếu xóa địa chỉ mặc định và còn địa chỉ khác, đặt địa chỉ đầu tiên làm mặc định
  if (diaChiXoa.macDinh && danhSachDiaChi.value.length > 0) {
    danhSachDiaChi.value[0].macDinh = true
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

// Show confirm modal for set default address
const setDefaultAddress = (index: number) => {
  addressToSetDefault.value = index
  showDefaultConfirm.value = true
}

// Actual set default address implementation
const performSetDefaultAddress = (index: number) => {
  // Bỏ mặc định của tất cả địa chỉ
  danhSachDiaChi.value.forEach((addr) => {
    addr.macDinh = false
  })
  // Đặt địa chỉ được chọn làm mặc định
  danhSachDiaChi.value[index].macDinh = true
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

// Show confirm modal for update customer
const showConfirm = () => {
  showUpdateConfirm.value = true
}

const confirmUpdateCustomer = async () => {
  await luuKhach()
  showUpdateConfirm.value = false
}

const cancelUpdateCustomer = () => {
  showUpdateConfirm.value = false
}

// Handle submit
onMounted(() => {
  layChiTietKhach()
})
</script>

<style scoped>
.update-customer-page {
  padding: 16px 20px;
}
</style>
