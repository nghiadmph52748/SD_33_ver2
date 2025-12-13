import axios from "./interceptor";
import type { HttpResponse } from "./interceptor";

// Cached product-image mapping fetched from backend anh_san_pham
const productImageCache: Record<string, string[]> = {};
const pendingImageRequests: Record<string, Promise<void>> = {};
let playlistLoaded = false;

function normalizeKey(value: string): string {
  return value
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase()
    .replace(/\s+/g, " ")
    .trim();
}

function addImageToCache(name: string, url: string) {
  if (!name || !url) return;
  const key = normalizeKey(name);
  if (!productImageCache[key]) {
    productImageCache[key] = [];
  }
  if (!productImageCache[key].includes(url)) {
    productImageCache[key].push(url);
  }
}

async function ensurePlaylistImages(): Promise<void> {
  if (playlistLoaded) return;
  try {
    const res = await axios.get("/api/anh-san-pham-management/playlist");
    const list = Array.isArray(res.data) ? res.data : [];
    list.forEach((item: any) => {
      const rawName = (item?.tenAnh || "").trim();
      const url = typeof item?.duongDanAnh === "string" ? item.duongDanAnh : "";
      if (!rawName || !url) return;
      addImageToCache(rawName, url);
      // If the name follows pattern "Product - Color", also index the product name only
      const [productName] = rawName.split(/\s*-\s*/);
      if (productName && productName !== rawName) {
        addImageToCache(productName, url);
      }
    });
    playlistLoaded = true;
  } catch (e) {
    Object.keys(productImageCache).forEach(
      (key) => delete productImageCache[key]
    );
    playlistLoaded = true;
  }
}

function findImageInCache(
  candidates: string[],
  fallback?: string | null
): string | null {
  for (const candidate of candidates) {
    if (!candidate) continue;
    const key = normalizeKey(candidate);
    const list = productImageCache[key];
    if (list && list.length > 0) {
      return list[0];
    }
  }
  if (fallback) return fallback;
  const firstKey = Object.keys(productImageCache)[0];
  if (firstKey) {
    return productImageCache[firstKey][0];
  }
  return null;
}

export interface ProductVariant {
  id: number;
  idSanPham: number;
  idKichThuoc: number;
  tenKichThuoc?: string;
  idMauSac: number;
  tenMauSac?: string;
  soLuong: number;
  giaBan: number; // Original price
  giaTriGiamGia?: number; // Discount percentage (0-100)
  idDotGiamGia?: number;
  tenDotGiamGia?: string;
  trangThai: boolean;
}

export interface ProductImage {
  id: number;
  idSanPham: number;
  urlAnh: string;
  trangThai: boolean;
}

export interface BackendProduct {
  id: number;
  maSanPham?: string;
  tenSanPham: string;
  moTa?: string;
  giaBan?: number;
  giaNhoNhat?: number;
  giaLonNhat?: number;
  idDanhMuc?: number;
  tenDanhMuc?: string;
  idNhaSanXuat: number;
  tenNhaSanXuat?: string;
  trangThai: boolean;
  soLuongBienThe?: number;
  bienThe?: ProductVariant[];
  hinhAnh?: ProductImage[];
}

// Frontend product interface matching existing store structure
export interface Product {
  id: string;
  color: string;
  description: string;
  gender: "Male" | "Female";
  name: string;
  review: string;
  starrating: number;
  price: number; // Min price after discount
  priceMax?: number; // Max price after discount (if range exists)
  originalPrice?: number; // Original max price (strikethrough when has discount)
  img: string;
  sizes?: string[];
  category?: string; // Category name from backend (tenDanhMuc)
  brand?: string; // Brand name from backend (tenNhaSanXuat)
}

// Convert backend product to frontend format
function buildImageCandidates(
  productName: string,
  primaryColor?: string | null
): string[] {
  const candidates = new Set<string>();
  const trimmedName = productName?.trim() || "";
  const trimmedColor = primaryColor?.trim() || "";
  if (trimmedName && trimmedColor) {
    candidates.add(`${trimmedName} - ${trimmedColor}`);
    candidates.add(`${trimmedName} ${trimmedColor}`);
  }
  if (trimmedName) {
    candidates.add(trimmedName);
  }
  if (trimmedColor) {
    candidates.add(trimmedColor);
  }
  return Array.from(candidates);
}

