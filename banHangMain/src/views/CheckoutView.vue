<template>
  <div class="checkout-page">
    <div class="container">
      <h1>{{ $t("cart.checkout") }}</h1>
      <div class="grid">
        <section class="details">
          <form class="form" @submit.prevent>
            <div class="row two">
              <div>
                <label for="fullName">{{ $t("checkout.fullName") }}</label>
                <input
                  id="fullName"
                  v-model.trim="contact.fullName"
                  :class="{ invalid: errs.fullName }"
                  type="text"
                  placeholder="Nguyen Van A"
                />
                <small v-if="errs.fullName" class="error-text">{{
                  errs.fullName
                }}</small>
              </div>
              <div>
                <label for="phone">{{ $t("checkout.phone") }}</label>
                <input
                  id="phone"
                  v-model.trim="contact.phone"
                  :class="{ invalid: errs.phone }"
                  type="tel"
                  placeholder="090..."
                />
                <small v-if="errs.phone" class="error-text">{{
                  errs.phone
                }}</small>
              </div>
            </div>
            <div class="row">
              <label for="email">{{ $t("checkout.email") }}</label>
              <input
                id="email"
                v-model.trim="contact.email"
                :class="{ invalid: errs.email }"
                type="email"
                placeholder="you@example.com"
              />
              <small v-if="errs.email" class="error-text">{{
                errs.email
              }}</small>
            </div>
            <div class="row two">
              <div>
                <label for="province">{{ $t("checkout.province") }}</label>
                <select
                  id="province"
                  v-model="address.province"
                  @change="onProvinceChange"
                  :class="{ invalid: errs.province }"
                >
                  <option value="">{{ "—" }}</option>
                  <option v-for="p in provinces" :key="p.code" :value="p.value">
                    {{ p.label }}
                  </option>
                </select>
                <small v-if="errs.province" class="error-text">{{
                  errs.province
                }}</small>
              </div>
              <div>
                <label for="district">{{ $t("checkout.district") }}</label>
                <select
                  id="district"
                  v-model="address.district"
                  @change="onDistrictChange"
                  :disabled="!districts.length"
                  :class="{ invalid: errs.district }"
                >
                  <option value="">{{ "—" }}</option>
                  <option v-for="d in districts" :key="d.code" :value="d.value">
                    {{ d.label }}
                  </option>
                </select>
                <small v-if="errs.district" class="error-text">{{
                  errs.district
                }}</small>
              </div>
            </div>
            <div class="row two">
              <div>
                <label for="ward">{{ $t("checkout.ward") }}</label>
                <select
                  id="ward"
                  v-model="address.ward"
                  :disabled="!wards.length"
                  :class="{ invalid: errs.ward }"
                >
                  <option value="">{{ "—" }}</option>
                  <option v-for="w in wards" :key="w.value" :value="w.value">
                    {{ w.label }}
                  </option>
                </select>
                <small v-if="errs.ward" class="error-text">{{
                  errs.ward
                }}</small>
              </div>
              <div>
                <label for="street">{{ $t("checkout.street") }}</label>
                <input
                  id="street"
                  v-model.trim="address.street"
                  :class="{ invalid: errs.street }"
                  type="text"
                  placeholder="123 Street"
                />
                <small v-if="errs.street" class="error-text">{{
                  errs.street
                }}</small>
              </div>
            </div>
            <div class="row">
              <label>{{ $t("checkout.paymentMethod") }}</label>
              <div class="payment-methods">
                <label class="payment-option">
                  <input type="radio" v-model="paymentMethod" value="cod" />
                  <span class="payment-label">
                    <strong>{{ $t("checkout.cod") }}</strong>
                    <small>{{ $t("checkout.codDesc") }}</small>
                  </span>
                </label>
                <label class="payment-option">
                  <input type="radio" v-model="paymentMethod" value="vnpay" />
                  <span class="payment-label">
                    <strong>VNPAY</strong>
                    <small>{{ $t("checkout.vnpayDesc") }}</small>
                  </span>
                </label>
              </div>
            </div>
            <div class="row" v-if="paymentMethod === 'vnpay'">
              <label for="bank">{{ $t("checkout.bankOptional") }}</label>
              <select id="bank" v-model="bankCode">
                <option value="">{{ "—" }}</option>
                <option value="NCB">NCB</option>
                <option value="VCB">VCB</option>
                <option value="ABB">ABBANK</option>
              </select>
            </div>
          </form>
          <!-- Inline alert inside details to avoid shifting the summary column -->
          <div v-if="showAllEmptyAlert" class="form-alert">
            {{
              t("checkout.err.requiredAll") ||
              "Vui lòng điền đầy đủ thông tin trước khi thanh toán."
            }}
          </div>
        </section>
        <section class="summary">
          <h2>{{ $t("cart.summary") }}</h2>
          <div class="row">
            <span>{{ $t("cart.subtotal") }}</span>
            <span>{{ cartCount === 0 ? "—" : formatCurrency(cartTotal) }}</span>
          </div>
          <div class="row muted">
            <span>{{ $t("cart.estimatedDelivery") }}</span>
            <span>{{
              cartCount === 0 ? "—" : formatCurrency(shippingFee)
            }}</span>
          </div>
          <div class="row muted">
            <span>VAT (10%)</span>
            <span>{{ cartCount === 0 ? "—" : formatCurrency(vat) }}</span>
          </div>
          <hr />
          <div class="row total">
            <span>{{ $t("cart.total") }}</span>
            <span>{{
              cartCount === 0 ? "—" : formatCurrency(orderTotal)
            }}</span>
          </div>
          <div class="actions">
            <button
              class="btn btn-block"
              :disabled="cartCount === 0 || paying || !paymentMethod"
              @click="handleCheckout"
            >
              <span v-if="!paying">
                <span v-if="paymentMethod === 'cod'"
                  >{{ $t("checkout.placeOrder") }} — COD</span
                >
                <span v-else-if="paymentMethod === 'vnpay'"
                  >{{ $t("cart.checkout") }} — VNPAY</span
                >
                <span v-else>{{ $t("cart.checkout") }}</span>
              </span>
              <span v-else>{{ $t("cart.processing") }}</span>
            </button>
          </div>
          <p v-if="error" class="error">{{ error }}</p>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import { storeToRefs } from "pinia";
