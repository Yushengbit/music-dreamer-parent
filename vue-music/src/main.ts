// 引入全局样式
import './assets/main.css'
// 引入 Vue 的 createApp，用来创建应用实例
import { createApp } from 'vue'
// 根组件
import App from './App.vue'
// 路由实例
import router from './router'
// 引入 element-plus 的“组件库本体”（注意：组件和样式是分开的两个东西）
import ElementPlus from 'element-plus'
// 引入 element-plus 的全局样式（只引样式还不够，组件仍需 use 注册）
import 'element-plus/dist/index.css'

// 创建应用实例
const app = createApp(App)

// 注册路由
app.use(router)//index.ts

// 关键：全局注册 element-plus 的所有组件（el-card / el-form / el-input / el-button ...）
// 如果少了这一行，模板里的 <el-card> 等会被当作“未知组件”，页面上渲染不出来
app.use(ElementPlus)

// 把应用挂载到 index.html 里 id="app" 的节点上
app.mount('#app')
