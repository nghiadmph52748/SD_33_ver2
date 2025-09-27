<template>
  <div class="add-product-page">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- Product Form -->
    <a-card title="Thông tin sản phẩm" class="product-form-card">
      <a-form :model="formData" :rules="formRules" ref="formRef" layout="vertical">
        <!-- Basic Information -->
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="Tên sản phẩm" field="name" required>
              <a-input v-model="formData.name" placeholder="Nhập tên sản phẩm" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="Nhà sản xuất" field="manufacturer" required>
              <a-select v-model="formData.manufacturer" placeholder="Chọn nhà sản xuất">
                <a-option value="nike">Nike</a-option>
                <a-option value="adidas">Adidas</a-option>
                <a-option value="gucci">Gucci</a-option>
                <a-option value="puma">Puma</a-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="Xuất xứ" field="origin" required>
              <a-select v-model="formData.origin" placeholder="Chọn xuất xứ">
                <a-option value="viet-nam">Việt Nam</a-option>
                <a-option value="trung-quoc">Trung Quốc</a-option>
                <a-option value="my">Mỹ</a-option>
                <a-option value="duc">Đức</a-option>
                <a-option value="y">Ý</a-option>
                <a-option value="phap">Pháp</a-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="Chất liệu" field="material" required>
              <a-select v-model="formData.material" placeholder="Chọn chất liệu">
                <a-option v-for="option in materialOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </a-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="Đế giày" field="shoeSole" required>
              <a-select v-model="formData.shoeSole" placeholder="Chọn đế giày">
                <a-option v-for="option in shoeSoleOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </a-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-card>

    <!-- Product Attributes -->
    <a-card title="Thuộc tính sản phẩm" class="product-form-card" style="margin-top: 16px">
      <a-form :model="formData" :rules="formRules" ref="attributesFormRef" layout="vertical">
        <!-- Colors -->
        <a-row :gutter="16" class="attribute-row">
          <a-col :span="24">
            <a-form-item label="Màu sắc" required>
              <div class="attribute-selector">
                <a-button type="outline" @click="showColorModal" class="select-attribute-btn">
                  <template #icon>
                    <icon-palette />
                  </template>
                  Chọn màu sắc ({{ formData.colors.length }})
                </a-button>
                <div class="selected-items">
                  <a-tag v-for="color in formData.colors" :key="color" closable @close="removeColor(color)" class="color-tag">
                    {{ colorOptions.find((c) => c.value === color)?.label }}
                  </a-tag>
                </div>
              </div>
            </a-form-item>
          </a-col>
        </a-row>

        <!-- Sizes -->
        <a-row :gutter="16" class="attribute-row">
          <a-col :span="24">
            <a-form-item label="Kích thước" required>
              <div class="attribute-selector">
                <a-button type="outline" @click="showSizeModal" class="select-attribute-btn">
                  <template #icon>
                    <icon-expand />
                  </template>
                  Chọn kích thước ({{ formData.sizes.length }})
                </a-button>
                <div class="selected-items">
                  <a-tag v-for="size in formData.sizes" :key="size" closable @close="removeSize(size)" class="size-tag">
                    {{ sizeOptions.find((s) => s.value === size)?.label }}
                  </a-tag>
                </div>
              </div>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-card>

    <!-- Product Variants -->
    <a-card v-if="variants.length > 0" title="Biến thể sản phẩm" class="product-form-card" style="margin-top: 16px">
      <div class="variants-header">
        <a-button type="primary" @click="showQuickAddModal">
          <template #icon>
            <icon-plus />
          </template>
          Thêm nhanh
        </a-button>
      </div>
      <div v-for="colorVariant in variants" :key="colorVariant.color" class="color-variant">
        <h4>{{ colorOptions.find((c) => c.value === colorVariant.color)?.label }}</h4>
        <a-table :columns="variantColumns" :data="colorVariant.variants" :pagination="false" size="small" class="variant-table">
          <template #manufacturer="{ record }">
            {{ manufacturerOptions.find((m) => m.value === record.manufacturer)?.label || record.manufacturer }}
          </template>
          <template #origin="{ record }">
            {{ originOptions.find((o) => o.value === record.origin)?.label || record.origin }}
          </template>
          <template #weight="{ record }">
            <a-input-number v-model="record.weight" :min="0" :step="0.1" size="small" style="width: 100px" :precision="1" />
          </template>
          <template #stock="{ record }">
            <a-input-number v-model="record.stock" :min="0" size="small" style="width: 80px" />
          </template>
          <template #price="{ record }">
            <a-input-number
              v-model="record.price"
              :min="0"
              :step="1000"
              size="small"
              style="width: 100px"
              :formatter="(value) => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')"
            />
          </template>
          <template #attributes="{ record }">
            <a-tooltip placement="top">
              <template #content>
                <div class="attributes-tooltip">
                  <div v-for="attr in record.attributes || []" :key="attr.label" class="attribute-item">
                    <span class="attribute-label">{{ attr.label }}:</span>
                    <span class="attribute-value">{{ attr.value }}</span>
                  </div>
                </div>
              </template>
              <div class="attributes-display">
                <span class="primary-attr">
                  {{ record.attributes?.find((a) => a.label === 'Màu sắc')?.value || 'N/A' }}/
                  {{ record.attributes?.find((a) => a.label === 'Kích thước')?.value || 'N/A' }}
                </span>
                <span
                  v-if="
                    record.attributes?.some((a) => a.label === 'Chất liệu' && a.value !== 'Chưa chọn') ||
                    record.attributes?.some((a) => a.label === 'Đế giày' && a.value !== 'Chưa chọn')
                  "
                  class="more-attrs"
                >
                  +{{
                    record.attributes?.filter((a) => (a.label === 'Chất liệu' || a.label === 'Đế giày') && a.value !== 'Chưa chọn')
                      .length || 0
                  }}
                </span>
              </div>
            </a-tooltip>
          </template>
          <template #images="{ record }">
            <div class="image-upload-container">
              <a-upload
                v-model:file-list="record.images"
                list-type="picture-card"
                :multiple="true"
                :max="5"
                accept="image/*"
                :show-upload-list="{ showPreviewIcon: true, showRemoveIcon: true }"
                class="variant-image-upload"
              >
                <template #upload-button>
                  <div v-if="record.images.length < 5" class="upload-button">
                    <icon-plus />
                    <div>Ảnh</div>
                  </div>
                </template>
              </a-upload>
            </div>
          </template>
          <template #actions="{ record }">
            <a-button type="text" size="small" @click="deleteVariant(record.sku)" danger>
              <template #icon>
                <icon-delete />
              </template>
            </a-button>
          </template>
        </a-table>
      </div>

      <!-- Action Buttons -->
      <div class="variants-actions">
        <a-space>
          <a-button @click="handleCancel">
            <template #icon>
              <icon-close />
            </template>
            Huỷ bỏ
          </a-button>
          <a-button type="primary" @click="handleSubmit" :loading="loading">
            <template #icon>
              <icon-check />
            </template>
            Thêm sản phẩm
          </a-button>
        </a-space>
      </div>
    </a-card>

    <!-- Quick Add Modal -->
    <a-modal v-model:visible="quickAddVisible" title="Thêm nhanh biến thể" width="600px" @ok="handleQuickAdd" @cancel="resetQuickAddForm">
      <a-form :model="quickAddForm" :rules="quickAddRules" ref="quickAddFormRef" layout="vertical">
        <!-- Color Selection -->
        <a-form-item label="Chọn màu sắc áp dụng" field="selectedColors" required>
          <a-checkbox-group v-model="quickAddForm.selectedColors" @change="handleColorSelectionChange">
            <a-checkbox
              v-for="color in formData.colors"
              :key="color"
              :value="color"
              :disabled="quickAddForm.selectedColors.includes('all')"
            >
              {{ colorOptions.find((c) => c.value === color)?.label }}
            </a-checkbox>
            <a-checkbox value="all" style="margin-top: 8px; font-weight: 500">Chọn tất cả màu</a-checkbox>
          </a-checkbox-group>
        </a-form-item>

        <!-- Weight -->
        <a-form-item label="Trọng lượng (kg)" field="weight" required>
          <a-input-number
            v-model="quickAddForm.weight"
            :min="0"
            :step="0.1"
            placeholder="Nhập trọng lượng"
            :precision="1"
            style="width: 100%"
          />
        </a-form-item>

        <!-- Stock -->
        <a-form-item label="Số lượng" field="stock" required>
          <a-input-number v-model="quickAddForm.stock" :min="0" placeholder="Nhập số lượng" style="width: 100%" />
        </a-form-item>

        <!-- Price -->
        <a-form-item label="Đơn giá (VNĐ)" field="price" required>
          <a-input-number
            v-model="quickAddForm.price"
            :min="0"
            :step="1000"
            placeholder="Nhập đơn giá"
            :formatter="(value) => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')"
            :parser="(value) => value.replace(/\$\s?|(,*)/g, '')"
            style="width: 100%"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Color Selection Modal -->
    <a-modal
      v-model:visible="colorModalVisible"
      title="Chọn màu sắc"
      width="500px"
      :mask-closable="false"
      :closable="false"
      @ok="handleColorModalOk"
    >
      <div class="attribute-modal-content">
        <div class="attribute-options-list">
          <div v-for="color in colorOptions" :key="color.value" class="color-option-item">
            <a-checkbox
              :checked="tempColors.includes(color.value)"
              @change="(checked) => handleColorOptionChange(color.value, checked)"
              class="attribute-option-checkbox"
            >
              {{ color.label }}
            </a-checkbox>
            <input type="color" :value="getColorHexValue(color.value)" disabled class="color-preview-input" />
          </div>
        </div>
      </div>
    </a-modal>

    <!-- Size Selection Modal -->
    <a-modal
      v-model:visible="sizeModalVisible"
      title="Chọn kích thước"
      width="500px"
      :mask-closable="false"
      :closable="false"
      @ok="handleSizeModalOk"
    >
      <div class="attribute-modal-content">
        <div class="attribute-options-list">
          <a-checkbox
            v-for="size in sizeOptions"
            :key="size.value"
            :checked="tempSizes.includes(size.value)"
            @change="(checked) => handleSizeOptionChange(size.value, checked)"
            class="attribute-option-checkbox"
          >
            {{ size.label }}
          </a-checkbox>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'