import { useCartStore } from "@/stores/cart";
import { useUserStore } from "@/stores/user";
import { formatCurrency } from "@/utils/currency";
import { createVnPayPayment } from "@/api/payment";
import { createOrderFromCart } from "@/api/orders";
import { fetchVariantsByProduct } from "@/api/variants";
import type { CustomerAddress } from "@/api/auth";

// i18n
const { t } = useI18n();
const router = useRouter();
const cartStore = useCartStore();
const userStore = useUserStore();
const { cartCount, cartTotal, cart } = storeToRefs(cartStore);

const shippingFee = 250000;
const vat = computed(() => Math.round((cartTotal.value + shippingFee) * 0.1));
const orderTotal = computed(() => cartTotal.value + shippingFee + vat.value);

const paying = ref(false);
const error = ref("");
const paymentMethod = ref<"cod" | "vnpay" | "">("cod"); // Default to COD
const bankCode = ref<string>("");
const contact = ref({ fullName: "", email: "", phone: "", address: "" });

// Address picker (provinces.open-api.vn)
const provincesApiUrl =
  import.meta.env.VITE_PROVINCES_API_URL || "https://provinces.open-api.vn/api";
const provinces = ref<{ value: string; label: string; code: number }[]>([]);
const districts = ref<{ value: string; label: string; code: number }[]>([]);
const wards = ref<{ value: string; label: string }[]>([]);
const address = ref({ province: "", district: "", ward: "", street: "" });
const hasPrefilledAddress = ref(false);

const defaultCustomerAddress = computed<CustomerAddress | null>(() => {
  const rawAddresses = (userStore.profile as any)?.listDiaChi;
  if (!Array.isArray(rawAddresses) || rawAddresses.length === 0) {
    return null;
  }
  const addresses = rawAddresses as CustomerAddress[];
  return addresses.find((addr) => addr?.macDinh) || addresses[0] || null;
});

// Prefill contact info when a customer is logged in
watch(
  () => userStore.profile,
  (profile) => {
    if (!profile) return;
    if (!contact.value.fullName) {
      contact.value.fullName = profile.tenKhachHang || "";
    }
    if (!contact.value.email) {
      contact.value.email = profile.email || "";
    }
    if (!contact.value.phone) {
      contact.value.phone = profile.soDienThoai || "";
    }
  },
  { immediate: true }
);

watch(
  () => userStore.profile?.id,
  () => {
    hasPrefilledAddress.value = false;
  }
);

watch(
  [defaultCustomerAddress, () => provinces.value.length],
  async ([addr, provinceCount]) => {
    if (!addr) {
      hasPrefilledAddress.value = false;
      return;
    }
    if (provinceCount === 0 || hasPrefilledAddress.value) {
      return;
    }
    await prefillAddressFromCustomer(addr);
  },
  { immediate: true }
);

