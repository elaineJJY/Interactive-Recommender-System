const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    proxy: {
      '/api': {
        target: process.env.VUE_APP_API_BASE_URL || 'http://localhost:8081',
        changeOrigin: true,
        pathRewrite: { '^/api': '' },
      },
    },
  },
});
