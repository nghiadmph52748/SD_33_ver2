<template>
  <div class="delivery-thank-you">
    <div class="thank-you-card">
      <header class="brand">
        <RouterLink to="/" class="brand-link">GearUp</RouterLink>
      </header>

      <section v-if="state === 'loading'" class="state state--loading">
        <div class="emoji">📦</div>
        <h1>Đang xác nhận giao hàng...</h1>
        <p>
          Vui lòng chờ trong giây lát, chúng tôi đang hoàn tất việc xác nhận đơn
          hàng của bạn.
        </p>
      </section>

      <section v-else-if="state === 'success'" class="state state--success">
        <div class="emoji">✅</div>
        <h1>Cảm ơn bạn đã xác nhận!</h1>
        <p>
          Đơn hàng
          <span class="order-code" v-if="orderCodeLabel">{{
            orderCodeLabel
          }}</span>
          đã được chuyển sang trạng thái <strong>Hoàn thành</strong>.
        </p>
        <p class="muted">
          {{ successMessage || "Cảm ơn bạn đã tin tưởng GearUp." }}
        </p>
        <div class="actions">
          <RouterLink :to="orderDetailLink" class="btn primary"
            >Xem trạng thái đơn hàng</RouterLink
          >
          <RouterLink to="/" class="btn secondary">Tiếp tục mua sắm</RouterLink>
        </div>
      </section>

      <section v-else class="state state--error">
        <div class="emoji">⚠️</div>
        <h1>Không thể xác nhận đơn hàng</h1>
        <p>{{ errorMessage }}</p>
        <div class="actions">
          <button
            type="button"
            class="btn primary"
            @click="retry"
            :disabled="state === 'loading'"
          >
            Thử lại
          </button>
          <RouterLink :to="orderDetailLink" class="btn secondary"
            >Tra cứu đơn hàng</RouterLink
          >
        </div>
        <p class="muted">
          Nếu bạn cần hỗ trợ thêm, vui lòng liên hệ hotline
          <strong>1900-1234</strong> hoặc email
          <strong>support@gearup.com</strong>.
        </p>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import type { LocationQueryValue } from "vue-router";

type ViewState = "loading" | "success" | "error";

const route = useRoute();
const router = useRouter();

const orderId = ref<number | null>(
  resolveOrderId(route.query.orderId ?? route.query.id)
);
const orderCode = ref<string | null>(
  resolveOrderCode(route.query.orderCode ?? route.query.code)
);
const state = ref<ViewState>(orderId.value ? "loading" : "error");
const errorMessage = ref<string>(
  orderId.value ? "" : "Thiếu mã đơn hàng để xác nhận."
);
const successMessage = ref<string>("");

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || window.location.origin;

const orderCodeLabel = computed(() => {
  if (orderCode.value) return orderCode.value;
  if (orderId.value) return `#${String(orderId.value).padStart(6, "0")}`;
  return "";
});

const orderDetailLink = computed(() => {
  if (orderCode.value) {
    return { path: "/tra-cuu-hoa-don", query: { code: orderCode.value } };
  }
  return { path: "/tra-cuu-hoa-don" };
});

watch(
  state,
  (value) => {
    if (value === "loading") {
      document.title = "GearUp - Đang xác nhận đơn hàng";
    } else if (value === "success") {
      document.title = "GearUp - Cảm ơn bạn";
    } else {
      document.title = "GearUp - Không thể xác nhận";
    }
  },
  { immediate: true }
);

function resolveOrderId(
  value: LocationQueryValue | LocationQueryValue[] | undefined
): number | null {
  const raw = Array.isArray(value) ? value[0] : value;
  if (raw == null) return null;
  const parsed = Number(raw);
  if (!Number.isFinite(parsed) || parsed <= 0) return null;
  return parsed;
}

function resolveOrderCode(
  value: LocationQueryValue | LocationQueryValue[] | undefined
): string | null {
  const raw = Array.isArray(value) ? value[0] : value;
  if (raw == null) return null;
  const normalized = String(raw).trim();
  return normalized ? normalized.toUpperCase() : null;
}

