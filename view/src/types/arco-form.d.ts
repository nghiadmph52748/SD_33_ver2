declare module '@arco-design/web-vue/es/form' {
  import type { FormInstance as BaseFormInstance, FormItemInstance, FormItemRule } from '@arco-design/web-vue'

  export type FormInstance = BaseFormInstance
  export type FormRules = Record<string, FormItemRule[]>
  export type FormItem = FormItemInstance
}

