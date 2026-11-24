package com.ming.config;

import lombok.SneakyThrows;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 检索增强配置：把 classpath:rag/company.txt 读进 SimpleVectorStore
 *
 * @author Ming
 */
@Configuration
public class RagConfig {

    @Autowired
    private ResourceLoader resourceLoader;

    /**
     * 构建一个VectorStore对象，用于存储文档
     *
     * @param embeddingModel 嵌入模型，用于将文本转换为向量
     * @return VectorStore对象
     */
    @SneakyThrows
    @Bean
    VectorStore vectorStore(EmbeddingModel embeddingModel) {
        // 创建一个SimpleVectorStore对象，用于存储文档
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(embeddingModel).build();
        // 从classpath下加载文件
        Resource resource = resourceLoader.getResource("classpath:rag/company.txt");
        // 读取文件内容
        String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        // 生成一个 RAG 文档
        List<Document> documents = List.of(new Document(content));
        // 将文档添加到SimpleVectorStore中
        simpleVectorStore.add(documents);
        // 返回SimpleVectorStore对象
        return simpleVectorStore;
    }
}
