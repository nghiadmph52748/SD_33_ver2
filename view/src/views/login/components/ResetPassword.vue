<template>
  <div class="login-bg">
    <div class="container">
      <div class="content">
        <div class="logo">
          <img
            alt="logo"
            src="//p3-armor.byteimg.com/tos-cn-i-49unhts6dw/dfdba5317c0c20ce20e64fac803d52bc.svg~tplv-49unhts6dw-image.image"
          />
          <div class="logo-text">GearUp</div>
        </div>

        <div class="content-inner">
          <h2 class="reset-title">Đặt lại mật khẩu</h2>

          <div v-if="loading" class="info-text">Đang kiểm tra token...</div>

          <div v-else>
            <form v-if="validToken && !success" @submit.prevent="handleReset" class="reset-form">
              <a-form-item label="Mật khẩu mới" class="mb-6">
                <input type="password" v-model="newPassword" class="input" placeholder="Nhập mật khẩu mới" required />
              </a-form-item>

              <button type="submit" :disabled="loading" class="submit-btn">Xác nhận đổi mật khẩu</button>
            </form>

            <!-- Success view -->
            <div v-if="success" class="reset-success">
              <icon-check-circle-fill class="success-icon" />
              <h2>Đổi mật khẩu thành công</h2>
              <p class="success-desc">
                Mật khẩu của bạn đã được cập nhật. Bạn có thể quay lại màn hình đăng nhập để đăng nhập bằng mật khẩu mới.
              </p>
              <button class="submit-btn" @click="goToLogin">Quay lại đăng nhập</button>
            </div>

            <p v-else-if="!validToken" class="error-text">{{ error }}</p>
            <p v-else-if="message" class="success-text">{{ message }}</p>
          </div>

          <p v-if="!success" class="back-login">
            <router-link to="/login">← Quay lại đăng nhập</router-link>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { IconCheckCircleFill } from '@arco-design/web-vue/es/icon'

const newPassword = ref('')
const token = ref('')
const message = ref('')
const error = ref('')
const loading = ref(true)
const validToken = ref(false)
const success = ref(false)

const router = useRouter()
const goToLogin = () => {
  router.push('/login')
}

onMounted(async () => {
  const params = new URLSearchParams(window.location.search)
  token.value = params.get('token')

  if (!token.value) {
    error.value = 'Liên kết không hợp lệ hoặc thiếu token.'
    loading.value = false
    return
  }

  try {
    const res = await axios.post('http://localhost:8080/api/auth/verify-token', { token: token.value })
    const data = res.data.data || res.data

    if (data.valid && !data.expired) {
      validToken.value = true
    } else {
      error.value = 'Token đã hết hạn hoặc không hợp lệ. Vui lòng yêu cầu lại.'
    }
  } catch (err) {
    console.error(err)
    error.value = 'Có lỗi xảy ra khi kiểm tra token.'
  } finally {
    loading.value = false
  }
})

const handleReset = async () => {
  if (!token.value || !newPassword.value) return

  loading.value = true
  try {
    const res = await axios.post('http://localhost:8080/api/auth/reset-password', {
      token: token.value,
      newPassword: newPassword.value,
    })

    if (res.data.success) {
      // show success UI instead of immediate redirect
      message.value = res.data.message || 'Đổi mật khẩu thành công!'
      error.value = ''
      success.value = true
      // do not auto-redirect; give user control to go back to login
    } else {
      error.value = res.data.message || 'Đổi mật khẩu thất bại!'
    }
  } catch (err) {
    console.error(err)
    error.value = 'Có lỗi xảy ra khi đổi mật khẩu!'
  } finally {
    loading.value = false
  }
}
</script>

<style lang="less" scoped>
.login-bg {
  min-height: 100vh;
  width: 100vw;
  background: linear-gradient(135deg, #e0e7ff 0%, #f8fafc 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}
.container {
  display: flex;
  min-height: 500px;
  width: 520px;
  max-width: 96vw;
  border-radius: 18px;
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.15);
  background: #fff;
  position: relative;
  overflow: hidden;
  margin: 40px 0;
  @media (max-width: 900px) {
    width: 98vw;
    min-height: unset;
  }
  .content {
    border-radius: 18px;
    position: relative;
    display: flex;
    width: 100%;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 40px 0;
    background: #fff;
    @media (max-width: 900px) {
      padding: 24px 0;
    }
  }
}
.content-inner {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}
.logo {
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.85);
  padding: 6px 18px 6px 10px;
  border-radius: 24px;
  box-shadow: 0 2px 8px 0 rgba(31, 38, 135, 0.08);
  &-text {
    margin-left: 8px;
    color: #00308f;
    font-size: 22px;
    font-weight: bold;
    letter-spacing: 1px;
  }
  img {
    height: 32px;
    width: 32px;
  }
  @media (max-width: 900px) {
    margin: 16px auto;
    justify-content: center;
    box-shadow: none;
    background: transparent;
  }
}
.reset-title {
  margin: 8px 0 4px;
  font-size: 22px;
  font-weight: 600;
  color: #0f172a;
}
.info-text {
  color: #64748b;
}
.error-text {
  color: #ef4444;
}
.success-text {
  color: #16a34a;
}
.reset-form {
  width: 320px;
}
.input {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #e6e9ef;
  border-radius: 12px;
  outline: none;
  transition: box-shadow 0.15s ease;
}
.input:focus {
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.08);
}
.submit-btn {
  width: 320px;
  margin-top: 12px;
  background: #00308f;
  color: #fff;
  padding: 12px 0;
  border-radius: 24px;
  border: none;
  cursor: pointer;
}
.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.back-login {
  margin-top: 12px;
  color: #64748b;
}

.reset-success {
  width: 320px;
  text-align: center;
}

.reset-success h2 {
  margin: 16px 0 8px;
  font-size: 24px;
  font-weight: 600;
  color: #0f172a;
}

.reset-success p.success-desc {
  color: #64748b;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 24px;
}

.success-icon {
  font-size: 48px;
  color: #22c55e;
}

.reset-success :deep(.arco-btn) {
  border-radius: 24px;
}

:deep(.arco-btn) {
  border-radius: 24px !important;
}
</style>
