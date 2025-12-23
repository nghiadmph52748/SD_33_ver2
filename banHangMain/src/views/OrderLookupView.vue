<template>
  <div class="order-lookup-wrapper">
    <div class="order-lookup">
      <div class="container narrow">
        <header class="lookup-hero">
          <p class="eyebrow">{{ $t("orderLookup.eyebrow") }}</p>
          <h1>{{ $t("orderLookup.title") }}</h1>
          <p class="lead">{{ $t("orderLookup.subtitle") }}</p>
        </header>

        <div class="lookup-grid">
          <form class="lookup-form" @submit.prevent="lookupOrder">
            <label class="field">
              <span>{{ $t("orderLookup.form.codeLabel") }}</span>
              <input v-model.trim="orderCode" type="text" :placeholder="$t('orderLookup.form.codePlaceholder')"
                autocomplete="off" inputmode="text" @blur="orderCode = orderCode.toUpperCase()" />
              <small v-if="formErrors.code" class="field-error">{{
                formErrors.code
              }}</small>
            </label>

            <label class="field" v-if="isAuthenticated">
              <span>{{ $t("orderLookup.form.contactLabel") }}</span>
              <input v-model.trim="contactValue" type="text" :placeholder="$t('orderLookup.form.contactPlaceholder')"
                autocomplete="off" inputmode="text" />
              <small v-if="formErrors.contact" class="field-error">{{
                formErrors.contact
              }}</small>
            </label>
            <p v-else class="form-hint">
              {{ $t("orderLookup.form.guestHint") }}
            </p>

            <button class="submit-btn" type="submit" :disabled="isLoading">
              <span class="btn-content">
                <span v-if="isLoading" class="inline-spinner" aria-hidden="true"></span>
                {{
                  isLoading
                    ? $t("orderLookup.form.checking")
                    : $t("orderLookup.form.submit")
                }}
              </span>
            </button>

            <p class="form-hint">{{ $t("orderLookup.form.hint") }}</p>
          </form>

          <div class="result-card" :class="resultCardClass">
            <template v-if="state === 'success' && order">
              <dl class="result-grid">
                <div>
                  <dt>{{ $t("orderLookup.results.orderCode") }}</dt>
                  <dd class="order-code-value">{{ orderCodeDisplay }}</dd>
                </div>
                <div class="order-action-row" v-if="hasPrimaryAction">
                  <dt>{{ $t("orderLookup.actions.label") }}</dt>
                  <dd class="cancel-action-cell">
                    <button type="button" :class="primaryActionClasses" :disabled="primaryActionDisabled"
                      @click="handlePrimaryAction">
                      <span v-if="isProcessingAction" class="inline-spinner" aria-hidden="true"></span>
                      <span>{{ primaryActionLabel }}</span>
                    </button>
                  </dd>
                </div>
                <div>
                  <dt>{{ $t("orderLookup.results.placedOn") }}</dt>
                  <dd>{{ placedOnLabel }}</dd>
                </div>
                <div>
                  <dt>{{ $t("orderLookup.results.total") }}</dt>
                  <dd>{{ totalLabel }}</dd>
                </div>
                <div>
                  <dt>{{ $t("orderLookup.results.shippingTo") }}</dt>
                  <dd>{{ shippingAddress || "—" }}</dd>
                </div>
                <div v-if="isAuthenticated">
                  <dt>{{ $t("orderLookup.results.contact") }}</dt>
                  <dd style="white-space: pre-line">{{ contactSummary }}</dd>
                </div>
                <div>
                  <dt>Trạng thái gần nhất</dt>
                  <dd class="latest-status-dd">
                    <span class="status-chip" :class="statusTone">
                      {{ currentStatusLabel }}
                    </span>
                    <span v-if="hasStatusUpdatedTime" class="latest-status-time">
                      {{ statusUpdatedLabel }}
                    </span>
                  </dd>
                </div>
              </dl>
            </template>

            <template v-else-if="state === 'error'">
              <h3>{{ $t("orderLookup.error.title") }}</h3>
              <p class="muted">
                {{ errorMessage || $t("orderLookup.error.generic") }}
              </p>
              <RouterLink to="/" class="text-link">{{
                $t("orderLookup.help")
              }}</RouterLink>
            </template>

            <template v-else>
              <h3>{{ $t("orderLookup.empty.title") }}</h3>
              <p class="muted">{{ $t("orderLookup.empty.subtitle") }}</p>
              <RouterLink to="/" class="text-link">{{
                $t("orderLookup.help")
              }}</RouterLink>
            </template>
          </div>
        </div>

        <section class="timeline-section" v-if="state === 'success'">
          <div class="section-heading">
            <h2>{{ $t("orderLookup.timeline.heading") }}</h2>
            <span class="muted">{{ timelineEvents.length }}
              {{ $t("orderLookup.timeline.updates") }}</span>
          </div>

          <div v-if="timelineEvents.length" class="timeline-list">
            <article v-for="item in timelineEvents" :key="item.id" class="timeline-item">
              <div class="timeline-marker"></div>
              <div class="timeline-body">
                <div class="timeline-meta">
                  <span class="time">{{ formatDateTimeVN(item.thoiGian) }}</span>
                </div>
                <h3>{{ item.tenTrangThaiDonHang }}</h3>
                <p v-if="item.ghiChu" class="muted">{{ item.ghiChu }}</p>
              </div>
            </article>
          </div>
          <div v-else class="timeline-empty">
            <p class="muted">{{ $t("orderLookup.timeline.emptyTitle") }}</p>
            <p>{{ $t("orderLookup.timeline.emptySubtitle") }}</p>
          </div>
        </section>

        <section class="items-section" v-if="state === 'success'">
          <div class="section-heading">
            <h2>{{ $t("orderLookup.items.heading") }}</h2>
            <span class="muted" v-if="orderItems.length">{{ orderItems.length }}
              {{ $t("orderLookup.items.itemsCount") }}</span>
          </div>

          <div v-if="orderItems.length" class="items-table">
            <div class="items-header">
              <span>{{ $t("orderLookup.items.product") }}</span>
              <span>{{ $t("orderLookup.items.quantity") }}</span>
              <span>{{ $t("orderLookup.items.price") }}</span>
              <span>{{ $t("orderLookup.items.subtotal") }}</span>
            </div>
            <div v-for="item in orderItems" :key="item.id" class="items-row">
              <div class="product-details">
                <div class="product-main">
                  {{ item.productName
                  }}<span v-if="item.color"> - {{ item.color }}</span><span v-if="item.size"> - {{ item.size }}</span>
                </div>
                <div v-if="item.productCode" class="product-code">
                  {{ item.productCode }}
                </div>
              </div>
              <span>{{ item.quantity }}</span>
              <span>{{ formatCurrency(item.price) }}</span>
              <span>{{ formatCurrency(item.subtotal) }}</span>
            </div>
          </div>
          <div v-else class="items-empty">
            <p>{{ $t("orderLookup.items.empty") }}</p>
          </div>
        </section>
      </div>
    </div>

    <div v-if="showCancelConfirm" class="cancel-confirm-overlay" @click.self="closeCancelConfirm">
      <div class="cancel-confirm-dialog">
        <h3>{{ t("orderLookup.actions.confirmTitle") }}</h3>
        <p>{{ t("orderLookup.actions.cancelConfirm") }}</p>
        <div class="cancel-confirm-actions">
          <button type="button" class="cancel-confirm-secondary" :disabled="isProcessingAction"
            @click="closeCancelConfirm">
            {{ t("orderLookup.actions.keepOrder") }}
          </button>
          <button type="button" class="cancel-confirm-danger" :disabled="isProcessingAction" @click="cancelOrder">
            <span v-if="isProcessingAction" class="inline-spinner" aria-hidden="true"></span>
            <span>{{ t("orderLookup.actions.cancelNow") }}</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import type { LocationQuery } from "vue-router";
