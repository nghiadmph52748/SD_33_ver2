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
        <transition name="fade" mode="out-in">
          <div class="content-inner" :key="mode">
            <LoginForm v-if="mode === 'login'" @forgot-password="goToForgot" />
            <ForgotPasswordForm
              v-else-if="mode === 'forgot'"
              :email="forgotEmail"
              @back-to-login="backToLogin"
              @submitted="handleForgotSubmitted"
            />
            <div v-else class="reset-success">
              <icon-check-circle-fill class="success-icon" />
              <h2>Đã gửi yêu cầu</h2>
              <p>
                Hướng dẫn đặt lại mật khẩu đã được gửi tới
                <strong>{{ successEmail }}</strong>
                . Vui lòng kiểm tra hộp thư (và thư rác) để tiếp tục.
              </p>
              <a-button type="primary" long @click="backToLogin">Quay lại đăng nhập</a-button>
            </div>
          </div>
        </transition>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue'
import { IconCheckCircleFill } from '@arco-design/web-vue/es/icon'
import LoginForm from './components/login-form.vue'
import ForgotPasswordForm from './components/forgot-password-form.vue'

export default defineComponent({
  components: {
    LoginForm,
    ForgotPasswordForm,
    IconCheckCircleFill,
  },
  setup() {
    const mode = ref<'login' | 'forgot' | 'success'>('login')
    const successEmail = ref('')
    const forgotEmail = ref('')

    const goToForgot = (email: string) => {
      forgotEmail.value = email
      mode.value = 'forgot'
    }

    const backToLogin = () => {
      mode.value = 'login'
      successEmail.value = ''
    }

    const handleForgotSubmitted = (email: string) => {
      successEmail.value = email
      mode.value = 'success'
    }

    return {
      mode,
      successEmail,
      forgotEmail,
      goToForgot,
      backToLogin,
      handleForgotSubmitted,
    }
  },
})
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

.reset-success p {
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

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
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
</style>
