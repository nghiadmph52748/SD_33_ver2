<template>
  <Transition name="confirm-fade">
    <div v-if="visible" class="confirm-modal-overlay" @click.self="$emit('update:visible', false)">
      <div class="confirm-modal">
        <div class="modal-header">
          <h3>Xác nhận xóa sản phẩm</h3>
          <button class="close-btn" @click="$emit('update:visible', false)">✕</button>
        </div>

        <div class="modal-body" v-if="product">
          <div class="product-preview">
            <img v-if="product.image" :src="product.image" :alt="product.productName" />
            <div class="product-info">
              <div class="name">{{ product.productName }}</div>
              <div class="meta">Số lượng: {{ product.quantity }}</div>
              <div class="price">{{ formatCurrency(product.price * product.quantity) }}</div>
            </div>
          </div>

          <div class="warning-box">
            <div class="warning-icon">⚠️</div>
            <div class="warning-text">
              Bạn có chắc chắn muốn xóa sản phẩm này? Sản phẩm sẽ được trả lại kho.
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn-cancel" @click="$emit('update:visible', false)">Hủy</button>
          <button class="btn-confirm" @click="$emit('ok')">Xác nhận xóa</button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<style scoped lang="scss">
.confirm-modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 20px;
}

.confirm-modal {
  background: #ffffff;
  border-radius: 20px;
  width: 100%;
  max-width: 480px;
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  animation: modal-pop 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes modal-pop {
  from {
    transform: scale(0.9);
    opacity: 0;
  }

  to {
    transform: scale(1);
    opacity: 1;
  }
}

.confirm-fade-enter-active,
.confirm-fade-leave-active {
  transition: opacity 0.2s ease;
}

.confirm-fade-enter-from,
.confirm-fade-leave-to {
  opacity: 0;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;

  h3 {
    margin: 0;
    font-size: 18px;
    font-weight: 700;
    color: #1a1a1a;
  }

  .close-btn {
    background: none;
    border: none;
    font-size: 18px;
    cursor: pointer;
    color: #999;
    transition: color 0.2s;

    &:hover {
      color: #333;
    }
  }
}

.modal-body {
  padding: 24px;

  .product-preview {
    display: flex;
    gap: 16px;
    margin-bottom: 24px;
    padding: 16px;
    background: #f8f9fa;
    border-radius: 12px;

    img {
      width: 72px;
      height: 72px;
      object-fit: cover;
      border-radius: 8px;
      background: #fff;
    }

    .product-info {
      flex: 1;

      .name {
        font-weight: 600;
        font-size: 15px;
        margin-bottom: 4px;
        color: #1a1a1a;
      }

      .meta {
        font-size: 13px;
        color: #666;
        margin-bottom: 4px;
      }

      .price {
        font-weight: 700;
        color: #ff4d4f;
        font-size: 16px;
      }
    }
  }

  .warning-box {
    display: flex;
    gap: 12px;
    padding: 16px;
    background: #fffbe6;
    border: 1px solid #ffe58f;
    border-radius: 12px;

    .warning-icon {
      font-size: 20px;
    }

    .warning-text {
      font-size: 13px;
      color: #856404;
      line-height: 1.5;
    }
  }
}

.modal-footer {
  padding: 16px 24px 24px;
  display: flex;
  gap: 12px;
  justify-content: flex-end;

  button {
    padding: 10px 20px;
    border: none;
    border-radius: 10px;
    font-weight: 600;
    font-size: 14px;
    cursor: pointer;
    transition: all 0.2s;
  }

  .btn-cancel {
    background: #f0f0f0;
    color: #666;

    &:hover {
      background: #e5e5e5;
    }
  }

  .btn-confirm {
    background: #ff4d4f;
    color: white;

    &:hover {
      background: #ff7875;
      box-shadow: 0 4px 12px rgba(255, 77, 79, 0.3);
    }
  }
}
</style>

<script setup lang="ts">
interface Product {
  image?: string
  productName: string
  quantity: number
  price: number
}

defineProps<{ visible: boolean; product: Product | null }>()

defineEmits<{ (e: 'update:visible', value: boolean): void; (e: 'ok'): void }>()

function formatCurrency(value: number) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', minimumFractionDigits: 0, maximumFractionDigits: 0 }).format(
    value || 0
  )
}
</script>
