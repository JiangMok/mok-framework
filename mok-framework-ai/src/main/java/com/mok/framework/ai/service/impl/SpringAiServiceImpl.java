package com.mok.framework.ai.service.impl;

import com.mok.framework.ai.service.SpringAiService;
import com.mok.framework.model.entity.SpringAiEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 *
 * @author: mok
 * @date: 2026/6/17
 */
@Service
public class SpringAiServiceImpl implements SpringAiService {

    private static final Logger log = LoggerFactory.getLogger(SpringAiServiceImpl.class);
    private final ChatClient chatClient;
    private final ChatMemoryRepository chatMemoryRepository;
    private final MessageChatMemoryAdvisor mesasgeChatMemoryAdvisor;

    // 注入Builder，构建出ChatClient实例
    public SpringAiServiceImpl(ChatClient.Builder builder,
                               ChatMemoryRepository chatMemoryRepository,
                               ChatMemory chatMemory) {
        this.chatMemoryRepository = chatMemoryRepository;
        this.mesasgeChatMemoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
        this.chatClient = builder.defaultAdvisors(mesasgeChatMemoryAdvisor).build();
    }

    // 核心调用，一行代码
    @Override
    public String syncChat(SpringAiEntity springAiEntity) {
        return chatClient.prompt()
                .user(springAiEntity.getUserInput())
                .advisors(advisor -> advisor.param(
                        ChatMemory.CONVERSATION_ID,springAiEntity.getConversationId()))
                .call()
                .content();
    }

    @Override
    public Flux<String> chatFlux(SpringAiEntity springAiEntity) {
        return chatClient.prompt()
                .user(springAiEntity.getUserInput())
                .advisors(advisor -> advisor.param(
                        ChatMemory.CONVERSATION_ID,springAiEntity.getConversationId()))
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
