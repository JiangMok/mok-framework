package com.mok.framework.test.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import com.mok.framework.common.R;
import com.mok.framework.common.annotation.OperationLog;
import com.mok.framework.common.annotation.PreventDuplicate;
import com.mok.framework.common.enums.BusinessType;
import com.mok.framework.common.enums.PreventDuplicateType;
import com.mok.framework.common.utils.LogUtils;
import org.slf4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    private final static Logger log = LogUtils.getLogger(TestController.class);
    private final RedisTemplate<String, Object> redisTemplate;

    public TestController(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 验证必须登录
     *
     * @return
     */
    @SaCheckLogin
    @GetMapping("/testSaTokenCheckLogin")
    @OperationLog(title = "测试接口 - 必须登录", businessType = BusinessType.OTHER)
    public R<String> testSaTokenCheckLogin() {
        log.info(">>>> 当前 token : {}", StpUtil.getTokenInfo());
        return R.ok("测试登录 --- 必须登录");
    }

    /**
     * 验证无需登录
     *
     * @return
     */
    @SaCheckPermission("system:user:list")
    @GetMapping("/testSaTokenCheckNoLogin")
    @OperationLog(title = "测试接口 - 无需登录", businessType = BusinessType.OTHER)
    public R<String> testSaTokenCheckNoLogin() {
        return R.ok("测试登录 --- 无需登录");
    }

    /**
     * 测试分布式锁,防止重复提交
     *
     * @param userId 用户id
     * @param type   业务
     * @return
     */
    @SaIgnore
    @GetMapping("/testDistributedLock/{userId}/{type}")
    @PreventDuplicate(
            key = "#userId",
            type = PreventDuplicateType.DEFAULT,
            lockTime = 5,
            message = "请勿重复提交")
    public R<String> testDistributedLock(@PathVariable String userId, @PathVariable String type) {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return R.ok();
    }

}
