<template>
  <div class="product-attribute-color-page">
    <!-- Filters and Search -->
    <a-card class="filters-card">
      <a-form :model="filters" layout="vertical">
        <a-row :gutter="12">
          <a-col :span="8">
            <a-form-item label="Tìm kiếm">
              <a-input v-model="filters.search" placeholder="Tìm theo mã hoặc tên màu sắc..." allow-clear
                @input="searchColors" />
            </a-form-item>
          </a-col>

          <a-col :span="8">
            <a-form-item label="Trạng thái">
              <a-radio-group v-model="filters.status" type="button" @change="searchColors">
                <a-radio value="">Tất cả</a-radio>
                <a-radio value="active">Hoạt động</a-radio>
                <a-radio value="inactive">Không hoạt động</a-radio>
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
          <a-button @click="exportColors">
            <template #icon>
              <icon-download />
            </template>
            Xuất Excel
          </a-button>
          <a-button type="primary" @click="showCreateModal">
            <template #icon>
              <icon-plus />
            </template>
            Thêm Màu sắc
          </a-button>
        </a-space>
      </div>
    </a-card>

    <!-- Colors Table -->
    <a-card title="Danh sách màu sắc" class="table-card">
      <a-table :columns="columns" :data="colors" :pagination="pagination" :loading="loading" :scroll="{ x: 1000 }"
        @page-change="getMauSacPage($event - 1)" @page-size-change="
          (size) => {
            pagination.pageSize = size
            getMauSacPage(0)
          }
        ">
        <template #stt="{ rowIndex }">
          <div>{{ rowIndex + 1 }}</div>
        </template>

        <template #code="{ record }">
          <span>{{ record.maMauSac }}</span>
        </template>

        <template #name="{ record }">
          <span>{{ record.tenMauSac }}</span>
        </template>

        <template #color_preview="{ record }">
          <div class="color-display">
            <div class="color-circle-small" :style="{ backgroundColor: record.maMau }"></div>
            <span class="color-hex">{{ record.maMau?.toUpperCase() }}</span>
          </div>
        </template>

        <template #status="{ record }">
          <a-tag :color="record.trangThai ? 'green' : 'red'">
            {{ record.trangThai ? 'Hoạt động' : 'Không hoạt động' }}
          </a-tag>
        </template>

        <template #action="{ record }">
          <a-space>
            <a-tooltip content="Thay đổi trạng thái">
              <a-switch :model-value="record.trangThai" type="round" @click="toggleStatus(record)"
                :loading="record.updating">
                <template #checked-icon>
                  <icon-check />
                </template>
                <template #unchecked-icon>
                  <icon-close />
                </template>
              </a-switch>
            </a-tooltip>
            <a-button type="text" @click="viewColor(record)">
              <template #icon>
                <icon-eye />
              </template>
            </a-button>
            <a-button type="text" @click="editColor(record)">
              <template #icon>
                <icon-edit />
              </template>
            </a-button>
            <!-- <a-button type="text" danger @click="deleteColor(record)">
              <template #icon>
                <icon-delete />
              </template>
            </a-button> -->
          </a-space>
        </template>
      </a-table>
    </a-card>

    <!-- Add Color Modal -->
    <a-modal v-model:visible="addModalVisible" title="Thêm màu sắc" width="600px" :mask-closable="false"
      :closable="true" @cancel="closeAddModal" @ok="confirmAddColor">
      <a-form :model="colorForm" :rules="formRules" layout="vertical" ref="addFormRef">
        <a-form-item>
          <template #label>
            <span>Tên màu sắc</span>
          </template>
          <a-input v-model="colorForm.tenMauSac" placeholder="Tên màu sẽ tự động cập nhật khi chọn mã màu" readonly />
        </a-form-item>
        <a-form-item>
          <template #label>
            <span class="required-field">Mã màu</span>
          </template>
          <input type="color" v-model="colorForm.maMau" class="arco-input" style="width: 100%; height: 32px"
            @input="onColorChange" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Detail Color Modal -->
    <a-modal v-model:visible="detailModalVisible" title="Chi tiết màu sắc" width="600px" :mask-closable="false"
      :closable="true" @cancel="closeDetailModal" @ok="closeDetailModal" ok-text="Đóng"
      :cancel-button-props="{ style: { display: 'none' } }">
      <a-descriptions :column="1" size="small">
        <a-descriptions-item label="Mã màu sắc">{{ selectedColor?.maMauSac }}</a-descriptions-item>
        <a-descriptions-item label="Tên màu sắc">{{ selectedColor?.tenMauSac }}</a-descriptions-item>
        <a-descriptions-item label="Mã màu">{{ selectedColor?.maMau }}</a-descriptions-item>
        <a-descriptions-item label="Trạng thái">
          <a-tag :color="selectedColor?.trangThai ? 'green' : 'red'">
            {{ selectedColor?.trangThai ? 'Hoạt động' : 'Không hoạt động' }}
          </a-tag>
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- Update Color Modal -->
    <a-modal v-model:visible="updateModalVisible" title="Cập nhật màu sắc" width="600px" :mask-closable="false"
      :closable="true" @cancel="closeUpdateModal" @ok="confirmUpdateColor">
      <a-form :model="colorForm" :rules="formRules" layout="vertical" ref="updateFormRef">
        <a-form-item>
          <template #label>
            <span class="required-field">Tên màu sắc</span>
          </template>
          <a-input v-model="colorForm.tenMauSac" placeholder="Tên màu sẽ tự động cập nhật khi chọn mã màu" readonly />
        </a-form-item>
        <a-form-item>
          <template #label>
            <span class="required-field">Mã màu</span>
          </template>
          <input type="color" v-model="colorForm.maMau" class="arco-input" style="width: 100%; height: 32px"
            @input="onColorChange" />
        </a-form-item>
        <a-form-item>
          <template #label>
            <span class="required-field">Trạng thái</span>
          </template>
          <a-radio-group v-model="colorForm.trangThai" type="button">
            <a-radio :value="true">Hoạt động</a-radio>
            <a-radio :value="false">Không hoạt động</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Confirmation Modal -->
    <a-modal v-model:visible="confirmModalVisible" title="Xác nhận" width="400px" :mask-closable="false"
      :closable="true" @cancel="cancelConfirm" @ok="executeConfirmedAction">
      <p>{{ confirmMessage }}</p>
    </a-modal>

    <!-- Status Toggle Confirm Modal -->
    <a-modal v-model:visible="showStatusConfirm" title="Xác nhận thay đổi trạng thái" ok-text="Xác nhận"
      cancel-text="Huỷ" @ok="confirmToggleStatus" @cancel="cancelToggleStatus">
      <template #default>
        <div v-if="colorToToggleStatus">
          <div>Bạn có chắc chắn muốn {{ colorToToggleStatus.trangThai ? 'tạm ngưng' : 'kích hoạt' }} màu sắc này?</div>
          <div>
            Tên màu sắc:
            <strong>{{ colorToToggleStatus.tenMauSac }}</strong>
          </div>
          <div>
            Trạng thái hiện tại:
            <strong>{{ colorToToggleStatus.trangThai ? 'Hoạt động' : 'Không hoạt động' }}</strong>
          </div>
        </div>
      </template>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { Message } from '@arco-design/web-vue'