import {
  cancelOrder as cancelOrderRequest,
  confirmOrderDelivery,
  fetchLatestOrderStatus,
  fetchOrderById,
  fetchOrderByCode,
  fetchOrderTimeline,
  type OrderStatusSnapshot,
  type OrderTimelineEntry,
  type OrderTrackingDetail,
} from "@/api/orders";
import { formatCurrency } from "@/utils/currency";
import { useUserStore } from "@/stores/user";
import { showMessageError, showMessageSuccess } from "@/utils/message";

type LookupState = "idle" | "loading" | "success" | "error";

const orderCode = ref("");
const contactValue = ref("");
const errorMessage = ref("");
const order = ref<OrderTrackingDetail | null>(null);
const timeline = ref<OrderTimelineEntry[]>([]);
const snapshot = ref<OrderStatusSnapshot | null>(null);
const state = ref<LookupState>("idle");
const formErrors = reactive<{ code?: string; contact?: string }>({});
const isProcessingAction = ref(false);
const showCancelConfirm = ref(false);
const { t } = useI18n();
const userStore = useUserStore();
const isAuthenticated = computed(() => userStore.isAuthenticated);
const route = useRoute();

onMounted(() => {
  const initialCode = resolveInitialOrderCode(route.query);
  if (!initialCode) return;
  orderCode.value = initialCode;
  lookupOrder();
});

function hydrateContactFromProfile() {
  if (!isAuthenticated.value) return;
  if (contactValue.value) return;
  const profile = userStore.profile;
  contactValue.value = profile?.email || profile?.soDienThoai || "";
}

hydrateContactFromProfile();

watch(isAuthenticated, (loggedIn) => {
  if (loggedIn) {
    hydrateContactFromProfile();
  } else {
    contactValue.value = "";
    formErrors.contact = undefined;
  }
});

const isLoading = computed(() => state.value === "loading");

const orderCodeDisplay = computed(() => {
  if (!order.value) return "—";
  if (order.value.maHoaDon) return order.value.maHoaDon;
  if (order.value.id) return `HD${String(order.value.id).padStart(6, "0")}`;
  return "—";
});

const placedOnLabel = computed(() =>
  formatDateTimeVN(order.value?.ngayTao || order.value?.createAt)
);

