package com.mok.framework.ai.service.impl;

import com.mok.framework.ai.service.SpringAiService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 *
 * @author: mok
 * @date: 2026/6/17
 */
@Service
public class SpringAiServiceImpl implements SpringAiService {

    private final ChatClient chatClient;
    private final ChatMemoryRepository chatMemoryRepository;

    // 注入Builder，构建出ChatClient实例
    public SpringAiServiceImpl(ChatClient.Builder builder,
                               ChatMemoryRepository chatMemoryRepository) {
        this.chatClient = builder.build();
        this.chatMemoryRepository = chatMemoryRepository;
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

    @Override
    public List<Message> getHistoryByConversationId(String conversationId) {
        return chatMemoryRepository.findByConversationId(conversationId);
    }

    @Override
    public List<String> getAllConversationIds() {
        return chatMemoryRepository.findConversationIds();
    }
}
