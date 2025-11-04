<template>
  <div class="floating-ai">
    <!-- AI Chat fullscreen modal -->
    <a-modal
      v-model:visible="modalVisible"
      :closable="true"
      fullscreen
      :mask-closable="false"
      unmount-on-close
      :footer="false"
      class="ai-fullscreen-modal"
    >
      <template #title>
        <div class="modal-title">
          <icon-robot />
          <span>Trợ Lý AI</span>
          <a-badge :status="isConnected ? 'success' : 'error'" :text="isConnected ? 'Online' : 'Offline'" />
        </div>
      </template>

      <div class="modal-content">
        <AIChatbot
          ref="chatbotRef"
          :suppress-connection-notice="true"
          :enable-health-check="false"
          :connection-status="isConnected"
          @session-state="handleSessionState"
        />
      </div>
    </a-modal>

    <!-- Floating AI button - positioned on right side, beside chat button -->
    <a-button
      v-if="!modalVisible"
      type="primary"
      shape="circle"
      size="large"
      class="floating-btn-ai"
      @click="toggleDrawer"
      :style="buttonPositionStyle"
    >
      <template #icon>
        <icon-robot :size="24" />
      </template>
    </a-button>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { IconRobot } from '@arco-design/web-vue/es/icon'
import AIChatbot from './AIChatbot.vue'
import { useAppStore } from '@/store'
import { checkAIHealth } from '@/api/ai'
import { useRoute } from 'vue-router'

const appStore = useAppStore()

const modalVisible = ref(false)
const isScrolledDown = ref(false)
const isConnected = ref(false)
const chatbotRef = ref<InstanceType<typeof AIChatbot> | null>(null)
const route = useRoute()

const isOnChatPage = computed(() => route.path.startsWith('/chat'))
const hasFloatingChatButton = computed(() => !isOnChatPage.value)

const buttonPositionStyle = computed(() => {
  const baseRight = hasFloatingChatButton.value ? 96 : 24
  const scrolledRight = hasFloatingChatButton.value ? 168 : 96
  const right = isScrolledDown.value ? scrolledRight : baseRight
  const bottom = isOnChatPage.value ? 96 : 24

  return {
    right: `${right}px`,
    bottom: `${bottom}px`,
  }
})

function toggleDrawer() {
  modalVisible.value = !modalVisible.value

  if (modalVisible.value && !isConnected.value) {
    checkConnection()
  }
}

async function checkConnection() {
  try {
    await checkAIHealth()
    isConnected.value = true
  } catch (error) {
    isConnected.value = false
  }
}

function handleSessionState(state: any) {
  // Handle session state if needed
  console.log('Session state updated:', state)
}

// Track scroll to move button when scroll-to-top appears
const checkScroll = () => {
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop
  isScrolledDown.value = scrollTop > 300
}

onMounted(() => {
  if (typeof window !== 'undefined') {
    window.addEventListener('scroll', checkScroll)
    checkConnection()
  }
})

onUnmounted(() => {
  if (typeof window !== 'undefined') {
    window.removeEventListener('scroll', checkScroll)
  }
})

// Check connection when modal opens
watch(modalVisible, async (visible) => {
  if (visible && !isConnected.value) {
    await checkConnection()
  }
})
</script>

<style scoped lang="less">
.floating-ai {
  position: relative;
}

.floating-btn-ai {
  position: fixed;
  width: 56px;
  height: 56px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  z-index: 998; // Same level as chat button
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  padding: 0 !important;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  border: none !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  line-height: 1 !important;

  &:hover {
    transform: scale(1.1);
    box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
  }

  :deep(.arco-btn-icon) {
    margin: 0 !important;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  :deep(.arco-icon) {
    margin: 0 !important;
    display: block;
  }

  :deep(.arco-badge) {
    position: absolute;
    top: -4px;
    right: -4px;
  }
}

.modal-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
}

.modal-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 0;
  margin: 0;
  overflow: hidden;
  flex: 1;
  min-height: 0;

  :deep(.ai-chatbot) {
    height: 100%;
    flex: 1;
    display: flex;
    flex-direction: column;
    min-height: 0;

    .chatbot-card {
      flex: 1;
      display: flex;
      flex-direction: column;
      box-shadow: none;
      border: none;
      min-height: 0;

      :deep(.arco-card-header) {
        flex-shrink: 0;
      }

      :deep(.arco-card-body) {
        flex: 1;
        display: flex !important;
        flex-direction: column !important;
        padding: 0 !important;
        overflow: hidden !important;
        min-height: 0 !important;
      }
    }

    .messages-container {
      overflow-y: auto;
      padding: 24px;
      max-width: 1200px;
      margin: 0 auto;
      width: 100%;
      flex: 1 1 auto !important;
      min-height: 0 !important;
      margin-bottom: 0 !important;
      padding-bottom: 24px;
    }

    .input-container {
      padding: 20px 24px 24px;
      border-top: 1px solid var(--color-border-2);
      background: var(--color-bg-2);
      max-width: 1200px;
      width: 100%;
      margin: auto auto 0;
      margin-top: auto;
      flex-shrink: 0 !important;
      position: static !important;
      bottom: auto !important;
    }
  }
}

:global(.ai-fullscreen-modal) {
  .arco-modal {
    display: flex;
    flex-direction: column;
    height: 100vh;
    top: 0 !important;
    margin: 0 !important;
  }

  .arco-modal-content {
    height: 100%;
    display: flex;
    flex-direction: column;
    border-radius: 0 !important;
    overflow: hidden;
  }

  .arco-modal-body {
    padding: 0 !important;
    flex: 1;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    min-height: 0;
  }

  .arco-modal-header {
    border-bottom: 1px solid var(--color-border-2);
    flex-shrink: 0;
  }
}

// Animation
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