import { exportToExcel, EXPORT_HEADERS } from '@/utils/export-excel'
import {
  IconPlus,
  IconPalette,
  IconGift,
  IconLocation,
  IconStar,
  IconSearch,
  IconDownload,
  IconEdit,
  IconEye,
  IconDelete,
  IconRefresh,
  IconCheck,
  IconClose,
} from '@arco-design/web-vue/es/icon'
import { useUserStore } from '@/store'
import colorNamer from 'color-namer'
import { createMauSac, getMauSacList, updateMauSac, deleteMauSac, type MauSac } from '../../../../../api/san-pham/thuoc-tinh/mau-sac'

// Breadcrumb setup
const userStore = useUserStore()
// Filters
const filters = ref({
  search: '',
  status: '',
  folder: '',
  sort: 'newest',
})

// Data
const colors = ref<MauSac[]>([])

// Filtered colors computed property
const filteredColors = computed(() => {
  let filtered = [...colors.value]

  // Filter by search term
  if (filters.value.search) {
    const searchTerm = filters.value.search.toLowerCase()
    filtered = filtered.filter(
      (color) =>
        color.maMauSac?.toLowerCase().includes(searchTerm) ||
        color.tenMauSac?.toLowerCase().includes(searchTerm) ||
        color.maMau?.toLowerCase().includes(searchTerm)
    )
  }

  // Filter by status
  if (filters.value.status) {
    const statusFilter = filters.value.status === 'active'
    filtered = filtered.filter((color) => color.trangThai === statusFilter)
  }

  return filtered
})