const totalLabel = computed(() => {
  const amount = order.value?.tongTienSauGiam ?? order.value?.tongTien ?? 0;
  return formatCurrency(amount);
});

const shippingAddress = computed(() => {
  return (
    order.value?.diaChiNhanHang ||
    order.value?.diaChiNguoiNhan ||
    order.value?.diaChi ||
    ""
  );
});

const contactSummary = computed(() => {
  const email =
    order.value?.emailNguoiNhan ||
    order.value?.email ||
    order.value?.emailKhachHang;
  const phone =
    order.value?.soDienThoaiNguoiNhan ||
    order.value?.soDienThoai ||
    order.value?.soDienThoaiKhachHang;

  // Format with line break for better display
  const parts: string[] = [];
  if (email) parts.push(email);
  if (phone) parts.push(phone);

  return parts.length > 0 ? parts.join('\n') : "—";
});

const orderItems = computed(() => {
  // Support both 'hoaDonChiTiets' and 'items' field names
  const items = order.value?.hoaDonChiTiets || order.value?.items;

  if (!items?.length) {
    console.warn("[OrderLookup] No order items found in order:", order.value);
    return [];
  }

  return items.map((item, index) => {
    // Tên sản phẩm - từ nhiều nguồn khác nhau
    const productName =
      item.tenSanPhamChiTiet ||
      item.tenSanPham ||
      item.idChiTietSanPham?.idSanPham?.tenSanPham ||
      t("orderLookup.items.product");

    // Mã sản phẩm chi tiết
    const productCode =
      item.maSanPham ||
      item.maHoaDonChiTiet ||
      item.idChiTietSanPham?.maChiTietSanPham ||
      "";

    // Màu sắc - trực tiếp hoặc nested
    const color =
      item.tenMauSac ||
      item.idChiTietSanPham?.idMauSac?.tenMauSac ||
      item.idChiTietSanPham?.idMauSac?.tenMau ||
      "";

    // Kích thước - trực tiếp hoặc nested
    const size =
      item.tenKichThuoc ||
      item.idChiTietSanPham?.idKichThuoc?.tenKichThuoc ||
      item.idChiTietSanPham?.idKichThuoc?.kichThuoc ||
      "";

    const quantity = item.soLuong ?? 0;
    const price = item.giaBan ?? 0;
    const subtotal = item.thanhTien ?? quantity * price;

    const processedItem = {
      id: item.id ?? index,
      productName,
      productCode,
      color,
      size,
      quantity,
      price,
      subtotal,
    };
    return processedItem;
  });
});

const timelineEvents = computed(() => {
  // Sort newest first using robust parsing to handle non-ISO formats
  return [...timeline.value].sort((a, b) => {
    const timeA = parseTimelineInstant(a.thoiGian);
    const timeB = parseTimelineInstant(b.thoiGian);
    return timeB - timeA;
  });
});

const latestTimelineEntry = computed(() => {
  if (!timeline.value.length) return null;

  let best = timeline.value[0];
  let bestScore = parseTimelineInstant(best.thoiGian);

  for (const item of timeline.value) {
    const score = parseTimelineInstant(item.thoiGian);
    if (score >= bestScore) {
      best = item;
      bestScore = score;
    }
  }

  if (bestScore === Number.NEGATIVE_INFINITY) {
    return timeline.value[timeline.value.length - 1];
  }

  return best;
});

const latestStatusId = computed(() => {
  const fromTimeline = extractStatusId(latestTimelineEntry.value);
  if (fromTimeline != null) return fromTimeline;

  const fromSnapshot = extractStatusId(snapshot.value);
  if (fromSnapshot != null) return fromSnapshot;

  const fromOrder = extractStatusId(order.value);
  if (fromOrder != null) return fromOrder;

  return null;
});

const statusUpdatedLabel = computed(() => {
  const source =
    latestTimelineEntry.value?.thoiGian ||
    snapshot.value?.thoiGian ||
    order.value?.ngayTao ||
    null;
  return formatDateTimeVN(source);
});

const hasStatusUpdatedTime = computed(() =>
  Boolean(statusUpdatedLabel.value && statusUpdatedLabel.value !== "—")
);

const currentStatusLabel = computed(() => {
  if (latestTimelineEntry.value?.tenTrangThaiDonHang) {
    return latestTimelineEntry.value.tenTrangThaiDonHang;
  }
  if (latestTimelineEntry.value?.trangThaiMoi) {
    return latestTimelineEntry.value.trangThaiMoi;
  }
  if (snapshot.value?.tenTrangThaiDonHang) {
    return snapshot.value.tenTrangThaiDonHang;
  }
  return t("orderLookup.status.unknown");
});

const statusTone = computed(() => {
  const key = (currentStatusLabel.value || "").toLowerCase();
  if (key.includes("hủy") || key.includes("cancel")) return "tone-danger";
  if (key.includes("giao") || key.includes("delivered")) return "tone-success";
  if (key.includes("chuẩn") || key.includes("processing")) return "tone-warn";
  return "tone-neutral";
});

