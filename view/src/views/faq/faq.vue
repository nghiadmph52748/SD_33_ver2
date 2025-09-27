<template>
  <div class="faq-page">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />

    <div class="faq-content">
      <div class="faq-section">
        <h2>Câu hỏi về đăng nhập và tài khoản</h2>

        <div class="faq-item">
          <h3>Làm thế nào để đăng nhập vào hệ thống?</h3>
          <p>
            Để đăng nhập, bạn cần nhập tên đăng nhập và mật khẩu được cung cấp bởi quản trị viên. Sau đó nhấn nút "Đăng nhập" để truy cập
            vào hệ thống.
          </p>
        </div>

        <div class="faq-item">
          <h3>Tôi quên mật khẩu thì sao?</h3>
          <p>Vui lòng liên hệ với quản trị viên hệ thống để được hỗ trợ đặt lại mật khẩu.</p>
        </div>

        <div class="faq-item">
          <h3>Tại sao tài khoản của tôi bị khóa?</h3>
          <p>
            Tài khoản có thể bị khóa do nhập sai mật khẩu quá nhiều lần hoặc do quản trị viên tạm khóa. Hãy liên hệ quản trị viên để được hỗ
            trợ.
          </p>
        </div>
      </div>

      <div class="faq-section">
        <h2>Câu hỏi về quản lý sản phẩm</h2>

        <div class="faq-item">
          <h3>Làm thế nào để thêm sản phẩm mới?</h3>
          <p>Truy cập menu "Quản lý sản phẩm" > "Danh mục sản phẩm", sau đó nhấn nút "Thêm mới" và điền đầy đủ thông tin sản phẩm.</p>
        </div>

        <div class="faq-item">
          <h3>Có thể chỉnh sửa thông tin sản phẩm không?</h3>
          <p>Có, bạn có thể chỉnh sửa thông tin sản phẩm bằng cách chọn sản phẩm và nhấn nút "Chỉnh sửa" trong danh sách sản phẩm.</p>
        </div>

        <div class="faq-item">
          <h3>Làm thế nào để thêm biến thể sản phẩm?</h3>
          <p>Trong menu "Quản lý sản phẩm" > "Biến thể sản phẩm", bạn có thể thêm các biến thể như kích thước, màu sắc cho sản phẩm.</p>
        </div>
      </div>

      <div class="faq-section">
        <h2>Câu hỏi về bán hàng</h2>

        <div class="faq-item">
          <h3>Làm thế nào để tạo đơn hàng mới?</h3>
          <p>Truy cập menu "Bán hàng tại quầy", chọn sản phẩm cần bán, nhập số lượng và thông tin khách hàng, sau đó nhấn "Thanh toán".</p>
        </div>

        <div class="faq-item">
          <h3>Hệ thống có hỗ trợ hóa đơn không?</h3>
          <p>Có, hệ thống tự động tạo hóa đơn cho mỗi đơn hàng. Bạn có thể xem và in hóa đơn trong menu "Quản lý hóa đơn".</p>
        </div>
      </div>

      <div class="faq-section">
        <h2>Câu hỏi về báo cáo và thống kê</h2>

        <div class="faq-item">
          <h3>Làm thế nào để xem báo cáo doanh thu?</h3>
          <p>Truy cập menu "Thống kê" > "Thống kê doanh thu" để xem các báo cáo về doanh thu theo thời gian.</p>
        </div>

        <div class="faq-item">
          <h3>Tôi có thể xuất báo cáo ra file không?</h3>
          <p>Có, hệ thống hỗ trợ xuất báo cáo ra định dạng Excel hoặc PDF. Nhấn nút "Xuất báo cáo" trong các trang thống kê.</p>
        </div>
      </div>

      <div class="faq-section">
        <h2>Câu hỏi kỹ thuật</h2>

        <div class="faq-item">
          <h3>Hệ thống chạy trên trình duyệt nào?</h3>
          <p>Hệ thống tương thích với các trình duyệt hiện đại như Chrome, Firefox, Safari, Edge.</p>
        </div>

        <div class="faq-item">
          <h3>Gặp sự cố khi sử dụng hệ thống thì sao?</h3>
          <p>Vui lòng liên hệ bộ phận kỹ thuật hoặc quản trị viên để được hỗ trợ khắc phục sự cố.</p>
        </div>
      </div>

      <div class="contact-info">
        <h3>Cần hỗ trợ thêm?</h3>
        <p>Nếu bạn không tìm thấy câu trả lời cho câu hỏi của mình, vui lòng liên hệ:</p>
        <ul>
          <li>Email: support@company.com</li>
          <li>Điện thoại: 1900-xxxx</li>
          <li>Website: www.company.com</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import Breadcrumb from '@/components/breadcrumb/breadcrumb.vue'
import useBreadcrumb from '@/hooks/breadcrumb'

// Breadcrumb setup
const { breadcrumbItems } = useBreadcrumb()

onMounted(() => {
  // console.log('FAQ page loaded')
})
</script>

<style scoped>
.faq-page {
  padding: 0 20px 20px 20px;
}

.faq-content {
  background: var(--color-bg-2);
  border-radius: 6px;
  padding: 24px;
  margin-bottom: 24px;
}

.faq-section {
  margin-bottom: 32px;
}

.faq-section h2 {
  color: var(--color-text-1);
  font-size: 1.25rem;
  margin-bottom: 16px;
  font-weight: 600;
}

.faq-item {
  margin-bottom: 16px;
  padding: 16px;
  border: 1px solid var(--color-border-2);
  border-radius: 6px;
  background: var(--color-bg-3);
}

.faq-item h3 {
  color: var(--color-text-1);
  font-size: 1rem;
  margin-bottom: 8px;
  font-weight: 600;
}

.faq-item p {
  color: var(--color-text-2);
  line-height: 1.6;
  margin: 0;
}

.contact-info {
  background: var(--color-success-light-1);
  border: 1px solid var(--color-success-light-3);
  border-radius: 6px;
  padding: 20px;
  text-align: center;
  margin-top: 24px;
}

.contact-info h3 {
  color: var(--color-text-1);
  margin-bottom: 8px;
  font-size: 1.1rem;
}

.contact-info p {
  color: var(--color-text-2);
  margin-bottom: 12px;
  font-size: 0.9rem;
}

.contact-info ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.contact-info li {
  color: #1d2129;
  margin-bottom: 4px;
  font-weight: 500;
  font-size: 0.9rem;
}

/* Responsive */
@media (max-width: 768px) {
  .faq-page {
    padding: 16px;
  }

  .faq-content {
    padding: 16px;
  }
}
</style>