// Validation errors
const errs = ref<Record<string, string>>({
  fullName: "",
  email: "",
  phone: "",
  province: "",
  district: "",
  ward: "",
  street: "",
});

// Show a global alert when all fields are empty and user clicks pay
const showAllEmptyAlert = ref(false);

function normalizeLocationName(name: string): string {
  if (!name) {
    return "";
  }
  let normalized = name
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase();

  normalized = normalized.replace(/[^a-z0-9\s\.]/g, " ");
  normalized = normalized.replace(/\b(tp|t\s*p)\.?(?=\s)/g, "thanh pho");
  normalized = normalized.replace(/\b(q|quan)\.?(?=\s|$)/g, "quan");
  normalized = normalized.replace(/\b(p|phuong)\.?(?=\s|$)/g, "phuong");

  return normalized.replace(/\s+/g, " ").trim();
}

function isSameLocationName(a?: string, b?: string): boolean {
  if (!a || !b) return false;
  return normalizeLocationName(a) === normalizeLocationName(b);
}

async function loadDistrictsForProvinceName(
  provinceName: string
): Promise<boolean> {
  if (!provinceName) {
    return false;
  }

  const province = provinces.value.find(
    (p) => p.value === provinceName || isSameLocationName(p.value, provinceName)
  );

  if (!province) {
    return false;
  }

  districts.value = [];
  wards.value = [];
  address.value.district = "";
  address.value.ward = "";

  try {
    const res = await fetch(`${provincesApiUrl}/p/${province.code}?depth=2`);
    const data = await res.json();
    districts.value = (data.districts || []).map((d: any) => ({
      value: d.name,
      label: d.name,
      code: d.code,
    }));
    address.value.province = province.value;
    return true;
  } catch {
    return false;
  }
}

async function loadWardsForDistrictName(
  districtName: string
): Promise<boolean> {
  if (!districtName) {
    return false;
  }

  const district = districts.value.find(
    (d) => d.value === districtName || isSameLocationName(d.value, districtName)
  );

  if (!district) {
    return false;
  }

  wards.value = [];
  address.value.ward = "";

  try {
    const res = await fetch(`${provincesApiUrl}/d/${district.code}?depth=2`);
    const data = await res.json();
    wards.value = (data.wards || []).map((w: any) => ({
      value: w.name,
      label: w.name,
    }));
    address.value.district = district.value;
    return true;
  } catch {
    return false;
  }
}

async function prefillAddressFromCustomer(
  addr: CustomerAddress | null
): Promise<void> {
  if (!addr) {
    return;
  }

  let provinceLoaded = true;

  if (addr.thanhPho) {
    const needsProvinceLoad =
      !address.value.province ||
      !isSameLocationName(address.value.province, addr.thanhPho) ||
      districts.value.length === 0;

    if (needsProvinceLoad) {
      provinceLoaded = await loadDistrictsForProvinceName(addr.thanhPho);
    }
  }

  if (provinceLoaded && addr.quan) {
    if (districts.value.length === 0) {
      await loadDistrictsForProvinceName(
        address.value.province || addr.thanhPho || ""
      );
    }
    const districtMatch = districts.value.find((d) =>
      isSameLocationName(d.value, addr.quan)
    );
    if (districtMatch) {
      address.value.district = districtMatch.value;
      await loadWardsForDistrictName(districtMatch.value);
    }
  }

  if (wards.value.length && addr.phuong) {
    const wardMatch = wards.value.find((w) =>
      isSameLocationName(w.value, addr.phuong)
    );
    if (wardMatch) {
      address.value.ward = wardMatch.value;
    }
  }

  if (addr.diaChiCuThe && !address.value.street) {
    address.value.street = addr.diaChiCuThe;
  }

  hasPrefilledAddress.value = true;
}

function validateDetails(): boolean {
  errs.value = {
    fullName: "",
    email: "",
    phone: "",
    province: "",
    district: "",
    ward: "",
    street: "",
  };
  let ok = true;
  if (!contact.value.fullName.trim()) {
    errs.value.fullName = t("checkout.err.fullName") as string;
    ok = false;
  }
  const email = contact.value.email.trim();
  if (!email || !/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(email)) {
    errs.value.email = t("checkout.err.email") as string;
    ok = false;
  }
  const phone = contact.value.phone.trim();
  if (!phone || !/^[0-9+\-\s]{8,15}$/.test(phone)) {
    errs.value.phone = t("checkout.err.phone") as string;
    ok = false;
  }
  if (!address.value.province) {
    errs.value.province = t("checkout.err.province") as string;
    ok = false;
  }
  if (!address.value.district) {
    errs.value.district = t("checkout.err.district") as string;
    ok = false;
  }
  if (!address.value.ward) {
    errs.value.ward = t("checkout.err.ward") as string;
    ok = false;
  }
  if (!address.value.street.trim()) {
    errs.value.street = t("checkout.err.street") as string;
    ok = false;
  }
  return ok;
}