const normalizedStatusTexts = computed(() => {
  const labels = new Set<string>();
  const pipeline = (value: unknown) => {
    if (value == null) return;

    let raw: string | null = null;
    if (typeof value === "string") {
      raw = value;
    } else if (typeof value === "number") {
      raw = String(value);
    }

    if (!raw) return;

    const normalized = removeDiacritics(raw)
      .toLowerCase()
      .replace(/\s+/g, " ")
      .trim();
    if (normalized) labels.add(normalized);
  };

  pipeline(currentStatusLabel.value);
  pipeline(latestTimelineEntry.value?.tenTrangThaiDonHang);
  pipeline(snapshot.value?.tenTrangThaiDonHang);
  pipeline(order.value?.trangThai);
  pipeline((order.value as any)?.tenTrangThaiDonHang);

  return Array.from(labels);
});

const orderIdForMutation = computed(() => {
  if (order.value?.id && order.value.id > 0) {
    return order.value.id;
  }
  const extracted = extractOrderId(orderCodeDisplay.value);
  return extracted ?? null;
});

const STATUS_PENDING_KEYWORDS = [
  "cho xac nhan",
  "pending confirmation",
  "pending",
];
const STATUS_DELIVERED_KEYWORDS = ["da giao hang", "delivered"];
const STATUS_COMPLETED_KEYWORDS = ["hoan thanh", "completed"];
const STATUS_DELIVERED_ID = 5;
const STATUS_COMPLETED_ID = 9;

const canCancelOrder = computed(() => {
  // Only authenticated users can cancel orders
  if (!isAuthenticated.value) return false;
  if (!order.value) return false;
  if (isProcessingAction.value) return false;
  if (!orderIdForMutation.value) return false;
  const statusTexts = normalizedStatusTexts.value;
  const statusId = latestStatusId.value;
  if (statusId === STATUS_COMPLETED_ID) return false;
  if (!statusTexts.length) return false;
  return STATUS_PENDING_KEYWORDS.some((keyword) =>
    statusTexts.some((text) => text.includes(keyword))
  );
});

const canConfirmDelivery = computed(() => {
  if (!order.value) return false;
  if (isProcessingAction.value) return false;
  if (!orderIdForMutation.value) return false;
  const statusTexts = normalizedStatusTexts.value;
  const statusId = latestStatusId.value;
  if (!statusTexts.length && statusId == null) return false;

  const deliveredById = statusId === STATUS_DELIVERED_ID;
  const completedById = statusId === STATUS_COMPLETED_ID;
  const deliveredByText = STATUS_DELIVERED_KEYWORDS.some((keyword) =>
    statusTexts.some((text) => text.includes(keyword))
  );
  const completedByText = STATUS_COMPLETED_KEYWORDS.some((keyword) =>
    statusTexts.some((text) => text.includes(keyword))
  );

  const delivered = deliveredById || deliveredByText;
  const completed = completedById || completedByText;
  return delivered && !completed;
});

const primaryActionType = computed<"confirm" | "cancel" | "none">(() => {
  if (canConfirmDelivery.value) return "confirm";
  if (canCancelOrder.value) return "cancel";
  return "none";
});

const primaryActionLabel = computed(() => {
  if (primaryActionType.value === "confirm") {
    return isProcessingAction.value
      ? t("orderLookup.actions.completing")
      : t("orderLookup.actions.complete");
  }
  if (primaryActionType.value === "cancel") {
    return isProcessingAction.value
      ? t("orderLookup.actions.cancelling")
      : t("orderLookup.actions.cancel");
  }
  return t("orderLookup.actions.unavailable");
});

const primaryActionDisabled = computed(
  () => primaryActionType.value === "none" || isProcessingAction.value
);

const hasPrimaryAction = computed(() => primaryActionType.value !== "none");

const primaryActionClasses = computed(() => {
  const classes = ["order-action-btn"];
  if (primaryActionType.value === "confirm") {
    classes.push("order-action-btn--confirm");
  } else if (primaryActionType.value === "cancel") {
    classes.push("order-action-btn--cancel");
  }
  return classes;
});

const resultCardClass = computed(() => {
  if (state.value === "success") return "result-card--success";
  if (state.value === "error") return "result-card--error";
  return "result-card--idle";
});

