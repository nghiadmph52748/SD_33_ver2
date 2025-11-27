<template>
  <div class="login-form-wrapper">
    <div class="login-form-error-msg">{{ errorMessage }}</div>
    <a-form ref="loginForm" :model="userInfo" class="login-form" layout="vertical" @submit="handleSubmit">
      <a-form-item
        field="username"
        :rules="[{ required: true, message: $t('login.form.userName.errMsg') }]"
        :validate-trigger="['change', 'blur']"
        hide-label
      >
        <a-input v-model="userInfo.username" :placeholder="$t('login.form.userName.placeholder')">
          <template #prefix>
            <icon-user />
          </template>
        </a-input>
      </a-form-item>
      <a-form-item
        field="password"
        :rules="[{ required: true, message: $t('login.form.password.errMsg') }]"
        :validate-trigger="['change', 'blur']"
        hide-label
      >
        <a-input-password v-model="userInfo.password" :placeholder="$t('login.form.password.placeholder')" allow-clear>
          <template #prefix>
            <icon-lock />
          </template>
        </a-input-password>
      </a-form-item>
      <a-space :size="16" direction="vertical">
        <div class="login-form-password-actions">
          <a-checkbox checked="rememberPassword" :model-value="loginConfig.rememberPassword" @change="setRememberPassword as any">
            {{ $t('login.form.rememberPassword') }}
          </a-checkbox>
          <a-link @click.prevent="emit('forgot-password', userInfo.username || '')">{{ $t('login.form.forgetPassword') }}</a-link>
        </div>
        <a-button type="primary" html-type="submit" long :loading="loading">
          {{ $t('login.form.login') }}
        </a-button>
      </a-space>
    </a-form>
    <a-modal v-model:visible="showStartModal" title="Bắt đầu ca" width="420" @ok="confirmStartShift" @cancel="cancelStartShift">
      <div style="display: flex; flex-direction: column; gap: 12px">
        <div>Vui lòng nhập số tiền mặt ban đầu để bắt đầu ca làm việc.</div>
        <a-input-number v-model:value="startCash" :min="0" style="width: 100%" placeholder="Số tiền mặt (VND)" />
      </div>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import type { LoginData } from '@/api/user'
import useLoading from '@/hooks/loading'
import { useUserStore } from '@/store'
import { Message } from '@arco-design/web-vue'
import { getGiaoCa, suaGiaoCa } from '@/api/giao-ca'
import { ValidatedError } from '@arco-design/web-vue/es/form/interface'
import { useStorage } from '@vueuse/core'
import { reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

const emit = defineEmits<{
  (e: 'forgot-password', email: string): void
}>()

const router = useRouter()
const { t } = useI18n()
const errorMessage = ref('')
const { loading, setLoading } = useLoading()
const userStore = useUserStore()

const loginConfig = useStorage('login-config', {
  rememberPassword: true,
  username: 'admin', // 演示默认值
  password: 'admin', // demo default value
})
const userInfo = reactive({
  username: loginConfig.value.username,
  password: loginConfig.value.password,
})

const handleSubmit = async ({ errors, values }: { errors: Record<string, ValidatedError> | undefined; values: Record<string, any> }) => {
  if (loading.value) return
  if (!errors) {
    setLoading(true)
    try {
      await userStore.login(values as LoginData)

      // Both admin and staff must go to shift handover page first
      router.push('/lich-lam-viec/giao-ca')
      Message.success(t('login.form.login.success'))
      const { rememberPassword } = loginConfig.value
      const { username, password } = values
      // 实际生产环境需要进行加密存储。
      // The actual production environment requires encrypted storage.
      loginConfig.value.username = rememberPassword ? username : ''
      loginConfig.value.password = rememberPassword ? password : ''
    } catch (err) {
      errorMessage.value = (err as Error).message
      console.error(err)
    } finally {
      setLoading(false)
    }
  }
}
const setRememberPassword = (value: boolean) => {
  loginConfig.value.rememberPassword = value
}

// Start-shift modal state & handlers
const showStartModal = ref(false)
const startCash = ref<number | null>(null)
const assignedShift = ref<any>(null)

async function confirmStartShift() {
  if (!startCash.value && startCash.value !== 0) {
    // If no money entered, log the user out per requirement
    Message.error('Vui lòng nhập số tiền mặt để bắt đầu ca. Bạn sẽ bị đăng xuất nếu không nhập.')
    try {
      await userStore.logout()
    } catch (_) {}
    return
  }

  try {
    const payload = {
      thoiGianGiaoCa: new Date().toISOString().replace('T', ' ').substring(0, 19),
      tongTienBanDau: Number(startCash.value) || 0,
      trangThai: 'Đang hoạt động',
    }
    if (assignedShift.value && assignedShift.value.id) {
      await suaGiaoCa(assignedShift.value.id, payload)
      Message.success('Bắt đầu ca thành công')
      showStartModal.value = false
      router.push('/ban-hang-tai-quay/index')
    } else {
      // fallback: just continue to sales
      router.push('/ban-hang-tai-quay/index')
    }
  } catch (e) {
    console.error('Lỗi khi bắt đầu ca (login flow)', e)
    Message.error('Không thể bắt đầu ca. Bạn sẽ bị đăng xuất.')
    try {
      await userStore.logout()
    } catch (_) {}
  }
}

function cancelStartShift() {
  // User cancelled -> logout
  userStore.logout().finally(() => {
    router.push('/login')
  })
}
</script>

<style lang="less" scoped>
.login-form {
  &-wrapper {
    width: 320px;
  }

  &-title {
    color: var(--color-text-1);
    font-weight: 500;
    font-size: 24px;
    line-height: 32px;
  }

  &-sub-title {
    color: var(--color-text-3);
    font-size: 16px;
    line-height: 24px;
  }

  &-error-msg {
    height: 32px;
    color: rgb(var(--red-6));
    line-height: 32px;
  }

  &-password-actions {
    display: flex;
    justify-content: space-between;
  }

  &-register-btn {
    color: var(--color-text-3) !important;
  }
}

:deep(.arco-btn) {
  border-radius: 24px !important;
}
:deep(.arco-input),
:deep(.arco-input-wrapper),
:deep(.arco-input-password) {
  border-radius: 18px !important;
}
</style>
