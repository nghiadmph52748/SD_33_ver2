import localeMessageBox from '@/components/message-box/locale/en-US'
import localeLogin from '@/views/login/locale/en-US'

import localeCardList from '@/views/list/card/locale/en-US'
import localeSearchTable from '@/views/list/search-table/locale/en-US'

import localeGroupForm from '@/views/form/group/locale/en-US'
import localeStepForm from '@/views/form/step/locale/en-US'

import localeBasicProfile from '@/views/profile/basic/locale/en-US'

import localeDataAnalysis from '@/views/visualization/data-analysis/locale/en-US'
import localeMultiDAnalysis from '@/views/visualization/multi-dimension-data-analysis/locale/en-US'

import localeError from '@/views/result/error/locale/en-US'
import localeSuccess from '@/views/result/success/locale/en-US'

import locale403 from '@/views/exception/403/locale/en-US'
import locale404 from '@/views/exception/404/locale/en-US'
import locale500 from '@/views/exception/500/locale/en-US'

import localeUserInfo from '@/views/user/info/locale/en-US'
import localeUserSetting from '@/views/user/setting/locale/en-US'

// Dashboard components don't use localization
/** simple end */
import localeSettings from './en-US/settings'

export default {
  'menu.thong-ke': 'Statistics',
  'menu.thong-ke.chung': 'General Statistics',
  'menu.thong-ke.kho-hang': 'Inventory Statistics',
  'menu.thong-ke.doanh-thu': 'Revenue Statistics',
  'menu.ban-hang-tai-quay': 'Point of Sale',
  'menu.ban-hang-tai-quay.index': 'Point of Sale',
  'menu.quan-ly-hoa-don': 'Invoice Management',
  'menu.quan-ly-hoa-don.index': 'Invoice Management',
  'menu.quan-ly-san-pham': 'Product Management',
  'menu.quan-ly-san-pham.danh-muc': 'Product Categories',
  'menu.quan-ly-san-pham.them-san-pham': 'Add Product',
  'menu.quan-ly-san-pham.bien-the': 'Product Variants',
  'menu.quan-ly-san-pham.thuoc-tinh': 'Product Attributes',
  'menu.quan-ly-san-pham.thuoc-tinh.anh-san-pham': 'Product Images',
  'menu.quan-ly-san-pham.thuoc-tinh.nha-san-xuat': 'Manufacturers',
  'menu.quan-ly-san-pham.thuoc-tinh.xuat-xu': 'Origins',
  'menu.quan-ly-san-pham.thuoc-tinh.mau-sac': 'Colors',
  'menu.quan-ly-san-pham.thuoc-tinh.kich-thuoc': 'Sizes',
  'menu.quan-ly-san-pham.thuoc-tinh.de-giay': 'Shoe Soles',
  'menu.quan-ly-san-pham.thuoc-tinh.chat-lieu': 'Materials',
  'menu.quan-ly-san-pham.thuoc-tinh.trong-luong': 'Weights',
  'menu.khach-hang-nhan-su': 'Customer & Staff Management',
  'menu.khach-hang-nhan-su.khach-hang': 'Customer Management',
  'menu.khach-hang-nhan-su.nhan-vien': 'Staff Management',
  'menu.quan-ly-giam-gia': 'Discount Management',
  'menu.quan-ly-giam-gia.dot-khuyen-mai': 'Promotion Management',
  'menu.quan-ly-giam-gia.phieu-giam-gia': 'Coupon Management',
  'menu.dashboard': 'Dashboard',
  'menu.server.dashboard': 'Dashboard-Server',
  'menu.server.workplace': 'Workplace-Server',
  'menu.server.monitor': 'Monitor-Server',
  'menu.list': 'List',
  'menu.result': 'Result',
  'menu.exception': 'Exception',
  'menu.form': 'Form',
  'menu.profile': 'Profile',
  'menu.visualization': 'Data Visualization',
  'menu.user': 'User Center',
  'menu.arcoWebsite': 'Arco Design',
  'menu.faq': 'FAQ',
  'menu.faq.index': 'FAQ',
  'menu.faq.main': 'FAQ',
  'navbar.docs': 'Docs',
  'navbar.action.locale': 'Switch to English',
  ...localeSettings,
  ...localeMessageBox,
  ...localeLogin,
  ...localeSearchTable,
  ...localeCardList,
  ...localeStepForm,
  ...localeGroupForm,
  ...localeBasicProfile,
  ...localeDataAnalysis,
  ...localeMultiDAnalysis,
  ...localeSuccess,
  ...localeError,
  ...locale403,
  ...locale404,
  ...locale500,
  ...localeUserInfo,
  ...localeUserSetting,
  /** simple end */
}