async function lookupOrder() {
  resetState();
  if (!validateForm()) {
    state.value = "idle";
    return;
  }

  const code = orderCode.value.trim().toUpperCase();
  if (!code) {
    formErrors.code = t("orderLookup.error.invalidCode");
    state.value = "idle";
    return;
  }

  const hasLetters = /[A-Za-z]/.test(code);
  const extractedId = extractOrderId(code);
  if (!hasLetters && !extractedId) {
    formErrors.code = t("orderLookup.error.invalidCode");
    state.value = "idle";
    return;
  }

  state.value = "loading";
  try {
    const orderRequest = hasLetters
      ? fetchOrderByCode(code)
      : fetchOrderById(extractedId as number);
    const orderRes = await orderRequest;
    const orderData = orderRes.data;
    if (!orderData) {
      throw new Error(t("orderLookup.error.notFound"));
    }
    if (
      isAuthenticated.value &&
      !matchesContact(orderData, contactValue.value)
    ) {
      throw new Error(t("orderLookup.error.contactMismatch"));
    }

    order.value = orderData;

    const orderIdForTimeline = orderData.id ?? extractedId ?? null;
    if (orderIdForTimeline) {
      // Fetch all timeline events from timeline_don_hang table
      // Each event has thoiGian (time) and hanhDong (action/activity)
      const [timelineRes, statusRes] = await Promise.allSettled([
        fetchOrderTimeline(orderIdForTimeline),
        fetchLatestOrderStatus(orderIdForTimeline),
      ]);
      if (timelineRes.status === "fulfilled" && timelineRes.value.data) {
        const timelineData = timelineRes.value.data;

        // Handle both array and single object responses
        if (Array.isArray(timelineData)) {
          timeline.value = timelineData;
        } else if (timelineData && typeof timelineData === "object") {
          timeline.value = [timelineData];
        } else {
          timeline.value = [];
        }
      } else {
        console.warn(
          "[OrderLookup] Timeline fetch failed:",
          timelineRes.status,
          timelineRes
        );
        timeline.value = [];
      }
      if (statusRes.status === "fulfilled") {
        snapshot.value = statusRes.value.data;
      } else {
        console.warn(
          "[OrderLookup] Status fetch failed:",
          statusRes.status,
          statusRes
        );
        snapshot.value = null;
      }
    } else {
      timeline.value = [];
      snapshot.value = null;
    }
    errorMessage.value = "";
    state.value = "success";
  } catch (error: any) {
    console.warn("Order lookup failed", error);
    errorMessage.value = error?.message ?? t("orderLookup.error.generic");
    order.value = null;
    timeline.value = [];
    snapshot.value = null;
    state.value = "error";
  }
}

function openCancelConfirm() {
  if (!canCancelOrder.value || isProcessingAction.value) return;

  if (!orderIdForMutation.value) {
    showMessageError(t("orderLookup.actions.missingId"));
    return;
  }

  showCancelConfirm.value = true;
}

function closeCancelConfirm() {
  if (isProcessingAction.value) return;
  showCancelConfirm.value = false;
}

async function cancelOrder() {
  // Security: Only authenticated users can cancel orders
  if (!isAuthenticated.value) {
    showMessageError(t("orderLookup.actions.loginRequired"));
    showCancelConfirm.value = false;
    return;
  }

  if (isProcessingAction.value) return;

  const orderId = orderIdForMutation.value;
  if (!orderId) {
    showMessageError(t("orderLookup.actions.missingId"));
    showCancelConfirm.value = false;
    return;
  }

  isProcessingAction.value = true;
  try {
    const reason = t("orderLookup.actions.cancelNote");
    await cancelOrderRequest(orderId, {
      reason,
    });
    showMessageSuccess(t("orderLookup.actions.cancelled"));
    showCancelConfirm.value = false;
    await lookupOrder();
  } catch (error: any) {
    console.error("[OrderLookup] Cancel order failed:", error);
    const message =
      error?.message && typeof error.message === "string"
        ? error.message
        : t("orderLookup.actions.cancelFailed");
    showMessageError(message);
  } finally {
    isProcessingAction.value = false;
  }
}

async function completeOrder() {
  if (isProcessingAction.value) return;

  const orderId = orderIdForMutation.value;
  if (!orderId) {
    showMessageError(t("orderLookup.actions.missingId"));
    return;
  }

  isProcessingAction.value = true;
  try {
    const response = await confirmOrderDelivery(orderId);
    const successText = response?.message || t("orderLookup.actions.completed");
    showMessageSuccess(successText);
    await lookupOrder();
  } catch (error: any) {
    console.error("[OrderLookup] Complete order failed:", error);
    const message =
      error?.message && typeof error.message === "string"
        ? error.message
        : t("orderLookup.actions.completeFailed");
    showMessageError(message);
  } finally {
    isProcessingAction.value = false;
  }
}

function handlePrimaryAction() {
  if (primaryActionDisabled.value) return;
  if (primaryActionType.value === "confirm") {
    completeOrder();
    return;
  }
  if (primaryActionType.value === "cancel") {
    openCancelConfirm();
  }
}

function resetState() {
  formErrors.code = undefined;
  formErrors.contact = undefined;
  errorMessage.value = "";
  if (state.value !== "loading") {
    order.value = null;
    timeline.value = [];
    snapshot.value = null;
  }
}

function validateForm() {
  const code = orderCode.value.trim();
  const contact = contactValue.value.trim();
  if (!code) {
    formErrors.code = t("orderLookup.form.codeError");
  }
  if (isAuthenticated.value) {
    if (!contact) {
      formErrors.contact = t("orderLookup.form.contactError");
    } else if (!isEmail(contact) && !isPhone(contact)) {
      formErrors.contact = t("orderLookup.form.contactFormatError");
    }
  }
  return !formErrors.code && (!isAuthenticated.value || !formErrors.contact);
}

