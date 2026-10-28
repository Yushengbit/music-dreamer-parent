// 引入 axios：一个基于 Promise 的 HTTP 客户端，用来发 AJAX 请求
// （比浏览器原生 fetch 更简洁，自带拦截器、超时、JSON 自动解析等能力）
import axios from 'axios'

// axios.create 创建一个“实例”，可以给这个实例统一设置默认配置，
// 之后所有用 request 发的请求都会自动带上这些配置。
const request = axios.create({
  // baseURL 是请求地址的公共前缀。
  // 这里写成相对路径 '/api'，而不是 'http://127.0.0.1:8080'，原因很关键：
  //   1) 浏览器访问的是前端开发服务器 http://localhost:5173，
  //      请求 '/api/login' 属于“同源”请求，再由 vite.config.ts 里的 proxy
  //      把 /api 转发到后端网关 8080，并 rewrite 成 /auth/login（命中后端白名单）。
  //   2) 如果写成绝对地址 http://127.0.0.1:8080，浏览器会“跨域”直连后端，
  //      后端网关若没配 CORS，请求会被浏览器直接拦截，登录必失败。
  baseURL: '/api',

  // 超时时间（毫秒）：超过 10 秒还没响应就自动取消请求，避免一直转圈
  timeout: 10000,
})

// 响应拦截器：后端返回的数据会先经过这里，我们可以在这一层“统一拆包装”
request.interceptors.response.use(
  // 第一个函数：HTTP 状态码为 2xx 时进入（请求本身成功）
  (resp) => {
    // resp.data 是后端统一的返回体 R{ code, msg, data }
    // 约定 code === 200 表示业务成功
    if (resp.data.code === 200) {
      // 直接把“业务数据” data 返回给调用方。
      // 这样在页面里拿到的就是 LoginVO（包含 token），不用再写 res.data.data
      return resp.data.data
    }
    // 业务失败（例如用户名或密码错误）：把后端返回的 msg 作为错误信息抛出，
    // 会被页面里的 catch 捕获并弹窗提示
    return Promise.reject(new Error(resp.data.msg || '请求失败'))
  },
  // 第二个函数：HTTP 网络错误 / 5xx 等进入（请求本身失败）
  (error) => {
    return Promise.reject(error)
  },
)

// 默认导出这个配置好的实例，供 src/api/auth.js 等模块 import 使用
export default request
