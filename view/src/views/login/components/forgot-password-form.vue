<template>
  <div class="forgot-form-wrapper">
    <div class="forgot-heading">
      <h2>Khôi phục mật khẩu</h2>
      <p>Nhập địa chỉ email của bạn, chúng tôi sẽ gửi liên kết thiết lập mật khẩu mới.</p>
    </div>
    <a-form ref="formRef" :model="formState" layout="vertical" class="forgot-form" @submit="handleSubmit">
      <a-form-item field="email" label="Email" :rules="emailRules">
        <a-input v-model="formState.email" placeholder="example@gearup.vn" allow-clear>
          <template #prefix>
            <icon-email />
          </template>
        </a-input>
      </a-form-item>
      <a-space direction="vertical" :size="16" style="width: 100%">
        <a-button type="primary" html-type="submit" long :loading="loading">
          Gửi hướng dẫn đặt lại
        </a-button>
        <a-button long @click="emit('back-to-login')">Quay lại đăng nhập</a-button>
      </a-space>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { Message, type FormInstance, type FormItemRule } from '@arco-design/web-vue'
import { IconEmail } from '@arco-design/web-vue/es/icon'
import useLoading from '@/hooks/loading'
import { forgotPassword } from '@/api/user'

const props = defineProps<{ email?: string }>()

const emit = defineEmits<{
  (e: 'back-to-login'): void
  (e: 'submitted', email: string): void
}>()

const formRef = ref<FormInstance>()
const formState = reactive({
  email: props.email ?? '',
})

watch(
  () => props.email,
  (value) => {
    formState.email = value ?? ''
  }
)

const emailRules: FormItemRule[] = [
  { required: true, message: 'Vui lòng nhập email đã đăng ký' },
  {
    validator: (value: string, callback) => {
      const emailRegex = /[^\s@]+@[^\s@]+\.[^\s@]+/
      if (!emailRegex.test(value)) {
        callback('Email không hợp lệ')
      } else {
        callback()
      }
    },
  },
]

const { loading, setLoading } = useLoading()

const handleSubmit = async () => {
  if (loading.value) return
  const form = formRef.value
  if (!form) return
  try {
    await form.validate()
  } catch (error) {
    return
  }
  setLoading(true)
  try {
    const email = formState.email.trim()
    await forgotPassword({ email })
    Message.success('Đã gửi hướng dẫn đặt lại mật khẩu tới email của bạn')
    emit('submitted', email)
  } catch (error) {
    Message.error((error as Error).message || 'Không thể gửi yêu cầu. Vui lòng thử lại')
  } finally {
    setLoading(false)
  }
}
</script>

<style scoped>
.forgot-form-wrapper {
  width: 320px;
  text-align: left;
}

.forgot-heading {
  margin-bottom: 20px;
}

.forgot-heading h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #0f172a;
}

.forgot-heading p {
  margin: 8px 0 0;
  color: #64748b;
  font-size: 14px;
  line-height: 1.5;
}

.forgot-form :deep(.arco-btn) {
  border-radius: 24px;
}
</style>