import { IconCheck, IconClose, IconDelete, IconPlus, IconPalette, IconExpand } from '@arco-design/web-vue/es/icon'

// Breadcrumb setup
const { breadcrumbItems } = useBreadcrumb()
const router = useRouter()

// Form state
const loading = ref(false)
const formRef = ref()
const attributesFormRef = ref()

// Quick add modal state
const quickAddVisible = ref(false)
const quickAddFormRef = ref()

// Attribute modal states
const colorModalVisible = ref(false)
const sizeModalVisible = ref(false)
const tempColors = ref<string[]>([])
const tempSizes = ref<string[]>([])

// Attribute change handlers

// Form data
const formData = reactive({
  name: '',
  manufacturer: '',
  origin: '',
  material: '',
  shoeSole: '',
  // Product attributes
  colors: [] as string[],
  sizes: [] as string[],
})

// Quick add form
const quickAddForm = reactive({
  selectedColors: [] as string[],
  weight: 0,
  stock: 0,
  price: 0,
})

// Manufacturer and origin options for display
const manufacturerOptions = [
  { label: 'Nike', value: 'nike' },
  { label: 'Adidas', value: 'adidas' },
  { label: 'Gucci', value: 'gucci' },
  { label: 'Puma', value: 'puma' },
]

const originOptions = [
  { label: 'Việt Nam', value: 'viet-nam' },
  { label: 'Trung Quốc', value: 'trung-quoc' },
  { label: 'Mỹ', value: 'my' },
  { label: 'Đức', value: 'duc' },
  { label: 'Ý', value: 'y' },
  { label: 'Pháp', value: 'phap' },
]

