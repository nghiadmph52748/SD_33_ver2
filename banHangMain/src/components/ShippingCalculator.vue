<template>
  <div class="shipping-calculator">
    <h3>{{ $t("shipping.title") || "Tính Phí Giao Hàng" }}</h3>

    <form @submit.prevent="calculateFee" class="shipping-form">
      <div class="form-group">
        <label for="province"
          >{{ $t("shipping.province") || "Tỉnh/TP" }} *</label
        >
        <input
          id="province"
          v-model="location.thanhPho"
          type="text"
          placeholder="Ví dụ: Hà Nội"
          @change="onLocationChange"
          class="form-input"
        />
      </div>

      <div class="form-group">
        <label for="district"
          >{{ $t("shipping.district") || "Quận/Huyện" }} *</label
        >
        <input
          id="district"
          v-model="location.quan"
          type="text"
          placeholder="Ví dụ: Hoàn Kiếm"
          @change="onLocationChange"
          class="form-input"
        />
      </div>

      <div class="form-group">
        <label for="ward">{{ $t("shipping.ward") || "Phường/Xã" }} *</label>
        <input
          id="ward"
          v-model="location.phuong"
          type="text"
          placeholder="Ví dụ: Hàng Gai"
          @change="onLocationChange"
          class="form-input"
        />
      </div>

      <div class="form-group">
        <label for="address">{{
          $t("shipping.address") || "Địa chỉ cụ thể"
        }}</label>
        <input
          id="address"
          v-model="location.diaChiCuThe"
          type="text"
          placeholder="Ví dụ: Số 1 Hàng Gai"
          class="form-input"
        />
      </div>

      <div class="form-row">
        <div class="form-group">
          <label for="weight">{{
            $t("shipping.weight") || "Trọng lượng (g)"
          }}</label>
          <input
            id="weight"
            v-model.number="weight"
            type="number"
            min="0"
            placeholder="500"
            class="form-input"
          />
        </div>

        <div class="form-group">
          <label for="length">{{ $t("shipping.length") || "Dài (cm)" }}</label>
          <input
            id="length"
            v-model.number="length"
            type="number"
            min="0"
            placeholder="10"
            class="form-input"
          />
        </div>

        <div class="form-group">
          <label for="width">{{ $t("shipping.width") || "Rộng (cm)" }}</label>
          <input
            id="width"
            v-model.number="width"
            type="number"
            min="0"
            placeholder="10"
            class="form-input"
          />
        </div>

        <div class="form-group">
          <label for="height">{{ $t("shipping.height") || "Cao (cm)" }}</label>
          <input
            id="height"
            v-model.number="height"
            type="number"
            min="0"
            placeholder="10"
            class="form-input"
          />
        </div>
      </div>

      <button
        type="submit"
        class="btn-calculate"
        :disabled="isCalculating || !isFormValid"
      >
        {{
          isCalculating
            ? this.$t("common.calculating") || "Đang tính..."
            : this.$t("shipping.calculate") || "Tính Phí"
        }}
      </button>
    </form>

    <div v-if="result" class="shipping-result">
      <div class="result-item">
        <span class="label">{{ $t("shipping.fee") || "Phí Giao Hàng" }}:</span>
        <span class="value">{{ formatCurrency(result.fee) }}</span>
      </div>
      <div class="result-item">
        <span class="label">{{ $t("shipping.estimated") || "Ước tính" }}:</span>
        <span class="value">{{ result.estimatedDays }}</span>
      </div>
      <div v-if="result.message" class="result-message">
        {{ result.message }}
      </div>
    </div>

    <div v-if="error" class="shipping-error">
      {{ error }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, emit } from "vue";
import {
  calculateShippingFee,
  ShippingLocation,
  ShippingFeeResponse,
} from "@/api/shipping";
import { formatCurrency } from "@/utils/currency";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const location = ref<ShippingLocation>({
  thanhPho: "",
  quan: "",
  phuong: "",
  diaChiCuThe: "",
});

const weight = ref(500);
const length = ref(10);
const width = ref(10);
const height = ref(10);

const isCalculating = ref(false);
const result = ref<ShippingFeeResponse | null>(null);
const error = ref("");

const isFormValid = computed(() => {
  return (
    location.value.thanhPho?.trim() &&
    location.value.quan?.trim() &&
    location.value.phuong?.trim()
  );
});

async function calculateFee() {
  if (!isFormValid.value) return;

  error.value = "";
  isCalculating.value = true;

  try {
    result.value = await calculateShippingFee({
      location: location.value,
      weight: weight.value,
      length: length.value,
      width: width.value,
      height: height.value,
    });

    // Emit the result to parent component
    emit("shippingCalculated", result.value);
  } catch (err) {
    error.value = err instanceof Error ? err.message : "Lỗi khi tính phí";
    console.error("Shipping calculation error:", err);
  } finally {
    isCalculating.value = false;
  }
}

function onLocationChange() {
  // Auto-calculate when location changes
  if (isFormValid.value) {
    calculateFee();
  }
}

// Expose methods for parent component
defineExpose({
  calculateFee,
  getLocation: () => location.value,
  getShippingFee: () => result.value?.fee || 0,
});
</script>

<style scoped lang="scss">
.shipping-calculator {
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 20px;
  margin: 16px 0;

  h3 {
    margin: 0 0 16px;
    font-size: 16px;
    font-weight: 600;
  }
}

.shipping-form {
  display: grid;
  gap: 12px;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;

  @media (max-width: 768px) {
    grid-template-columns: repeat(2, 1fr);
  }

  @media (max-width: 480px) {
    grid-template-columns: 1fr;
  }
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 4px;

  label {
    font-size: 13px;
    font-weight: 600;
    color: #374151;
  }
}

.form-input {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.15s ease;

  &:focus {
    outline: none;
    border-color: #111;
    box-shadow: 0 0 0 2px rgba(17, 17, 17, 0.1);
  }

  &:disabled {
    background: #f3f4f6;
    cursor: not-allowed;
  }
}

.btn-calculate {
  padding: 10px 16px;
  background: #111;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.15s ease;
  margin-top: 8px;

  &:hover:not(:disabled) {
    background: #000;
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

.shipping-result {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  padding: 12px;
  margin-top: 16px;
  display: grid;
  gap: 8px;
}

.result-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;

  .label {
    color: #4b5563;
    font-weight: 500;
  }

  .value {
    color: #111;
    font-weight: 600;
  }
}

.result-message {
  font-size: 12px;
  color: #6b7280;
  font-style: italic;
  padding-top: 4px;
  border-top: 1px solid #e5e7eb;
}

.shipping-error {
  background: #fee2e2;
  color: #991b1b;
  padding: 12px;
  border-radius: 6px;
  margin-top: 12px;
  font-size: 13px;
}
</style>
