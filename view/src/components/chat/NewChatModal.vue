<template>
  <a-modal v-model:visible="visible" title="Tin nhắn mới" :width="500" @ok="handleSelectUser" @cancel="handleCancel">
    <a-input-search v-model="searchQuery" placeholder="Tìm kiếm nhân viên..." class="search-input" allow-clear />

    <a-spin :loading="loading" class="spin-container">
      <div class="user-list">
        <div v-if="filteredUsers.length > 0" class="user-items">
          <div v-for="user in filteredUsers" :key="user.id" class="user-item" :class="{ selected: selectedUserId === user.id }" @click="selectedUserId = user.id">
            <a-avatar :size="40">
              <icon-user />
            </a-avatar>
            <div class="user-info">
              <div class="user-name">{{ user.hoTen }}</div>
              <div class="user-email">{{ user.email || user.sdt }}</div>
            </div>
            <icon-check-circle-fill v-if="selectedUserId === user.id" class="check-icon" />
          </div>
        </div>

        <div v-else class="empty-container">
          <a-empty description="Không tìm thấy nhân viên phù hợp" />
        </div>
      </div>
    </a-spin>

    <template #footer>
      <a-button @click="handleCancel">Hủy</a-button>
      <a-button type="primary" :disabled="!selectedUserId" @click="handleSelectUser">Bắt đầu chat</a-button>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { IconUser, IconCheckCircleFill } from '@arco-design/web-vue/es/icon'
import { Message } from '@arco-design/web-vue'
import axios from 'axios'
import useChatStore from '@/store/modules/chat'
import useUserStore from '@/store/modules/user'

const props = defineProps<{
  visible: boolean
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'selected', userId: number): void
}>()

const chatStore = useChatStore()
const userStore = useUserStore()

const visible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val),
})

const searchQuery = ref('')
const selectedUserId = ref<number | null>(null)
const users = ref<any[]>([])
const loading = ref(false)

const filteredUsers = computed(() => {
  if (!searchQuery.value) return users.value
  const query = searchQuery.value.toLowerCase()
  return users.value.filter((user) => user.hoTen.toLowerCase().includes(query) || (user.email && user.email.toLowerCase().includes(query)) || (user.sdt && user.sdt.includes(query)))
})

async function fetchUsers() {
  loading.value = true
  try {
    const response = await axios.get('/api/nhan-vien-management/playlist')
    console.log('API Response:', response.data)
    
    // Thử nhiều cách parse khác nhau
    let allUsers = []
    
    if (Array.isArray(response.data)) {
      allUsers = response.data
    } else if (response.data.data) {
      if (Array.isArray(response.data.data)) {
        allUsers = response.data.data
      } else if (response.data.data.content) {
        allUsers = response.data.data.content
      }
    } else if (response.data.content) {
      allUsers = response.data.content
    }
    
    console.log('All users:', allUsers.length, allUsers)
    
    // Map và loại bỏ user hiện tại
    users.value = allUsers
      .map((user: any) => ({
        ...user,
        hoTen: user.tenNhanVien || user.email || user.soDienThoai || 'Không có tên',
      }))
      .filter((user: any) => user.id !== userStore.id)
    
    console.log('Filtered users:', users.value.length, 'người', users.value)
  } catch (error: any) {
    console.error('Fetch users error:', error)
    Message.error(`Không thể tải danh sách nhân viên: ${error.message}`)
  } finally {
    loading.value = false
  }
}

function handleSelectUser() {
  if (!selectedUserId.value) return
  emit('selected', selectedUserId.value)
  handleCancel()
}

function handleCancel() {
  visible.value = false
  searchQuery.value = ''
  selectedUserId.value = null
}

watch(visible, (newVal) => {
  if (newVal) {
    fetchUsers()
  }
})
</script>

<style scoped lang="less">
.search-input {
  margin-bottom: 16px;
}

.spin-container {
  display: block;
  min-height: 250px;
}

.user-list {
  max-height: 400px;
  overflow-y: auto;
  min-height: 250px;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--color-border-3);
    border-radius: 3px;
  }
}

.user-items {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.empty-container {
  min-height: 250px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;

  &:hover {
    background: var(--color-fill-2);
  }

  &.selected {
    background: var(--color-primary-light-1);
    border: 1px solid var(--color-primary-6);
  }
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-weight: 500;
  font-size: 14px;
  margin-bottom: 2px;
}

.user-email {
  font-size: 12px;
  color: var(--color-text-3);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.check-icon {
  color: rgb(var(--primary-6));
  font-size: 20px;
}
</style>
