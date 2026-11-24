package com.ming.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 记忆通知：PromptChatMemoryAdvisor + InMemoryChatMemory
 *
 * @author Ming
 */
@RequestMapping("/api/v1/chatMemory")
@RestController
@CrossOrigin
public class ChatMemoryController {

    private final ChatClient chatClient;

    public ChatMemoryController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(new PromptChatMemoryAdvisor(new InMemoryChatMemory()))
                .build();
    }

    @GetMapping("base")
    public String base(@RequestParam("msg") String msg) {
        return chatClient
                .prompt()
                .user(msg)
                // 设置记忆大小：每次携带最近 10 条消息
                .advisors(e -> e.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .content();
    }

    @GetMapping(value = "stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestParam("msg") String msg) {
        return chatClient
                .prompt()
                .user(msg)
                // 设置记忆大小
                .advisors(e -> e.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .stream()
                .content()
                .concatWith(Flux.just("[over]"));
    }
}