// Color and size options
const colorOptions = [
  { label: 'Đỏ', value: 'red' },
  { label: 'Xanh dương', value: 'blue' },
  { label: 'Xanh lá', value: 'green' },
  { label: 'Vàng', value: 'yellow' },
  { label: 'Đen', value: 'black' },
  { label: 'Trắng', value: 'white' },
  { label: 'Xám', value: 'gray' },
  { label: 'Tím', value: 'purple' },
]

const sizeOptions = [
  { label: '35', value: '35' },
  { label: '36', value: '36' },
  { label: '37', value: '37' },
  { label: '38', value: '38' },
  { label: '39', value: '39' },
  { label: '40', value: '40' },
  { label: '41', value: '41' },
  { label: '42', value: '42' },
  { label: '43', value: '43' },
  { label: '44', value: '44' },
  { label: '45', value: '45' },
]

// Material and shoe sole options
const materialOptions = [
  { label: 'Da thật', value: 'da-that' },
  { label: 'Da tổng hợp', value: 'da-tong-hop' },
  { label: 'Vải', value: 'vai' },
  { label: 'Nhựa', value: 'nhua' },
  { label: 'Caosu', value: 'caosu' },
]

const shoeSoleOptions = [
  { label: 'Đế cao su', value: 'de-cao-su' },
  { label: 'Đế PU', value: 'de-pu' },
  { label: 'Đế EVA', value: 'de-eva' },
  { label: 'Đế TPU', value: 'de-tpu' },
  { label: 'Đế composite', value: 'de-composite' },
]

