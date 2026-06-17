package com.mok.framework.ai.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.mok.framework.ai.service.SpringAiService;
import com.mok.framework.common.R;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

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
    @PostMapping("/chatWithSpringAi")
    public R<String> chat(@RequestParam("userInput") String userInput) {
        return R.ok(springAiService.chat(userInput));
    }

    @SaIgnore
    @GetMapping(value = "/chatWithSpringAiFlux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatFlux(@RequestParam("userInput") String userInput) {
        return springAiService.chatFlux(userInput);
    }
}
