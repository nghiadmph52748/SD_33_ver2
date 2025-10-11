import { DEFAULT_LAYOUT } from '../base'
import { AppRouteRecordRaw } from '../types'

const DEMO: AppRouteRecordRaw = {
  path: '/demo',
  name: 'demo',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.demo',
    requiresAuth: true,
    icon: 'icon-apps',
    order: 1000,
  },
  children: [
    {
      path: 'product-list',
      name: 'ProductListDemo',
      component: () => import('@/views/demo/product-list-demo.vue'),
      meta: {
        locale: 'menu.demo.productList',
        requiresAuth: true,
        roles: ['*'],
      },
    },
    {
      path: 'product-table-example',
      name: 'ProductTableExample',
      component: () => import('@/views/demo/product-table-example.vue'),
      meta: {
        locale: 'menu.demo.productTableExample',
        requiresAuth: true,
        roles: ['*'],
      },
    },
    {
      path: 'test-product-table',
      name: 'TestProductTable',
      component: () => import('@/views/demo/test-product-table.vue'),
      meta: {
        locale: 'menu.demo.testProductTable',
        requiresAuth: true,
        roles: ['*'],
      },
    },
    {
      path: 'product-from-invoice',
      name: 'ProductFromInvoiceDemo',
      component: () => import('@/views/demo/product-from-invoice-demo.vue'),
      meta: {
        locale: 'menu.demo.productFromInvoice',
        requiresAuth: true,
        roles: ['*'],
      },
    },
    {
      path: 'debug-product-from-invoice',
      name: 'DebugProductFromInvoice',
      component: () => import('@/views/demo/debug-product-from-invoice.vue'),
      meta: {
        locale: 'menu.demo.debugProductFromInvoice',
        requiresAuth: true,
        roles: ['*'],
      },
    },
  ],
}

export default DEMO
