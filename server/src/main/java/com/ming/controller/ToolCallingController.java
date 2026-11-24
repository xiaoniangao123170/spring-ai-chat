package com.ming.controller;

import com.ming.tools.WeatherTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 工具调用：把 WeatherTool 注册到 ChatClient，由模型自主决定调用
 *
 * @author Ming
 */
@RequestMapping("/api/v1/toolCalling")
@RestController
@CrossOrigin
public class ToolCallingController {

    private final ChatClient chatClient;

    public ToolCallingController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultTools(new WeatherTool())
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
