import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {path: '/',name: 'home',component: HomeView,},
    {path: '/about',name: 'about',component: () => import('../views/AboutView.vue'),},
    {path: '/login',name: 'login',component:()=>import('../views/Login.vue'),},
    {path:'/register',name:'register',component:()=>import('../views/Register.vue')},
  ]
})
// 全局前置守卫：每次路由跳转前都会执行
// 作用：除了登录页，其他页面都必须带 token 才能访问
router.beforeEach((to, from, next) => {
  // 从 localStorage 读取登录时保存的 token
  const token = localStorage.getItem('token')
  // 注意：这里比较的是路由的 name（字符串 'login'）。
  // 之前写成 '/login' 会永远不相等：没 token 时每次导航都被重定向到一个
  // 不存在的 name，形成无限重定向，页面 RouterView 渲染为空白
  if (to.name !== 'login' && !token) {
    // 没登录 -> 强制跳到登录页（用 name 跳转，路由表里 name 是 'login'）
    next({ name: 'login' })
  } else {
    // 已登录，或访问的就是登录页 -> 放行
    next()
  }
})
export default router