// Computed: Generate base variants based on selected colors and sizes
const baseVariants = computed(() => {
  if (!formData.colors.length || !formData.sizes.length) return []

  return formData.colors.map((color) => {
    const colorVariants = formData.sizes.map((size) => ({
      // Basic info
      sku: `${formData.name.replace(/\s+/g, '-').toLowerCase()}-${color}-${size}`,
      productName: formData.name,
      manufacturer: formData.manufacturer,
      origin: formData.origin,
      attributes: [
        { label: 'Màu sắc', value: colorOptions.find((c) => c.value === color)?.label || 'N/A' },
        { label: 'Kích thước', value: sizeOptions.find((s) => s.value === size)?.label || 'N/A' },
        { label: 'Chất liệu', value: materialOptions.find((m) => m.value === formData.material)?.label || 'Chưa chọn' },
        { label: 'Đế giày', value: shoeSoleOptions.find((s) => s.value === formData.shoeSole)?.label || 'Chưa chọn' },
      ],
      weight: 0,
      stock: 0,
      price: 0,
      images: [] as any[],
      // For internal use
      color,
      size,
    }))

    return {
      color,
      variants: colorVariants,
    }
  })
})

// Editable variants ref that can be modified
const variants = ref([])

// Sync base variants to editable variants when base variants change
watch(
  baseVariants,
  (newBaseVariants) => {
    variants.value = JSON.parse(JSON.stringify(newBaseVariants)) // Deep clone to avoid reference issues
  },
  { immediate: true }
)

// Delete variant by SKU
const deleteVariant = (sku: string) => {
  // Find which color group this variant belongs to
  const updatedVariants = variants.value.map((colorGroup) => {
    const variantIndex = colorGroup.variants.findIndex((v) => v.sku === sku)
    if (variantIndex > -1) {
      // Create new array without the deleted item to trigger reactivity
      return {
        ...colorGroup,
        variants: colorGroup.variants.filter((v) => v.sku !== sku),
      }
    }
    return colorGroup
  })

  // Update variants to trigger reactivity
  variants.value = updatedVariants
}