function resolveInitialOrderCode(query: LocationQuery): string | null {
  const candidate = query.code ?? query.orderCode;
  if (candidate == null) return null;
  const value = Array.isArray(candidate) ? candidate[0] : candidate;
  if (typeof value !== "string") return null;
  const trimmed = value.trim();
  if (!trimmed) return null;
  return trimmed.toUpperCase();
}

function extractOrderId(code: string) {
  const digits = code.replace(/\D/g, "");
  if (!digits) return null;
  const parsed = Number(digits);
  if (!Number.isFinite(parsed) || parsed <= 0) return null;
  return parsed;
}

function removeDiacritics(value: string) {
  return value.normalize("NFD").replace(/[\u0300-\u036f]/g, "");
}

function matchesContact(orderData: OrderTrackingDetail, input: string) {
  if (!isAuthenticated.value) return true;
  const trimmed = input.trim();
  if (!trimmed) return false;
  if (isEmail(trimmed)) {
    const normalizedInput = trimmed.toLowerCase();
    const candidate =
      orderData.emailNguoiNhan ||
      orderData.email ||
      orderData.emailKhachHang ||
      "";
    return candidate.toLowerCase() === normalizedInput;
  }

  const normalizedInput = normalizePhone(trimmed);
  if (!normalizedInput) return false;
  const candidates = [
    orderData.soDienThoaiNguoiNhan,
    orderData.soDienThoai,
    orderData.soDienThoaiKhachHang,
  ];
  return candidates.some((value) => {
    const phone = normalizePhone(value || "");
    return (
      phone && (phone === normalizedInput || phone.endsWith(normalizedInput))
    );
  });
}

function normalizePhone(value: string) {
  const digits = value.replace(/\D/g, "");
  return digits.startsWith("0") ? digits.slice(1) : digits;
}

function extractStatusId(source: unknown): number | null {
  if (!source || typeof source !== "object") {
    return null;
  }

  const candidateKeys = [
    "idTrangThaiDonHang",
    "trangThaiDonHangId",
    "idTrangThai",
    "statusId",
  ];

  for (const key of candidateKeys) {
    const raw = (source as Record<string, unknown>)[key];
    const numeric =
      typeof raw === "number"
        ? raw
        : typeof raw === "string"
          ? Number(raw)
          : NaN;

    if (Number.isFinite(numeric) && numeric > 0) {
      return numeric;
    }
  }

  return null;
}

function parseTimelineInstant(value?: string | null) {
  if (!value) return Number.NEGATIVE_INFINITY;

  const raw = String(value).trim();
  if (!raw) return Number.NEGATIVE_INFINITY;

  const normalized =
    raw.includes("T") || raw.includes("Z") ? raw : raw.replace(" ", "T");

  const isoTime = Date.parse(normalized);
  if (!Number.isNaN(isoTime)) {
    return isoTime;
  }

  const match = raw.match(
    /(\d{1,2})[\/\-](\d{1,2})[\/\-](\d{2,4})(?:[ T](\d{1,2}):(\d{2})(?::(\d{2}))?)?/
  );
  if (!match) {
    return Number.NEGATIVE_INFINITY;
  }

  const day = Number(match[1]);
  const month = Number(match[2]) - 1;
  const year = Number(match[3].length === 2 ? `20${match[3]}` : match[3]);
  const hour = Number(match[4] ?? 0);
  const minute = Number(match[5] ?? 0);
  const second = Number(match[6] ?? 0);

  const utcTime = Date.UTC(year, month, day, hour, minute, second);
  if (Number.isNaN(utcTime)) {
    return Number.NEGATIVE_INFINITY;
  }

  return utcTime;
}

function isEmail(value: string) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value.toLowerCase());
}

function isPhone(value: string) {
  return /\d{6,}/.test(value.replace(/\D/g, ""));
}

function formatDateTimeVN(value?: string | null) {
  if (!value) return "—";

  try {
    // Handle both ISO 8601 formats from Java:
    // 1. LocalDateTime format: 2025-01-15T10:30:45
    // 2. Instant format: 2025-01-15T10:30:45Z or 2025-01-15T10:30:45.123Z
    let dateStr = String(value).trim();

    // If it contains space, replace with T for ISO format
    if (dateStr.includes(" ") && !dateStr.includes("T")) {
      dateStr = dateStr.replace(" ", "T");
    }

    const date = new Date(dateStr);
    if (Number.isNaN(date.getTime())) {
      console.warn("[OrderLookup] Invalid date format:", value);
      return "—";
    }

    // Format with Vietnam timezone (Asia/Ho_Chi_Minh - UTC+7)
    return new Intl.DateTimeFormat("vi-VN", {
      timeZone: "Asia/Ho_Chi_Minh",
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
      second: "2-digit",
    }).format(date);
  } catch (error) {
    console.error("[OrderLookup] Error formatting date:", value, error);
    return "—";
  }
}

