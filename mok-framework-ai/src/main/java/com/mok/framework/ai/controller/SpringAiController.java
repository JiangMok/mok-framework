package com.mok.framework.ai.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.core.util.IdUtil;
import com.mok.framework.ai.service.SpringAiService;
import com.mok.framework.common.R;
import com.mok.framework.model.entity.SpringAiEntity;
import org.springframework.ai.chat.messages.Message;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * spring ai
 *
 * @author: mok
 * @date: 2026/6/17
 */
@RestController
@RequestMapping("/springAi")
public class SpringAiController {

    private final SpringAiService springAiService;

    public SpringAiController(SpringAiService springAiService) {
        this.springAiService = springAiService;
    }

    /**
     * @description: 使用spring AI进行AI对话交互
     * @author: mok
     * @date: 2026/6/17 15:46
     * @param: [userInput]
     * @return: com.mok.framework.common.R<java.lang.String>
     **/
    @SaIgnore
    @PostMapping("/syncChat")
    public R<String> syncChat(@RequestBody SpringAiEntity springAiEntity) {
        return R.ok(springAiService.syncChat(springAiEntity));
    }

    @SaIgnore
    @PostMapping(value = "/streamChat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestBody SpringAiEntity springAiEntity) {
        return springAiService.chatFlux(springAiEntity);
    }

    @SaIgnore
    @GetMapping("/history/{conversationId}")
    public R<List<Message>> getHistory(@PathVariable String conversationId) {
        return R.ok(springAiService.getHistoryByConversationId(conversationId));
    }

    @SaIgnore
    @GetMapping("/getConversationId")
    public R<SpringAiEntity> getConversationId() {
        SpringAiEntity springAiEntity = new SpringAiEntity();
        springAiEntity.setConversationId("CID_"+IdUtil.fastSimpleUUID());
        return R.ok(springAiEntity);
    }

    @SaIgnore
    @GetMapping("/getAllConversationIds")
    public R<List<String>> getAllConversationIds() {
        return R.ok(springAiService.getAllConversationIds());
    }
}
