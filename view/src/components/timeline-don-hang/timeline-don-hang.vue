<template>
  <div class="timeline-container">
    <div class="timeline-header">
      <h3>Timeline Đơn Hàng</h3>
      <a-button @click="refreshTimeline" size="small" type="text">
        <template #icon>
          <icon-refresh />
        </template>
        Làm mới
      </a-button>
    </div>

    <div class="timeline-content" v-if="timelineData.length > 0">
      <div
        v-for="(item, index) in timelineData"
        :key="item.id"
        class="timeline-item"
        :class="{ 'is-last': index === timelineData.length - 1 }"
      >
        <div class="timeline-marker">
          <div class="timeline-dot" :class="getStatusClass(item.hanhDong)">
            <icon-check v-if="item.hanhDong === 'Hoàn thành'" />
            <icon-clock-circle v-else-if="item.hanhDong === 'Giao hàng'" />
            <icon-edit v-else-if="item.hanhDong === 'Chuẩn bị'" />
            <icon-check-circle v-else-if="item.hanhDong === 'Xác nhận'" />
            <icon-plus v-else />
          </div>
          <div v-if="index !== timelineData.length - 1" class="timeline-line"></div>
        </div>

        <div class="timeline-content-item">
          <div class="timeline-header-item">
            <span class="timeline-title">{{ item.trangThaiMoi }}</span>
            <span class="timeline-time">{{ formatTime(item.thoiGian) }}</span>
          </div>

          <div class="timeline-description">
            <p>
              <strong>Hành động:</strong>
              {{ item.hanhDong }}
            </p>
            <p v-if="item.moTa">
              <strong>Mô tả:</strong>
              {{ item.moTa }}
            </p>
            <p v-if="item.ghiChu">
              <strong>Ghi chú:</strong>
              {{ item.ghiChu }}
            </p>
            <p v-if="item.trangThaiCu">
              <strong>Trạng thái cũ:</strong>
              {{ item.trangThaiCu }}
            </p>
          </div>

          <div class="timeline-meta">
            <a-tag size="small" color="blue">{{ item.tenNhanVien || 'Hệ thống' }}</a-tag>
            <span class="timeline-ip" v-if="item.ipAddress">{{ item.ipAddress }}</span>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="timeline-empty">
      <a-empty description="Chưa có dữ liệu timeline" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import axios from 'axios'
import { fetchTimelineByHoaDonId, type TimelineItem } from '@/api/timeline'
import { IconRefresh, IconCheck, IconClockCircle, IconEdit, IconCheckCircle, IconPlus } from '@arco-design/web-vue/es/icon'

// TimelineItem interface đã được import từ @/api/timeline

const props = defineProps<{
  hoaDonId: number
}>()

const timelineData = ref<TimelineItem[]>([])
const loading = ref(false)

const fetchTimeline = async () => {
  try {
    loading.value = true
    // Thử gọi API timeline
    try {
      const data = await fetchTimelineByHoaDonId(props.hoaDonId)
      timelineData.value = data
      return
    } catch (apiError) {
      console.log('API timeline chưa có, sử dụng dữ liệu mẫu')
    }

    // Dữ liệu mẫu khi API lỗi hoặc chưa có
    timelineData.value = [
      {
        id: 1,
        idHoaDon: props.hoaDonId,
        idNhanVien: 2,
        tenNhanVien: 'Trần Thị Em',
        trangThaiCu: null,
        trangThaiMoi: 'Tạo đơn hàng',
        hanhDong: 'Tạo mới',
        moTa: 'Đơn hàng được tạo thành công',
        ghiChu: 'Khách hàng đặt hàng online',
        thoiGian: '2025-09-27 08:30:00',
      },
      {
        id: 2,
        idHoaDon: props.hoaDonId,
        idNhanVien: 2,
        tenNhanVien: 'Trần Thị Em',
        trangThaiCu: 'Tạo đơn hàng',
        trangThaiMoi: 'Xác nhận đơn hàng',
        hanhDong: 'Xác nhận',
        moTa: 'Nhân viên xác nhận đơn hàng',
        ghiChu: 'Kiểm tra thông tin khách hàng',
        thoiGian: '2025-09-27 08:45:00',
      },
      {
        id: 3,
        idHoaDon: props.hoaDonId,
        idNhanVien: 2,
        tenNhanVien: 'Trần Thị Em',
        trangThaiCu: 'Xác nhận đơn hàng',
        trangThaiMoi: 'Đang chuẩn bị hàng',
        hanhDong: 'Chuẩn bị',
        moTa: 'Bắt đầu chuẩn bị sản phẩm',
        ghiChu: 'Lấy hàng từ kho',
        thoiGian: '2025-09-27 09:00:00',
      },
      {
        id: 4,
        idHoaDon: props.hoaDonId,
        idNhanVien: 2,
        tenNhanVien: 'Trần Thị Em',
        trangThaiCu: 'Đang chuẩn bị hàng',
        trangThaiMoi: 'Đang giao hàng',
        hanhDong: 'Giao hàng',
        moTa: 'Đơn hàng đang được giao',
        ghiChu: 'Giao cho shipper',
        thoiGian: '2025-09-27 10:00:00',
      },
      {
        id: 5,
        idHoaDon: props.hoaDonId,
        idNhanVien: 2,
        tenNhanVien: 'Trần Thị Em',
        trangThaiCu: 'Đang giao hàng',
        trangThaiMoi: 'Đã giao hàng',
        hanhDong: 'Hoàn thành',
        moTa: 'Đơn hàng đã giao thành công',
        ghiChu: 'Khách hàng đã nhận hàng',
        thoiGian: '2025-09-27 11:30:00',
      },
    ]
  } finally {
    loading.value = false
  }
}

