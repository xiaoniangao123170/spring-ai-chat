package com.ming.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 基础问答：同步 + 流式
 *
 * @author Ming
 */
@RequestMapping("/api/v1/aiChat")
@RestController
@CrossOrigin
public class AiChatController {

    private final ChatClient chatClient;

    public AiChatController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * 同步问答：等模型生成完整回答后一次性返回
     */
    @GetMapping("base")
    public String base(@RequestParam("msg") String msg) {
        return chatClient
                // 初始化新会话并创建一个包含消息上下文的提示对象
                .prompt()
                // 设置客户端传递过来的对话内容
                .user(msg.trim())
                // 同步发送消息，一次性拿到结果
                .call()
                // 获取响应的文本内容
                .content();
    }

    /**
     * 流式问答：以 SSE 形式逐段推送
     */
    @GetMapping(value = "stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestParam("msg") String msg) {
        Flux<String> content = chatClient
                .prompt()
                .user(msg.trim())
                // 流式响应
                .stream()
                .content();
        // 约定结束标记，方便前端关闭 SSE 连接
        return content.concatWith(Flux.just("[over]"));
    }
}