/**
 * Calculate price range with discount support
 * Returns: { minPrice, maxPrice, minOriginalPrice, maxOriginalPrice }
 */
function calculatePriceRange(variants: ProductVariant[] | undefined) {
  if (!variants || variants.length === 0) {
    return {
      minPrice: 0,
      maxPrice: 0,
      maxOriginalPrice: 0,
    };
  }

  // Calculate prices for each variant considering discounts
  const prices = variants
    .filter((v) => v.giaBan != null && v.giaBan > 0)
    .map((v) => {
      const originalPrice = Number(v.giaBan);
      let discountedPrice = originalPrice;

      // Calculate discounted price if giaTriGiamGia exists
      if (
        v.giaTriGiamGia != null &&
        v.giaTriGiamGia > 0 &&
        v.giaTriGiamGia <= 100
      ) {
        discountedPrice = (originalPrice * (100 - v.giaTriGiamGia)) / 100;
      }

      return { originalPrice, discountedPrice };
    });

  if (prices.length === 0) {
    return {
      minPrice: 0,
      maxPrice: 0,
      maxOriginalPrice: 0,
    };
  }

  const minPrice = Math.min(...prices.map((p) => p.discountedPrice));
  const maxPrice = Math.max(...prices.map((p) => p.discountedPrice));
  const maxOriginalPrice = Math.max(...prices.map((p) => p.originalPrice));

  return { minPrice, maxPrice, maxOriginalPrice };
}

function mapBackendToFrontend(backendProduct: BackendProduct): Product {
  // Calculate price range with discounts
  const priceInfo = calculatePriceRange(backendProduct.bienThe);

  // Get primary color from variants (if available)
  const primaryColor = backendProduct.bienThe?.[0]?.tenMauSac || "Black";

  // Get available sizes from variants (if available)
  const sizes =
    backendProduct.bienThe
      ?.filter((v) => v.trangThai && v.soLuong > 0)
      ?.map((v) => v.tenKichThuoc || String(v.idKichThuoc))
      ?.filter((v, i, arr) => arr.indexOf(v) === i) || // Remove duplicates
    [];

  const primaryImage =
    backendProduct.hinhAnh?.[0]?.urlAnh ||
    findImageInCache(
      buildImageCandidates(backendProduct.tenSanPham, primaryColor)
    );

  const imagePath = mapImageToLocalPath(
    backendProduct.tenSanPham,
    primaryImage
  );

  // Build product name with brand prefix if brand exists
  const brandName = backendProduct.tenNhaSanXuat?.trim();
  const productName = backendProduct.tenSanPham?.trim() || "";
  const displayName = brandName && productName && !productName.toLowerCase().startsWith(brandName.toLowerCase())
    ? `${brandName} ${productName}`
    : productName;

  return {
    id: String(backendProduct.id),
    color: primaryColor,
    description: backendProduct.moTa || backendProduct.tenSanPham,
    gender: determineGender(backendProduct.tenDanhMuc || ""),
    name: displayName,
    review: `Quality product from ${backendProduct.tenNhaSanXuat || "GearUp"}`,
    starrating: 4,
    price: priceInfo.minPrice,
    priceMax:
      priceInfo.minPrice !== priceInfo.maxPrice
        ? priceInfo.maxPrice
        : undefined,
    originalPrice:
      priceInfo.maxOriginalPrice > priceInfo.maxPrice
        ? priceInfo.maxOriginalPrice
        : undefined,
    img: imagePath,
    sizes: sizes.length > 0 ? sizes : undefined,
    category: backendProduct.tenDanhMuc || undefined,
    brand: backendProduct.tenNhaSanXuat || undefined,
  };
}

