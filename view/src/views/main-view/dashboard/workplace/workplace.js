import { createApp, h } from 'vue'

export default {
  name: 'Workplace',
  render() {
    return h('div', { class: 'dashboard' }, [
      h('h1', 'Workplace Dashboard'),
      h('p', 'Welcome to your dashboard')
    ])
  }
}
