<template>
  <div class="schedule-add-page">
    <a-card class="form-card" title="Thêm lịch làm việc">
      <a-form :model="form" layout="vertical">
        <a-row :gutter="16">
          <!-- Nhân viên -->
          <a-col :span="12">
            <a-form-item label="Nhân viên" required>
              <a-select
                v-model="form.nhanVien"
                placeholder="Chọn nhân viên"
                allow-clear
                :loading="loadingNhanVien"
              >
                <a-option
                  v-for="nv in nhanViens"
                  :key="nv.id"
                  :value="nv.id"
                >
                  {{ nv.tenNhanVien }}
                </a-option>
              </a-select>
            </a-form-item>
          </a-col>

          <!-- Ca làm việc -->
          <a-col :span="12">
            <a-form-item label="Ca làm việc" required>
              <a-select
                v-model="form.caLamViec"
                placeholder="Chọn ca làm việc"
                allow-clear
                :loading="loadingCa"
              >
                <a-option
                  v-for="ca in caLamViecs"
                  :key="ca.id"
                  :value="ca.id"
                >
                  {{ ca.tenCa }} 
                </a-option>
              </a-select>
            </a-form-item>
          </a-col>

          <!-- Ngày làm việc -->
          <a-col :span="12">
            <a-form-item label="Ngày làm việc" required>
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
            <a-form-item label="Ghi chú">
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
          <a-button type="primary" :loading="loadingSubmit" @click="handleSubmit">
            Lưu lịch
          </a-button>
        </a-space>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import dayjs from 'dayjs'
import { Message } from '@arco-design/web-vue'
import { useRouter } from 'vue-router'

import { layDanhSachNhanVien } from '@/api/nhan-vien'
import { getCaLamViec } from '@/api/ca-lam-viec'
import { themLichLamViec } from '@/api/lich-lam-viec'

const router = useRouter()

// 🧩 Interface chuẩn hóa dữ liệu
interface NhanVien {
  id: number
  tenNhanVien: string
}

interface CaLamViec {
  id: number
  tenCa: string
  thoiGianBatDau: string
  thoiGianKetThuc: string
  ghiChu?: string
  trangThai: boolean | string
}

// 🧾 Form dữ liệu
const form = ref({
  nhanVien: null as number | null,
  caLamViec: null as number | null,
  ngayLamViec: null as string | null,
  ghiChu: ''
})

// 🧩 Dữ liệu từ backend
const nhanViens = ref<NhanVien[]>([])
const caLamViecs = ref<CaLamViec[]>([])

// Loading states
const loadingNhanVien = ref(false)
const loadingCa = ref(false)
const loadingSubmit = ref(false)

// ===========================
// 🧩 Lấy danh sách nhân viên
// ===========================
const fetchNhanViens = async () => {
  try {
    loadingNhanVien.value = true
    const res = await layDanhSachNhanVien()
    console.log('📦 API nhân viên:', res)

    // Lấy dữ liệu từ response (tùy backend trả về)
    const list = res.data?.data ?? res.data ?? []

    nhanViens.value = list.map((item: any) => ({
      id: item.id,
      tenNhanVien: item.tenNhanVien || item.tennhanvien || 'Không rõ'
    }))
  } catch (err) {
    console.error('❌ Lỗi tải nhân viên:', err)
    Message.error('Không thể tải danh sách nhân viên')
  } finally {
    loadingNhanVien.value = false
  }
}

// ===========================
// 🧩 Lấy danh sách ca làm việc
// ===========================
const fetchCaLamViecs = async () => {
  try {
    loadingCa.value = true;
    const res = await getCaLamViec(); // Gọi API lấy danh sách ca làm việc
    console.log('📦 API ca làm việc:', res);

    // Kiểm tra res trực tiếp là mảng, không cần res.data nữa
    const data = Array.isArray(res) ? res : [];

    if (data.length === 0) {
      throw new Error('Không có dữ liệu ca làm việc');
    }

    // Lọc và chuẩn hóa dữ liệu (chỉ lấy id và tenCa)
    caLamViecs.value = data.map((item: any) => ({
      id: item.id,
      tenCa: item.tenCa || item.tenca // chỉ lấy tên ca
    }));

    console.log('✅ Danh sách ca làm việc:', caLamViecs.value);
  } catch (err: any) {
    console.error('❌ Lỗi tải ca làm việc:', err);
    Message.error(`Không thể tải danh sách ca làm việc: ${err.message || 'Lỗi không xác định'}`);
  } finally {
    loadingCa.value = false;
  }
}



// ===========================
// 🧩 Submit form thêm lịch làm việc
// ===========================
const handleSubmit = async () => {
  // Kiểm tra các trường bắt buộc
  if (!form.value.nhanVien || !form.value.caLamViec || !form.value.ngayLamViec) {
    Message.warning('Vui lòng điền đầy đủ thông tin bắt buộc');
    return;
  }

  // Kiểm tra và ép kiểu dữ liệu
  const nhanVienId = Number(form.value.nhanVien);
  const caLamViecId = Number(form.value.caLamViec);
  const ngayLamViec = dayjs(form.value.ngayLamViec).format('YYYY-MM-DD'); // Đảm bảo định dạng đúng

  // Kiểm tra nếu nhanVienId hợp lệ (không phải NaN)
  if (Number.isNaN(nhanVienId) || nhanVienId <= 0) {
    Message.warning('Thông tin nhân viên không hợp lệ');
    return;
  }

  // Kiểm tra nếu caLamViecId hợp lệ (không phải NaN)
  if (Number.isNaN(caLamViecId) || caLamViecId <= 0) {
    Message.warning('Thông tin ca làm việc không hợp lệ');
    return;
  }

  // Cập nhật payload với cấu trúc yêu cầu của backend
  const payload = {
    nhanVienId,       // Truyền đúng tên tham số như backend yêu cầu
    caLamViecId,      // Truyền đúng tên tham số như backend yêu cầu
    ngayLamViec,
    trangThai: true,   // Hoặc false nếu backend mặc định
    ghiChu: form.value.ghiChu || null // Nếu không có ghi chú thì để null
  };

  console.log('📤 Payload gửi đi:', payload); // Kiểm tra payload

  try {
    loadingSubmit.value = true;

    // Gửi yêu cầu lên API để thêm lịch làm việc
    await themLichLamViec(payload);

    // Thông báo thành công và chuyển hướng
    Message.success('Thêm lịch làm việc thành công!');
    router.push('/lich-lam-viec/danh-sach');
  } catch (err: any) {
    console.error('❌ Lỗi khi thêm lịch làm việc:', err);

    // Hiển thị thông báo lỗi chi tiết nếu có
    if (err.response && err.response.data) {
      console.error('Chi tiết lỗi:', err.response.data);
      Message.error(`Lỗi: ${err.response.data.message || 'Không thể thêm lịch làm việc!'}`);
    } else {
      Message.error('Không thể thêm lịch làm việc!');
    }
  } finally {
    loadingSubmit.value = false;
  }
};



// ===========================
// 🧩 Reset form
// ===========================
const handleCancel = () => {
  form.value = {
    nhanVien: null,
    caLamViec: null,
    ngayLamViec: null,
    ghiChu: ''
  }
  router.push('/lich-lam-viec/danh-sach')
}

// ===========================
// 🔄 Khi mount, load dữ liệu
// ===========================
onMounted(() => {
  fetchNhanViens()
  fetchCaLamViecs()
})
</script>


<style scoped>
.schedule-add-page {
  padding: 0 20px 20px 20px;
}

.form-card {
  border-radius: 12px;
}

.actions-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
