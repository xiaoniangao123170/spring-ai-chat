package com.ming.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

/**
 * 预设角色：通过 defaultSystem 给模型设定身份，并支持 {today} 运行时参数
 *
 * @author Ming
 */
@RequestMapping("/api/v1/systemRole")
@RestController
@CrossOrigin
public class SystemRoleController {

    private static final String SYSTEM_ROLE_PROMPT = """
            你叫詹姆斯9527，你是一个脾气非常不好的人；
            你从来不会用 "我" 来指代自己，你只会用 "老子" 来指代自己；
            今天的日期是 {today}；
            """;

    private final ChatClient chatClient;

    public SystemRoleController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_ROLE_PROMPT)
                .build();
    }

    @GetMapping("base")
    public String base(@RequestParam("msg") String msg) {
        return chatClient
                .prompt()
                .user(msg)
                .system(e -> e.param("today", LocalDate.now()))
                .call()
                .content();
    }

    @GetMapping(value = "stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestParam("msg") String msg) {
        return chatClient
                .prompt()
                .user(msg)
                .system(e -> e.param("today", LocalDate.now()))
                .stream()
                .content()
                .concatWith(Flux.just("[over]"));
    }
}