// Map product name to local image assets
function mapImageToLocalPath(
  productName: string,
  backendImageUrl: string | null
): string {
  // If backend provided an URL or an absolute public path, use it
  if (backendImageUrl) {
    if (backendImageUrl.startsWith("http") || backendImageUrl.startsWith("/")) {
      return backendImageUrl;
    }
    // Treat as file name in public /products
    return `/products/${backendImageUrl}`;
  }
  const cached = findImageInCache(buildImageCandidates(productName));
  if (cached) return cached;

  const normalizedName = productName
    .toLowerCase()
    .replace(/\s+/g, "-")
    .replace(/[^a-z0-9-]/g, "");

  // Known mappings to images included in this repo
  const productImageMap: Record<string, string> = {
    "nike-alphafly-3": "/products/images/nike/nike-alphafly-3/image_1.png",
    "nike-alphafly-3-premium":
      "/products/images/nike/nike-alphafly-3-premium/image_1.png",
    "nike-pegasus-premium":
      "/products/images/nike/nike-pegasus-premium/image_1.png",
    "nike-pegasus-premium-lv8":
      "/products/images/nike/nike-pegasus-premium-lv8/image_1.png",
    "nike-shox-z": "/products/images/nike/nike-shox-z/image_1.png",
    "nike-shox-z-se": "/products/images/nike/nike-shox-z-se/image_1.png",
    "nike-vaporfly-4": "/products/images/nike/nike-vaporfly-4/image_1.png",
    "nike-zoom-fly-6": "/products/images/nike/nike-zoom-fly-6/image_1.png",
    "nike-zoom-fly-6-eliud-kipchoge":
      "/products/images/nike/nike-zoom-fly-6-eliud-kipchoge/image_1.png",
    "adizero-evo-sl-shoes":
      "/products/images/adidas/adizero-evo-sl-shoes/image_1.jpg",
    "adizero-evo-sl": "/products/images/adidas/adizero-evo-sl/image_1.jpg",
    "ultraboost-5x-shoes":
      "/products/images/adidas/ultraboost-5x-shoes/image_1.jpg",
    "team-court-20-shoes":
      "/products/images/adidas/team-court-20-shoes/image_1.jpg",
    "galaxy-6-shoes": "/products/images/adidas/galaxy-6-shoes/image_1.jpg",
    "ultraboost-20-shoes":
      "/products/images/adidas/ultraboost-20-shoes/image_1.jpg",
    "4dfwd-x-strung-running-shoes":
      "/products/images/adidas/4dfwd-x-strung-running-shoes/image_1.jpg",
    "duramo-sl-shoes": "/products/images/adidas/duramo-sl-shoes/image_1.jpg",
    "ultrarun-5-running-shoes":
      "/products/images/adidas/ultrarun-5-running-shoes/image_1.jpg",
  };

  if (productImageMap[normalizedName]) {
    return productImageMap[normalizedName];
  }

  if (normalizedName.includes("nike")) {
    return `/products/images/nike/${normalizedName}/image_1.png`;
  }
  if (normalizedName.includes("adidas") || normalizedName.includes("adizero")) {
    return `/products/images/adidas/${normalizedName}/image_1.jpg`;
  }

  // Fallback to flat public folder by name
  return `/products/${normalizedName}.jpg`;
}

// Determine gender from category name
function determineGender(categoryName: string): "Male" | "Female" {
  const lower = categoryName.toLowerCase();
  if (
    lower.includes("nữ") ||
    lower.includes("women") ||
    lower.includes("female")
  ) {
    return "Female";
  }
  if (
    lower.includes("nam") ||
    lower.includes("men") ||
    lower.includes("male")
  ) {
    return "Male";
  }
  return "Male"; // Default
}

