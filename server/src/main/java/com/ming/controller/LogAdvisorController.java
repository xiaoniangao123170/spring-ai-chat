package com.ming.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 日志通知：SimpleLoggerAdvisor 自动记录请求/响应
 *
 * @author Ming
 */
@RequestMapping("/api/v1/logAdvisor")
@RestController
@CrossOrigin
public class LogAdvisorController {

    private final ChatClient chatClient;

    public LogAdvisorController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                // p1：请求日志打印内容（lambda 方式）
                // p2：响应日志打印内容（lambda 方式）
                // p3：通知顺序，值越小越靠前
                .defaultAdvisors(new SimpleLoggerAdvisor(
                        req -> req.userText(),
                        res -> res.getResult().getOutput().getText(),
                        0
                ))
                .build();
    }

    @GetMapping("base")
    public String base(@RequestParam("msg") String msg) {
        return chatClient
                .prompt()
                .user(msg)
                .call()
                .content();
    }

    @GetMapping(value = "stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestParam("msg") String msg) {
        return chatClient
                .prompt()
                .user(msg)
                .stream()
                .content()
                .concatWith(Flux.just("[over]"));
    }
}
