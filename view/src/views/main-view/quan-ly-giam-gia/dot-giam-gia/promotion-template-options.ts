import dayjs from 'dayjs'

export type PromotionTemplateAccent = 'blue' | 'green' | 'orange' | 'purple' | 'red'
export type PromotionTemplateIcon = 'gift' | 'fire' | 'lightning' | 'star' | 'tag' | 'trophy'

export interface PromotionTemplateOption {
  id: string
  title: string
  description: string
  accent: PromotionTemplateAccent
  icon: PromotionTemplateIcon
  highlights: string[]
  config: {
    discountValue: number
    durationDays?: number
    description?: string
  }
}

export interface PromotionTemplatePayload {
  discountValue: number
  dateRange: [string, string]
}

export const promotionTemplateOptions: PromotionTemplateOption[] = [
  {
    id: 'new_arrival_sale',
    title: 'Sale hàng mới về',
    description: 'Giảm 15% cho toàn bộ sản phẩm mới nhập trong tháng.',
    accent: 'blue',
    icon: 'star',
    highlights: ['Hàng mới', 'Nổi bật'],
    config: {
      discountValue: 15,
      durationDays: 30,
      description: 'Ưu đãi cho sản phẩm mới về trong tháng, thu hút khách hàng.',
    },
  },
  {
    id: 'seasonal_clearance',
    title: 'Thanh lý theo mùa',
    description: 'Giảm 30% cho hàng tồn kho cuối mùa, cần xả hàng nhanh.',
    accent: 'orange',
    icon: 'fire',
    highlights: ['Thanh lý', 'Hot deal'],
    config: {
      discountValue: 30,
      durationDays: 14,
      description: 'Giảm giá mạnh cho hàng tồn cuối mùa, thu hồi vốn nhanh.',
    },
  },
  {
    id: 'flash_deal',
    title: 'Flash Deal 24h',
    description: 'Giảm sốc 40% trong 24 giờ, tạo hiệu ứng khan hiếm.',
    accent: 'red',
    icon: 'lightning',
    highlights: ['Khẩn cấp', '24h'],
    config: {
      discountValue: 40,
      durationDays: 1,
      description: 'Sale sốc 24 giờ, áp dụng cho sản phẩm được chọn.',
    },
  },
  {
    id: 'weekend_special',
    title: 'Cuối tuần vui vẻ',
    description: 'Giảm 20% mỗi cuối tuần, kích thích mua sắm.',
    accent: 'purple',
    icon: 'gift',
    highlights: ['Cuối tuần', 'Định kỳ'],
    config: {
      discountValue: 20,
      durationDays: 3,
      description: 'Khuyến mãi đặc biệt cuối tuần, từ thứ 6 đến chủ nhật.',
    },
  },
  {
    id: 'bestseller_promo',
    title: 'Top bán chạy',
    description: 'Giảm 18% cho các sản phẩm bestseller, tăng doanh số.',
    accent: 'green',
    icon: 'trophy',
    highlights: ['Bán chạy', 'Ưu tiên'],
    config: {
      discountValue: 18,
      durationDays: 21,
      description: 'Giảm giá cho nhóm sản phẩm bán chạy nhất, duy trì động lực mua.',
    },
  },
  {
    id: 'combo_deal',
    title: 'Mua nhiều giảm sâu',
    description: 'Giảm 25% khi mua từ 2 sản phẩm trở lên trong danh mục.',
    accent: 'blue',
    icon: 'tag',
    highlights: ['Combo', 'Số lượng'],
    config: {
      discountValue: 25,
      durationDays: 30,
      description: 'Ưu đãi mua nhiều, khuyến khích khách tăng giỏ hàng.',
    },
  },
]

export const buildTemplatePayload = (template: PromotionTemplateOption): PromotionTemplatePayload => {
  const durationDays = template.config.durationDays ?? 14
  const start = dayjs().startOf('day')
  const end = dayjs().add(durationDays, 'day').endOf('day')

  return {
    discountValue: template.config.discountValue,
    dateRange: [start.format('YYYY-MM-DDTHH:mm:ss'), end.format('YYYY-MM-DDTHH:mm:ss')],
  }
}