// Modal states
const addModalVisible = ref(false)
const detailModalVisible = ref(false)
const updateModalVisible = ref(false)
const confirmModalVisible = ref(false)

// Form refs
const addFormRef = ref()
const updateFormRef = ref()

// Selected color for detail/update
const selectedColor = ref<MauSac | null>(null)

// Color form data
const colorForm = reactive({
  tenMauSac: '',
  maMau: '',
  trangThai: true,
})

// Form validation rules
const formRules = {
  tenMauSac: [{ required: true, message: 'Vui lòng chọn mã màu để tự động điền tên màu sắc' }],
  maMau: [
    { required: true, message: 'Vui lòng nhập mã màu' },
    { pattern: /^#[0-9A-F]{6}$/i, message: 'Mã màu phải có định dạng HEX (VD: #FF0000)' },
  ],
  trangThai: [{ required: true, message: 'Vui lòng chọn trạng thái' }],
}

// Hàm phân tích mã màu hex và trả về tên tiếng Việt
const getVietnameseColorFromHex = (hexColor: string): string => {
  // Chuyển hex sang RGB
  const r = parseInt(hexColor.slice(1, 3), 16)
  const g = parseInt(hexColor.slice(3, 5), 16)
  const b = parseInt(hexColor.slice(5, 7), 16)

  // Tính độ sáng và độ bão hòa
  const max = Math.max(r, g, b)
  const min = Math.min(r, g, b)
  const lightness = (max + min) / 2
  const saturation = max === 0 ? 0 : (max - min) / max

  // Xác định màu dựa trên giá trị RGB
  if (saturation < 0.1) {
    // Màu xám/xanh
    if (lightness < 0.2) return 'Đen'
    if (lightness > 0.8) return 'Trắng'
    return 'Xám'
  }

  // Xác định màu chính
  if (r > g && r > b) {
    if (g > b * 1.5) return 'Cam'
    if (b > r * 0.5) return 'Đỏ hồng'
    return 'Đỏ'
  }

  if (g > r && g > b) {
    if (b > g * 0.7) return 'Xanh ngọc'
    return 'Xanh lá'
  }

  if (b > r && b > g) {
    if (r > b * 0.7) return 'Tím'
    return 'Xanh dương'
  }

  if (r === g && g === b) {
    return 'Xám'
  }

  // Màu hỗn hợp
  if (r > 200 && g > 150) return 'Vàng'
  if (r > 150 && b > 150) return 'Tím'
  return 'Màu hỗn hợp'
}

// Color name mapping function
const getColorName = (hexColor: string): string => {
  if (!hexColor || !hexColor.startsWith('#')) return ''

  try {
    const colorResults = colorNamer(hexColor)

    // Mapping tên màu tiếng Anh sang tiếng Việt
    const vietnameseColorNames: Record<string, string> = {
      red: 'Đỏ',
      blue: 'Xanh dương',
      green: 'Xanh lá',
      yellow: 'Vàng',
      orange: 'Cam',
      purple: 'Tím',
      pink: 'Hồng',
      brown: 'Nâu',
      gray: 'Xám',
      grey: 'Xám',
      black: 'Đen',
      white: 'Trắng',
      cyan: 'Xanh ngọc',
      magenta: 'Đỏ tươi',
      lime: 'Chanh',
      maroon: 'Nâu đỏ',
      navy: 'Xanh navy',
      olive: 'Ô liu',
      teal: 'Xanh teal',
      aqua: 'Xanh nước',
      fuchsia: 'Đỏ hồng',
      silver: 'Bạc',
      gold: 'Vàng',
      indigo: 'Chàm',
      violet: 'Tía',
      turquoise: 'Thủy ngọc',
      salmon: 'Hồng cá',
      coral: 'San hô',
      crimson: 'Đỏ thắm',
      khaki: 'Kaki',
      plum: 'Mận',
      orchid: 'Lan',
      lavender: 'Oải hương',
      beige: 'Be',
      mint: 'Bạc hà',
      peach: 'Đào',
      rose: 'Hoa hồng',
      'sky blue': 'Xanh trời',
      'royal blue': 'Xanh hoàng gia',
      'forest green': 'Xanh rừng',
      'sea green': 'Xanh biển',
      'lime green': 'Xanh chanh',
      'dark green': 'Xanh đậm',
      'light blue': 'Xanh nhạt',
      'dark blue': 'Xanh đậm',
      'light green': 'Xanh nhạt',
      'dark red': 'Đỏ đậm',
      'light red': 'Đỏ nhạt',
      'dark yellow': 'Vàng đậm',
      'light yellow': 'Vàng nhạt',
      'dark orange': 'Cam đậm',
      'light orange': 'Cam nhạt',
      'dark purple': 'Tím đậm',
      'light purple': 'Tím nhạt',
      'dark pink': 'Hồng đậm',
      'light pink': 'Hồng nhạt',
      'dark brown': 'Nâu đậm',
      'light brown': 'Nâu nhạt',
      'dark gray': 'Xám đậm',
      'light gray': 'Xám nhạt',
      'dark grey': 'Xám đậm',
      'light grey': 'Xám nhạt',
      aliceblue: 'Xanh nhạt',
      antiquewhite: 'Trắng cổ',
      aquamarine: 'Xanh ngọc biển',
      azure: 'Xanh da trời',
      bisque: 'Vàng nhạt',
      blanchedalmond: 'Hạnh nhân nhạt',
      blueviolet: 'Tím xanh',
      burlywood: 'Nâu gỗ',
      cadetblue: 'Xanh cadet',
      chartreuse: 'Vàng chanh',
      chocolate: 'Sô-cô-la',
      cornflowerblue: 'Xanh hoa cỏ',
      cornsilk: 'Lụa ngô',
      darkcyan: 'Xanh ngọc đậm',
      darkgoldenrod: 'Vàng kim đậm',
      darkkhaki: 'Kaki đậm',
      darkmagenta: 'Đỏ tươi đậm',
      darkolivegreen: 'Ô liu xanh đậm',
      darkorchid: 'Lan đậm',
      darksalmon: 'Hồng cá đậm',
      darkseagreen: 'Xanh biển đậm',
      darkslateblue: 'Xanh đá đậm',
      darkslategray: 'Xám đá đậm',
      darkslategrey: 'Xám đá đậm',
      darkturquoise: 'Thủy ngọc đậm',
      darkviolet: 'Tía đậm',
      deeppink: 'Hồng sâu',
      deepskyblue: 'Xanh trời sâu',
      dimgray: 'Xám mờ',
      dimgrey: 'Xám mờ',
      dodgerblue: 'Xanh dodger',
      firebrick: 'Gạch lửa',
      floralwhite: 'Trắng hoa',
      gainsboro: 'Xám gainsboro',
      ghostwhite: 'Trắng ma',
      goldenrod: 'Vàng kim',
      greenyellow: 'Vàng xanh lá',
      honeydew: 'Mật ong',
      hotpink: 'Hồng nóng',
      indianred: 'Đỏ ấn độ',
      ivory: 'Ngà',
      lavenderblush: 'Hồng oải hương',
      lawngreen: 'Xanh cỏ',
      lemonchiffon: 'Vàng chanh nhạt',
      lightcoral: 'San hô nhạt',
      lightcyan: 'Xanh ngọc nhạt',
      lightgoldenrodyellow: 'Vàng kim nhạt',
      lightpink: 'Hồng nhạt',
      lightsalmon: 'Hồng cá nhạt',
      lightseagreen: 'Xanh biển nhạt',
      lightskyblue: 'Xanh trời nhạt',
      lightslategray: 'Xám đá nhạt',
      lightslategrey: 'Xám đá nhạt',
      lightsteelblue: 'Xanh thép nhạt',
      lightyellow: 'Vàng nhạt',
      linen: 'Vải lanh',
      mediumaquamarine: 'Xanh ngọc trung bình',
      mediumblue: 'Xanh dương trung bình',
      mediumorchid: 'Lan trung bình',
      mediumpurple: 'Tím trung bình',
      mediumseagreen: 'Xanh biển trung bình',
      mediumslateblue: 'Xanh đá trung bình',
      mediumspringgreen: 'Xanh xuân trung bình',
      mediumturquoise: 'Thủy ngọc trung bình',
      mediumvioletred: 'Đỏ tía trung bình',
      midnightblue: 'Xanh nửa đêm',
      mintcream: 'Kem bạc hà',
      mistyrose: 'Hoa hồng sương mù',
      moccasin: 'Da hươu',
      navajowhite: 'Trắng navajo',
      oldlace: 'Ren cũ',
      olivedrab: 'Ô liu xám',
      orangered: 'Cam đỏ',
      palegoldenrod: 'Vàng kim nhạt',
      palegreen: 'Xanh lá nhạt',
      paleturquoise: 'Thủy ngọc nhạt',
      palevioletred: 'Đỏ tía nhạt',
      papayawhip: 'Kem đu đủ',
      peachpuff: 'Hồng đào',
      peru: 'Nâu peru',
      powderblue: 'Xanh bột',
      rosybrown: 'Nâu hồng',
      saddlebrown: 'Nâu yên ngựa',
      sandybrown: 'Nâu cát',
      seashell: 'Vỏ sò',
      sienna: 'Nâu sienna',
      slateblue: 'Xanh đá',
      slategray: 'Xám đá',
      slategrey: 'Xám đá',
      snow: 'Tuyết',
      springgreen: 'Xanh xuân',
      steelblue: 'Xanh thép',
      tan: 'Nâu tan',
      thistle: 'Cây kế',
      tomato: 'Cà chua',
      wheat: 'Lúa mì',
      whitesmoke: 'Khói trắng',
      yellowgreen: 'Vàng xanh lá',
    }

    // Lấy tên màu từ palette 'html' (các tên màu HTML chuẩn)
    const htmlColor = colorResults.html?.[0]
    if (htmlColor && htmlColor.distance < 10) {
      // Chỉ lấy tên màu nếu khoảng cách nhỏ (tương tự)
      const vietnameseName = vietnameseColorNames[htmlColor.name.toLowerCase()]
      if (vietnameseName) {
        return vietnameseName
      }
      // Nếu không có mapping, phân tích mã màu để xác định loại màu
      return getVietnameseColorFromHex(hexColor)
    }

    // Nếu không có tên HTML chuẩn, lấy từ palette 'ntc' (Name That Color)
    const ntcColor = colorResults.ntc?.[0]
    if (ntcColor) {
      const vietnameseName = vietnameseColorNames[ntcColor.name.toLowerCase()]
      if (vietnameseName) {
        return vietnameseName
      }
      // Nếu không có mapping, phân tích mã màu để xác định loại màu
      return getVietnameseColorFromHex(hexColor)
    }

    // Nếu không có tên màu từ thư viện, phân tích trực tiếp từ hex
    return getVietnameseColorFromHex(hexColor)
  } catch (error) {
    console.error('Error getting color name:', error)
    return ''
  }
}

// Watch for color code changes and auto-fill color name
watch(
  () => colorForm.maMau,
  (newColor) => {
    if (newColor && newColor.length === 7) {
      // Luôn cập nhật tên màu khi mã màu thay đổi
      const colorName = getColorName(newColor)
      colorForm.tenMauSac = colorName
    }
  }
)

// Handle color input change
const onColorChange = (event: Event) => {
  const target = event.target as HTMLInputElement
  const newColor = target.value
  if (newColor && newColor.length === 7) {
    const colorName = getColorName(newColor)
    colorForm.tenMauSac = colorName
  }
}

// Confirmation modal
const confirmMessage = ref('')
const confirmAction = ref<'add' | 'update' | 'delete' | null>(null)

// Status toggle modal
const showStatusConfirm = ref(false)
const colorToToggleStatus = ref(null)

// Computed properties for safe access removed

// Table
const loading = ref(false)
const columns = [
  {
    title: 'STT',
    dataIndex: 'stt',
    slotName: 'stt',
    width: 50,
    align: 'center',
  },
  {
    title: 'Mã màu sắc',
    dataIndex: 'code',
    slotName: 'code',
    width: 100,
  },
  {
    title: 'Tên màu sắc',
    dataIndex: 'name',
    width: 120,
    slotName: 'name',
  },
  {
    title: 'Màu sắc',
    dataIndex: 'color_preview',
    slotName: 'color_preview',
    width: 120,
    align: 'center',
  },
  {
    title: 'Trạng thái',
    dataIndex: 'is_active',
    slotName: 'status',
    width: 100,
    // align: 'center',
  },
  {
    title: 'Thao tác',
    slotName: 'action',
    width: 120,
    fixed: 'right',
    align: 'center',
  },
]

// Pagination
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 3,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: true,
})

