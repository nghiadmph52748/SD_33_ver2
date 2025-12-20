<template>
    <Transition name="confirm-fade">
        <div v-if="isOpen" class="confirm-modal-overlay" @click.self="emit('close')">
            <div class="confirm-modal">
                <div class="confirm-body">
                    <div class="confirm-icon">
                        <svg viewBox="0 0 24 24" width="32" height="32">
                            <path fill="currentColor"
                                d="M11 15h2v2h-2v-2zm0-8h2v6h-2V7zm.99-5C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8z" />
                        </svg>
                    </div>
                    <h3>{{ title }}</h3>
                    <p>{{ message }}</p>
                </div>
                <div class="confirm-footer">
                    <button class="btn-cancel" @click="emit('close')">
                        {{ cancelText || 'Huỷ' }}
                    </button>
                    <button class="btn-confirm" @click="emit('confirm')">
                        {{ okText || 'Xác nhận' }}
                    </button>
                </div>
            </div>
        </div>
    </Transition>
</template>

<script setup lang="ts">
defineProps<{
    isOpen: boolean;
    title: string;
    message: string;
    okText?: string;
    cancelText?: string;
}>();

const emit = defineEmits<{
    (e: "close"): void;
    (e: "confirm"): void;
}>();
</script>

<style scoped lang="scss">
.confirm-modal-overlay {
    position: fixed;
    inset: 0;
    background: rgba(15, 23, 42, 0.4);
    backdrop-filter: blur(4px);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 2000;
    padding: 20px;
}

.confirm-modal {
    background: #ffffff;
    border-radius: 24px;
    width: 100%;
    max-width: 400px;
    box-shadow: 0 20px 50px rgba(0, 0, 0, 0.1);
    overflow: hidden;
    animation: modal-enter 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes modal-enter {
    from {
        transform: scale(0.9) translateY(20px);
        opacity: 0;
    }

    to {
        transform: scale(1) translateY(0);
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

.confirm-body {
    padding: 32px 24px 24px;
    text-align: center;

    .confirm-icon {
        width: 64px;
        height: 64px;
        background: #fff1f0;
        color: #ff4d4f;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin: 0 auto 20px;
    }

    h3 {
        margin: 0 0 12px;
        font-size: 20px;
        font-weight: 700;
        color: #111;
    }

    p {
        margin: 0;
        font-size: 15px;
        color: #666;
        line-height: 1.5;
    }
}

.confirm-footer {
    display: flex;
    gap: 12px;
    padding: 0 24px 24px;

    button {
        flex: 1;
        padding: 12px;
        border: none;
        border-radius: 12px;
        font-size: 15px;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.2s ease;
    }

    .btn-cancel {
        background: #f5f5f5;
        color: #666;

        &:hover {
            background: #eeeeee;
        }
    }

    .btn-confirm {
        background: #ff4d4f;
        color: white;

        &:hover {
            background: #ff7875;
            box-shadow: 0 4px 12px rgba(255, 77, 79, 0.3);
        }

        &:active {
            transform: translateY(1px);
        }
    }
}
</style>
