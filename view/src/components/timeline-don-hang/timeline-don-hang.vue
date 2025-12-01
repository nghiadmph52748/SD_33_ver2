<template>
  <div class="timeline-container">
    <div class="timeline-header">
      <h3>Timeline Đơn Hàng</h3>
      <a-button @click="refreshTimeline" size="small" type="text" :loading="loading">
        <template #icon>
          <icon-refresh />
        </template>
        Làm mới
      </a-button>
    </div>

    <!-- Thông tin đơn hàng -->
    <div v-if="orderInfo && !loading" class="order-info-section">
      <div class="order-info-card">
        <div class="order-info-header">
          <h4>Thông tin đơn hàng</h4>
        </div>
        <div class="order-info-content">
          <div class="order-info-row">
            <span class="info-label">Mã đơn hàng:</span>
            <span class="info-value">{{ orderInfo.maHoaDon || `HD${String(orderInfo.id).padStart(6, '0')}` }}</span>
          </div>
          <div class="order-info-row">
            <span class="info-label">Ngày tạo:</span>
            <span class="info-value">{{ formatDate(orderInfo.ngayTao) }}</span>
          </div>
          <div class="order-info-row">
            <span class="info-label">Tổng tiền:</span>
            <span class="info-value amount">{{ formatCurrency(orderInfo.tongTienSauGiam || orderInfo.tongTien || 0) }}</span>
          </div>
          <div class="order-info-row">
            <span class="info-label">Trạng thái hiện tại:</span>
            <a-tag :color="getCurrentStatusColor()" size="small">
              {{ getCurrentStatusText() }}
            </a-tag>
          </div>
        </div>
      </div>
    </div>

    <!-- Timeline content -->
    <div class="timeline-content-wrapper">
      <!-- Debug info -->
      <div v-if="false" style="padding: 8px; background: #f0f0f0; margin-bottom: 8px; font-size: 12px">
        Debug: loading={{ loading }}, timelineData.length={{ timelineData.length }}, hoaDonId={{ props.hoaDonId }}
      </div>

      <div v-if="loading" class="timeline-loading">
        <a-spin />
        <p>Đang tải timeline...</p>
      </div>

      <div v-else-if="timelineData.length > 0" class="timeline-content">
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
              <icon-check-circle v-else-if="item.hanhDong === 'Xác nhận giao hàng'" />
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
              <a-tag v-if="item.id > 1000000000000" size="small" color="orange">Tạm thời</a-tag>
              <span class="timeline-ip" v-if="item.ipAddress">{{ item.ipAddress }}</span>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="timeline-empty">
        <a-empty description="Chưa có dữ liệu timeline">
          <template #image>
            <icon-file :size="48" />
          </template>
          <p style="color: #86909c; margin-top: 8px">
            Đơn hàng này chưa có lịch sử cập nhật trạng thái.
            <br />
            Timeline sẽ được cập nhật khi có thay đổi trạng thái đơn hàng.
          </p>
        </a-empty>
      </div>
    </div>

    <!-- Actions để chuyển trạng thái -->
    <div v-if="!loading && timelineData.length > 0" class="timeline-actions">
      <div class="actions-header">
        <h4>Cập nhật trạng thái</h4>
      </div>
      <div class="actions-buttons">
        <a-button
          v-for="action in availableActions"
          :key="action.key"
          :type="action.type"
          :loading="actionLoading === action.key"
          @click="handleStatusChange(action)"
        >
          <template #icon>
            <icon-check-circle v-if="action.icon === 'IconCheckCircle'" />
            <icon-edit v-else-if="action.icon === 'IconEdit'" />
            <icon-clock-circle v-else-if="action.icon === 'IconClockCircle'" />
            <icon-check v-else-if="action.icon === 'IconCheck'" />
            <icon-play-arrow v-else />
          </template>
          {{ action.label }}
        </a-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, withDefaults } from 'vue'
import { fetchTimelineByHoaDonId, type TimelineItem } from '@/api/timeline'
import { fetchInvoiceById, type InvoiceApiModel } from '@/api/invoice'
import { fetchHoaDonById, type HoaDonApiModel } from '@/api/hoa-don'
import {
  IconRefresh,
  IconCheck,
  IconClockCircle,
  IconEdit,
  IconCheckCircle,
  IconPlus,
  IconFile,
  IconArrowRight,
  IconPlayArrow,
} from '@arco-design/web-vue/es/icon'
import { createTimelineItem } from '@/api/timeline'
import { getUserInfo } from '@/api/user'

const props = withDefaults(
  defineProps<{
    hoaDonId: number | string
    useInvoiceApi?: boolean
  }>(),
  {
    useInvoiceApi: false,
  }
)

const timelineData = ref<TimelineItem[]>([])
const orderInfo = ref<InvoiceApiModel | HoaDonApiModel | null>(null)
const loading = ref(false)
const actionLoading = ref<string | null>(null)
const currentUser = ref<any>(null)

const getHoaDonId = () => {
  const id = typeof props.hoaDonId === 'string' ? Number(props.hoaDonId) : props.hoaDonId
  return id && !isNaN(id) ? id : null
}

const shouldUseInvoiceApi = computed(() => props.useInvoiceApi === true)