// Methods
const getColorColor = (color: string) => {
  switch (color) {
    case 'red':
      return 'red'
    case 'blue':
      return 'blue'
    case 'green':
      return 'green'
    case 'yellow':
      return 'orange'
    case 'black':
      return 'gray'
    default:
      return 'gray'
  }
}

const resetFilters = () => {
  filters.value = {
    search: '',
    status: '',
    folder: '',
    sort: 'newest',
  }
}

const searchColors = () => {
  // TODO: Implement search functionality
}

const showCreateModal = () => {
  colorForm.tenMauSac = ''
  colorForm.maMau = '#000000' // Màu đen mặc định
  colorForm.trangThai = true
  addModalVisible.value = true
}

const closeAddModal = () => {
  addModalVisible.value = false
  addFormRef.value?.resetFields()
}

const confirmAddColor = () => {
  addFormRef.value
    ?.validate()
    .then(() => {
      confirmMessage.value = 'Bạn có chắc chắn muốn thêm màu sắc này?'
      confirmAction.value = 'add'
      confirmModalVisible.value = true
    })
    .catch(() => {
      // Validation failed
    })
}

const viewColor = (color: any) => {
  selectedColor.value = color
  detailModalVisible.value = true
}

const closeDetailModal = () => {
  detailModalVisible.value = false
  selectedColor.value = null
}

