package com.mok.framework.test.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import com.mok.framework.common.R;
import top.jiangmok.operationlog.annotation.OperationLog;
import top.jiangmok.operationlog.enums.BusinessType;
import com.mok.framework.common.utils.LogUtils;
import top.jiangmok.ratelimiter.annotation.PreventDuplicate;
import top.jiangmok.ratelimiter.enums.PreventDuplicateType;
import org.slf4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

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
    @SaIgnore
    @SaCheckPermission("system:user:list")
    @PostMapping("/testSaTokenCheckNoLogin")
    @OperationLog(title = "测试接口 - 无需登录", businessType = BusinessType.OTHER)
    @PreventDuplicate(
            key = "#testEntity.username",
            lockTime = 5,
            message = "请勿重复提交",
            type = PreventDuplicateType.DEFAULT
    )
    public R<String> testSaTokenCheckNoLogin(@RequestBody TestEntity testEntity) {
        return R.ok("测试登录 --- 无需登录 : "+testEntity.username);
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
    public R<String> testDistributedLock(@PathVariable String userId, @PathVariable String type) {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return R.ok();
    }

}
