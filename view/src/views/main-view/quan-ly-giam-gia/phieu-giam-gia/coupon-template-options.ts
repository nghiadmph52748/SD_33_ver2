import dayjs from 'dayjs'

export type CouponTemplateAccent = 'blue' | 'green' | 'orange' | 'purple'
export type CouponTemplateIcon = 'gift' | 'user' | 'lightning' | 'calendar' | 'crown'

export interface CouponTemplateOption {
  id: string
  title: string
  description: string
  accent: CouponTemplateAccent
  icon: CouponTemplateIcon
  highlights: string[]
  config: {
    discountMode: 'percentage' | 'amount'
    discountValue: number
    maxDiscount?: number | null
    minOrder?: number | null
    quantity?: number | null
    featured?: boolean
    durationDays?: number
    description?: string
  }
}

export interface CouponTemplatePayload {
  discountMode: 'percentage' | 'amount'
  discountValue: number
  maxDiscount: number | null
  minOrder: number | null
  quantity: number | null
  description: string
  featured: boolean
  dateRange: [string, string]
}

export const couponTemplateOptions: CouponTemplateOption[] = [
  {
    id: 'welcome_percent',
    title: 'Ưu đãi khách mới',
    description: 'Giảm 15% cho đơn đầu tiên, khuyến khích khách hoàn tất mua hàng.',
    accent: 'blue',
    icon: 'user',
    highlights: ['Khách mới', 'Tự động'],
    config: {
      discountMode: 'percentage',
      discountValue: 15,
      maxDiscount: 300000,
      minOrder: 500000,
      quantity: 500,
      durationDays: 30,
      description: 'Ưu đãi chào mừng khách hàng mới đăng ký hoặc hoàn tất đơn đầu tiên.',
    },
  },
  {
    id: 'vip_amount',
    title: 'Tri ân VIP',
    description: 'Tặng 300.000 VND cho khách thân thiết đạt chi tiêu cao.',
    accent: 'purple',
    icon: 'crown',
    highlights: ['VIP', 'RFM'],
    config: {
      discountMode: 'amount',
      discountValue: 300000,
      minOrder: 1500000,
      quantity: 200,
      durationDays: 45,
      description: 'Áp dụng cho nhóm khách hàng trung thành, đơn tối thiểu 1.500.000 VND.',
    },
  },
  {
    id: 'flash_sale',
    title: 'Flash Sale 48h',
    description: 'Giảm 25% tối đa 700.000 VND trong 48 giờ.',
    accent: 'orange',
    icon: 'lightning',
    highlights: ['Khẩn cấp', 'Sự kiện'],
    config: {
      discountMode: 'percentage',
      discountValue: 25,
      maxDiscount: 700000,
      minOrder: 0,
      quantity: 150,
      durationDays: 2,
      description: 'Khuyến mãi tốc độ cao trong 48 giờ, áp dụng toàn bộ khách hàng.',
    },
  },
  {
    id: 'birthday_private',
    title: 'Quà sinh nhật',
    description: 'Gửi riêng voucher 20% tối đa 400.000 VND cho khách sinh nhật.',
    accent: 'green',
    icon: 'calendar',
    highlights: ['Riêng tư', 'Sinh nhật'],
    config: {
      discountMode: 'percentage',
      discountValue: 20,
      maxDiscount: 400000,
      minOrder: 400000,
      quantity: 1,
      featured: true,
      durationDays: 14,
      description: 'Voucher riêng cho khách sinh nhật trong tháng, áp dụng theo danh sách chọn.',
    },
  },
  {
    id: 'clearance_amount',
    title: 'Xả kho cuối mùa',
    description: 'Giảm ngay 500.000 VND cho đơn tối thiểu 2.000.000 VND.',
    accent: 'blue',
    icon: 'gift',
    highlights: ['Hết mùa', 'Tồn kho'],
    config: {
      discountMode: 'amount',
      discountValue: 500000,
      minOrder: 2000000,
      quantity: 300,
      durationDays: 21,
      description: 'Khuyến mãi đẩy hàng tồn cuối mùa, tập trung đơn giá trị cao.',
    },
  },
]

export const buildTemplatePayload = (template: CouponTemplateOption): CouponTemplatePayload => {
  const durationDays = template.config.durationDays ?? 14
  const start = dayjs().startOf('day')
  const end = dayjs().add(durationDays, 'day').endOf('day')

  return {
    discountMode: template.config.discountMode,
    discountValue: template.config.discountValue,
    maxDiscount: template.config.maxDiscount ?? null,
    minOrder: template.config.minOrder ?? null,
    quantity: template.config.quantity ?? null,
    description: template.config.description ?? template.description,
    featured: Boolean(template.config.featured),
    dateRange: [start.format('YYYY-MM-DD HH:mm:ss'), end.format('YYYY-MM-DD HH:mm:ss')],
  }
}
