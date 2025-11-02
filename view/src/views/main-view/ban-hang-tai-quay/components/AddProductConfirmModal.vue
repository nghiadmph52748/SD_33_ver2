<template>
  <a-modal
    :visible="visible"
    title="Thêm Sản Phẩm"
    width="500px"
    :ok-button-props="{ disabled: !isQuantityValid || confirmLoading, loading: confirmLoading }"
    :cancel-button-props="{ disabled: confirmLoading }"
    @ok="$emit('ok')"
    @update:visible="$emit('update:visible', $event)"
  >
    <div v-if="product">
      <div style="display: flex; gap: 12px; margin-bottom: 12px; padding-bottom: 8px; border-bottom: 1px solid #e5e5e5">
        <img v-if="product.anhSanPham?.[0]" :src="product.anhSanPham[0]" style="width: 80px; height: 80px; object-fit: cover; border-radius: 4px" />
        <div style="flex: 1">
          <div style="font-weight: 600; font-size: 14px; margin-bottom: 4px">{{ product.tenSanPham }}</div>
          <div style="font-size: 12px; color: #999; margin-bottom: 12px">Mã: {{ product.maChiTietSanPham }}</div>

          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-bottom: 12px; font-size: 11px">
            <div>
              <div v-if="product.tenChatLieu" style="margin-bottom: 6px">
                <div style="color: #999; font-size: 10px">Chất liệu</div>
                <div style="font-weight: 500">{{ product.tenChatLieu }}</div>
              </div>
              <div v-if="product.tenDeGiay">
                <div style="color: #999; font-size: 10px">Đế</div>
                <div style="font-weight: 500">{{ product.tenDeGiay }}</div>
              </div>
            </div>
            <div>
              <div v-if="product.tenNhaSanXuat" style="margin-bottom: 6px">
                <div style="color: #999; font-size: 10px">Nhà sản xuất</div>
                <div style="font-weight: 500">{{ product.tenNhaSanXuat }}</div>
              </div>
              <div v-if="product.tenXuatXu">
                <div style="color: #999; font-size: 10px">Xuất xứ</div>
                <div style="font-weight: 500">{{ product.tenXuatXu }}</div>
              </div>
            </div>
          </div>

          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-bottom: 12px; font-size: 11px">
            <div v-if="product.tenMauSac">
              <div style="color: #999; font-size: 10px">Màu sắc</div>
              <div style="display: flex; align-items: center; gap: 8px; margin-top: 4px">
                <div v-if="product.maMau" style="width: 24px; height: 24px; border-radius: 4px; border: 1px solid #e5e5e5; background-color: #ffffff" :style="{ backgroundColor: product.maMau }" :title="product.maMau" />
                <div>
                  <div style="font-weight: 600">{{ product.tenMauSac }}</div>
                  <div v-if="product.maMau" style="color: #999; font-size: 10px">{{ product.maMau }}</div>
                </div>
              </div>
            </div>
            <div v-if="product.tenKichThuoc">
              <div style="color: #999; font-size: 10px">Kích thước</div>
              <div style="font-weight: 600; margin-top: 4px">{{ product.tenKichThuoc }}</div>
            </div>
          </div>

          <div style="color: #999; font-size: 10px; margin-bottom: 6px">Giá bán</div>
          <div v-if="product.giaTriGiamGia && product.giaTriGiamGia > 0" style="margin-bottom: 0">
            <div style="text-decoration: line-through; color: #999; font-size: 12px; margin-bottom: 2px">{{ formatCurrency(product.giaBan) }}</div>
            <div style="font-weight: 600; color: #f5222d; font-size: 14px">{{ formatCurrency(product.giaBan * (1 - product.giaTriGiamGia / 100)) }}</div>
          </div>
          <div v-else style="font-weight: 600; color: #f5222d; font-size: 14px">{{ formatCurrency(product.giaBan) }}</div>
        </div>
      </div>

      <div>
        <label style="display: block; margin-bottom: 6px; font-weight: 600; font-size: 13px">Số Lượng (Tồn kho: {{ product.soLuong || 0 }})</label>
        <a-input-number :model-value="quantity" style="width: 100%; margin-bottom: 12px" placeholder="Nhập số lượng" @update:model-value="$emit('update:quantity', $event)" />

        <a-alert v-if="quantity > (product.soLuong || 0)" type="error" closable>
          <template #title>Tồn kho không đủ!</template>
          <div style="font-size: 12px">Yêu cầu: {{ quantity }} cái | Tồn kho: {{ product.soLuong || 0 }} cái</div>
        </a-alert>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import type { BienTheSanPham } from '@/api/san-pham/bien-the'

defineProps<{ visible: boolean; product: BienTheSanPham | null; quantity: number; isQuantityValid: boolean; confirmLoading: boolean }>()

defineEmits<{ (e: 'update:visible', value: boolean): void; (e: 'update:quantity', value: number): void; (e: 'ok'): void }>()

function formatCurrency(value: number) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value || 0)
}
</script>
