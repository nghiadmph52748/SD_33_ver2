<template>
  <div class="ai-chatbot-page">
    <Breadcrumb :items="breadcrumbItems" />

    <a-row :gutter="16">
      <a-col :span="16">
        <AIChatbot />
      </a-col>

      <a-col :span="8">
        <!-- Info Card -->
        <a-card title="ℹ️ Giới thiệu" :bordered="false" style="margin-bottom: 16px">
          <p>
            <strong>GearUp AI</strong> là trợ lý thông minh giúp bạn tra cứu thông tin nhanh
            chóng trong hệ thống GearUp.
          </p>
          <a-divider />
          <div class="feature-list">
            <div class="feature-item">
              <span class="icon">📊</span>
              <span>Tra cứu sản phẩm bán chạy</span>
            </div>
            <div class="feature-item">
              <span class="icon">💰</span>
              <span>Thống kê doanh thu</span>
            </div>
            <div class="feature-item">
              <span class="icon">⚠️</span>
              <span>Cảnh báo tồn kho thấp</span>
            </div>
            <div class="feature-item">
              <span class="icon">📋</span>
              <span>Trạng thái đơn hàng</span>
            </div>
            <div class="feature-item">
              <span class="icon">👥</span>
              <span>Top khách hàng chi tiêu</span>
            </div>
            <div class="feature-item">
              <span class="icon">🎉</span>
              <span>Đợt giảm giá hoạt động</span>
            </div>
            <div class="feature-item">
              <span class="icon">👨‍💼</span>
              <span>Hiệu suất nhân viên</span>
            </div>
            <div class="feature-item">
              <span class="icon">🛒</span>
              <span>Phân bố kênh bán hàng</span>
            </div>
          </div>
        </a-card>

        <!-- Tips Card -->
        <a-card title="💡 Mẹo sử dụng" :bordered="false">
          <a-list :bordered="false" size="small">
            <a-list-item>
              <a-list-item-meta>
                <template #description>
                  Sử dụng các nút "Gợi ý nhanh" để truy vấn thông tin phổ biến
                </template>
              </a-list-item-meta>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta>
                <template #description>
                  Hỏi bằng ngôn ngữ tự nhiên, ví dụ: "Giày nào bán nhiều nhất?"
                </template>
              </a-list-item-meta>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta>
                <template #description>
                  AI sẽ truy vấn dữ liệu thực tế từ hệ thống và trả lời chính xác
                </template>
              </a-list-item-meta>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta>
                <template #description>
                  Dữ liệu được cache để tối ưu tốc độ phản hồi
                </template>
              </a-list-item-meta>
            </a-list-item>
          </a-list>

          <a-divider />
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import AIChatbot from '@/components/ai/AIChatbot.vue'
import useBreadcrumb from '@/hooks/breadcrumb'

const { breadcrumbItems } = useBreadcrumb()
</script>

<style scoped lang="less">
.ai-chatbot-page {
  padding: 20px;

  .feature-list {
    .feature-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 8px 0;

      .icon {
        font-size: 20px;
      }

      span:not(.icon) {
        font-size: 14px;
        color: #4e5969;
      }
    }
  }

  .tech-info {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
  }
}
</style>
