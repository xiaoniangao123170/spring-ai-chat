<template>
  <div class="app">
    <header class="header">
      <div class="brand">
        <div class="logo">AI</div>
        <div>
          <div class="title">智能客服</div>
          <div class="subtitle">Spring AI · 通义千问 / 通义万相</div>
        </div>
      </div>
      <div class="ops">
        <el-select v-model="module" :disabled="isReplying" style="width: 150px">
          <el-option v-for="item in MODULES" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-checkbox v-model="enableStream" :disabled="module === 'image'" border>流式</el-checkbox>
        <el-button :disabled="isReplying" @click="reset">清空</el-button>
      </div>
    </header>

    <div class="tips">
      <span class="tip-label">{{ current.label }}</span>
      <span class="tip-text">{{ current.path }}{{ suffix }}</span>
      <el-button link type="primary" @click="fillDemo">填入示例</el-button>
    </div>

    <div ref="timelineEl" class="timeline">
      <el-timeline>
        <el-timeline-item
            v-for="(e, i) in activities"
            :key="i"
            :timestamp="e.timestamp"
            :color="e.role === 'user' ? '#409eff' : '#67c23a'"
            :style="{ textAlign: e.role === 'user' ? 'right' : 'left' }">
          <template v-if="e.type === 'image'">
            <img v-if="e.src" class="image" :src="e.src" alt="AI 生成的图片" />
            <span class="bubble ai">{{ e.content }}</span>
          </template>
          <span v-else :class="['bubble', e.role]">{{ e.content }}</span>
        </el-timeline-item>
      </el-timeline>
    </div>

    <div class="sender">
      <el-input
          v-model="msg"
          size="large"
          :disabled="isReplying"
          :placeholder="current.demo"
          @keydown.enter="sendMsg" />
      <el-button type="primary" size="large" :loading="isReplying" @click="sendMsg">发送</el-button>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, ref } from 'vue'
import axios from 'axios'

// 后端地址：springai-server 端口 5501
const BASE_URL = 'http://localhost:5501'

// 八个功能模块，一一对应后端的 8 个 Controller
const MODULES = [
  { value: 'aiChat', label: '基础问答', path: '/api/v1/aiChat', demo: '讲个笑话' },
  { value: 'systemRole', label: '预设角色', path: '/api/v1/systemRole', demo: '你是谁啊' },
  { value: 'logAdvisor', label: '日志通知', path: '/api/v1/logAdvisor', demo: '讲个笑话' },
  { value: 'myAdvisor', label: '自定义通知', path: '/api/v1/myAdvisor', demo: '用纯英文讲个笑话' },
  { value: 'chatMemory', label: '对话记忆', path: '/api/v1/chatMemory', demo: '我今年100岁了' },
  { value: 'toolCalling', label: '工具调用', path: '/api/v1/toolCalling', demo: '北京天气如何' },
  { value: 'rag', label: '检索增强', path: '/api/v1/rag', demo: '公司成员架构是怎么样的' },
  { value: 'image', label: '文生图', path: '/api/v1/image', demo: '一只戴墨镜的橘猫，赛博朋克风格，霓虹灯背景' }
]

// 文生图比较慢，超时时间给到 300 秒
const AJAX = axios.create({ timeout: 300000 })

// 当前功能模块
const module = ref('aiChat')
// 是否开启流式问答
const enableStream = ref(true)
// 用户输入的内容
const msg = ref('')
// AI 是否正在回复中
const isReplying = ref(false)
// 对话记录（时间线）
const activities = ref([{ content: '我是AI，有何指教？', timestamp: now(), role: 'ai' }])
const timelineEl = ref(null)

// SSE 客户端对象
let sse = null

const current = computed(() => MODULES.find((m) => m.value === module.value) || MODULES[0])

const suffix = computed(() => {
  if (module.value === 'image') return '/base（返回 JPEG 图片）'
  return enableStream.value ? '/stream（SSE 流式）' : '/base（同步）'
})

// 获取当前时间
function now() {
  const d = new Date()
  return d.toLocaleDateString() + ' ' + d.toLocaleTimeString()
}

// 始终滚动到底部
function scrollToBottom() {
  nextTick(() => {
    const el = timelineEl.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

function fillDemo() {
  msg.value = current.value.demo
}

function closeSse() {
  if (sse) {
    sse.close()
    sse = null
  }
}

function reset() {
  closeSse()
  activities.value = [{ content: '我是AI，有何指教？', timestamp: now(), role: 'ai' }]
}

// 当用户发送消息时触发
function sendMsg() {
  const text = msg.value.trim()
  // 输入为空、或 AI 正在回复时不处理
  if (!text || isReplying.value) return

  isReplying.value = true
  activities.value.push({ content: text, timestamp: now(), role: 'user' })
  activities.value.push({ content: 'waiting...', timestamp: now(), role: 'ai' })
  const index = activities.value.length - 1
  msg.value = ''
  scrollToBottom()

  const path = current.value.path
  const isImage = module.value === 'image'
  const task = isImage
      ? imageQA(path, text, index)
      : enableStream.value
          ? streamQA(path + '/stream', text, index)
          : baseQA(path + '/base', text, index)

  Promise.resolve(task)
      .catch((err) => {
        activities.value[index].content = '请求失败：' + describe(err)
        scrollToBottom()
      })
      .finally(() => {
        isReplying.value = false
      })
}

/* ========== 基础对话（同步） ========== */
async function baseQA(url, text, index) {
  const res = await AJAX.get(url, { params: { msg: text } })
  activities.value[index].content = res.status === 200 ? res.data : '请求失败，请稍后重试！'
  scrollToBottom()
}

/* ========== 流式对话（SSE） ========== */
function streamQA(url, text, index) {
  return new Promise((resolve) => {
    closeSse()
    let buffer = ''
    activities.value[index].content = ''
    sse = new EventSource(`${url}?msg=${encodeURIComponent(text)}`)

    // 收到服务端推送的消息
    sse.onmessage = (ev) => {
      // 读到 [over] 结束标记就关闭连接
      if (ev.data === '[over]') {
        closeSse()
        resolve()
        return
      }
      buffer += ev.data
      activities.value[index].content = buffer
      scrollToBottom()
    }

    // 连接异常：关闭后不再自动重连
    sse.onerror = () => {
      closeSse()
      if (buffer === '') {
        activities.value[index].content = '请求失败：SSE 连接异常，请确认后端已启动（http://localhost:5501）'
      }
      resolve()
    }
  })
}

/* ========== 文生图 ========== */
async function imageQA(url, text, index) {
  const res = await AJAX.get(url, { params: { msg: text }, responseType: 'blob' })
  const contentType = res.headers['content-type'] || ''
  // 后端异常时默认返回 JSON，这里转成可读的错误信息
  if (contentType.includes('application/json')) {
    const raw = await res.data.text()
    throw new Error(raw || '后端返回了错误信息')
  }
  activities.value[index].type = 'image'
  activities.value[index].content = '图片已生成！'
  activities.value[index].src = URL.createObjectURL(res.data)
  scrollToBottom()
}

function describe(err) {
  if (err && err.response) {
    return `HTTP ${err.response.status}，请检查后端控制台日志和 DASHSCOPE_API_KEY`
  }
  return (err && err.message) || '未知错误'
}

onBeforeUnmount(closeSse)
</script>
