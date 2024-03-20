import { createApp } from 'vue'
import App from './App.vue'
import globalState from './config/globalState';
import Antd from 'ant-design-vue';
import 'ant-design-vue/dist/reset.css';
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
// import 'ant-design-vue/dist/antd.css';

const app = createApp(App)
app.use(globalState);
app.use(Antd);
app.use(ElementPlus)
app.mount('#app')
