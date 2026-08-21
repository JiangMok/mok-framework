package com.mok.framework.ai.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.core.util.IdUtil;
import com.mok.framework.ai.service.SpringAiService;
import com.mok.framework.common.R;
import com.mok.framework.model.entity.SpringAiEntity;
import top.jiangmok.ratelimiter.annotation.PreventDuplicate;
import top.jiangmok.ratelimiter.annotation.RateLimit;
import top.jiangmok.ratelimiter.enums.RateLimitScope;
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
    @RateLimit(scope = RateLimitScope.USER, limit = 5, message = "AI调用过于频繁，请稍后重试")
    @PreventDuplicate(lockTime = 5, message = "请勿重复提交AI请求")
    @PostMapping("/syncChat")
    public R<String> syncChat(@RequestBody SpringAiEntity springAiEntity) {
        return R.ok(springAiService.syncChat(springAiEntity));
    }

    @RateLimit(scope = RateLimitScope.USER, limit = 5, message = "AI调用过于频繁，请稍后重试")
    @PreventDuplicate(lockTime = 5, message = "请勿重复提交AI请求")
    @PostMapping(value = "/streamChat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestBody SpringAiEntity springAiEntity) {
        return springAiService.chatFlux(springAiEntity);
    }

    @RateLimit(scope = RateLimitScope.USER, limit = 30)
    @GetMapping("/history/{conversationId}")
    public R<List<Message>> getHistory(@PathVariable String conversationId) {
        return R.ok(springAiService.getHistoryByConversationId(conversationId));
    }

    @RateLimit(scope = RateLimitScope.USER, limit = 30)
    @GetMapping("/getConversationId")
    public R<SpringAiEntity> getConversationId() {
        SpringAiEntity springAiEntity = new SpringAiEntity();
        springAiEntity.setConversationId("CID_"+IdUtil.fastSimpleUUID());
        return R.ok(springAiEntity);
    }

    @RateLimit(scope = RateLimitScope.USER, limit = 30)
    @GetMapping("/getAllConversationIds")
    public R<List<String>> getAllConversationIds() {
        return R.ok(springAiService.getAllConversationIds());
    }
}
