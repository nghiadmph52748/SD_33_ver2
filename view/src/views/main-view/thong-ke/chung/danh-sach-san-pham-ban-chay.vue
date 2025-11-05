<template>
  <a-card class="chart-card">
    <template #title>
      <span class="card-title">{{ $t('thongKe.topProducts.title') }}</span>
    </template>
    <template #extra>
      <a-select :model-value="kyThongKe" size="small" style="width: 120px" @change="$emit('update:kyThongKe', $event)">
        <a-option value="week">{{ $t('thongKe.topProducts.week') }}</a-option>
        <a-option value="month">{{ $t('thongKe.topProducts.month') }}</a-option>
      </a-select>
    </template>

    <div class="products-wrapper">
      <a-list :data="duLieuSanPham" :bordered="false">
        <template #item="{ item, index }">
          <a-list-item class="product-item">
            <a-list-item-meta>
              <template #avatar>
                <a-badge :count="index + 1" :number-style="getBadgeStyle(index)">
                  <a-avatar :size="48" :style="getAvatarStyle(index)">
                    <img v-if="item.image" :src="item.image" :alt="item.name" style="width: 100%; height: 100%; object-fit: cover" />
                    <icon-image v-else style="font-size: 24px" />
                  </a-avatar>
                </a-badge>
              </template>

              <template #title>
                <div class="product-header">
                  <span class="product-name">{{ item.name }}</span>
                  <a-tag v-if="index < 3" color="red" size="small">
                    <template #icon><icon-fire /></template>
                    {{ $t('thongKe.topProducts.hot') }}
                  </a-tag>
                </div>
              </template>

              <template #description>
                <a-space :size="16" class="product-stats">
                  <span class="stat-item">
                    <icon-apps class="stat-icon" />
                    <span class="stat-label">{{ $t('thongKe.topProducts.sold') }}:</span>
                    <span class="stat-value">{{ item.value.toLocaleString() }}</span>
                  </span>
                  <a-divider direction="vertical" />
                  <span class="stat-item">
                    <icon-gift class="stat-icon" />
                    <span class="stat-label">{{ $t('thongKe.topProducts.revenue') }}:</span>
                    <span class="stat-value primary">{{ dinhDangTienNgan(item.revenue) }}</span>
                  </span>
                </a-space>
              </template>
            </a-list-item-meta>
          </a-list-item>
        </template>
      </a-list>

      <a-empty v-if="duLieuSanPham.length === 0" />
    </div>
  </a-card>
</template>

<script setup lang="ts">
import { IconFire, IconApps, IconGift, IconImage } from '@arco-design/web-vue/es/icon'
import type { SanPhamBanChay } from '../types/thong-ke.types'

interface Props {
  duLieuSanPham: SanPhamBanChay[]
  kyThongKe: string
}

const props = defineProps<Props>()

interface Emits {
  (e: 'update:kyThongKe', value: string): void
}

defineEmits<Emits>()

const dinhDangTienNgan = (soTien: number) => {
  if (soTien >= 1000000) {
    return `${(soTien / 1000000).toFixed(1)}M₫`
  }
  if (soTien >= 1000) {
    return `${(soTien / 1000).toFixed(0)}K₫`
  }
  return `${soTien}₫`
}

const getBadgeStyle = (index: number) => {
  if (index < 3) {
    const colors = ['#FFD700', '#C0C0C0', '#CD7F32']
    return {
      backgroundColor: colors[index],
      color: '#fff',
      fontWeight: 'bold',
    }
  }
  return {
    backgroundColor: 'rgb(var(--primary-6))',
    color: '#fff',
  }
}

const getAvatarStyle = (index: number) => {
  return {
    background: 'var(--color-fill-2)',
    border: '2px solid var(--color-border-2)',
  }
}
</script>

<style scoped>
.chart-card {
  height: 100%;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-1);
}

.products-wrapper {
  max-height: 450px;
  overflow-y: auto;
}

.product-item {
  padding: 12px 0 !important;
  border-bottom: 1px solid var(--color-border-2);
}

.product-item:last-child {
  border-bottom: none;
}

.product-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.product-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-1);
}

.product-stats {
  margin-bottom: 8px;
}

.stat-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--color-text-2);
}

.stat-icon {
  font-size: 14px;
  color: var(--color-text-3);
}

.stat-label {
  color: var(--color-text-3);
}

.stat-value {
  font-weight: 600;
  color: var(--color-text-1);
}

.stat-value.primary {
  color: rgb(var(--primary-6));
}

/* Scrollbar - Hidden */
.products-wrapper {
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE/Edge */
}

.products-wrapper::-webkit-scrollbar {
  display: none; /* Chrome/Safari/Opera */
}
</style>
