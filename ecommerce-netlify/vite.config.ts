import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import path from "node:path";
import { vitePluginForArco } from "@arco-plugins/vite-vue";

export default defineConfig({
  plugins: [
    vue(),
    vitePluginForArco({})
  ],
  server: {
    port: 5174,
    strictPort: false,
  },
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "src")
    }
  },
  css: {
    preprocessorOptions: {
      less: {
        javascriptEnabled: true,
      },
    },
  },
});