const editColor = (color: any) => {
  selectedColor.value = color
  colorForm.tenMauSac = color.tenMauSac
  colorForm.maMau = color.maMau
  colorForm.trangThai = color.trangThai
  updateModalVisible.value = true
}

const closeUpdateModal = () => {
  updateModalVisible.value = false
  updateFormRef.value?.resetFields()
  selectedColor.value = null
}

const confirmUpdateColor = () => {
  updateFormRef.value
    ?.validate()
    .then(() => {
      confirmMessage.value = 'Bạn có chắc chắn muốn cập nhật màu sắc này?'
      confirmAction.value = 'update'
      confirmModalVisible.value = true
    })
    .catch(() => {
      // Validation failed
    })
}

const deleteColor = (color: any) => {
  selectedColor.value = color
  confirmMessage.value = 'Bạn có chắc chắn muốn xóa màu sắc này?'
  confirmAction.value = 'delete'
  confirmModalVisible.value = true
}

const cancelConfirm = () => {
  confirmModalVisible.value = false
  confirmMessage.value = ''
  confirmAction.value = null
}

// Toggle status function - show confirm first
const toggleStatus = (record: any) => {
  colorToToggleStatus.value = record
  showStatusConfirm.value = true
}

// Actual toggle status implementation
const performToggleStatus = async (record: any) => {
  try {
    // Set loading state for this specific record
    record.updating = true

    // Call API to update color status
    const updateData = {
      tenMauSac: record.tenMauSac,
      maMau: record.maMau,
      trangThai: !record.trangThai, // Toggle status
      deleted: record.deleted,
      createAt: record.createAt,
      createBy: record.createBy,
      updateAt: new Date().toISOString().split('T')[0],
      updateBy: userStore.id,
    }

    const response = await updateMauSac(record.id, updateData)

    if (response.success || response.status === 200) {
      // Update local data immediately for better UX
      record.trangThai = !record.trangThai

      // Update in colors array
      const index = colors.value.findIndex((c) => c.id === record.id)
      if (index !== -1) {
        colors.value[index].trangThai = record.trangThai
      }

      const statusText = record.trangThai ? 'Hoạt động' : 'Không hoạt động'
      Message.success(`Đã cập nhật trạng thái thành: ${statusText}`)
    } else {
      console.error('API response not successful:', response)
      Message.error('Cập nhật trạng thái thất bại')
    }
  } catch (error) {
    console.error('Error toggling status:', error)
    Message.error('Có lỗi xảy ra khi cập nhật trạng thái')
  } finally {
    // Remove loading state
    record.updating = false
  }
}