const fetchOrderInfo = async () => {
  try {
    const id = getHoaDonId()
    if (!id) {
      console.warn('hoaDonId không hợp lệ:', props.hoaDonId)
      return
    }
    const data = shouldUseInvoiceApi.value ? await fetchInvoiceById(id) : await fetchHoaDonById(id)
    orderInfo.value = data
    console.log('Đã lấy thông tin đơn hàng:', data)

    // Nếu timeline đang trống và có thông tin đơn hàng, tạo fallback
    if (timelineData.value.length === 0 && orderInfo.value) {
      console.log('Timeline trống, tạo fallback từ thông tin đơn hàng vừa lấy')
      const fallbackTimeline = createFallbackTimeline()
      if (fallbackTimeline.length > 0) {
        timelineData.value = fallbackTimeline
        console.log('Đã tạo fallback timeline với', fallbackTimeline.length, 'mốc')
      }
    }
  } catch (error) {
    console.error('Lỗi khi lấy thông tin đơn hàng:', error)
    orderInfo.value = null
  }
}

const createFallbackTimeline = (): TimelineItem[] => {
  // Lấy ID từ nhiều nguồn
  let id = getHoaDonId()
  if (!id && orderInfo.value?.id) {
    id = orderInfo.value.id
  }
  if (!id && props.hoaDonId) {
    id = typeof props.hoaDonId === 'string' ? Number(props.hoaDonId) : props.hoaDonId
    if (isNaN(id)) id = null
  }

  // Nếu vẫn không có ID, sử dụng ID từ props trực tiếp
  if (!id && props.hoaDonId) {
    const parsed = Number(props.hoaDonId)
    if (!isNaN(parsed)) {
      id = parsed
    }
  }

  if (!id || id <= 0) {
    console.error(' Không thể lấy ID đơn hàng để tạo fallback timeline', {
      propsHoaDonId: props.hoaDonId,
      orderInfoId: orderInfo.value?.id,
      getHoaDonIdResult: getHoaDonId(),
    })
    // Vẫn trả về timeline cơ bản với ID = 0
    const now = new Date().toISOString().slice(0, 19).replace('T', ' ')
    return [
      {
        id: Date.now(),
        idHoaDon: 0,
        trangThaiMoi: 'Tạo đơn hàng',
        hanhDong: 'Tạo mới',
        moTa: 'Đơn hàng đã được tạo',
        thoiGian: now,
        tenNhanVien: 'Hệ thống',
      },
    ]
  }

  console.log(' Tạo fallback timeline với ID:', id)

  const timeline: TimelineItem[] = []
  const now = new Date().toISOString().slice(0, 19).replace('T', ' ')

  // Nếu có thông tin đơn hàng từ API, sử dụng thông tin đó
  if (orderInfo.value) {
    console.log(' Có thông tin đơn hàng, tạo timeline từ thông tin đầy đủ')
    // Tạo mốc timeline từ thông tin đơn hàng
    if (orderInfo.value.ngayTao) {
      // Format ngayTao về YYYY-MM-DD HH:mm:ss nếu cần
      let ngayTaoFormatted = orderInfo.value.ngayTao
      if (ngayTaoFormatted.includes('T')) {
        ngayTaoFormatted = ngayTaoFormatted.slice(0, 19).replace('T', ' ')
      }
      // Sử dụng ID nhỏ cho fallback items để không conflict với database IDs
      timeline.push({
        id: -1, // ID âm để phân biệt với database IDs
        idHoaDon: id,
        trangThaiMoi: 'Tạo đơn hàng',
        hanhDong: 'Tạo mới',
        moTa: 'Đơn hàng được tạo thành công',
        thoiGian: ngayTaoFormatted,
        tenNhanVien: orderInfo.value.tenNhanVien || 'Hệ thống',
      })
    } else {
      // Nếu không có ngày tạo, vẫn tạo mốc với thời gian hiện tại
      timeline.push({
        id: -1, // ID âm để phân biệt với database IDs
        idHoaDon: id,
        trangThaiMoi: 'Tạo đơn hàng',
        hanhDong: 'Tạo mới',
        moTa: 'Đơn hàng đã được tạo',
        thoiGian: now,
        tenNhanVien: orderInfo.value.tenNhanVien || 'Hệ thống',
      })
    }

    // Nếu có ngày thanh toán, thêm mốc thanh toán
    if (orderInfo.value.ngayThanhToan) {
      let ngayThanhToanFormatted = orderInfo.value.ngayThanhToan
      if (ngayThanhToanFormatted.includes('T')) {
        ngayThanhToanFormatted = ngayThanhToanFormatted.slice(0, 19).replace('T', ' ')
      }
      timeline.push({
        id: -2, // ID âm để phân biệt với database IDs
        idHoaDon: id,
        trangThaiCu: 'Tạo đơn hàng',
        trangThaiMoi: orderInfo.value.trangThai ? 'Đã thanh toán' : 'Chờ xác nhận',
        hanhDong: orderInfo.value.trangThai ? 'Hoàn thành' : 'Xác nhận',
        moTa: 'Đơn hàng đã được thanh toán',
        thoiGian: ngayThanhToanFormatted,
        tenNhanVien: orderInfo.value.tenNhanVien || 'Hệ thống',
      })
    }
  } else {
    // Fallback: Tạo timeline cơ bản khi không có thông tin đơn hàng
    console.log(' Tạo timeline fallback cơ bản (không có thông tin đơn hàng)')
    timeline.push({
      id: -1, // ID âm để phân biệt với database IDs
      idHoaDon: id,
      trangThaiMoi: 'Tạo đơn hàng',
      hanhDong: 'Tạo mới',
      moTa: 'Đơn hàng đã được tạo',
      thoiGian: now,
      tenNhanVien: 'Hệ thống',
    })
  }

  console.log(' Fallback timeline đã tạo với', timeline.length, 'mốc')
  return timeline
}

