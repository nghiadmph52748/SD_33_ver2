import axios from "./interceptor";

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export interface CustomerAddress {
  tenDiaChi?: string;
  diaChiCuThe?: string;
  thanhPho?: string;
  quan?: string;
  phuong?: string;
  macDinh?: boolean;
}

export interface CustomerProfile {
  id: number;
  maKhachHang?: string;
  tenKhachHang: string;
  tenTaiKhoan?: string;
  email?: string;
  soDienThoai?: string;
  diaChi?: string;
  listDiaChi?: CustomerAddress[];
}

export interface LoginResponseData {
  accessToken: string;
  refreshToken?: string;
  khachHang: CustomerProfile;
}

export interface RegisterPayload {
  fullName: string;
  email: string;
  password: string;
}

export function loginCustomer(
  identifier: string,
  password: string
): Promise<ApiResponse<LoginResponseData>> {
  return axios
    .post<ApiResponse<LoginResponseData>>("/api/khach-hang/auth/login", {
      identifier,
      password,
    })
    .then((res) => res as unknown as ApiResponse<LoginResponseData>);
}

export function registerCustomer(
  payload: RegisterPayload
): Promise<ApiResponse<LoginResponseData>> {
  return axios
    .post<ApiResponse<LoginResponseData>>('/api/khach-hang/auth/register', payload)
    .then((res) => res as unknown as ApiResponse<LoginResponseData>);
}

export function refreshToken(
  refreshToken: string
): Promise<ApiResponse<{ accessToken: string }>> {
  return axios
    .post<ApiResponse<{ accessToken: string }>>(
      "/api/khach-hang/auth/refresh",
      { refreshToken }
    )
    .then((res) => res as unknown as ApiResponse<{ accessToken: string }>);
}

export function getMe(): Promise<ApiResponse<CustomerProfile>> {
  return axios
    .get<ApiResponse<CustomerProfile>>("/api/khach-hang/auth/me")
    .then((res) => res as unknown as ApiResponse<CustomerProfile>);
}

export interface UpdateProfilePayload {
  tenKhachHang?: string;
  soDienThoai?: string;
  email?: string;
}

export function updateProfile(
  payload: UpdateProfilePayload
): Promise<ApiResponse<CustomerProfile>> {
  return axios
    .put<ApiResponse<CustomerProfile>>("/api/khach-hang/auth/update-profile", payload)
    .then((res) => res as unknown as ApiResponse<CustomerProfile>);
}

export interface OAuthLoginPayload {
  token: string;
  provider: 'google';
}

export function oauthLogin(
  payload: OAuthLoginPayload
): Promise<ApiResponse<LoginResponseData>> {
  return axios
    .post<ApiResponse<LoginResponseData>>('/api/khach-hang/auth/oauth/login', payload)
    .then((res) => res as unknown as ApiResponse<LoginResponseData>);
}