// Filter products to only sneakers
function isSneaker(product: BackendProduct): boolean {
  const categoryName = product.tenDanhMuc?.toLowerCase() || "";
  const productName = product.tenSanPham?.toLowerCase() || "";

  // Check if it's a sneaker based on category or name
  // More lenient matching - if it contains shoe-related keywords, it's a sneaker
  const sneakerKeywords = [
    "giày",
    "giay",
    "sneaker",
    "sneakers",
    "shoe",
    "shoes",
    "running",
    "sport",
    "athletic",
    "nike",
    "adidas",
    "puma",
    "converse",
    "vans",
    "jordan",
    "air",
    "boost",
    "zoom",
    "pegasus",
    "cortez",
    "blazer",
    "dunk",
    "force",
    "max",
    "react",
    "alphafly",
    "vaporfly",
    "ultra",
    "yeezy",
    "superstar",
    "stan smith",
    "gazelle",
    "samba",
    "luka",
    "kyrie",
    "lebron",
    "sabrina",
    "kd",
    "durant",
    "giannis",
    "curry",
    "tatum",
    "ja",
    "morant",
  ];

  // Also exclude non-sneaker items
  const excludeKeywords = [
    "bag",
    "túi",
    "backpack",
    "wallet",
    "ví",
    "accessory",
  ];
  const hasExclude = excludeKeywords.some(
    (keyword) => categoryName.includes(keyword) || productName.includes(keyword)
  );

  if (hasExclude) return false;

  return sneakerKeywords.some(
    (keyword) => categoryName.includes(keyword) || productName.includes(keyword)
  );
}

export function getProductsList(): Promise<HttpResponse<BackendProduct[]>> {
  return axios.get("/api/san-pham-management/playlist");
}

export function getProductsPaging(
  page: number = 0,
  size: number = 20
): Promise<HttpResponse<any>> {
  return axios.get("/api/san-pham-management/paging", {
    params: { page, size },
  });
}

export function getProductById(
  id: number | string
): Promise<HttpResponse<BackendProduct>> {
  return axios.get(`/api/san-pham-management/detail/${id}`);
}

export async function getProductsByCategory(
  categoryId: number
): Promise<HttpResponse<BackendProduct[]>> {
  const response = await getProductsList();
  // Response from interceptor is { success: true, data: [...], message: "..." }
  const allProducts = Array.isArray(response.data) ? response.data : [];
  const filtered = allProducts.filter(
    (p: BackendProduct) => p.idDanhMuc === categoryId
  );
  return {
    success: response.success,
    message: response.message,
    data: filtered,
  };
}

// Get only sneaker products (filtered)
async function ensureImagesForCandidates(candidates: string[]): Promise<void> {
  for (const candidate of candidates) {
    if (!candidate) continue;
    const key = normalizeKey(candidate);
    if (productImageCache[key]?.length) {
      continue;
    }
    if (!pendingImageRequests[key]) {
      pendingImageRequests[key] = (async () => {
        try {
          const res = await axios.get(
            "/api/anh-san-pham-management/by-product-name",
            {
              params: { tenAnh: candidate },
            }
          );
          const list = Array.isArray(res.data) ? res.data : [];
          list.forEach((item: any) => {
            const rawName = (item?.tenAnh || "").trim();
            const url =
              typeof item?.duongDanAnh === "string" ? item.duongDanAnh : "";
            if (!rawName || !url) return;
            addImageToCache(rawName, url);
            const [productName] = rawName.split(/\s*-\s*/);
            if (productName && productName !== rawName) {
              addImageToCache(productName, url);
            }
          });
        } catch {
          // swallow errors; fallbacks will handle missing images
        } finally {
          delete pendingImageRequests[key];
        }
      })();
    }
    await pendingImageRequests[key];
  }
}

async function resolveProductImage(
  product: BackendProduct,
  primaryColor: string
): Promise<string | null> {
  const candidates = buildImageCandidates(product.tenSanPham, primaryColor);
  const cached = findImageInCache(candidates);
  if (cached) return cached;
  await ensureImagesForCandidates(candidates);
  return findImageInCache(candidates);
}