const fetchTimeline = async () => {
  try {
    loading.value = true
    const id = getHoaDonId()
    if (!id) {
      console.warn(' hoaDonId không hợp lệ:', props.hoaDonId)
      // Vẫn tạo fallback timeline ngay cả khi không có ID
      const fallbackTimeline = createFallbackTimeline()
      if (fallbackTimeline.length > 0) {
        timelineData.value = fallbackTimeline
        console.log(' Đã tạo fallback timeline (không có ID hợp lệ):', fallbackTimeline.length, 'mốc')
      } else {
        timelineData.value = []
      }
      loading.value = false
      return
    }
    console.log(' Đang lấy timeline cho đơn hàng:', id)

    let apiSucceeded = false
    try {
      const data = await fetchTimelineByHoaDonId(id)
      console.log(' Dữ liệu timeline nhận được từ API:', data)

      // Đảm bảo data là array và sắp xếp theo thời gian
      if (Array.isArray(data) && data.length > 0) {
        // Sắp xếp timeline từ API theo thời gian
        const sortedData = [...data].sort((a, b) => {
          const parseTime = (timeStr: string) => {
            if (!timeStr) return 0
            try {
              const normalized =
                typeof timeStr === 'string' && timeStr.includes('T')
                  ? timeStr
                  : typeof timeStr === 'string'
                    ? timeStr.replace(' ', 'T')
                    : new Date(timeStr).toISOString()
              const date = new Date(normalized)
              return isNaN(date.getTime()) ? 0 : date.getTime()
            } catch {
              return 0
            }
          }
          const timeA = parseTime(a.thoiGian)
          const timeB = parseTime(b.thoiGian)
          if (timeA === timeB) return (a.id || 0) - (b.id || 0)
          return timeA - timeB
        })
        timelineData.value = sortedData
        console.log(' Đã sắp xếp timeline từ API:', sortedData.length, 'mốc')
      } else {
        timelineData.value = []
      }
      apiSucceeded = true

      if (timelineData.value.length === 0) {
        console.warn(' Timeline trống từ API cho đơn hàng:', id)
      } else {
        console.log(' Đã lấy', timelineData.value.length, 'mốc timeline từ API')
      }
    } catch (apiError: any) {
      // Nếu API timeline lỗi, thử tạo timeline từ thông tin đơn hàng
      console.warn(' API timeline lỗi, sử dụng fallback:', apiError?.message)
      apiSucceeded = false
      timelineData.value = []
    }

    // Nếu timeline trống (API lỗi hoặc không có dữ liệu), thử tạo fallback
    if (timelineData.value.length === 0) {
      console.log(' Timeline trống, tạo fallback timeline')
      const fallbackTimeline = createFallbackTimeline()
      if (fallbackTimeline.length > 0) {
        timelineData.value = fallbackTimeline
        console.log(' Đã tạo fallback timeline với', fallbackTimeline.length, 'mốc')
      } else {
        console.error(' Không thể tạo fallback timeline')
        // Vẫn tạo timeline cơ bản
        const now = new Date().toISOString().slice(0, 19).replace('T', ' ')
        timelineData.value = [
          {
            id: Date.now(),
            idHoaDon: id || 0,
            trangThaiMoi: 'Tạo đơn hàng',
            hanhDong: 'Tạo mới',
            moTa: 'Đơn hàng đã được tạo',
            thoiGian: now,
            tenNhanVien: 'Hệ thống',
          },
        ]
        console.log(' Đã tạo timeline cơ bản:', timelineData.value.length, 'mốc')
      }
    }
  } catch (error: any) {
    console.error(' Lỗi khi lấy timeline:', error)
    console.error('Chi tiết lỗi:', {
      message: error?.message,
      status: error?.response?.status,
      data: error?.response?.data,
    })
    // Thử tạo fallback timeline
    const fallbackTimeline = createFallbackTimeline()
    if (fallbackTimeline.length > 0) {
      timelineData.value = fallbackTimeline
      console.log(' Đã tạo fallback timeline sau khi lỗi:', fallbackTimeline.length, 'mốc')
    } else {
      // Vẫn tạo timeline cơ bản
      const id = getHoaDonId() || 0
      const now = new Date().toISOString().slice(0, 19).replace('T', ' ')
      timelineData.value = [
        {
          id: Date.now(),
          idHoaDon: id,
          trangThaiMoi: 'Tạo đơn hàng',
          hanhDong: 'Tạo mới',
          moTa: 'Đơn hàng đã được tạo',
          thoiGian: now,
          tenNhanVien: 'Hệ thống',
        },
      ]
      console.log(' Đã tạo timeline cơ bản sau khi lỗi:', timelineData.value.length, 'mốc')
    }
  } finally {
    loading.value = false
    console.log(' Timeline cuối cùng có', timelineData.value.length, 'mốc')
  }
}

