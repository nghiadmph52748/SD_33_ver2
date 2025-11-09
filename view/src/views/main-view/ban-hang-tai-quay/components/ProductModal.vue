<template>
  <a-modal
    :visible="visible"
    title="Chọn Sản Phẩm"
    width="90%"
    :footer="null"
    @cancel="$emit('cancel')"
    @update:visible="$emit('update:visible', $event)"
  >
    <div style="margin-bottom: 16px">
      <a-input-search
        :model-value="searchText"
        placeholder="Tìm kiếm sản phẩm (tên, mã, màu, kích thước...)"
        allow-clear
        style="width: 100%; margin-bottom: 12px"
        @update:model-value="$emit('update:searchText', $event)"
      />
      <a-row :gutter="[12, 12]">
        <a-col :xs="24" :sm="12" :md="8" :lg="4">
          <a-select
            :model-value="productFilters.tenChatLieu"
            placeholder="Chất liệu"
            allow-clear
            :options="productMaterialOptions"
            size="small"
            @change="$emit('update-filter', { key: 'tenChatLieu', value: $event })"
          />
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="4">
          <a-select
            :model-value="productFilters.tenDeGiay"
            placeholder="Đế"
            allow-clear
            :options="productSoleOptions"
            size="small"
            @change="$emit('update-filter', { key: 'tenDeGiay', value: $event })"
          />
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="4">
          <a-select
            :model-value="productFilters.tenNhaSanXuat"
            placeholder="NSX"
            allow-clear
            :options="productManufacturerOptions"
            size="small"
            @change="$emit('update-filter', { key: 'tenNhaSanXuat', value: $event })"
          />
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="4">
          <a-select
            :model-value="productFilters.tenXuatXu"
            placeholder="Xuất xứ"
            allow-clear
            :options="productOriginOptions"
            size="small"
            @change="$emit('update-filter', { key: 'tenXuatXu', value: $event })"
          />
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="4">
          <a-select
            :model-value="productFilters.tenMauSac"
            placeholder="Màu Sắc"
            allow-clear
            :options="productColorOptions"
            size="small"
            @change="$emit('update-filter', { key: 'tenMauSac', value: $event })"
          />
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="4">
          <a-select
            :model-value="productFilters.tenKichThuoc"
            placeholder="Kích Thước"
            allow-clear
            :options="productSizeOptions"
            size="small"
            @change="$emit('update-filter', { key: 'tenKichThuoc', value: $event })"
          />
        </a-col>
      </a-row>
    </div>

    <a-table
      :key="`products-${productPagination.current}`"
      v-if="filteredProductVariants.length > 0"
      :columns="columns"
      :data="
        filteredProductVariants.map((p, idx) => ({
          ...p,
          stt: (productPagination.current - 1) * productPagination.pageSize + idx + 1,
        }))
      "
      size="small"
      :scroll="{ x: '100%' }"
      :pagination="{
        current: productPagination.current,
        pageSize: productPagination.pageSize,
        total: productPagination.total,
        showTotal: true,
      }"
      :page-position="'bottomCenter'"
      @page-change="$emit('page-change', $event)"
    >
      <template #product="{ record }">
        <div style="display: flex; gap: 8px; align-items: center">
          <div
            v-if="record.anhSanPham && record.anhSanPham.length > 0"
            style="position: relative; flex-shrink: 0; width: 50px; height: 50px"
          >
            <MiniCarousel :images="record.anhSanPham" :autoplay-interval="2500" class="product-modal-carousel" />
            <div
              v-if="record.anhSanPham.length > 1"
              style="
                position: absolute;
                top: 2px;
                right: 2px;
                background: rgba(0, 0, 0, 0.6);
                color: white;
                padding: 2px 4px;
                border-radius: 2px;
                font-size: 9px;
                font-weight: bold;
              "
            >
              +{{ record.anhSanPham.length - 1 }}
            </div>
          </div>
          <div v-else style="width: 50px; height: 50px; flex-shrink: 0; border-radius: 4px">
            <a-image src="" :width="50" :height="50" :preview="false" />
          </div>
          <div>
            <div style="font-weight: 600; font-size: 13px">{{ record.tenSanPham }}</div>
            <div style="font-size: 11px; color: #999">Mã: {{ record.maChiTietSanPham }}</div>
          </div>
        </div>
      </template>
      <template #info="{ record }">
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 8px; font-size: 11px">
          <div>
            <div v-if="record.tenChatLieu" style="margin-bottom: 4px">
              <span style="color: #999">Chất liệu:</span>
              <strong>{{ ' ' + record.tenChatLieu }}</strong>
            </div>
            <div v-if="record.tenDeGiay">
              <span style="color: #999">Đế:</span>
              <strong>{{ ' ' + record.tenDeGiay }}</strong>
            </div>
          </div>
          <div>
            <div v-if="record.tenNhaSanXuat" style="margin-bottom: 4px">
              <span style="color: #999">NSX:</span>
              <strong>{{ ' ' + record.tenNhaSanXuat }}</strong>
            </div>
            <div v-if="record.tenXuatXu">
              <span style="color: #999">Xuất xứ:</span>
              <strong>{{ ' ' + record.tenXuatXu }}</strong>
            </div>
          </div>
        </div>
      </template>
      <template #variant="{ record }">
        <div style="font-size: 12px; display: flex; align-items: center; gap: 12px">
          <div v-if="record.tenMauSac" style="display: flex; align-items: center; gap: 6px">
            <div
              v-if="record.maMau"
              style="width: 20px; height: 20px; border-radius: 3px; border: 1px solid #e5e5e5; background-color: #ffffff; flex-shrink: 0"
              :style="{ backgroundColor: record.maMau }"
              :title="record.maMau"
            />
            <div>
              <div style="font-weight: 600; line-height: 1">{{ record.tenMauSac }}</div>
              <div v-if="record.maMau" style="font-size: 10px; color: #999; line-height: 1">{{ record.maMau }}</div>
            </div>
          </div>
          <span v-if="record.tenMauSac && record.tenKichThuoc" style="color: #d9d9d9">|</span>
          <div v-if="record.tenKichThuoc" style="font-weight: 600">
            {{ record.tenKichThuoc }}
          </div>
        </div>
      </template>

      <template #price="{ record }">
        <div style="font-size: 12px">
          <div v-if="record.giaTriGiamGia && record.giaTriGiamGia > 0">
            <div style="text-decoration: line-through; color: #999; margin-bottom: 2px">
              {{ formatCurrency(record.giaBan) }}
            </div>
            <div style="font-weight: 600; color: #f5222d; font-size: 14px">
              {{ formatCurrency(record.giaBan * (1 - record.giaTriGiamGia / 100)) }}
            </div>
          </div>
          <div v-else style="font-weight: 600; color: #f5222d; font-size: 14px">
            {{ formatCurrency(record.giaBan) }}
          </div>
        </div>
      </template>

      <template #action="{ record }">
        <a-button type="primary" size="small" @click="$emit('select-product', record)">Chọn</a-button>
      </template>
    </a-table>
    <a-empty v-else description="Không có sản phẩm" />
  </a-modal>