// Variant table columns
const variantColumns = [
  {
    title: 'Mã biến thể',
    dataIndex: 'sku',
    width: 120,
  },
  {
    title: 'Tên sản phẩm',
    dataIndex: 'productName',
    width: 150,
  },
  {
    title: 'Nhà sản xuất',
    dataIndex: 'manufacturer',
    width: 120,
  },
  {
    title: 'Xuất xứ',
    dataIndex: 'origin',
    width: 100,
  },
  {
    title: 'Thuộc tính',
    dataIndex: 'attributes',
    width: 120,
  },
  {
    title: 'Trọng lượng (kg)',
    dataIndex: 'weight',
    width: 120,
    slotName: 'weight',
  },
  {
    title: 'Số lượng',
    dataIndex: 'stock',
    width: 100,
    slotName: 'stock',
  },
  {
    title: 'Đơn giá (VNĐ)',
    dataIndex: 'price',
    width: 120,
    slotName: 'price',
  },
  {
    title: 'Ảnh sản phẩm',
    dataIndex: 'images',
    width: 120,
    slotName: 'images',
  },
  {
    title: 'Thao tác',
    dataIndex: 'actions',
    width: 80,
    slotName: 'actions',
  },
]

// Quick add form validation rules
const quickAddRules = {
  selectedColors: [
    { required: true, message: 'Vui lòng chọn ít nhất một màu sắc' },
    { type: 'array', min: 1, message: 'Vui lòng chọn ít nhất một màu sắc' },
  ],
  stock: [
    { required: true, message: 'Vui lòng nhập số lượng' },
    { type: 'number', min: 0, message: 'Số lượng phải lớn hơn hoặc bằng 0' },
  ],
  price: [
    { required: true, message: 'Vui lòng nhập đơn giá' },
    { type: 'number', min: 0, message: 'Đơn giá phải lớn hơn 0' },
  ],
}

// Form validation rules
const formRules = {
  name: [
    { required: true, message: 'Vui lòng nhập tên sản phẩm' },
    { min: 2, max: 100, message: 'Tên sản phẩm phải từ 2-100 ký tự' },
  ],
}

// Methods for managing colors and sizes
const handleColorChange = (selectedColors: string[]) => {
  // Colors are automatically updated via v-model
  formData.colors = selectedColors
}

const handleSizeChange = (selectedSizes: string[]) => {
  // Sizes are automatically updated via v-model
  formData.sizes = selectedSizes
}

// Helper function to get hex color value for color preview
const getColorHexValue = (colorValue: string) => {
  const colorMap: { [key: string]: string } = {
    red: '#FF0000',
    blue: '#0000FF',
    green: '#00FF00',
    yellow: '#FFFF00',
    black: '#000000',
    white: '#FFFFFF',
    gray: '#808080',
    purple: '#800080',
  }
  return colorMap[colorValue] || '#000000'
}

// Color modal methods
const showColorModal = () => {
  tempColors.value = [...formData.colors]
  colorModalVisible.value = true
}

const handleColorModalOk = () => {
  formData.colors = [...tempColors.value]
  colorModalVisible.value = false
}

// Size modal methods
const showSizeModal = () => {
  tempSizes.value = [...formData.sizes]
  sizeModalVisible.value = true
}

const handleSizeModalOk = () => {
  formData.sizes = [...tempSizes.value]
  sizeModalVisible.value = false
}

// Individual option change handlers
const handleColorOptionChange = (colorValue: string, checked: boolean) => {
  if (checked) {
    if (!tempColors.value.includes(colorValue)) {
      tempColors.value.push(colorValue)
    }
  } else {
    const index = tempColors.value.indexOf(colorValue)
    if (index > -1) {
      tempColors.value.splice(index, 1)
    }
  }
}