const refreshTimeline = async () => {
  console.log(' Bắt đầu refresh timeline cho đơn hàng:', getHoaDonId())
  try {
    // Đảm bảo fetchOrderInfo xong trước để có thể tạo fallback timeline
    console.log(' Đang lấy thông tin đơn hàng...')
    await fetchOrderInfo()
    console.log(' Đã lấy thông tin đơn hàng:', orderInfo.value?.id)

    console.log(' Đang lấy timeline...')
    await fetchTimeline()
    console.log(' Đã lấy timeline, số lượng mốc:', timelineData.value.length)
  } catch (error) {
    console.error(' Lỗi khi refresh timeline:', error)
    // Đảm bảo timeline luôn có ít nhất 1 mốc
    if (timelineData.value.length === 0) {
      const fallback = createFallbackTimeline()
      if (fallback.length > 0) {
        timelineData.value = fallback
        console.log(' Đã tạo fallback timeline:', fallback.length, 'mốc')
      }
    }
  }
}

const formatTime = (timeString: string) => {
  if (!timeString) return 'N/A'
  try {
    // Nếu format là YYYY-MM-DD HH:mm:ss, convert sang Date object
    let dateStr = timeString
    if (timeString.includes(' ') && !timeString.includes('T')) {
      // Format: YYYY-MM-DD HH:mm:ss
      dateStr = timeString.replace(' ', 'T')
    }
    const date = new Date(dateStr)
    if (isNaN(date.getTime())) {
      console.warn('Không thể parse thời gian:', timeString)
      return timeString
    }
    return date.toLocaleString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    })
  } catch (error) {
    console.error('Lỗi khi format thời gian:', timeString, error)
    return timeString
  }
}

const formatDate = (dateString?: string) => {
  if (!dateString) return 'Chưa xác định'
  const date = new Date(dateString)
  return date.toLocaleString('vi-VN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const formatCurrency = (amount?: number) => {
  if (!amount) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount)
}