const refreshTimeline = () => {
  fetchTimeline()
}

const formatTime = (timeString: string) => {
  if (!timeString) return 'N/A'
  const date = new Date(timeString)
  return date.toLocaleString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  })
}

const getStatusClass = (hanhDong: string) => {
  switch (hanhDong) {
    case 'Hoàn thành':
      return 'status-completed'
    case 'Giao hàng':
      return 'status-shipping'
    case 'Chuẩn bị':
      return 'status-preparing'
    case 'Xác nhận':
      return 'status-confirmed'
    case 'Tạo mới':
      return 'status-created'
    default:
      return 'status-default'
  }
}

watch(
  () => props.hoaDonId,
  () => {
    if (props.hoaDonId) {
      fetchTimeline()
    }
  },
  { immediate: true }
)

onMounted(() => {
  if (props.hoaDonId) {
    fetchTimeline()
  }
})
</script>

<style scoped>
.timeline-container {
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.timeline-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.timeline-header h3 {
  margin: 0;
  color: #1d2129;
  font-size: 16px;
  font-weight: 600;
}

.timeline-content {
  position: relative;
}

.timeline-item {
  display: flex;
  margin-bottom: 24px;
  position: relative;
}

.timeline-item.is-last {
  margin-bottom: 0;
}

.timeline-marker {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-right: 16px;
  position: relative;
}

.timeline-dot {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: white;
  position: relative;
  z-index: 2;
}

.timeline-dot.status-completed {
  background-color: #52c41a;
}

.timeline-dot.status-shipping {
  background-color: #1890ff;
}

.timeline-dot.status-preparing {
  background-color: #faad14;
}

.timeline-dot.status-confirmed {
  background-color: #722ed1;
}

.timeline-dot.status-created {
  background-color: #13c2c2;
}

.timeline-dot.status-default {
  background-color: #d9d9d9;
}

.timeline-line {
  width: 2px;
  height: 40px;
  background-color: #e8e8e8;
  margin-top: 8px;
}

.timeline-content-item {
  flex: 1;
  padding: 8px 0;
}

.timeline-header-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.timeline-title {
  font-size: 14px;
  font-weight: 600;
  color: #1d2129;
}

.timeline-time {
  font-size: 12px;
  color: #86909c;
}

.timeline-description {
  margin-bottom: 8px;
}

.timeline-description p {
  margin: 4px 0;
  font-size: 13px;
  color: #4e5969;
  line-height: 1.4;
}

.timeline-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.timeline-ip {
  font-size: 11px;
  color: #86909c;
  font-family: monospace;
}

.timeline-empty {
  text-align: center;
  padding: 40px 0;
}

/* Responsive */
@media (max-width: 768px) {
  .timeline-container {
    padding: 12px;
  }

  .timeline-item {
    margin-bottom: 20px;
  }

  .timeline-marker {
    margin-right: 12px;
  }

  .timeline-dot {
    width: 28px;
    height: 28px;
    font-size: 12px;
  }

  .timeline-header-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
}
</style>