const confirmToggleStatus = async () => {
  await performToggleStatus(colorToToggleStatus.value)
  showStatusConfirm.value = false
  colorToToggleStatus.value = null
}

const cancelToggleStatus = () => {
  showStatusConfirm.value = false
  colorToToggleStatus.value = null
}

const getMauSacPage = async (page: number) => {
  try {
    const res = await getMauSacList(page, pagination.value.pageSize)
    if (res.success) {
      colors.value = (res.data.data || []).sort((a: any, b: any) => (b.id || 0) - (a.id || 0))
      pagination.value.total = res.data.totalElements
      pagination.value.pageSize = res.data.pageSize
      pagination.value.current = res.data.currentPage + 1
    } else {
      console.error('Failed to fetch colors:', res.message)
      colors.value = []
      pagination.value.total = 0
      pagination.value.pageSize = 10
      pagination.value.current = 1
    }
  } catch (error) {
    console.error('Failed to fetch colors:', error)
  }
}

const loadColorsWithUpdatedFirst = async (updatedId?: number, isNewItem: boolean = false) => {
  try {
    const res = await getMauSacList(0, 9)
    if (res.success) {
      let colorsData = res.data.data
      pagination.value.total = res.data.totalElements
      pagination.value.pageSize = res.data.pageSize
      pagination.value.current = res.data.currentPage + 1

      // If there's an updated item and it's not a new item, move it to the front
      if (updatedId && !isNewItem) {
        const updatedIndex = colorsData.findIndex((color) => color.id === updatedId)
        if (updatedIndex > 0) {
          const updatedItem = colorsData.splice(updatedIndex, 1)[0]
          colorsData = [updatedItem, ...colorsData.slice(0, updatedIndex), ...colorsData.slice(updatedIndex)]
        }
      }

      colors.value = colorsData
    } else {
      console.error('Failed to fetch colors:', res.message)
      colors.value = []
      pagination.value.total = 0
      pagination.value.pageSize = 10
      pagination.value.current = 1
    }
  } catch (error) {
    console.error('Failed to fetch colors:', error)
  }
}