const getStatusClass = (hanhDong: string) => {
  switch (hanhDong) {
    case 'Hoàn thành':
      return 'status-completed'
    case 'Giao hàng':
      return 'status-shipping'
    case 'Xác nhận giao hàng':
      return 'status-shipping-confirmed'
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

const getCurrentStatusText = () => {
  if (timelineData.value.length > 0) {
    const latestStatus = timelineData.value[timelineData.value.length - 1]
    return latestStatus.trangThaiMoi || 'Chưa xác định'
  }
  if (orderInfo.value?.trangThai) {
    return 'Hoàn thành'
  }
  return 'Chờ xác nhận'
}

const getCurrentStatusColor = () => {
  if (timelineData.value.length > 0) {
    const latestStatus = timelineData.value[timelineData.value.length - 1]
    const hanhDong = latestStatus.hanhDong

    switch (hanhDong) {
      case 'Hoàn thành':
        return 'green'
      case 'Giao hàng':
        return 'blue'
      case 'Xác nhận giao hàng':
        return 'cyan'
      case 'Chuẩn bị':
        return 'orange'
      case 'Xác nhận':
        return 'purple'
      case 'Tạo mới':
        return 'cyan'
      default:
        return 'gray'
    }
  }
  if (orderInfo.value?.trangThai) {
    return 'green'
  }
  return 'orange'
}

watch(
  () => props.hoaDonId,
  () => {
    if (props.hoaDonId) {
      refreshTimeline()
    }
  },
  { immediate: true }
)

const loadUserInfo = async () => {
  try {
    const response = await getUserInfo()
    if (response.data) {
      currentUser.value = {
        id: response.data.id,
        name: response.data.tenNhanVien,
        tenNhanVien: response.data.tenNhanVien,
        maNhanVien: response.data.maNhanVien,
      }
    }
  } catch (error) {
    console.error('Lỗi khi lấy thông tin user:', error)
    currentUser.value = { id: 0, name: 'Hệ thống', tenNhanVien: 'Hệ thống' }
  }
}

// Helper function để so sánh trạng thái
const matchesStatus = (status: string, ...patterns: string[]): boolean => {
  if (!status) return false
  const normalized = status.trim().toLowerCase()
  return patterns.some((pattern) => {
    const normalizedPattern = pattern.trim().toLowerCase()
    return normalized === normalizedPattern || normalized.includes(normalizedPattern) || normalizedPattern.includes(normalized)
  })
}

const availableActions = computed(() => {
  if (timelineData.value.length === 0) {
    console.log(' Timeline trống, không có actions')
    return []
  }

  const lastStatus = timelineData.value[timelineData.value.length - 1]
  const currentStatus = lastStatus.trangThaiMoi || lastStatus.hanhDong || ''
  const actions: any[] = []

  console.log(' Tính toán availableActions:')
  console.log('  - Timeline length:', timelineData.value.length)
  console.log('  - Last item:', {
    id: lastStatus.id,
    trangThaiMoi: lastStatus.trangThaiMoi,
    hanhDong: lastStatus.hanhDong,
    currentStatus: currentStatus,
  })

  // Xác định các action có sẵn dựa trên trạng thái hiện tại
  // Sử dụng cả trangThaiMoi và hanhDong để matching linh hoạt hơn
  if (matchesStatus(currentStatus, 'Tạo đơn hàng', 'Tạo mới', 'tạo đơn', 'tạo mới')) {
    actions.push({
      key: 'confirm',
      label: 'Xác nhận đơn hàng',
      type: 'primary',
      icon: 'IconCheckCircle',
      nextStatus: 'Xác nhận đơn hàng',
      action: 'Xác nhận',
      moTa: 'Nhân viên xác nhận đơn hàng',
      ghiChu: 'Kiểm tra thông tin khách hàng',
    })
  }

  // Check cả trangThaiMoi và hanhDong
  const isConfirmed =
    matchesStatus(currentStatus, 'Xác nhận đơn hàng', 'Xác nhận', 'xác nhận đơn', 'xác nhận') ||
    matchesStatus(lastStatus.hanhDong || '', 'Xác nhận', 'xác nhận')

  if (isConfirmed) {
    actions.push({
      key: 'prepare',
      label: 'Chuẩn bị hàng',
      type: 'primary',
      icon: 'IconEdit',
      nextStatus: 'Đang chuẩn bị hàng',
      action: 'Chuẩn bị',
      moTa: 'Bắt đầu chuẩn bị sản phẩm',
      ghiChu: 'Lấy hàng từ kho',
    })
  }

  if (matchesStatus(currentStatus, 'Đang chuẩn bị hàng', 'Chuẩn bị', 'chuẩn bị hàng', 'chuẩn bị')) {
    actions.push({
      key: 'confirmShipping',
      label: 'Xác nhận giao hàng',
      type: 'primary',
      icon: 'IconCheckCircle',
      nextStatus: 'Xác nhận giao hàng',
      action: 'Xác nhận giao hàng',
      moTa: 'Xác nhận đơn hàng đã sẵn sàng để giao',
      ghiChu: 'Kiểm tra hàng hóa và địa chỉ giao hàng',
    })
  }

  if (matchesStatus(currentStatus, 'Xác nhận giao hàng', 'xác nhận giao')) {
    actions.push({
      key: 'shipping',
      label: 'Giao hàng',
      type: 'primary',
      icon: 'IconClockCircle',
      nextStatus: 'Đang giao hàng',
      action: 'Giao hàng',
      moTa: 'Đơn hàng đang được giao',
      ghiChu: 'Giao cho shipper',
    })
  }

  if (matchesStatus(currentStatus, 'Đang giao hàng', 'Giao hàng', 'giao hàng')) {
    actions.push({
      key: 'complete',
      label: 'Hoàn thành',
      type: 'primary',
      icon: 'IconCheck',
      nextStatus: 'Hoàn thành',
      action: 'Hoàn thành',
      moTa: 'Đơn hàng đã giao thành công',
      ghiChu: 'Khách hàng đã nhận hàng',
    })
  }

  console.log(
    ' Available actions:',
    actions.length,
    actions.map((a) => a.label)
  )
  if (actions.length === 0) {
    console.warn(' Không tìm thấy action phù hợp cho trạng thái:', {
      currentStatus,
      trangThaiMoi: lastStatus.trangThaiMoi,
      hanhDong: lastStatus.hanhDong,
    })
  }
  return actions
})

const handleStatusChange = async (action: any) => {
  const id = getHoaDonId()
  if (!id) {
    console.error('Không có ID đơn hàng')
    alert('Không có ID đơn hàng')
    return
  }

  if (!currentUser.value) {
    await loadUserInfo()
  }

  const lastStatus = timelineData.value[timelineData.value.length - 1]

  try {
    actionLoading.value = action.key

    // Format thời gian giống như timeline-helper (YYYY-MM-DD HH:mm:ss)
    // Đảm bảo thời gian hiện tại để item mới luôn ở cuối sau khi sort
    const now = new Date().toISOString().slice(0, 19).replace('T', ' ')
    const nowTimestamp = Date.now() // Timestamp để sort

    const timelineDataPayload = {
      idHoaDon: id,
      idNhanVien: currentUser.value?.id || orderInfo.value?.idNhanVien || 0,
      tenNhanVien: currentUser.value?.tenNhanVien || currentUser.value?.name || orderInfo.value?.tenNhanVien || 'Hệ thống',
      trangThaiCu: lastStatus?.trangThaiMoi ?? '',
      trangThaiMoi: action.nextStatus,
      hanhDong: action.action,
      moTa: action.moTa || '',
      ghiChu: action.ghiChu || '',
      thoiGian: now,
    }

    console.log('Đang tạo timeline với dữ liệu:', timelineDataPayload)

    try {
      // Thử tạo timeline qua API
      const timelineItem = await createTimelineItem(timelineDataPayload)
      console.log('Đã tạo timeline mới qua API:', timelineItem)

      // Thêm timeline mới vào danh sách ngay lập tức để hiển thị
      // Convert thoiGian từ response (có thể là Instant string hoặc number) sang string
      let thoiGianString = now
      if (timelineItem?.thoiGian) {
        if (typeof timelineItem.thoiGian === 'number') {
          // Nếu là timestamp (milliseconds)
          thoiGianString = new Date(timelineItem.thoiGian).toISOString().slice(0, 19).replace('T', ' ')
        } else if (typeof timelineItem.thoiGian === 'string') {
          // Nếu đã là string, kiểm tra format
          if (timelineItem.thoiGian.includes('T') || timelineItem.thoiGian.includes('Z')) {
            // ISO format, convert to YYYY-MM-DD HH:mm:ss
            try {
              thoiGianString = new Date(timelineItem.thoiGian).toISOString().slice(0, 19).replace('T', ' ')
            } catch {
              thoiGianString = now
            }
          } else {
            // Đã đúng format YYYY-MM-DD HH:mm:ss
            thoiGianString = timelineItem.thoiGian
          }
        }
      }

      // Đảm bảo thoiGianString là thời gian hiện tại nếu không có từ API
      if (!thoiGianString || thoiGianString === now) {
        thoiGianString = now
      }

      const newTimelineItem: TimelineItem = {
        id: timelineItem?.id || nowTimestamp,
        idHoaDon: timelineItem?.idHoaDon || id,
        idNhanVien: timelineItem?.idNhanVien || currentUser.value?.id || orderInfo.value?.idNhanVien || 0,
        tenNhanVien:
          timelineItem?.tenNhanVien ||
          currentUser.value?.tenNhanVien ||
          currentUser.value?.name ||
          orderInfo.value?.tenNhanVien ||
          'Hệ thống',
        trangThaiCu: timelineItem?.trangThaiCu || lastStatus?.trangThaiMoi || undefined,
        trangThaiMoi: timelineItem?.trangThaiMoi || action.nextStatus,
        hanhDong: timelineItem?.hanhDong || action.action,
        moTa: timelineItem?.moTa || action.moTa || '',
        ghiChu: timelineItem?.ghiChu || action.ghiChu || '',
        thoiGian: thoiGianString,
        ipAddress: timelineItem?.ipAddress,
        userAgent: timelineItem?.userAgent,
      }

      console.log(' New timeline item được tạo:', {
        id: newTimelineItem.id,
        trangThaiMoi: newTimelineItem.trangThaiMoi,
        hanhDong: newTimelineItem.hanhDong,
        thoiGian: newTimelineItem.thoiGian,
        nowTimestamp: nowTimestamp,
      })

      // Thêm vào timeline hiện tại ngay lập tức để UI cập nhật
      // Tạo array mới để đảm bảo Vue reactivity
      const updatedTimeline = [...timelineData.value, newTimelineItem]

      // Log trước khi sort để debug
      console.log(
        ' Trước khi sort:',
        updatedTimeline.map((item) => ({
          id: item.id,
          trangThaiMoi: item.trangThaiMoi,
          thoiGian: item.thoiGian,
        }))
      )

      // Sắp xếp lại timeline theo thời gian để đảm bảo thứ tự đúng (tăng dần)
      // Item mới (thêm vào cuối cùng) sẽ có thời gian mới nhất và ID lớn nhất
      updatedTimeline.sort((a, b) => {
        // Parse thời gian: YYYY-MM-DD HH:mm:ss -> Date
        const parseTime = (timeStr: string) => {
          if (!timeStr) return 0
          try {
            // Replace space with T for ISO format
            let normalized = timeStr.includes('T') ? timeStr : timeStr.replace(' ', 'T')
            const date = new Date(normalized)
            const timestamp = isNaN(date.getTime()) ? 0 : date.getTime()

            // Nếu parse thành công, return timestamp
            // Nếu không parse được (timestamp = 0), dùng ID làm fallback
            return timestamp
          } catch (e) {
            console.warn(' Lỗi parse thời gian:', timeStr, e)
            return 0
          }
        }
        const timeA = parseTime(a.thoiGian)
        const timeB = parseTime(b.thoiGian)

        // Nếu cả hai đều không parse được thời gian, sort by ID
        if (timeA === 0 && timeB === 0) {
          return (a.id || 0) - (b.id || 0)
        }

        // Nếu một trong hai không parse được, item có thời gian hợp lệ sẽ được ưu tiên
        if (timeA === 0) return -1 // a không có thời gian -> đưa lên đầu
        if (timeB === 0) return 1 // b không có thời gian -> đưa lên đầu

        // Nếu thời gian bằng nhau, sort by ID
        // Database IDs (dương) sẽ được ưu tiên hơn fallback IDs (âm)
        // Trong database IDs, ID lớn hơn (item mới hơn) sẽ ở cuối
        if (timeA === timeB) {
          // Nếu cả hai đều là database IDs (dương), sort tăng dần
          if ((a.id || 0) > 0 && (b.id || 0) > 0) {
            return (a.id || 0) - (b.id || 0)
          }
          // Nếu một là database ID và một là fallback ID, database ID ở cuối
          if ((a.id || 0) > 0 && (b.id || 0) < 0) return 1
          if ((a.id || 0) < 0 && (b.id || 0) > 0) return -1
          // Cả hai đều là fallback IDs, sort tăng dần
          return (a.id || 0) - (b.id || 0)
        }

        // Sort tăng dần theo thời gian (cũ nhất -> mới nhất)
        return timeA - timeB
      })

      // Log sau khi sort để debug
      console.log(
        ' Sau khi sort:',
        updatedTimeline.map((item) => ({
          id: item.id,
          trangThaiMoi: item.trangThaiMoi,
          hanhDong: item.hanhDong,
          thoiGian: item.thoiGian,
          parsedTime: new Date(item.thoiGian.replace(' ', 'T')).getTime(),
        }))
      )

      // Update với array đã sắp xếp
      timelineData.value = updatedTimeline

      console.log(' Đã thêm timeline mới vào danh sách (từ API):', newTimelineItem)
      console.log(' Tổng số timeline hiện tại:', timelineData.value.length)
      console.log(' Trạng thái mới của timeline:', newTimelineItem.trangThaiMoi, '| Hanh dong:', newTimelineItem.hanhDong)

      // Log để debug availableActions - phải lấy item cuối cùng SAU KHI SẮP XẾP
      const lastItem = timelineData.value[timelineData.value.length - 1]
      console.log(' Last timeline item (SAU KHI SẮP XẾP):', {
        id: lastItem.id,
        trangThaiMoi: lastItem.trangThaiMoi,
        hanhDong: lastItem.hanhDong,
        thoiGian: lastItem.thoiGian,
        isNewItem: lastItem.id === newTimelineItem.id,
      })

      // Verify item mới có ở cuối không
      if (lastItem.id !== newTimelineItem.id) {
        console.error(' LỖI: Item mới KHÔNG ở cuối sau khi sort!')
        console.error('Item mới:', {
          id: newTimelineItem.id,
          trangThaiMoi: newTimelineItem.trangThaiMoi,
          thoiGian: newTimelineItem.thoiGian,
        })
        console.error('Item cuối:', {
          id: lastItem.id,
          trangThaiMoi: lastItem.trangThaiMoi,
          thoiGian: lastItem.thoiGian,
        })
        // Force item mới vào cuối
        timelineData.value = [...timelineData.value.filter((item) => item.id !== newTimelineItem.id), newTimelineItem]
        console.log(' Đã force item mới vào cuối')
      }

      // Log tất cả items để debug
      console.log(
        ' Tất cả timeline items (SAU KHI FIX):',
        timelineData.value.map((item) => ({
          id: item.id,
          trangThaiMoi: item.trangThaiMoi,
          hanhDong: item.hanhDong,
          thoiGian: item.thoiGian,
        }))
      )

      // Force Vue to recalculate computed property by triggering a reactive update
      // This ensures availableActions is recalculated immediately
      await new Promise((resolve) => setTimeout(resolve, 0))

      // Không cần refresh ngay vì API đã trả về data đúng
      // Chỉ refresh nếu muốn đảm bảo đồng bộ sau vài giây (optional)
      // setTimeout(async () => {
      //   console.log(' Đang refresh timeline từ server để đảm bảo đồng bộ...')
      //   try {
      //     const id = getHoaDonId()
      //     if (id) {
      //       const serverTimeline = await fetchTimelineByHoaDonId(id)
      //       if (Array.isArray(serverTimeline) && serverTimeline.length >= timelineData.value.length) {
      //         // Chỉ update nếu server có đủ hoặc nhiều hơn items
      //         timelineData.value = serverTimeline
      //         console.log(' Đã refresh timeline từ server:', serverTimeline.length, 'mốc')
      //       } else {
      //         console.log(' Server trả về ít items hơn, giữ timeline hiện tại')
      //       }
      //     }
      //   } catch (error) {
      //     console.warn(' Lỗi khi refresh timeline từ server, giữ timeline hiện tại:', error)
      //   }
      // }, 3000)

      alert('Cập nhật trạng thái thành công!')
    } catch (apiError: any) {
      // Nếu API lỗi, tạo timeline local tạm thời
      console.warn('API timeline lỗi, tạo timeline local:', apiError?.message)

      const newTimelineItem: TimelineItem = {
        id: Date.now(), // ID tạm thời
        idHoaDon: id,
        idNhanVien: currentUser.value?.id || orderInfo.value?.idNhanVien || 0,
        tenNhanVien: currentUser.value?.tenNhanVien || currentUser.value?.name || orderInfo.value?.tenNhanVien || 'Hệ thống',
        trangThaiCu: lastStatus?.trangThaiMoi || undefined,
        trangThaiMoi: action.nextStatus,
        hanhDong: action.action,
        moTa: action.moTa || '',
        ghiChu: (action.ghiChu || '') + ' (Lưu tạm thời - chờ API backend)',
        thoiGian: now,
      }

      // Thêm vào timeline hiện tại
      // Tạo array mới để đảm bảo Vue reactivity
      const updatedTimeline = [...timelineData.value, newTimelineItem]

      // Sắp xếp lại timeline theo thời gian (tăng dần)
      updatedTimeline.sort((a, b) => {
        // Parse thời gian: YYYY-MM-DD HH:mm:ss -> Date
        const parseTime = (timeStr: string) => {
          if (!timeStr) return 0
          try {
            const normalized = timeStr.includes('T') ? timeStr : timeStr.replace(' ', 'T')
            const date = new Date(normalized)
            const timestamp = isNaN(date.getTime()) ? 0 : date.getTime()
            return timestamp
          } catch {
            return 0
          }
        }
        const timeA = parseTime(a.thoiGian)
        const timeB = parseTime(b.thoiGian)

        // Nếu thời gian bằng nhau, sort by ID
        if (timeA === timeB) {
          return (a.id || 0) - (b.id || 0)
        }

        // Sort tăng dần theo thời gian (cũ nhất -> mới nhất)
        return timeA - timeB
      })

      // Update với array đã sắp xếp
      timelineData.value = updatedTimeline

      console.log(' Đã thêm timeline local:', newTimelineItem)
      console.log(' Tổng số timeline hiện tại:', timelineData.value.length)
      console.log(' Trạng thái mới của timeline:', newTimelineItem.trangThaiMoi, '| Hanh dong:', newTimelineItem.hanhDong)

      // Log để debug availableActions - phải lấy item cuối cùng SAU KHI SẮP XẾP
      const lastItem = timelineData.value[timelineData.value.length - 1]
      console.log(' Last timeline item (local, SAU KHI SẮP XẾP):', {
        id: lastItem.id,
        trangThaiMoi: lastItem.trangThaiMoi,
        hanhDong: lastItem.hanhDong,
        thoiGian: lastItem.thoiGian,
      })

      // Log tất cả items để debug
      console.log(
        ' Tất cả timeline items (local):',
        timelineData.value.map((item) => ({
          id: item.id,
          trangThaiMoi: item.trangThaiMoi,
          hanhDong: item.hanhDong,
          thoiGian: item.thoiGian,
        }))
      )

      // Force Vue to recalculate computed property
      await new Promise((resolve) => setTimeout(resolve, 0))

      alert('Cập nhật trạng thái thành công! (Lưu tạm thời - API backend chưa sẵn sàng)')
    }
  } catch (error: any) {
    console.error('Lỗi khi tạo timeline:', error)
    console.error('Chi tiết lỗi:', {
      message: error?.message,
      status: error?.response?.status,
      data: error?.response?.data,
      stack: error?.stack,
    })

    // Hiển thị thông báo lỗi chi tiết hơn
    let errorMessage = 'Lỗi khi cập nhật trạng thái'
    if (error?.response?.data) {
      const errorData = error.response.data
      if (typeof errorData === 'string') {
        errorMessage += ': ' + errorData
      } else if (errorData.message) {
        errorMessage += ': ' + errorData.message
      } else if (errorData.error) {
        errorMessage += ': ' + errorData.error
      }
    } else if (error?.message) {
      errorMessage += ': ' + error.message
    }

    alert(errorMessage)
  } finally {
    actionLoading.value = null
  }
}

onMounted(async () => {
  console.log(' TimelineDonHang component mounted với hoaDonId:', props.hoaDonId)
  await loadUserInfo()
  if (props.hoaDonId) {
    await refreshTimeline()
  } else {
    console.warn(' hoaDonId không có khi mount, timeline sẽ trống')
  }
})

// Watch prop để reload khi hoaDonId thay đổi
watch(
  () => props.hoaDonId,
  async (newId, oldId) => {
    console.log(' hoaDonId thay đổi từ', oldId, '->', newId)
    if (newId) {
      await refreshTimeline()
    } else {
      timelineData.value = []
      loading.value = false
    }
  },
  { immediate: false }
)

// Re-fetch timeline when switching between legacy and invoice APIs
watch(
  () => props.useInvoiceApi,
  async (newValue, oldValue) => {
    if (newValue === oldValue) return
    if (!props.hoaDonId) return
    await refreshTimeline()
  }
)
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

/* Order Info Section */
.order-info-section {
  margin-bottom: 24px;
}

.order-info-card {
  background: #f7f8fa;
  border: 1px solid #e5e6eb;
  border-radius: 8px;
  padding: 16px;
}

.order-info-header {
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e5e6eb;
}

.order-info-header h4 {
  margin: 0;
  color: #1d2129;
  font-size: 14px;
  font-weight: 600;
}

.order-info-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.info-label {
  font-weight: 500;
  color: #4e5969;
  font-size: 14px;
}

.info-value {
  font-weight: 400;
  color: #1d2129;
  font-size: 14px;
  text-align: right;
}

.info-value.amount {
  font-weight: 600;
  color: #00b42a;
  font-size: 16px;
}

/* Timeline Content Wrapper */
.timeline-content-wrapper {
  position: relative;
}

.timeline-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #86909c;
}

.timeline-loading p {
  margin-top: 12px;
  font-size: 14px;
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

/* Timeline Actions */
.timeline-actions {
  margin-top: 24px;
  padding: 16px;
  background: #f7f8fa;
  border: 1px solid #e5e6eb;
  border-radius: 8px;
}

.actions-header {
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e5e6eb;
}

.actions-header h4 {
  margin: 0;
  color: #1d2129;
  font-size: 14px;
  font-weight: 600;
}

.actions-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

/* Responsive */
@media (max-width: 768px) {
  .timeline-container {
    padding: 12px;
  }

  .order-info-card {
    padding: 12px;
  }

  .order-info-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }

  .info-value {
    text-align: left;
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
