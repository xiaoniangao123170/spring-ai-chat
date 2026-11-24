package com.ming.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 检索增强：QuestionAnswerAdvisor 每次提问自动检索知识库
 *
 * @author Ming
 */
@RequestMapping("/api/v1/rag")
@RestController
@CrossOrigin
public class RagController {

    private final ChatClient chatClient;

    public RagController(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore))
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