const handleSizeOptionChange = (sizeValue: string, checked: boolean) => {
  if (checked) {
    if (!tempSizes.value.includes(sizeValue)) {
      tempSizes.value.push(sizeValue)
    }
  } else {
    const index = tempSizes.value.indexOf(sizeValue)
    if (index > -1) {
      tempSizes.value.splice(index, 1)
    }
  }
}

// Remove methods for tags
const removeColor = (color: string) => {
  const index = formData.colors.indexOf(color)
  if (index > -1) {
    formData.colors.splice(index, 1)
  }
}

const removeSize = (size: string) => {
  const index = formData.sizes.indexOf(size)
  if (index > -1) {
    formData.sizes.splice(index, 1)
  }
}

// Update variant stock
const updateVariantStock = (color: string, size: string, stock: number) => {
  // This would be used to update stock for specific variant
  // Implementation depends on how variants are stored
}

// Quick add modal methods
const resetQuickAddForm = () => {
  quickAddForm.selectedColors = []
  quickAddForm.weight = 0
  quickAddForm.stock = 0
  quickAddForm.price = 0
}

const showQuickAddModal = () => {
  quickAddVisible.value = true
  // Reset form
  resetQuickAddForm()
}

// Handle color selection changes
const handleColorSelectionChange = (selectedValues: string[]) => {
  const hasAll = selectedValues.includes('all')
  const otherColors = selectedValues.filter((v) => v !== 'all')

  if (hasAll && otherColors.length > 0) {
    // If "all" is selected along with other colors, remove "all"
    quickAddForm.selectedColors = otherColors
  } else if (hasAll && otherColors.length === 0) {
    // If only "all" is selected, keep it
    quickAddForm.selectedColors = ['all']
  } else {
    // Normal selection
    quickAddForm.selectedColors = otherColors
  }
}

const handleQuickAdd = async () => {
  try {
    await quickAddFormRef.value.validate()

    // Determine which colors to apply to
    let targetColors = quickAddForm.selectedColors
    if (targetColors.includes('all')) {
      targetColors = formData.colors
    }

    // Apply values to variants of selected colors
    variants.value.forEach((colorGroup) => {
      if (targetColors.includes(colorGroup.color)) {
        colorGroup.variants.forEach((variant) => {
          if (quickAddForm.weight > 0) {
            variant.weight = quickAddForm.weight
          }
          variant.stock = quickAddForm.stock
          variant.price = quickAddForm.price
        })
      }
    })

    quickAddVisible.value = false
    resetQuickAddForm()
  } catch (error) {
    // Handle validation error
  }
}

// Methods
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    loading.value = true

    // TODO: Implement product creation API call

    // Simulate API call
    setTimeout(() => {
      loading.value = false
      // Navigate back to product list
      router.push({ name: 'DanhMucSanPham' })
    }, 2000)
  } catch (error) {
    // Handle form validation error
  }
}

const handleCancel = () => {
  router.push({ name: 'DanhMucSanPham' })
}
</script>

<style scoped>
.add-product-page {
  background-color: var(--color-fill-2);
  padding: 16px 20px;
  padding-bottom: 0;
}

.product-form-card {
  background-color: var(--color-bg-2);
  border-radius: 4px;
}

.product-form-card .arco-form-item-label {
  font-weight: 500;
  color: #1d2129;
  margin-bottom: 8px;
}

.attribute-row {
  margin-bottom: 16px;
}

