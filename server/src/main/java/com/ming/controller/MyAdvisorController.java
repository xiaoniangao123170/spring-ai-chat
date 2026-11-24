package com.ming.controller;

import com.ming.advisor.MyAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 自定义通知：使用自己写的 MyAdvisor
 *
 * @author Ming
 */
@RequestMapping("/api/v1/myAdvisor")
@RestController
@CrossOrigin
public class MyAdvisorController {

    private final ChatClient chatClient;

    public MyAdvisorController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(new MyAdvisor())
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
