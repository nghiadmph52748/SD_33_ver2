<template>
  <div class="customer-add-page">
    <a-breadcrumb>
    <a-breadcrumb-item>
        <router-link to="/khach-hang-nhan-su/khach-hang">Khách hàng</router-link>
    </a-breadcrumb-item>
    <a-breadcrumb-item>Thêm mới</a-breadcrumb-item>
    </a-breadcrumb>


    <a-card title="Thêm khách hàng mới">
      <a-form :model="form" layout="vertical" @submit.prevent="submitForm">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="Tên khách hàng" required>
              <a-input v-model="form.ten" placeholder="Nhập tên khách hàng" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="Email">
              <a-input v-model="form.email" placeholder="Nhập email" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="Số điện thoại">
              <a-input v-model="form.sdt" placeholder="Nhập số điện thoại" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="Phân loại">
              <a-select v-model="form.phanLoai" placeholder="Chọn phân loại">
                <a-option value="vip">VIP</a-option>
                <a-option value="regular">Thường xuyên</a-option>
                <a-option value="new">Mới</a-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="Giới tính">
              <a-radio-group v-model="form.gioiTinh">
                <a-radio value="Nam">Nam</a-radio>
                <a-radio value="Nữ">Nữ</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
        </a-row>

        <div class="form-actions">
          <a-space>
            <a-button type="primary" html-type="submit">Lưu</a-button>
            <a-button @click="quayLai">Quay lại</a-button>
          </a-space>
        </div>
      </a-form>
    </a-card>
  </div>
</template>

<script setup>
import { reactive } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();

const form = reactive({
  ten: "",
  email: "",
  sdt: "",
  phanLoai: "",
  gioiTinh: "",
  trangThai: "active",
});

const submitForm = () => {
  console.log("Form dữ liệu:", form);
  // TODO: gọi API lưu khách hàng
  router.push("/khach-hang-nhan-su/khach-hang"); // quay lại trang danh sách sau khi lưu
};

const quayLai = () => {
  router.push("/khach-hang-nhan-su/khach-hang");
};
</script>
