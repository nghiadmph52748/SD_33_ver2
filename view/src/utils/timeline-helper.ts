import { createTimelineItem, type TimelineItem } from '@/api/timeline'

/**
 * Tạo timeline tự động cho invoice dựa trên payment method
 * @param idHoaDon - ID hóa đơn
 * @param paymentMethod - Phương thức thanh toán: 'cash' | 'transfer' | 'both'
 * @param idNhanVien - ID nhân viên
 * @param tenNhanVien - Tên nhân viên
 */
export async function createInvoiceTimeline(
  idHoaDon: number,
  paymentMethod: 'cash' | 'transfer' | 'both',
  idNhanVien: number,
  tenNhanVien?: string
): Promise<void> {
  const now = new Date().toISOString().slice(0, 19).replace('T', ' ')

  try {
    // Luôn tạo timeline "Tạo đơn hàng" đầu tiên
    await createTimelineItem({
      idHoaDon,
      idNhanVien,
      tenNhanVien,
      trangThaiCu: null,
      trangThaiMoi: 'Tạo đơn hàng',
      hanhDong: 'Tạo mới',
      moTa: 'Đơn hàng được tạo thành công',
      thoiGian: now,
    })

    // Nếu thanh toán bằng tiền mặt tại quầy → hoàn thành ngay
    if (paymentMethod === 'cash') {
      await createTimelineItem({
        idHoaDon,
        idNhanVien,
        tenNhanVien,
        trangThaiCu: 'Tạo đơn hàng',
        trangThaiMoi: 'Hoàn thành',
        hanhDong: 'Hoàn thành',
        moTa: 'Đơn hàng đã được thanh toán tại quầy và hoàn thành',
        ghiChu: 'Thanh toán bằng tiền mặt',
        thoiGian: now,
      })
    }
    // Nếu thanh toán chuyển khoản hoặc both → tạo "Tạo đơn hàng thành công"
    else if (paymentMethod === 'transfer' || paymentMethod === 'both') {
      await createTimelineItem({
        idHoaDon,
        idNhanVien,
        tenNhanVien,
        trangThaiCu: 'Tạo đơn hàng',
        trangThaiMoi: 'Tạo đơn hàng thành công',
        hanhDong: 'Tạo mới',
        moTa: 'Đơn hàng đã được tạo và chờ xác nhận',
        ghiChu: paymentMethod === 'transfer' ? 'Thanh toán bằng chuyển khoản' : 'Thanh toán kết hợp tiền mặt và chuyển khoản',
        thoiGian: now,
      })
    }
  } catch (error) {
    console.error('Lỗi khi tạo timeline cho invoice:', error)
    // Không throw error để không ảnh hưởng đến flow chính
  }
}

/**
 * Tạo timeline khi xác nhận đơn hàng (chỉ cho transfer và both)
 * @param idHoaDon - ID hóa đơn
 * @param paymentMethod - Phương thức thanh toán: 'cash' | 'transfer' | 'both'
 * @param idNhanVien - ID nhân viên
 * @param tenNhanVien - Tên nhân viên
 */
export async function createConfirmOrderTimeline(
  idHoaDon: number,
  paymentMethod: 'cash' | 'transfer' | 'both',
  idNhanVien: number,
  tenNhanVien?: string
): Promise<void> {
  // Nếu thanh toán bằng tiền mặt, không cần tạo thêm timeline (đã hoàn thành khi tạo invoice)
  if (paymentMethod === 'cash') {
    return
  }

  const now = new Date().toISOString().slice(0, 19).replace('T', ' ')

  try {
    // Xác nhận đơn hàng
    await createTimelineItem({
      idHoaDon,
      idNhanVien,
      tenNhanVien,
      trangThaiCu: 'Tạo đơn hàng thành công',
      trangThaiMoi: 'Xác nhận đơn hàng',
      hanhDong: 'Xác nhận',
      moTa: 'Nhân viên đã xác nhận đơn hàng',
      ghiChu: 'Kiểm tra thông tin và xác nhận đơn hàng',
      thoiGian: now,
    })

    // Chuẩn bị hàng
    await createTimelineItem({
      idHoaDon,
      idNhanVien,
      tenNhanVien,
      trangThaiCu: 'Xác nhận đơn hàng',
      trangThaiMoi: 'Đang chuẩn bị hàng',
      hanhDong: 'Chuẩn bị',
      moTa: 'Bắt đầu chuẩn bị sản phẩm cho đơn hàng',
      ghiChu: 'Lấy hàng từ kho và đóng gói',
      thoiGian: now,
    })

    // Đang giao hàng
    await createTimelineItem({
      idHoaDon,
      idNhanVien,
      tenNhanVien,
      trangThaiCu: 'Đang chuẩn bị hàng',
      trangThaiMoi: 'Đang giao hàng',
      hanhDong: 'Giao hàng',
      moTa: 'Đơn hàng đang được giao đến khách hàng',
      ghiChu: 'Giao cho shipper hoặc đang vận chuyển',
      thoiGian: now,
    })

    // Hoàn thành (chờ xác nhận từ khách hàng hoặc nhân viên)
    await createTimelineItem({
      idHoaDon,
      idNhanVien,
      tenNhanVien,
      trangThaiCu: 'Đang giao hàng',
      trangThaiMoi: 'Chờ xác nhận hoàn thành',
      hanhDong: 'Chờ xác nhận',
      moTa: 'Đơn hàng đã được giao, chờ xác nhận hoàn thành',
      ghiChu: 'Chờ khách hàng hoặc nhân viên xác nhận đã nhận hàng',
      thoiGian: now,
    })
  } catch (error) {
    console.error('Lỗi khi tạo timeline xác nhận đơn hàng:', error)
    // Không throw error để không ảnh hưởng đến flow chính
  }
}

/**
 * Tạo timeline khi hoàn thành đơn hàng (cho transfer và both)
 * @param idHoaDon - ID hóa đơn
 * @param idNhanVien - ID nhân viên
 * @param tenNhanVien - Tên nhân viên
 */
export async function createCompleteOrderTimeline(idHoaDon: number, idNhanVien: number, tenNhanVien?: string): Promise<void> {
  const now = new Date().toISOString().slice(0, 19).replace('T', ' ')

  try {
    await createTimelineItem({
      idHoaDon,
      idNhanVien,
      tenNhanVien,
      trangThaiCu: 'Chờ xác nhận hoàn thành',
      trangThaiMoi: 'Hoàn thành',
      hanhDong: 'Hoàn thành',
      moTa: 'Đơn hàng đã được xác nhận hoàn thành',
      ghiChu: 'Khách hàng đã nhận hàng và đơn hàng đã hoàn thành',
      thoiGian: now,
    })
  } catch (error) {
    console.error('Lỗi khi tạo timeline hoàn thành đơn hàng:', error)
    throw error
  }
}
