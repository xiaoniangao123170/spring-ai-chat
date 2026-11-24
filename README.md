# Spring AI 智能客服

一个基于 Spring AI + Vue3 的智能客服示例项目，包含基础问答、流式回答、角色预设、Advisor、对话记忆、工具调用、RAG 检索和文生图。

## 结构

```text
server/   Spring Boot + Spring AI 后端
web/      Vite + Vue3 + Element Plus 前端
```

## 功能

- 基础问答
- SSE 流式回答
- 预设角色
- 日志 Advisor / 自定义 Advisor
- 对话记忆
- 工具调用
- RAG 检索增强
- 文生图

## 技术栈

- 后端：JDK 17+、Spring Boot 3.2.5、Spring AI 1.0.0-M6、spring-ai-alibaba-starter
- 前端：Vue 3、Vite、Element Plus、axios
- 模型：阿里云百炼 DashScope

## 启动

### 1. 设置 API Key

后端通过环境变量读取 DashScope Key：

```bash
DASHSCOPE_API_KEY=your_key
```

Windows PowerShell：

```powershell
$env:DASHSCOPE_API_KEY="your_key"
```

不要把真实 Key 写进 `application.yml`。

### 2. 启动后端

需要 JDK 17+、Maven 3.9+。

```bash
cd server
mvn spring-boot:run
```

后端端口：`5501`。

### 3. 启动前端

需要 Node 18+。

```bash
cd web
npm install
npm run dev
```

前端端口：`6051`，默认自动打开浏览器。

## 接口约定

前端请求地址在 `web/src/App.vue` 的 `BASE_URL`，默认：

```text
http://localhost:5501
```

后端 Controller 已配置跨域，前后端分开启动即可。

## 说明

这是 Spring AI 的学习实践项目，重点是 RAG、Advisor、Tool Calling 和流式输出这条链路。
