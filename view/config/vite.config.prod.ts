import { mergeConfig } from 'vite'
import configArcoResolverPlugin from './plugin/arcoResolver'
import configCompressPlugin from './plugin/compress'
import configVisualizerPlugin from './plugin/visualizer'
import baseConfig from './vite.config.base'

const prodConfig = mergeConfig(
  baseConfig,
  {
    mode: 'production',
    base: '/',
    // Disable Arco resolver in production to avoid SelectOption import issues
    // Components should be manually imported or globally registered
    plugins: [configCompressPlugin('gzip'), configVisualizerPlugin()],
    build: {
      chunkSizeWarningLimit: 20480,
      reportCompressedSize: false,
      rollupOptions: {
        onwarn: (warning, warn) => {
          // Ignore Arco Design CSS import warnings in production build
          if (warning.code === 'UNRESOLVED_IMPORT' && warning.id?.includes('@arco-design/web-vue')) {
            return
          }
          warn(warning)
        },
        output: {
          chunkFileNames: 'static/js/[name]-[hash].js',
          entryFileNames: 'static/js/[name]-[hash].js',
          assetFileNames: 'static/[ext]/[name]-[hash].[ext]',
        },
      },
      minify: 'esbuild',
      target: 'es2015',
      sourcemap: false,
    },
  }
)

// Ensure base is set to '/' for production
prodConfig.base = '/'

export default prodConfig