function formatDateTime(value?: string | null) {
  if (!value) return "—";
  const normalized = value.includes("T") ? value : value.replace(" ", "T");
  const date = new Date(normalized);
  if (Number.isNaN(date.getTime())) return "—";
  return new Intl.DateTimeFormat(undefined, {
    day: "2-digit",
    month: "short",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  }).format(date);
}
</script>

<style scoped lang="scss">
.order-lookup {
  padding: clamp(12px, 6vw, 12px) 0 clamp(12px, 8vw, 12px);
  background: linear-gradient(180deg,
      rgba(17, 17, 17, 0.02),
      rgba(17, 17, 17, 0));
}

.lookup-hero {
  text-align: center;
  margin-bottom: clamp(32px, 5vw, 56px);

  .eyebrow {
    margin: 0;
    text-transform: uppercase;
    letter-spacing: 0.12em;
    font-size: 12px;
    color: rgba(17, 17, 17, 0.55);
  }

  h1 {
    margin: 12px 0 12px;
    font-size: clamp(32px, 6vw, 44px);
    /* font-family: var(--font-family-serif); - Removed to switch to Inter */
    color: #111;
  }

  .lead {
    margin: 0 auto;
    max-width: 560px;
    color: rgba(17, 17, 17, 0.7);
    line-height: 1.6;
  }
}

.lookup-grid {
  display: grid;
  gap: 24px;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  margin-bottom: clamp(32px, 5vw, 64px);
}

.lookup-form,
.result-card {
  background: #fff;
  border: 1px solid rgba(17, 17, 17, 0.08);
  border-radius: 24px;
  padding: clamp(20px, 4vw, 32px);
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.06);
}

.lookup-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 14px;
  color: #111;

  input {
    border: 1px solid rgba(17, 17, 17, 0.15);
    border-radius: 14px;
    padding: 12px 14px;
    font-size: 15px;
    transition: border-color 0.2s ease, box-shadow 0.2s ease;
  }

  input:focus {
    outline: none;
    border-color: #111;
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
  }
}

.field-error {
  color: #d64545;
  font-size: 12px;
}

.submit-btn {
  margin-top: 8px;
  border: none;
  border-radius: 18px;
  padding: 12px 18px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  color: #fff;
  background: #111;
  transition: transform 0.1s ease, box-shadow 0.2s ease, opacity 0.2s ease;
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.2);
}

.btn-content {
  display: inline-flex;
  gap: 8px;
  align-items: center;
}

.inline-spinner {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-top-color: #fff;
  animation: spin 0.8s linear infinite;
}

.form-hint {
  margin: 0;
  font-size: 13px;
  color: rgba(17, 17, 17, 0.55);
}

.result-card {
  display: flex;
  flex-direction: column;
  gap: 16px;

  h3 {
    margin: 0;
    font-size: 20px;
  }
}

.result-card--success {
  border-color: rgba(53, 180, 126, 0.4);
}

.result-card--error {
  border-color: rgba(214, 69, 69, 0.4);
}

.status-chip {
  align-self: flex-start;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 600;
}

.latest-status-dd {
  display: flex;
  flex-direction: column;
  gap: 6px;
  align-items: flex-start;
}

.latest-status-time {
  font-size: 13px;
  color: rgba(17, 17, 17, 0.6);
}

.tone-danger {
  background: rgba(214, 69, 69, 0.12);
  color: #a02121;
}

.tone-success {
  background: rgba(53, 180, 126, 0.15);
  color: #1c7c4a;
}

.tone-warn {
  background: rgba(241, 196, 15, 0.15);
  color: #805a00;
}

.tone-neutral {
  background: rgba(17, 17, 17, 0.05);
  color: #111;
}

.result-grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  margin: 0;

  dt {
    text-transform: uppercase;
    letter-spacing: 0.08em;
    font-size: 11px;
    color: rgba(17, 17, 17, 0.55);
    margin-bottom: 4px;
  }

  dd {
    margin: 0;
    font-size: 15px;
    font-weight: 600;
    color: #111;
    word-break: break-word;
    overflow-wrap: break-word;
    /* modern syntax */
  }
}

.cancel-action-cell {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.order-action-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 16px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  border: 1px solid transparent;
  transition: background 0.2s ease, border-color 0.2s ease, color 0.2s ease,
    box-shadow 0.2s ease;
}

.order-action-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  box-shadow: none;
}

.order-action-btn--cancel {
  border-color: rgba(214, 69, 69, 0.35);
  background: rgba(214, 69, 69, 0.08);
  color: #a02121;
}

.order-action-btn--cancel:hover:not(:disabled) {
  background: rgba(214, 69, 69, 0.16);
  border-color: rgba(214, 69, 69, 0.5);
}

.order-action-btn--cancel .inline-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(160, 33, 33, 0.3);
  border-top-color: #a02121;
}

.order-action-btn--confirm {
  border-color: rgba(0, 180, 42, 0.35);
  background: rgba(0, 180, 42, 0.12);
  color: #087443;
  box-shadow: 0 8px 18px rgba(0, 180, 42, 0.12);
}

.order-action-btn--confirm:hover:not(:disabled) {
  background: rgba(0, 180, 42, 0.2);
  border-color: rgba(0, 180, 42, 0.5);
  box-shadow: 0 10px 22px rgba(0, 180, 42, 0.18);
}

