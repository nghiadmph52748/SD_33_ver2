import * as XLSX from 'xlsx'
import { Message } from '@arco-design/web-vue'

/**
 * Interface cho data Excel export
 */
export interface ExcelData {
  [key: string]: any
}

/**
 * Interface cho headers Excel
 */
export interface ExcelHeaders {
  [key: string]: string
}

/**
 * Hàm chính để xuất dữ liệu ra Excel
 * @param data - Mảng dữ liệu cần xuất
 * @param headers - Object mapping field names với tiêu đề cột
 * @param fileName - Tên file Excel (không cần extension)
 */
export const exportToExcel = (data: ExcelData[], headers: ExcelHeaders, fileName: string): void => {
  try {
    if (!data || data.length === 0) {
      Message.warning('Không có dữ liệu để xuất')
      return
    }

    // Chuyển đổi dữ liệu theo headers
    const exportData = data.map((item) => {
      const row: { [key: string]: any } = {}
      Object.keys(headers).forEach((key) => {
        let value = item[key]

        // Xử lý giá trị đặc biệt
        if (key === 'trangThai') {
          value = value === 1 || value === true ? 'Hoạt động' : 'Không hoạt động'
        } else if (value === null || value === undefined) {
          value = ''
        } else if (typeof value === 'number' && key.includes('gia')) {
          // Format giá tiền
          value = new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND',
          }).format(value)
        } else if (key.includes('At') && value) {
          // Format ngày tháng
          value = new Date(value).toLocaleString('vi-VN')
        }

        row[headers[key]] = value
      })
      return row
    })

    // Tạo workbook và worksheet
    const wb = XLSX.utils.book_new()
    const ws = XLSX.utils.json_to_sheet(exportData)

    // Tự động điều chỉnh độ rộng cột
    const wscols = Object.values(headers).map(() => ({ wch: 20 }))
    ws['!cols'] = wscols

    // Thêm worksheet vào workbook
    XLSX.utils.book_append_sheet(wb, ws, 'Data')

    // Xuất file
    const timestamp = new Date().toISOString().slice(0, 19).replace(/[:-]/g, '')
    const fullFileName = `${fileName}_${timestamp}.xlsx`

    XLSX.writeFile(wb, fullFileName)

    Message.success(`Xuất Excel thành công: ${fullFileName}`)
  } catch (error) {
    console.error('Lỗi khi xuất Excel:', error)
    Message.error('Có lỗi xảy ra khi xuất Excel')
  }
}

/**
 * Định nghĩa headers cho các loại dữ liệu khác nhau
 */
export const EXPORT_HEADERS = {
  // Biến thể sản phẩm
  BIEN_THE: {
    id: 'ID',
    maChiTietSanPham: 'Mã biến thể',
    tenSanPham: 'Tên sản phẩm',
    tenMauSac: 'Màu sắc',
    tenKichThuoc: 'Kích thước',
    tenDeGiay: 'Đế giày',
    tenChatLieu: 'Chất liệu',
    tenTrongLuong: 'Trọng lượng',
    tenNhaSanXuat: 'Nhà sản xuất',
    tenXuatXu: 'Xuất xứ',
    soLuong: 'Số lượng',
    giaBan: 'Giá bán',
    trangThai: 'Trạng thái',
    ghiChu: 'Ghi chú',
    createAt: 'Ngày tạo',
    updateAt: 'Ngày cập nhật',
    createBy: 'Người tạo',
    updateBy: 'Người cập nhật',
  },

  // Danh mục sản phẩm
  DANH_MUC: {
    id: 'ID',
    maSanPham: 'Mã sản phẩm',
    tenSanPham: 'Tên sản phẩm',
    soLuongBienThe: 'Số lượng biến thể',
    giaNhoNhat: 'Giá nhỏ nhất',
    giaLonNhat: 'Giá lớn nhất',
    trangThai: 'Trạng thái',
    tenNhaSanXuat: 'Nhà sản xuất',
    tenXuatXu: 'Xuất xứ',
    createAt: 'Ngày tạo',
    updateAt: 'Ngày cập nhật',
    createBy: 'Người tạo',
    updateBy: 'Người cập nhật',
  },

  // Màu sắc
  MAU_SAC: {
    id: 'ID',
    maMauSac: 'Mã màu sắc',
    tenMauSac: 'Tên màu sắc',
    maMau: 'Mã màu',
    trangThai: 'Trạng thái',
    createAt: 'Ngày tạo',
    updateAt: 'Ngày cập nhật',
    createBy: 'Người tạo',
    updateBy: 'Người cập nhật',
  },

  // Chất liệu
  CHAT_LIEU: {
    id: 'ID',
    maChatLieu: 'Mã chất liệu',
    tenChatLieu: 'Tên chất liệu',
    trangThai: 'Trạng thái',
    createAt: 'Ngày tạo',
    updateAt: 'Ngày cập nhật',
    createBy: 'Người tạo',
    updateBy: 'Người cập nhật',
  },

  // Đế giày
  DE_GIAY: {
    id: 'ID',
    maDeGiay: 'Mã đế giày',
    tenDeGiay: 'Tên đế giày',
    trangThai: 'Trạng thái',
    createAt: 'Ngày tạo',
    updateAt: 'Ngày cập nhật',
    createBy: 'Người tạo',
    updateBy: 'Người cập nhật',
  },

  // Kích thước
  KICH_THUOC: {
    id: 'ID',
    maKichThuoc: 'Mã kích thước',
    tenKichThuoc: 'Tên kích thước',
    trangThai: 'Trạng thái',
    createAt: 'Ngày tạo',
    updateAt: 'Ngày cập nhật',
    createBy: 'Người tạo',
    updateBy: 'Người cập nhật',
  },

  // Nhà sản xuất
  NHA_SAN_XUAT: {
    id: 'ID',
    maNhaSanXuat: 'Mã nhà sản xuất',
    tenNhaSanXuat: 'Tên nhà sản xuất',
    trangThai: 'Trạng thái',
    createAt: 'Ngày tạo',
    updateAt: 'Ngày cập nhật',
    createBy: 'Người tạo',
    updateBy: 'Người cập nhật',
  },

  // Trọng lượng
  TRONG_LUONG: {
    id: 'ID',
    maTrongLuong: 'Mã trọng lượng',
    tenTrongLuong: 'Tên trọng lượng',
    trangThai: 'Trạng thái',
    createAt: 'Ngày tạo',
    updateAt: 'Ngày cập nhật',
    createBy: 'Người tạo',
    updateBy: 'Người cập nhật',
  },

  // Xuất xứ
  XUAT_XU: {
    id: 'ID',
    maXuatXu: 'Mã xuất xứ',
    tenXuatXu: 'Tên xuất xứ',
    trangThai: 'Trạng thái',
    createAt: 'Ngày tạo',
    updateAt: 'Ngày cập nhật',
    createBy: 'Người tạo',
    updateBy: 'Người cập nhật',
  },
}