</template>

<script setup lang="ts">
import type { BienTheSanPham } from '@/api/san-pham/bien-the'
import MiniCarousel from '@/components/MiniCarousel.vue'

const props = defineProps<{
  visible: boolean
  searchText: string
  productFilters: {
    tenChatLieu: string | null
    tenDeGiay: string | null
    tenNhaSanXuat: string | null
    tenXuatXu: string | null
    tenMauSac: string | null
    tenKichThuoc: string | null
  }
  productMaterialOptions: Array<{ label: string; value: any }>
  productSoleOptions: Array<{ label: string; value: any }>
  productManufacturerOptions: Array<{ label: string; value: any }>
  productOriginOptions: Array<{ label: string; value: any }>
  productColorOptions: Array<{ label: string; value: any }>
  productSizeOptions: Array<{ label: string; value: any }>
  filteredProductVariants: BienTheSanPham[]
  productPagination: { current: number; pageSize: number; total: number }
}>()

defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'cancel'): void
  (e: 'update:searchText', value: string): void
  (e: 'update-filter', payload: { key: keyof typeof props.productFilters; value: string | null }): void
  (e: 'page-change', value: number): void
  (e: 'select-product', record: BienTheSanPham): void
}>()

const columns = [
  { title: 'STT', dataIndex: 'stt', key: 'stt', width: 50 },
  { title: 'Sản Phẩm', dataIndex: 'product', key: 'product', width: 200, slotName: 'product' },
  { title: 'Thông Tin', dataIndex: 'info', key: 'info', width: 200, slotName: 'info' },
  { title: 'Màu Sắc | Kích Thước', dataIndex: 'variant', key: 'variant', width: 150, slotName: 'variant' },
  { title: 'Giá Bán', dataIndex: 'price', key: 'price', width: 150, slotName: 'price' },
  { title: 'Số Lượng', dataIndex: 'soLuong', key: 'soLuong', width: 80 },
  { title: 'Thao Tác', key: 'action', width: 80, slotName: 'action' },
]

function formatCurrency(value: number) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', minimumFractionDigits: 0, maximumFractionDigits: 0 }).format(
    value || 0
  )
}
</script>

<style scoped>
:deep(.product-modal-carousel) {
  width: 100% !important;
  height: 100% !important;
}

:deep(.product-modal-carousel .mini-carousel-container) {
  width: 100% !important;
  height: 100% !important;
  border-radius: 4px;
}

:deep(.product-modal-carousel .carousel-slides) {
  width: 100% !important;
  height: 100% !important;
}

:deep(.product-modal-carousel .carousel-slide) {
  width: 100% !important;
  height: 100% !important;
}

:deep(.product-modal-carousel .carousel-slide-empty) {
  width: 100% !important;
  height: 100% !important;
}
</style>