async function mapBackendToFrontendAsync(
  backendProduct: BackendProduct
): Promise<Product> {
  // Calculate price range with discounts
  const priceInfo = calculatePriceRange(backendProduct.bienThe);

  const primaryColor = backendProduct.bienThe?.[0]?.tenMauSac || "Black";

  const sizes =
    backendProduct.bienThe
      ?.filter((v) => v.trangThai && v.soLuong > 0)
      ?.map((v) => v.tenKichThuoc || String(v.idKichThuoc))
      ?.filter((v, i, arr) => arr.indexOf(v) === i) || [];

  await ensurePlaylistImages();
  
  // Use the first image from hinhAnh array (which matches the first gallery image)
  // hinhAnh is built from active variants' images, so hinhAnh[0] should be the first variant's first image
  let resolvedImage: string | null = null;
  if (backendProduct.hinhAnh && backendProduct.hinhAnh.length > 0) {
    // Use the first image from hinhAnh, which should match the first gallery thumbnail
    const firstImageUrl = backendProduct.hinhAnh[0]?.urlAnh;
    if (firstImageUrl) {
      resolvedImage = firstImageUrl;
    }
  }
  
  // Fallback to resolving from product name/color if no hinhAnh
  if (!resolvedImage) {
    resolvedImage = await resolveProductImage(backendProduct, primaryColor);
  }

  const imagePath = mapImageToLocalPath(
    backendProduct.tenSanPham,
    resolvedImage
  );

  // Build product name with brand prefix if brand exists
  const brandName = backendProduct.tenNhaSanXuat?.trim();
  const productName = backendProduct.tenSanPham?.trim() || "";
  const displayName = brandName && productName && !productName.toLowerCase().startsWith(brandName.toLowerCase())
    ? `${brandName} ${productName}`
    : productName;

  return {
    id: String(backendProduct.id),
    color: primaryColor,
    description: backendProduct.moTa || backendProduct.tenSanPham,
    gender: determineGender(backendProduct.tenDanhMuc || ""),
    name: displayName,
    review: `Quality product from ${backendProduct.tenNhaSanXuat || "GearUp"}`,
    starrating: 4,
    price: priceInfo.minPrice,
    priceMax:
      priceInfo.minPrice !== priceInfo.maxPrice
        ? priceInfo.maxPrice
        : undefined,
    originalPrice:
      priceInfo.maxOriginalPrice > priceInfo.maxPrice
        ? priceInfo.maxOriginalPrice
        : undefined,
    img: imagePath,
    sizes: sizes.length > 0 ? sizes : undefined,
    category: backendProduct.tenDanhMuc || undefined,
    brand: backendProduct.tenNhaSanXuat || undefined,
  };
}

export async function getSneakerProducts(): Promise<Product[]> {
  try {
    await ensurePlaylistImages();
    const response = await getProductsList();
    const backendProducts: BackendProduct[] = Array.isArray(response.data)
      ? response.data
      : [];

    if (!Array.isArray(response.data) || backendProducts.length === 0) {
      return [];
    }

    // Show all active products without keyword filtering
    const activeProducts = backendProducts.filter(
      (p: BackendProduct) => p.trangThai
    );
    return await Promise.all(activeProducts.map(mapBackendToFrontendAsync));
  } catch (error: any) {
    console.error("Error fetching sneaker products:", error);
    console.error("Error details:", error.response?.data || error.message);
    return [];
  }
}

export async function getAllProducts(): Promise<Product[]> {
  try {
    await ensurePlaylistImages();
    const response = await getProductsList();
    const backendProducts: BackendProduct[] = Array.isArray(response.data)
      ? response.data
      : [];

    const activeProducts = backendProducts.filter(
      (p: BackendProduct) => p.trangThai
    );
    return await Promise.all(activeProducts.map(mapBackendToFrontendAsync));
  } catch (error: any) {
    console.error("Error fetching products:", error);
    console.error("Error details:", error.response?.data || error.message);
    return [];
  }
}

export async function getBestSellingProducts(limit: number = 3): Promise<Product[]> {
  try {
    await ensurePlaylistImages();
    const response = await axios.get<HttpResponse<BackendProduct[]>>("/api/san-pham-management/best-selling", {
      params: { limit },
    });
    const backendProducts: BackendProduct[] = Array.isArray(response.data?.data)
      ? response.data.data
      : [];

    if (backendProducts.length === 0) {
      return [];
    }

    return await Promise.all(backendProducts.map(mapBackendToFrontendAsync));
  } catch (error: any) {
    console.error("Error fetching best-selling products:", error);
    console.error("Error details:", error.response?.data || error.message);
    return [];
  }
}
