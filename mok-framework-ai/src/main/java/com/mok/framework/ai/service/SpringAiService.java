package com.mok.framework.ai.service;

import com.mok.framework.model.entity.SpringAiEntity;
import org.springframework.ai.chat.messages.Message;
import reactor.core.publisher.Flux;

import java.util.List;

public interface SpringAiService {
    
    /**
     * @description: 直接返回
     * @author: mok
     * @date: 2026/6/17 15:40
     * @param: [userInput]
     * @return: java.lang.String
    **/
    String syncChat(SpringAiEntity springAiEntity);

    /**
     * @description: 流式返回
     * @author: mok
     * @date: 2026/6/18 13:28
     * @param: [userInput]
     * @return: reactor.core.publisher.Flux<java.lang.String>
    **/
    Flux<String> chatFlux(SpringAiEntity springAiEntity);

    /**
     * @description: 返回对话历史
     * @author: mok
     * @date: 2026/6/18 13:32
     * @param: [conversationId]
     * @return: java.util.List<org.springframework.ai.chat.messages.Message>
    **/
    List<Message> getHistoryByConversationId(String conversationId);

    /**
     * @description:  获取所有会话ID
     * @author: mok
     * @date: 2026/6/18 13:35
     * @param: []
     * @return: java.util.List<java.lang.String>
    **/
    List<String> getAllConversationIds();

}