async function confirmDelivery(): Promise<void> {
  if (!orderId.value) {
    state.value = "error";
    errorMessage.value = "Thiếu mã đơn hàng để xác nhận.";
    return;
  }

  try {
    state.value = "loading";
    errorMessage.value = "";

    const response = await fetch(
      `${apiBaseUrl}/api/invoice-management/${orderId.value}/confirm-delivery`,
      {
        method: "POST",
        headers: {
          Accept: "application/json",
        },
      }
    );

    let payload: any = null;
    try {
      payload = await response.json();
    } catch (parseError) {
      if (!response.ok) {
        throw new Error("Không thể xác nhận đơn hàng. Vui lòng thử lại sau.");
      }
    }

    if (!response.ok || (payload && payload.success === false)) {
      const message =
        payload?.message ||
        "Không thể xác nhận đơn hàng. Vui lòng thử lại sau.";
      throw new Error(message);
    }

    const data = payload?.data ?? {};
    const resolvedCode =
      orderCode.value ||
      resolveOrderCode(data?.maHoaDon) ||
      resolveOrderCode(data?.maDonHang) ||
      resolveOrderCode(route.query.orderCode ?? route.query.code);
    if (resolvedCode) {
      orderCode.value = resolvedCode;
    }

    successMessage.value =
      payload?.message || "Đơn hàng đã được cập nhật trạng thái Hoàn thành.";
    state.value = "success";
  } catch (error: any) {
    console.error("[OrderDeliveryThankYou] Confirmation failed", error);
    errorMessage.value =
      error?.message || "Không thể xác nhận đơn hàng. Vui lòng thử lại sau.";
    state.value = "error";
  }
}

function retry(): void {
  if (state.value === "loading") return;
  if (!orderId.value) {
    // Attempt to re-parse from current URL in case user modified it manually
    orderId.value = resolveOrderId(route.query.orderId);
    if (!orderId.value) {
      errorMessage.value = "Thiếu mã đơn hàng để xác nhận.";
      return;
    }
  }
  confirmDelivery();
}

onMounted(() => {
  // Attempt to hydrate orderId/orderCode from navigation state if missing from URL
  if (!orderId.value) {
    const stateOrderId = resolveOrderId(
      router.currentRoute.value.state?.orderId as LocationQueryValue | undefined
    );
    if (stateOrderId) {
      orderId.value = stateOrderId;
    }
  }

  if (!orderCode.value) {
    const stateOrderCode = resolveOrderCode(
      router.currentRoute.value.state?.orderCode as
        | LocationQueryValue
        | undefined
    );
    if (stateOrderCode) {
      orderCode.value = stateOrderCode;
    }
  }

  if (!orderId.value) {
    state.value = "error";
    errorMessage.value = "Thiếu mã đơn hàng để xác nhận.";
    return;
  }

  confirmDelivery();
});
</script>

<style scoped lang="scss">
.delivery-thank-you {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 16px;
  background: linear-gradient(
    135deg,
    rgba(0, 180, 42, 0.08),
    rgba(22, 93, 255, 0.08)
  );
}

.thank-you-card {
  width: min(520px, 100%);
  background: #fff;
  border-radius: 24px;
  box-shadow: 0 32px 80px rgba(15, 23, 42, 0.12);
  padding: 32px clamp(20px, 5vw, 40px);
  text-align: center;
}

.brand {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}

.brand-link {
  font-size: 20px;
  font-weight: 600;
  color: #165dff;
  text-decoration: none;
}

.state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.state h1 {
  margin: 0;
  font-size: clamp(24px, 5vw, 28px);
  color: #111;
}

.state p {
  margin: 0;
  color: rgba(17, 17, 17, 0.72);
  line-height: 1.6;
}

.state .muted {
  color: rgba(17, 17, 17, 0.55);
}

.emoji {
  font-size: clamp(32px, 8vw, 42px);
}

.order-code {
  color: #165dff;
  font-weight: 600;
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: center;
  margin-top: 8px;
}

.btn {
  border: none;
  border-radius: 999px;
  padding: 10px 22px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  text-decoration: none;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.btn.primary {
  background: #165dff;
  color: #fff;
  box-shadow: 0 12px 30px rgba(22, 93, 255, 0.2);
}

.btn.primary:hover,
.btn.primary:focus-visible {
  transform: translateY(-1px);
  box-shadow: 0 16px 40px rgba(22, 93, 255, 0.25);
}

.btn.secondary {
  background: rgba(17, 17, 17, 0.08);
  color: #111;
}

.btn.secondary:hover,
.btn.secondary:focus-visible {
  transform: translateY(-1px);
  box-shadow: 0 16px 40px rgba(17, 17, 17, 0.12);
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

@media (max-width: 480px) {
  .thank-you-card {
    padding: 28px 20px;
  }

  .actions {
    flex-direction: column;
  }

  .btn {
    width: 100%;
  }
}
</style>
