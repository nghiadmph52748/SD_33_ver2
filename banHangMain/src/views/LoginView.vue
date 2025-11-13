<template>
  <div class="login-page">
    <div class="container">
      <div class="auth-card">
        <div class="auth-header">
          <h1>{{ isLogin ? $t('auth.login') : $t('auth.register') }}</h1>
          <p class="auth-subtitle">
            {{ isLogin ? $t('auth.loginSubtitle') : $t('auth.registerSubtitle') }}
          </p>
        </div>

        <div class="auth-toggle">
          <button
            type="button"
            :class="['toggle-btn', { active: isLogin }]"
            @click="isLogin = true"
          >
            {{ $t('auth.login') }}
          </button>
          <button
            type="button"
            :class="['toggle-btn', { active: !isLogin }]"
            @click="isLogin = false"
          >
            {{ $t('auth.register') }}
          </button>
        </div>

        <form @submit.prevent="handleSubmit" class="auth-form">
          <div v-if="!isLogin" class="form-row">
            <label>{{ $t('auth.fullName') }}</label>
            <a-input
              v-model="formData.fullName"
              :placeholder="$t('auth.fullNamePlaceholder')"
              allow-clear
            />
          </div>

          <div class="form-row">
            <label>{{ $t('auth.email') }}</label>
            <a-input
              v-model="formData.email"
              type="email"
              :placeholder="$t('auth.emailPlaceholder')"
              allow-clear
            />
          </div>

          <div class="form-row">
            <label>{{ $t('auth.password') }}</label>
            <a-input-password
              v-model="formData.password"
              :placeholder="$t('auth.passwordPlaceholder')"
              allow-clear
            />
          </div>

          <div v-if="!isLogin" class="form-row">
            <label>{{ $t('auth.confirmPassword') }}</label>
            <a-input-password
              v-model="formData.confirmPassword"
              :placeholder="$t('auth.confirmPasswordPlaceholder')"
              allow-clear
            />
          </div>

          <div v-if="isLogin" class="form-row-actions">
            <a-checkbox v-model="rememberMe">{{ $t('auth.rememberMe') }}</a-checkbox>
            <a href="#" class="forgot-link">{{ $t('auth.forgotPassword') }}</a>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn btn-primary" :disabled="loading">
              {{ loading ? $t('auth.loading') : (isLogin ? $t('auth.login') : $t('auth.register')) }}
            </button>
          </div>
        </form>

        <div class="auth-divider">
          <span>{{ $t('auth.or') }}</span>
        </div>

        <div class="social-auth">
          <button type="button" class="btn btn-social" @click="handleSocialLogin('google')">
            <svg width="18" height="18" viewBox="0 0 24 24" aria-hidden="true">
              <path fill="currentColor" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
              <path fill="currentColor" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
              <path fill="currentColor" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
              <path fill="currentColor" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
            </svg>
            {{ $t('auth.continueWithGoogle') }}
          </button>
          <button type="button" class="btn btn-social" @click="handleSocialLogin('facebook')">
            <svg width="18" height="18" viewBox="0 0 24 24" aria-hidden="true">
              <path fill="#1877F2" d="M22 12.06C22 6.5 17.52 2 12 2S2 6.5 2 12.06c0 5 3.66 9.15 8.44 9.94v-7.03H7.9v-2.91h2.54V9.41c0-2.5 1.49-3.89 3.77-3.89 1.09 0 2.23.2 2.23.2v2.46h-1.26c-1.24 0-1.63.77-1.63 1.56v1.87h2.78l-.44 2.91h-2.34V22c4.78-.79 8.44-4.94 8.44-9.94z"/>
            </svg>
            {{ $t('auth.continueWithFacebook') }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/stores/user'
import { Message } from '@arco-design/web-vue'

const { t } = useI18n()
const router = useRouter()
const userStore = useUserStore()

const isLogin = ref(true)
const loading = ref(false)
const rememberMe = ref(false)

const formData = ref({
  fullName: '',
  email: '',
  password: '',
  confirmPassword: ''
})

onMounted(() => {
  userStore.initFromStorage()
})

async function handleSubmit() {
  if (loading.value) return

  if (isLogin.value) {
    if (!formData.value.email || !formData.value.password) return
    loading.value = true
    try {
      await userStore.login(formData.value.email, formData.value.password)
      const redirect = (router.currentRoute.value.query.redirect as string) || '/'
      router.push(redirect)
      const name = userStore.profile?.tenKhachHang || userStore.profile?.tenTaiKhoan || formData.value.email
      Message.success(t('auth.loginSuccess', { name }))
    } catch (error) {
      // Global interceptor already displays error message
      // eslint-disable-next-line no-console
      console.error('Login failed', error)
    } finally {
      loading.value = false
    }
  } else {
    // Register logic
    if (!formData.value.fullName || !formData.value.email || !formData.value.password) {
      return
    }
    if (formData.value.password !== formData.value.confirmPassword) {
      return
    }
    loading.value = true
    // Simulate API call
    setTimeout(() => {
      loading.value = false
      router.push('/')
    }, 1000)
  }
}

function handleSocialLogin(provider: string) {
  // Social login logic
  console.log(`Login with ${provider}`)
}
</script>

<style scoped lang="scss">
.login-page {
  padding: 32px 0 48px 0;
  min-height: calc(100vh - 200px);
  display: flex;
  align-items: flex-start;
  justify-content: center;
}

.container {
  max-width: 480px;
  width: 100%;
  margin: 0 auto;
  padding: 0 24px;
}

.auth-card {
  background: #ffffff;
  border: 1px solid #f0f0f0;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.06);
}

.auth-header {
  text-align: center;
  margin-bottom: 32px;

  h1 {
    margin: 0 0 8px 0;
    font-size: 32px;
    font-weight: 700;
    color: #111111;
  }

  .auth-subtitle {
    margin: 0;
    color: #4e5969;
    font-size: 14px;
  }
}

.auth-toggle {
  display: flex;
  gap: 8px;
  margin-bottom: 32px;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 16px;
}

.toggle-btn {
  flex: 1;
  padding: 12px 16px;
  border: none;
  background: transparent;
  color: #4e5969;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  margin-bottom: -16px;
  transition: color 0.2s ease, border-color 0.2s ease;

  &:hover {
    color: #111111;
  }

  &.active {
    color: #111111;
    border-bottom-color: #111111;
    font-weight: 600;
  }
}

.auth-form {
  display: grid;
  gap: 20px;
}

.form-row {
  display: grid;
  gap: 8px;

  label {
    font-size: 14px;
    font-weight: 500;
    color: #111111;
  }
}

.form-row-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;

  .forgot-link {
    color: #111111;
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }
}