const executeConfirmedAction = async () => {
  try {
    if (confirmAction.value === 'add') {
      // TODO: Implement add API call
      const data = {
        tenMauSac: colorForm.tenMauSac,
        maMau: colorForm.maMau,
        trangThai: true,
        deleted: false,
        createAt: new Date().toISOString().split('T')[0],
        createBy: userStore.id,
      }
      const res = await createMauSac(data)
      closeAddModal()
      // Load data with new item first (always load from page 0 for new items)
      loadColorsWithUpdatedFirst(undefined, true)
      Message.success('Thêm màu sắc thành công!')
    } else if (confirmAction.value === 'update') {
      if (!selectedColor.value) return

      // TODO: Implement update API call
      const colorId = selectedColor.value.id
      const data = {
        tenMauSac: colorForm.tenMauSac,
        maMau: colorForm.maMau,
        trangThai: colorForm.trangThai,
        deleted: selectedColor.value.deleted,
        createAt: selectedColor.value.createAt,
        createBy: selectedColor.value.createBy,
        updateAt: new Date().toISOString().split('T')[0],
        updateBy: userStore.id,
      }
      await updateMauSac(colorId, data)
      closeUpdateModal()
      // Load data with updated item first
      loadColorsWithUpdatedFirst(colorId, false)
      Message.success('Cập nhật màu sắc thành công!')
    } else if (confirmAction.value === 'delete') {
      if (!selectedColor.value) return

      // TODO: Implement delete API call
      await deleteMauSac(selectedColor.value.id)
      // Refresh data
      loadColorsWithUpdatedFirst()
      Message.success('Xóa màu sắc thành công!')
    }
  } catch (error) {
    console.error('API call failed:', error)
    Message.error('Có lỗi xảy ra. Vui lòng thử lại!')
  } finally {
    confirmModalVisible.value = false
    confirmMessage.value = ''
    confirmAction.value = null
  }
}