.attribute-selector {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.select-attribute-btn {
  margin-bottom: 8px;
}

.selected-items {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  flex: 1;
}

.color-tag,
.size-tag {
  margin: 0;
}

/* Modal styles */
.attribute-modal-content {
  max-height: 400px;
  overflow-y: auto;
}

.attribute-options-list {
  display: flex;
  flex-direction: column;
}

.color-option-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.attribute-option-checkbox {
  margin: 0 0 8px 0;
  padding: 8px 12px;
  border-radius: 6px;
  border: 1px solid var(--color-border-2);
  transition: all 0.2s;
  background-color: var(--color-bg-2);
  flex: 1;
}

.attribute-option-checkbox:hover {
  background-color: var(--color-fill-1);
  border-color: var(--color-border-3);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* Active/checked state */
.attribute-option-checkbox[aria-checked='true'] {
  background-color: var(--color-primary-1);
  border-color: var(--color-primary-6);
  color: var(--color-primary-6);
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(22, 93, 255, 0.15);
}

.attribute-option-checkbox[aria-checked='true']:hover {
  background-color: var(--color-primary-2);
  border-color: var(--color-primary-7);
  box-shadow: 0 4px 12px rgba(22, 93, 255, 0.25);
}

.color-preview-input {
  width: 40px;
  height: 32px;
  border: 1px solid var(--color-border-2);
  border-radius: 4px;
  margin-left: 12px;
  cursor: pointer;
  background: none;
}

.color-preview-input:disabled {
  cursor: not-allowed;
  opacity: 1;
}

/* Variants Section - Now using a-card instead of custom styling */

.variants-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.variants-header h3 {
  margin: 0;
}

.variants-section h3 {
  margin: 0 0 16px 0;
  color: #1d2129;
  font-size: 16px;
  font-weight: 600;
}

.variants-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--color-border-2);
}

.color-variant {
  margin-bottom: 24px;
}

.color-variant h4 {
  margin: 0 0 12px 0;
  color: #4e5969;
  font-size: 14px;
  font-weight: 500;
  padding-left: 8px;
  border-left: 3px solid #165dff;
}

.variant-table {
  margin-bottom: 16px;
}

.variant-table .arco-table-td {
  padding: 8px 12px;
}

/* Image preview styles */
.image-preview {
  position: relative;
  display: inline-block;
}

.image-preview img {
  border-radius: 4px;
  border: 1px solid var(--color-border-2);
}

.image-count {
  position: absolute;
  top: -8px;
  right: -8px;
  background: var(--color-primary-6);
  color: white;
  border-radius: 50%;
  width: 16px;
  height: 16px;
  font-size: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 500;
}

.no-image {
  color: var(--color-text-3);
  font-size: 12px;
}

/* Image upload container */
.image-upload-container {
  display: flex;
  align-items: center;
}

.variant-image-upload {
  width: auto;
}

.variant-image-upload .arco-upload-list-picture-card .arco-upload-list-picture-card-container {
  width: 60px;
  height: 60px;
  margin-right: 8px;
  margin-bottom: 0;
}

.variant-image-upload .arco-upload-list-picture-card .arco-upload-list-item-picture {
  width: 60px;
  height: 60px;
}

.upload-button {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  border: 1px dashed var(--color-border-2);
  border-radius: 4px;
  background-color: var(--color-fill-1);
  color: var(--color-text-3);
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.upload-button:hover {
  border-color: var(--color-primary-6);
  color: var(--color-primary-6);
}

/* Attributes tooltip styles */
.attributes-tooltip {
  min-width: 200px;
}

.attribute-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
  border-bottom: 1px solid var(--color-border-2);
}

.attribute-item:last-child {
  border-bottom: none;
}

.attribute-label {
  font-weight: 500;
  color: var(--color-text-2);
  font-size: 12px;
  margin-right: 8px;
}

.attribute-value {
  font-weight: 600;
  color: var(--color-text-1);
  font-size: 12px;
}

.attributes-display {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: var(--color-primary-6);
  font-weight: 500;
  font-size: 12px;
}

.primary-attr {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 80px;
}

.more-attrs {
  background-color: var(--color-primary-1);
  color: var(--color-primary-6);
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 10px;
  font-weight: 600;
}

/* Responsive */
@media (max-width: 768px) {
  .add-product-page {
    padding: 16px;
  }
}
</style>
