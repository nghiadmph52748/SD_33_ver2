import Mock from 'mockjs'
import setupMock, { successResponseWrap } from '@/utils/setup-mock'

const haveReadIds: number[] = []
const getMessageList = () => {
  return [
    {
      id: 1,
      type: 'message',
      title: 'Nguyễn Văn A',
      subTitle: 'tin nhắn riêng',
      avatar: '//p1-arco.byteimg.com/tos-cn-i-uwbnlip3yd/8361eeb82904210b4f55fab888fe8416.png~tplv-uwbnlip3yd-webp.webp',
      content: 'Yêu cầu phê duyệt đã được gửi, vui lòng kiểm tra',
      time: 'Hôm nay 12:30:01',
    },
    {
      id: 2,
      type: 'message',
      title: 'Trần Thị B',
      subTitle: 'phản hồi',
      avatar: '//p1-arco.byteimg.com/tos-cn-i-uwbnlip3yd/3ee5f13fb09879ecb5185e440cef6eb9.png~tplv-uwbnlip3yd-webp.webp',
      content: 'Lỗi ở đây đã được sửa',
      time: 'Hôm nay 12:30:01',
    },
    {
      id: 3,
      type: 'message',
      title: 'Trần Thị B',
      subTitle: 'phản hồi',
      avatar: '//p1-arco.byteimg.com/tos-cn-i-uwbnlip3yd/3ee5f13fb09879ecb5185e440cef6eb9.png~tplv-uwbnlip3yd-webp.webp',
      content: 'Lỗi ở đây đã được sửa',
      time: 'Hôm nay 12:20:01',
    },
    {
      id: 4,
      type: 'notice',
      title: 'Thông báo gia hạn',
      subTitle: '',
      avatar: '',
      content: 'Thời hạn sử dụng sản phẩm của bạn sắp hết, nếu muốn tiếp tục sử dụng vui lòng mua...',
      time: 'Hôm nay 12:20:01',
      messageType: 3,
    },
    {
      id: 5,
      type: 'notice',
      title: 'Mở quy tắc thành công',
      subTitle: '',
      avatar: '',
      content: 'Quy tắc chặn nội dung đã được mở thành công vào 2021-12-01 và có hiệu lực',
      time: 'Hôm nay 12:20:01',
      messageType: 1,
    },
    {
      id: 6,
      type: 'todo',
      title: 'Thay đổi hàng đợi kiểm chất',
      subTitle: '',
      avatar: '',
      content: 'Hàng đợi kiểm tra chất lượng nội dung đã được thay đổi vào 2021-12-01 19:50:23, vui lòng...',
      time: 'Hôm nay 12:20:01',
      messageType: 0,
    },
  ].map((item) => ({
    ...item,
    status: haveReadIds.indexOf(item.id) === -1 ? 0 : 1,
  }))
}

setupMock({
  setup: () => {
    Mock.mock(new RegExp('/api/message/list'), () => {
      return successResponseWrap(getMessageList())
    })

    Mock.mock(new RegExp('/api/message/read'), (params: { body: string }) => {
      const { ids } = JSON.parse(params.body)
      haveReadIds.push(...(ids || []))
      return successResponseWrap(true)
    })
  },
})