const formatDate = (dateString: string) => {
  if (!dateString) return 'N/A'
  return new Date(dateString).toLocaleDateString('vi-VN')
}

const exportColors = () => {
  try {
    if (!colors.value || colors.value.length === 0) {
      Message.warning('Không có dữ liệu để xuất Excel')
      return
    }

    exportToExcel(colors.value, EXPORT_HEADERS.MAU_SAC, 'mau-sac')
  } catch (error) {
    console.error('Lỗi khi xuất Excel:', error)
    Message.error('Có lỗi xảy ra khi xuất Excel')
  }
}

onMounted(() => {
  getMauSacPage(0)
})
</script>

<style scoped>
.product-attribute-color-page {
  padding: 16px 20px;
}

.filters-card,
.table-card {
  margin-bottom: 16px;
}

.actions-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.color-display {
  display: flex;
  align-items: center;
  gap: 8px;
}

.color-circle-small {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 1px solid #d9d9d9;
}

.color-hex {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  color: #86909c;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.header-left {
  flex: 1;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #1d2129;
  margin: 0 0 8px 0;
}

.page-description {
  font-size: 16px;
  color: #86909c;
  margin: 0;
}

.header-right {
  display: flex;
  gap: 16px;
}

.active-icon {
  color: #52c41a;
}

.colors-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.color-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border: 1px solid #f0f0f0;

  background: #fff;
  cursor: pointer;
}

.color-item .color-preview {
  margin-right: 16px;
}

.color-circle {
  width: 40px;
  height: 40px;

  border: 2px solid #e8e8e8;
}

.color-info {
  flex: 1;
}

.color-name {
  font-weight: 600;
  color: #1d2129;
  margin-bottom: 4px;
}

.color-hex {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  color: #86909c;
  margin-bottom: 8px;
}

.color-usage {
  font-size: 12px;
}

.color-actions {
  margin-left: 16px;
}

.color-picker {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 8px;
  margin-top: 8px;
}

.preset-color {
  width: 32px;
  height: 32px;

  border: 2px solid #e8e8e8;
  cursor: pointer;
}

.preset-color .color-preview-small {
  width: 20px;
  height: 20px;

  border: 1px solid #d9d9d9;
}

.danger-item {
  color: #ff4d4f;
}

/* Custom required field styling */
.required-field::after {
  content: ' *' !important;
  color: #f53f3f !important;
  font-weight: bold !important;
}

/* Responsive */
@media (max-width: 768px) {
  .product-attribute-color-page {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    gap: 16px;
  }

  .colors-grid {
    grid-template-columns: 1fr;
  }

  .color-item {
    flex-direction: column;
    text-align: center;
  }

  .color-preview {
    margin-right: 0;
    margin-bottom: 12px;
  }

  .color-actions {
    margin-left: 0;
    margin-top: 12px;
  }

  .header-right {
    width: 100%;
    justify-content: center;
  }
}
</style>
