<template>
    <div v-if="isOpen" class="modal-overlay" @click.self="$emit('close')">
        <div class="modal-content">
            <div class="modal-header">
                <h2>Xác nhận đơn hàng</h2>
                <button class="close-btn" @click="$emit('close')" aria-label="Đóng">×</button>
            </div>

            <div class="modal-body">
                <div class="info-section">
                    <h3>Thông tin giao hàng</h3>
                    <p><strong>Người nhận:</strong> {{ orderInfo.fullName }}</p>
                    <p><strong>Số điện thoại:</strong> {{ orderInfo.phone }}</p>
                    <p><strong>Email:</strong> {{ orderInfo.email }}</p>
                    <p><strong>Địa chỉ:</strong> {{ orderInfo.address }}</p>
                </div>

                <div class="info-section">
                    <h3>Phương thức thanh toán</h3>
                    <p>{{ paymentMethodLabel }}</p>
                </div>

                <div class="info-section">
                    <h3>Tóm tắt đơn hàng</h3>
                    <div class="summary-row">
                        <span>Tạm tính:</span>
                        <span>{{ formatCurrency(orderInfo.subtotal) }}</span>
                    </div>
                    <div class="summary-row">
                        <span>Phí vận chuyển:</span>
                        <span>{{ formatCurrency(orderInfo.shippingFee) }}</span>
                    </div>
                    <div v-if="orderInfo.voucherDiscount > 0" class="summary-row discount">
                        <span>Giảm giá:</span>
                        <span>-{{ formatCurrency(orderInfo.voucherDiscount) }}</span>
                    </div>
                    <div class="summary-row total">
                        <span>Tổng cộng:</span>
                        <span>{{ formatCurrency(orderInfo.total) }}</span>
                    </div>
                </div>
            </div>

            <div class="modal-footer">
                <button class="btn btn-secondary" @click="$emit('close')" :disabled="loading">
                    Hủy
                </button>
                <button class="btn btn-primary" @click="$emit('confirm')" :disabled="loading">
                    {{ loading ? 'Đang xử lý...' : 'Xác nhận đặt hàng' }}
                </button>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { formatCurrency } from '@/utils/currency';

interface Props {
    isOpen: boolean;
    orderInfo: {
        fullName?: string;
        phone?: string;
        email?: string;
        address?: string;
        paymentMethod?: 'cod' | 'vnpay' | 'momo';
        subtotal?: number;
        shippingFee?: number;
        voucherDiscount?: number;
        total?: number;
    };
    loading?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
    isOpen: false,
    orderInfo: () => ({}),
    loading: false
});

defineEmits<{
    (e: 'close'): void;
    (e: 'confirm'): void;
}>();

const paymentMethodLabel = computed(() => {
    switch (props.orderInfo.paymentMethod) {
        case 'cod':
            return 'Thanh toán khi nhận hàng (COD)';
        case 'vnpay':
            return 'VNPAY';
        case 'momo':
            return 'MoMo';
        default:
            return '';
    }
});
</script>

<style scoped>
.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 9999;
    padding: 20px;
}

.modal-content {
    background: white;
    border-radius: 16px;
    max-width: 600px;
    width: 100%;
    max-height: 90vh;
    overflow-y: auto;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24px;
    border-bottom: 1px solid #e5e7eb;
}

.modal-header h2 {
    margin: 0;
    font-size: 24px;
    font-weight: 600;
}

.close-btn {
    background: none;
    border: none;
    font-size: 32px;
    cursor: pointer;
    color: #6b7280;
    padding: 0;
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: color 0.2s;
}

.close-btn:hover {
    color: #111827;
}

.modal-body {
    padding: 24px;
}

.info-section {
    margin-bottom: 24px;
}

.info-section:last-child {
    margin-bottom: 0;
}

.info-section h3 {
    font-size: 16px;
    font-weight: 600;
    margin: 0 0 12px 0;
    color: #111827;
}

.info-section p {
    margin: 8px 0;
    color: #4b5563;
    line-height: 1.6;
}

.summary-row {
    display: flex;
    justify-content: space-between;
    padding: 8px 0;
    color: #4b5563;
}

.summary-row.discount {
    color: #dc2626;
}

.summary-row.total {
    margin-top: 12px;
    padding-top: 12px;
    border-top: 2px solid #e5e7eb;
    font-size: 18px;
    font-weight: 600;
    color: #111827;
}

.modal-footer {
    padding: 24px;
    border-top: 1px solid #e5e7eb;
    display: flex;
    gap: 12px;
    justify-content: flex-end;
}

.btn {
    padding: 12px 24px;
    border-radius: 8px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;
    border: none;
    font-size: 16px;
}

.btn:disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

.btn-secondary {
    background: #f3f4f6;
    color: #374151;
}

.btn-secondary:hover:not(:disabled) {
    background: #e5e7eb;
}

.btn-primary {
    background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
    color: white;
}

.btn-primary:hover:not(:disabled) {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(37, 99, 235, 0.4);
}
</style>
