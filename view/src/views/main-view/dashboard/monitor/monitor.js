import { createApp, h } from 'vue'

export default {
  name: 'Monitor',
  render() {
    return h('div', { class: 'monitor' }, [h('h1', 'System Monitor'), h('p', 'Monitor your system status')])
  },
}
