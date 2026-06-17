package com.mok.framework.ai.service.impl;

import com.mok.framework.ai.service.SpringAiService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 *
 * @author: mok
 * @date: 2026/6/17
 */
@Service
public class SpringAiServiceImpl implements SpringAiService {

    private final ChatClient chatClient;

    // 注入Builder，构建出ChatClient实例
    public SpringAiServiceImpl(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    // 核心调用，一行代码
    @Override
    public String chat(String userInput) {
        return chatClient.prompt()
                .user(userInput)
                .call()
                .content();
    }

    @Override
    public Flux<String> chatFlux(String userInput) {
        return chatClient.prompt()
                .user(userInput)
                .stream()
                .content();
    }
}
