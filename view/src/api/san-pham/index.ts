// Export tất cả API quản lý sản phẩm - Fixed exports
// Chỉ export các hàm API để tránh xung đột interface trùng tên

// Thuộc tính sản phẩm
export {
  getNhaSanXuatList,
  getNhaSanXuatListAll,
  getNhaSanXuatById,
  createNhaSanXuat,
  updateNhaSanXuat,
  deleteNhaSanXuat,
} from './thuoc-tinh/nha-san-xuat'

export { getChatLieuList, getChatLieuById, createChatLieu, updateChatLieu, deleteChatLieu } from './thuoc-tinh/chat-lieu'

export { getMauSacList, getMauSacById, createMauSac, updateMauSac, deleteMauSac } from './thuoc-tinh/mau-sac'

export { getKichThuocList, getKichThuocById, createKichThuoc, updateKichThuoc, deleteKichThuoc } from './thuoc-tinh/kich-thuoc'

export { getXuatXuList, getXuatXuListAll, getXuatXuById, createXuatXu, updateXuatXu, deleteXuatXu } from './thuoc-tinh/xuat-xu'

export { getTrongLuongList, getTrongLuongById, createTrongLuong, updateTrongLuong, deleteTrongLuong } from './thuoc-tinh/trong-luong'

export { getDeGiayList, getDeGiayById, createDeGiay, updateDeGiay, deleteDeGiay } from './thuoc-tinh/de-giay'

// Danh mục sản phẩm
export {
  getDanhMucSanPhamList,
  getDanhMucSanPhamById,
  createDanhMucSanPham,
  updateDanhMucSanPham,
  deleteDanhMucSanPham,
  getNhaSanXuatOptions,
  getXuatXuOptions,
} from './danh-muc'

// Biến thể sản phẩm
export {
  getBienTheSanPhamList,
  getBienTheSanPhamById,
  createBienTheSanPham,
  updateBienTheSanPham,
  deleteBienTheSanPham,
  uploadAnhBienThe,
  deleteAnhBienThe,
  getSanPhamOptions,
  getMauSacOptions,
  getKichThuocOptions,
  getChatLieuOptions,
  getDeGiayOptions,
  getTrongLuongOptions,
  getAnhBienThe,
  themAnhChoBienThe,
  xoaAnhKhoiBienThe,
} from './bien-the'

// Ảnh sản phẩm
export {
  getAnhSanPhamList,
  getAnhSanPhamById,
  uploadAnhSanPham,
  uploadNhieuAnhSanPham,
  updateAnhSanPham,
  deleteAnhSanPham,
  getAllAnhSanPham,
} from './thuoc-tinh/anh-san-pham'
