export type RoleType = '' | '*' | 'admin' | 'user'
export interface UserState {
  id?: number
  maNhanVien?: string
  name?: string
  tenTaiKhoan?: string
  avatar?: string
  job?: string
  organization?: string
  location?: string
  email?: string
  introduction?: string
  personalWebsite?: string
  jobName?: string
  organizationName?: string
  locationName?: string
  phone?: string
  registrationDate?: string
  accountId?: string
  certification?: number
  roles: RoleType[] 
  idQuyenHan?: number
  tenQuyenHan?: string
  accessToken?: string
  refreshToken?: string
  
}