.form-actions {
  margin-top: 8px;
}

.btn {
  width: 100%;
  padding: 14px 18px;
  border-radius: 8px;
  font-weight: 700;
  font-size: 14px;
  border: 1px solid;
  cursor: pointer;
  transition: background-color 0.15s ease, color 0.15s ease, border-color 0.15s ease, transform 0.15s ease;

  &:hover:not(:disabled) {
    transform: translateY(-1px);
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

.btn-primary {
  background: #111111;
  color: #ffffff;
  border-color: #111111;

  &:hover:not(:disabled) {
    background: #000000;
    border-color: #000000;
  }
}

.auth-divider {
  margin: 32px 0;
  text-align: center;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 0;
    right: 0;
    height: 1px;
    background: #f0f0f0;
  }

  span {
    position: relative;
    background: #ffffff;
    padding: 0 16px;
    color: #4e5969;
    font-size: 14px;
  }
}

.social-auth {
  display: grid;
  gap: 12px;
}

.btn-social {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  background: #ffffff;
  color: #111111;
  border-color: #e5e7eb;
  padding: 12px 18px;

  &:hover {
    background: #f9fafb;
    border-color: #d1d5db;
  }

  svg {
    flex-shrink: 0;
  }
}

@media (max-width: 600px) {
  .auth-card {
    padding: 32px 24px;
  }

  .auth-header h1 {
    font-size: 28px;
  }
}
</style>