// Realtime, field-level validation
function validateField(field: keyof typeof errs.value) {
  switch (field) {
    case "fullName": {
      errs.value.fullName = contact.value.fullName.trim()
        ? ""
        : (t("checkout.err.fullName") as string);
      break;
    }
    case "email": {
      const email = contact.value.email.trim();
      errs.value.email =
        email && /^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(email)
          ? ""
          : (t("checkout.err.email") as string);
      break;
    }
    case "phone": {
      const phone = contact.value.phone.trim();
      errs.value.phone =
        phone && /^[0-9+\-\s]{8,15}$/.test(phone)
          ? ""
          : (t("checkout.err.phone") as string);
      break;
    }
    case "province": {
      errs.value.province = address.value.province
        ? ""
        : (t("checkout.err.province") as string);
      break;
    }
    case "district": {
      errs.value.district = address.value.district
        ? ""
        : (t("checkout.err.district") as string);
      break;
    }
    case "ward": {
      errs.value.ward = address.value.ward
        ? ""
        : (t("checkout.err.ward") as string);
      break;
    }
    case "street": {
      errs.value.street = address.value.street.trim()
        ? ""
        : (t("checkout.err.street") as string);
      break;
    }
  }
}

// Watchers to update validation in realtime as user edits
watch(
  () => contact.value.fullName,
  () => validateField("fullName")
);
watch(
  () => contact.value.email,
  () => validateField("email")
);
watch(
  () => contact.value.phone,
  () => validateField("phone")
);
watch(
  () => address.value.province,
  () => validateField("province")
);
watch(
  () => address.value.district,
  () => validateField("district")
);
watch(
  () => address.value.ward,
  () => validateField("ward")
);
watch(
  () => address.value.street,
  () => validateField("street")
);

async function loadProvinces() {
  try {
    const res = await fetch(`${provincesApiUrl}/p/`);
    const data = await res.json();
    provinces.value = data.map((p: any) => ({
      value: p.name,
      label: p.name,
      code: p.code,
    }));
  } catch {
    // ignore
  }
}
loadProvinces();

async function onProvinceChange() {
  await loadDistrictsForProvinceName(address.value.province);
}

async function onDistrictChange() {
  await loadWardsForDistrictName(address.value.district);
}

async function handleCheckout() {
  if (cartCount.value === 0 || paying.value || !paymentMethod.value) return;
  if (!validateDetails()) {
    // Check if all fields are empty to show a global alert
    const allEmpty =
      !contact.value.fullName.trim() &&
      !contact.value.email.trim() &&
      !contact.value.phone.trim() &&
      !address.value.province &&
      !address.value.district &&
      !address.value.ward &&
      !address.value.street.trim();
    showAllEmptyAlert.value = allEmpty;

    const first = document.querySelector(
      ".details .invalid"
    ) as HTMLElement | null;
    if (first?.focus) first.focus();
    return;
  }
  showAllEmptyAlert.value = false;
  paying.value = true;
  error.value = "";

  try {
    if (paymentMethod.value === "cod") {
      await handleCODCheckout();
    } else if (paymentMethod.value === "vnpay") {
      await handleVnpayCheckout();
    }
  } catch (e: any) {
    error.value = e?.message || "Checkout failed";
    paying.value = false;
  }
}

