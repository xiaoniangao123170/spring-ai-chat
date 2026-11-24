package com.ming.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisorChain;
import reactor.core.publisher.Flux;

/**
 * 自定义通知：同时拦截 call() 与 stream() 两条链路
 *
 * @author Ming
 */
@SuppressWarnings("all")
@Slf4j
public class MyAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    /**
     * 对应 chatClient 的 call() 方法的环绕通知
     *
     * @param advisedRequest 请求对象
     * @param chain          通知链，用于管理这些通知的执行顺序
     * @return 响应对象
     */
    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        log.info("进入 aroundCall() 方法");
        log.info("请求参数：{}", advisedRequest.userText());
        // 继续执行通知链中的下一个通知；如果已经是最后一个通知，则会调用目标方法
        AdvisedResponse advisedResponse = chain.nextAroundCall(advisedRequest);
        log.info("响应结果：{}", advisedResponse
                .response()
                .getResult()
                .getOutput()
                .getText());
        log.info("离开 aroundCall() 方法");
        return advisedResponse;
    }

    /**
     * 对应 chatClient 的 stream() 方法的环绕通知
     *
     * @param advisedRequest 请求对象
     * @param chain          通知链
     * @return Flux<AdvisedResponse>
     */
    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        log.info("进入 aroundStream() 方法");
        log.info("请求参数：{}", advisedRequest.userText());
        // 继续执行通知链中的下一个通知
        Flux<AdvisedResponse> advisedResponseFlux = chain.nextAroundStream(advisedRequest);
        log.info("离开 aroundStream() 方法");
        return advisedResponseFlux.doOnNext(advisedResponse -> log.info("响应结果：{}", advisedResponse
                .response()
                .getResult()
                .getOutput()
                .getText()));
    }

    /**
     * 唯一的 Advisor 名称
     */
    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * 在通知链中 Advisor 的执行顺序，数字越小越靠前
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
