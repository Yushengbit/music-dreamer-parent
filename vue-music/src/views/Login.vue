<template>
  <div class="login_container">
    <!-- el-card：element-plus 的卡片容器，用来包裹登录表单 -->
    <el-card class="login_card" v-if="isLogin">
      <!-- 登录标题 -->
      <span class="login_title">登录</span>

      <!--
        el-form：表单容器
        :model="form"       绑定表单数据对象（用户名/密码都存在 form 里）
        :rules="rules"      校验规则（哪些字段必填）
        ref="longinFormRef" 拿到表单实例，用来手动调用 validate() 触发校验
      -->
      <el-form :model="form" :rules="rules" ref="longinFormRef" label-width="0px">
        <!-- prop="username" 对应 rules 里的 username 规则 -->
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <el-form-item>
          <!-- 点击触发 handleLogin；:loading 让请求期间按钮显示加载动画并禁用 -->
          <el-button type="button" class="submit-btn" @click="handleLogin" :loading="loading"
            >登录</el-button
          >
          <el-button type="button" class="submit-btn" @click="switchRegister" :loading="loading"
            >注册</el-button
          >
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="login_card" v-else>
      <span class="login_title">注册</span>
      <el-form :model="regform" :rules="registrules" ref="registerFormRef" label-width="0px">
        <!-- prop="username" 对应 rules 里的 username 规则 -->
        <el-form-item prop="username">
          <el-input v-model="regform.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="regform.password" type="password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <el-for-item prop="nickname">
          <el-input v-model="regform.nickname" type="nickname" placeholder="输入昵称"></el-input>
        </el-for-item>
        <el-form-item>
          <!-- 点击触发 handleLogin；:loading 让请求期间按钮显示加载动画并禁用 -->
          <el-button type="button" class="submit-btn" @click="handleBackToLogin" :loading="loading"
            >登录</el-button
          >
          <el-button type="button" class="submit-btn" @click="handleRegister" :loading="loading"
            >注册</el-button
          >
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
// reactive：把普通对象变成“响应式”的，form 字段变化会驱动页面更新
import { reactive, ref } from 'vue'
// useRouter：拿到路由对象，用于登录成功后跳转页面
import { useRouter } from 'vue-router'
// ElMessage：element-plus 提供的全局消息提示（成功 / 失败弹窗）
import { ElMessage } from 'element-plus'
// loginApi：封装好的登录请求（见 src/api/auth.js，最终走 request.js）
import { loginApi, registApi } from '../api/auth'
import type { LoginParams, LoginVO, RegisterParams } from '../api/auth'
import type { FormInstance } from 'element-plus'
import { tr } from 'element-plus/es/locales.mjs'
import request from '@/api/request'
const router = useRouter()
// ref：创建响应式引用，访问真正的值要加 .value
const longinFormRef = ref<FormInstance>()
const registerFormRef = ref<FormInstance>()
const loading = ref(false)
const isLogin = ref(true)
const form = reactive<LoginParams>({
  username: '',
  password: '',
})
const regform = reactive<RegisterParams>({
  username: '',
  password: '',
  nickname: '',
})
// 校验规则：两个字段都必填，失去焦点(blur)时校验
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}
const registrules = {
  username: [{ required: true, message: '请输入新账户', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  nickname: [{ required: true, message: '输入昵称', trigger: 'blur' }],
}

const handleLogin = () => {
  // validate 先按 rules 校验表单，回调里 valid=true 才继续提交
  longinFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      loading.value = true
      try {
        // loginApi 内部经 request.js 拦截器，已经把 R{data} 拆出，
        // 所以这里 res 直接就是 LoginVO 对象，res.token 就是 JWT 令牌
        const res = await loginApi(form)
        ElMessage.success('登录成功')
        // 把 token 存到浏览器 localStorage，后续请求和路由守卫会用到它
        localStorage.setItem('token', res.token)
        // 跳转到首页。
        // 注意：路由表里是用 name:'home' 注册（对应 path '/'），
        // 所以用 { name: 'home' } 跳转，而不是写 path '/home'（那个 path 不存在）
        router.push({ name: 'home' })
      } catch (error: any) {
        // error.message 来自 request.js 抛出的后端 msg，例如“用户名或密码错误”
        ElMessage.error(error.message || '登录失败')
      } finally {
        // 无论成功还是失败，都关闭按钮的 loading 状态
        loading.value = false
      }
    }
  })
}
const handleRegister = () => {
  registerFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      loading.value = true
      try {
        await registApi(regform)
        ElMessage.success('注册成功,请登录')
        isLogin.value = true
      } catch (error: any) {
        ElMessage.error(error.message || '注册失败')
      } finally {
        loading.value = false
      }
    }
  })
}
const switchRegister = () => {
  isLogin.value = false
}

const handleBackToLogin = () => {
  isLogin.value = true
}
</script>
<style scoped>
/* 下一次写请注意main.css里面的全局样式 */
.login_container {
  background-image: url('/image/20262.jpg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden; /*防止浏览器的默认滚动条*/
  padding-left: 65%;
}
/* 设置格式要到父元素上   父元素管理高度宽度和背景，其他的东西有子元素设置*/
.login_card {
  width: 450px !important;
  padding: 30px;
  background-color: rgba(255, 255, 255, 0.8);
  border-radius: 12px; /* 设置圆角*/
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}
.login_title {
  display: block;
  font-size: 24px;
  text-align: center;
  margin-bottom: 20px;
}
</style>