async function handleCODCheckout() {
  try {
    // Build delivery address
    const deliveryAddress = [
      address.value.street,
      address.value.ward,
      address.value.district,
      address.value.province,
    ]
      .filter(Boolean)
      .join(", ");

    // Ensure cart items have variant IDs
    const cartItemsWithVariants = await Promise.all(
      cart.value.map(async (item) => {
        // If variant ID already exists, use it
        if ((item as any).idBienThe) {
          return item;
        }

        // Otherwise, try to find variant ID from backend
        try {
          const productId = Number(item.id);
          if (!Number.isNaN(productId)) {
            const variants = await fetchVariantsByProduct(productId);
            // Find matching variant by color and size
            const matchingVariant = variants.find((v) => {
              const variantColor = (v.tenMauSac || "").trim().toLowerCase();
              const variantSize = (v.tenKichThuoc || "").trim();
              const itemColor = (item.color || "").trim().toLowerCase();
              const itemSize = item.size?.trim();

              return variantColor === itemColor && variantSize === itemSize;
            });

            if (matchingVariant?.id) {
              return { ...item, idBienThe: matchingVariant.id };
            }
          }
        } catch (error) {
          console.warn("Failed to fetch variant for item:", item.id, error);
        }

        // Return item as-is if variant not found (backend may handle it)
        return item;
      })
    );

    // Create order with COD payment method (typically ID 1 or 2, adjust based on your backend)
    // COD payment method ID is usually 1 or 2, VNPAY might be 2 or 3
    const codPaymentMethodId = 1; // Adjust this based on your backend

    const orderNotes = `Giao hàng đến: ${deliveryAddress}\nNgười nhận: ${contact.value.fullName}\nSĐT: ${contact.value.phone}\nEmail: ${contact.value.email}`;

    const order = await createOrderFromCart(cartItemsWithVariants, {
      customerId: userStore.id || undefined,
      paymentMethodId: codPaymentMethodId,
      notes: orderNotes,
      shippingFee,
      subtotal: cartTotal.value,
      totalAmount: orderTotal.value,
      recipient: {
        fullName: contact.value.fullName,
        phone: contact.value.phone,
        email: contact.value.email,
        address: deliveryAddress,
      },
    });

    if (!order) {
      throw new Error("Không thể tạo đơn hàng");
    }

    // Clear cart after successful order
    cartStore.clearCart();

    // Redirect to COD success page
    router.push({
      path: "/payment/cod/result",
      query: {
        status: "success",
        orderId: order.maHoaDon || order.id.toString(),
      },
    });
  } catch (e: any) {
    throw new Error(e?.message || "Không thể tạo đơn hàng COD");
  }
}

async function handleVnpayCheckout() {
  try {
    const res = await createVnPayPayment({
      amount: Math.round(orderTotal.value),
      orderInfo: "Thanh toan don hang",
      locale: "vn",
      bankCode: bankCode.value || undefined,
    });
    const url = res.data?.payUrl;
    if (!url) {
      throw new Error("Missing payUrl");
    }
    window.location.href = url;
  } catch (e: any) {
    throw new Error(e?.message || "Payment init failed");
  }
}
</script>

<style scoped lang="scss">
.checkout-page {
  padding: 24px 0 48px;
}
.grid {
  display: grid;
  grid-template-columns: 1fr 400px;
  gap: 24px;
}
.details {
  background: #ffffff;
  border: 1px solid #f0f0f0;
  border-radius: 16px;
  padding: 20px;
}
.form-alert {
  margin-top: 8px;
  padding: 10px 12px;
  border-radius: 8px;
  background: #fff7f7;
  border: 1px solid #fecaca;
  color: #b91c1c;
  font-size: 13px;
}
.form {
  display: grid;
  gap: 12px;
}
.row {
  display: grid;
  gap: 6px;
}
.row.two {
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.row.two > div {
  min-width: 0;
}
.row label {
  font-size: 13px;
  color: #4e5969;
}
.row input,
.row select {
  width: 100%;
  border: 1px solid #e5e5e5;
  border-radius: 8px;
  padding: 10px 12px;
  font: inherit;
}
.row .invalid {
  border-color: #dc2626;
  outline: 0;
  box-shadow: 0 0 0 3px rgba(220, 38, 38, 0.12);
}
.error-text {
  color: #dc2626;
  font-size: 12px;
}
.payment-methods {
  display: grid;
  gap: 12px;
}
.payment-option {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  border: 2px solid #e5e5e5;
  border-radius: 8px;
  cursor: pointer;
  transition: border-color 0.15s ease, background-color 0.15s ease;
}
.payment-option:hover {
  border-color: #d1d5db;
  background-color: #f9fafb;
}
.payment-option input[type="radio"] {
  width: auto;
  margin-top: 2px;
  cursor: pointer;
}
.payment-option input[type="radio"]:checked + .payment-label {
  color: #111;
}
.payment-option:has(input[type="radio"]:checked) {
  border-color: #111;
  background-color: #f9fafb;
}
.payment-label {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
}
.payment-label strong {
  font-size: 14px;
  font-weight: 600;
  color: #111;
}
.payment-label small {
  font-size: 12px;
  color: #6b7280;
}
.summary {
  background: #ffffff;
  border: 1px solid #f0f0f0;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.06);
}
.summary h2 {
  margin: 0 0 12px 0;
}
.row {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
}
.row.muted span:first-child {
  color: #4e5969;
}
.row.total span:first-child {
  font-weight: 700;
}
.actions {
  margin-top: 12px;
}
.error {
  color: #c53030;
  margin: 12px 0 0;
}

@media (max-width: 980px) {
  .grid {
    grid-template-columns: 1fr;
  }
}
</style>
