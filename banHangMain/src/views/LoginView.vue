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

onMounted(async () => {
  await userStore.initFromStorage()
  
  // Handle Google OAuth callback (implicit flow - ID token in URL fragment)
  const hash = window.location.hash.substring(1)
  const hashParams = new URLSearchParams(hash)
  const idToken = hashParams.get('id_token')
  const state = hashParams.get('state')
  const error = hashParams.get('error') || new URLSearchParams(window.location.search).get('error')
  
  if (error) {
    Message.error('Đăng nhập Google thất bại: ' + error)
    window.history.replaceState({}, document.title, '/login')
    return
  }
  
  if (idToken && state) {
    const savedState = sessionStorage.getItem('google_oauth_state')
    if (savedState !== state) {
      Message.error('Xác thực không hợp lệ. Vui lòng thử lại.')
      window.history.replaceState({}, document.title, '/login')
      return
    }
    
    loading.value = true
    try {
      console.log('Processing Google OAuth callback with idToken:', idToken?.substring(0, 20) + '...')
      await userStore.oauthLogin(idToken, 'google')
      console.log('OAuth login successful, redirecting...')
      
      // Clean up session storage
      const redirect = sessionStorage.getItem('google_oauth_redirect')
      sessionStorage.removeItem('google_oauth_state')
      sessionStorage.removeItem('google_oauth_redirect')
      
      // Clean up URL first
      window.history.replaceState({}, document.title, '/login')
      
      // Determine redirect destination (avoid redirecting back to login)
      let redirectPath = '/'
      if (redirect && redirect !== '/login' && !redirect.includes('/login')) {
        redirectPath = redirect
      }
      
      // Wait a moment for state to update, then redirect
      setTimeout(() => {
        // Use replace instead of push to avoid adding to history
        router.replace(redirectPath).then(() => {
          const name = userStore.profile?.tenKhachHang || userStore.profile?.tenTaiKhoan || ''
          if (name) {
            Message.success(t('auth.loginSuccess', { name }))
          }
        }).catch((err) => {
          console.error('Redirect error', err)
          // Fallback to home using window.location (more reliable)
          window.location.href = redirectPath
        })
      }, 300)
    } catch (error: any) {
      console.error('OAuth callback error:', error)
      console.error('Error details:', {
        message: error.message,
        response: error.response?.data,
        status: error.response?.status
      })
      const errorMessage = error.response?.data?.message || error.message || 'Đăng nhập Google thất bại'
      Message.error(errorMessage)
      window.history.replaceState({}, document.title, '/login')
      loading.value = false
    }
  }
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
      Message.error(t('auth.missingFields') as string || 'Vui lòng nhập đầy đủ thông tin')
      return
    }
    if (formData.value.password !== formData.value.confirmPassword) {
      Message.error(t('auth.passwordNotMatch') as string || 'Mật khẩu xác nhận không khớp')
      return
    }
    loading.value = true
    try {
      const ok = await userStore.register(
        formData.value.fullName,
        formData.value.email,
        formData.value.password
      )
      if (ok) {
        const redirect = (router.currentRoute.value.query.redirect as string) || '/'
        router.push(redirect)
        const name =
          userStore.profile?.tenKhachHang ||
          userStore.profile?.tenTaiKhoan ||
          formData.value.fullName
        Message.success(t('auth.registerSuccess', { name }))
      }
    } catch (error: any) {
      // Error message is already shown via interceptor or thrown error
      // eslint-disable-next-line no-console
      console.error('Register failed', error)
    } finally {
      loading.value = false
    }
  }
}

async function handleSocialLogin(provider: 'google') {
  if (loading.value) return
  
  if (provider === 'google') {
    // Check if Google Client ID is configured
    const googleClientId = import.meta.env.VITE_GOOGLE_CLIENT_ID
    if (!googleClientId || googleClientId.trim() === '') {
      Message.error('Google Client ID chưa được cấu hình. Vui lòng thêm VITE_GOOGLE_CLIENT_ID vào file .env')
      return
    }
    // For Google, we redirect immediately, so no need to wait for token
    // Don't await - just call and let it redirect
    handleGoogleLogin().catch((err) => {
      console.error('Google login redirect error:', err)
      Message.error('Không thể chuyển hướng đến Google. Vui lòng thử lại.')
    })
    // handleGoogleLogin redirects, so we won't reach here
    return
  }
}

function handleGoogleLogin(): Promise<string | null> {
  return new Promise((resolve) => {
    const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID
    if (!clientId || clientId.trim() === '') {
      const errorMsg = 'Google Client ID chưa được cấu hình. Vui lòng thêm VITE_GOOGLE_CLIENT_ID vào file .env'
      Message.error(errorMsg)
      console.error(errorMsg)
      resolve(null)
      return
    }

    // Generate state for CSRF protection
    const state = Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15)
    sessionStorage.setItem('google_oauth_state', state)
    
    // Store redirect path (avoid storing login page)
    const currentPath = router.currentRoute.value.fullPath
    const redirectPath = (currentPath && currentPath !== '/login' && !currentPath.includes('/login?')) 
      ? currentPath 
      : (router.currentRoute.value.query.redirect as string) || '/'
    sessionStorage.setItem('google_oauth_redirect', redirectPath)

    // Build Google OAuth URL (using implicit flow to get ID token directly)
    const redirectUri = `${window.location.origin}/login`
    const scope = 'openid email profile'
    const responseType = 'id_token' // Use implicit flow to get ID token directly
    
    const authUrl = `https://accounts.google.com/o/oauth2/v2/auth?` +
      `client_id=${encodeURIComponent(clientId)}&` +
      `redirect_uri=${encodeURIComponent(redirectUri)}&` +
      `response_type=${responseType}&` +
      `scope=${encodeURIComponent(scope)}&` +
      `state=${encodeURIComponent(state)}&` +
      `nonce=${encodeURIComponent(Math.random().toString(36).substring(2, 15))}&` +
      `prompt=select_account`

    // Redirect to Google login page
    window.location.href = authUrl
    
    // This promise will never resolve because we're redirecting
    // The actual handling will be done in onMounted when user returns
    resolve(null)
  })
}

function loadGoogleScript(): Promise<void> {
  return new Promise((resolve, reject) => {
    if (document.querySelector('script[src*="accounts.google.com"]')) {
      resolve()
      return
    }
    const script = document.createElement('script')
    script.src = 'https://accounts.google.com/gsi/client'
    script.async = true
    script.defer = true
    script.onload = () => resolve()
    script.onerror = () => reject(new Error('Failed to load Google script'))
    document.head.appendChild(script)
  })
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

