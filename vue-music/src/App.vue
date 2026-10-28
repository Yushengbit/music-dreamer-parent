<script setup lang="ts">
// RouterView 是 vue-router 的“路由出口”：
// 当前 URL 匹配到哪个路由，就把那个路由对应的组件渲染在这里。
// 例如访问 /login 就渲染 views/Login.vue，访问 / 就渲染 views/HomeView.vue
import { RouterView } from 'vue-router'
</script>

<template>
  <div>
      <span></span>
  </div>
  <RouterView />
</template>