.order-action-btn--confirm .inline-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(8, 116, 67, 0.3);
  border-top-color: #087443;
}

.cancel-confirm-overlay {
  position: fixed;
  inset: 0;
  background: rgba(17, 17, 17, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  z-index: 1200;
  animation: cancelOverlayFade 0.18s ease;
}

.cancel-confirm-dialog {
  width: min(360px, 100%);
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.18);
  display: flex;
  flex-direction: column;
  gap: 16px;
  animation: cancelDialogSlide 0.22s ease;
}

.cancel-confirm-dialog h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #111;
}

.cancel-confirm-dialog p {
  margin: 0;
  font-size: 14px;
  color: rgba(17, 17, 17, 0.75);
}

.cancel-confirm-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.cancel-confirm-secondary,
.cancel-confirm-danger {
  border-radius: 18px;
  padding: 10px 18px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  border: 1px solid transparent;
  transition: transform 0.1s ease, box-shadow 0.2s ease, opacity 0.2s ease;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.cancel-confirm-secondary {
  background: #fff;
  border-color: rgba(17, 17, 17, 0.12);
  color: #111;
}

.cancel-confirm-secondary:hover:not(:disabled) {
  border-color: rgba(17, 17, 17, 0.3);
  box-shadow: 0 10px 24px rgba(17, 17, 17, 0.08);
}

.cancel-confirm-danger {
  background: #a02121;
  color: #fff;
  border-color: transparent;
}

.cancel-confirm-danger:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 14px 32px rgba(160, 33, 33, 0.32);
}

.cancel-confirm-secondary:disabled,
.cancel-confirm-danger:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  box-shadow: none;
  transform: none;
}

.cancel-confirm-danger .inline-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-top-color: #fff;
}

@keyframes cancelOverlayFade {
  from {
    opacity: 0;
  }

  to {
    opacity: 1;
  }
}

@keyframes cancelDialogSlide {
  from {
    transform: translateY(16px) scale(0.98);
    opacity: 0;
  }

  to {
    transform: translateY(0) scale(1);
    opacity: 1;
  }
}

.muted {
  color: rgba(17, 17, 17, 0.6);
  margin: 0;
  font-size: 14px;
}

.text-link {
  font-weight: 600;
  color: #111;
  text-decoration: underline;
}

.section-heading {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  h2 {
    margin: 0;
    font-size: 24px;
  }
}

.timeline-section,
.items-section {
  background: #fff;
  border-radius: 24px;
  border: 1px solid rgba(17, 17, 17, 0.08);
  padding: clamp(20px, 4vw, 32px);
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.06);
  margin-bottom: clamp(24px, 4vw, 40px);
}

.timeline-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.timeline-item {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 16px;
}

.timeline-marker {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid #111;
  margin-top: 6px;
  position: relative;
}

.timeline-marker::after {
  content: "";
  position: absolute;
  width: 2px;
  height: calc(100% + 20px);
  background: rgba(17, 17, 17, 0.12);
  left: 50%;
  top: 12px;
  transform: translateX(-50%);
}

.timeline-item:last-child .timeline-marker::after {
  display: none;
}

.timeline-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 13px;
  color: rgba(17, 17, 17, 0.6);
  margin-bottom: 4px;
}

.timeline-body h3 {
  margin: 0;
  font-size: 18px;
}

.timeline-body .note {
  margin: 6px 0 0;
  color: rgba(17, 17, 17, 0.7);
}

.timeline-empty {
  text-align: center;
  padding: 24px;
  border-radius: 18px;
  background: rgba(17, 17, 17, 0.03);
}

.items-table {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.items-header,
.items-row {
  display: grid;
  grid-template-columns: 2fr 0.6fr 0.8fr 0.8fr;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid rgba(17, 17, 17, 0.08);
}

.items-header {
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: rgba(17, 17, 17, 0.6);
}

.items-row {
  font-size: 14px;
  align-items: center;
}

.product-details {
  font-weight: 600;
  color: #111;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.product-main {
  font-size: 14px;
  font-weight: 600;
  color: #111;
}

.product-code {
  font-size: 11px;
  color: rgba(17, 17, 17, 0.5);
  font-weight: 400;
}

.items-empty {
  padding: 24px;
  text-align: center;
  color: rgba(17, 17, 17, 0.6);
}

@keyframes spin {
  from {
    transform: rotate(0);
  }

  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 768px) {
  .lookup-grid {
    grid-template-columns: 1fr;
  }

  .items-header,
  .items-row {
    grid-template-columns: 1fr;
    grid-template-areas:
      "details"
      "qty"
      "price"
      "subtotal";
  }

  .items-row>div:nth-child(1) {
    grid-area: details;
  }

  .items-row>span:nth-child(2) {
    grid-area: qty;
  }

  .items-row>span:nth-child(3) {
    grid-area: price;
  }

  .items-row>span:nth-child(4) {
    grid-area: subtotal;
    font-weight: 600;
  }
}
</style>
