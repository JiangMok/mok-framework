package com.mok.framework.ai.service;

import reactor.core.publisher.Flux;

public interface SpringAiService {
    
    /**
     * @description: 聊天
     * @author: mok
     * @date: 2026/6/17 15:40
     * @param: [userInput]
     * @return: java.lang.String
    **/
    String chat(String userInput);
    Flux<String> chatFlux(String userInput);
}
