package com.mok.framework.ratelimiter.strategy;

import com.mok.framework.common.utils.LogUtils;
import com.mok.framework.ratelimiter.core.RateLimitStrategy;
import com.mok.framework.ratelimiter.enums.RateLimitType;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 限流策略工厂
 * 作用：根据类型获取对应的限流策略实例
 * @author mok
 */
// 注册为 spring bean
@Component
public class RateLimitStrategyFactory implements ApplicationContextAware {
    private static final Logger log = LogUtils.getLogger(RateLimitStrategyFactory.class);

    // 策略映射：key为策略类型字符串，value为策略Bean
    private final Map<String, RateLimitStrategy> strategyMap = new ConcurrentHashMap<>();
    //Spring 应用上下文
    private ApplicationContext applicationContext;

    /**
     * 设置ApplicationContext，在Bean初始化时调用
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        // 初始化策略映射
        initializeStrategies();
    }

    /**
     * 初始化策略映射：从Spring容器中获取所有RateLimitStrategy类型的Bean，按类型放入map
     */
    private void initializeStrategies() {
        Map<String, RateLimitStrategy> strategies = applicationContext.getBeansOfType(RateLimitStrategy.class);
        for (RateLimitStrategy strategy : strategies.values()) {
            strategyMap.put(strategy.getType(), strategy);
        }
    }

    /**
     * 根据限流类型枚举获取策略
     * @param type 限流类型枚举
     * @return 对应的策略实例
     */
    public RateLimitStrategy getStrategy(RateLimitType type) {

        if (type == null) {
            return getDefaultStrategy();
        }

        RateLimitStrategy strategy = strategyMap.get(type.getValue());
        if (strategy == null) {
            throw new IllegalArgumentException("不支持的限流类型: " + type);
        }
        return strategy;
    }

    /**
     * 根据字符串类型获取策略
     * @param type 类型字符串
     * @return 策略实例，如果不存在则返回默认策略
     */
    public RateLimitStrategy getStrategy(String type) {
        RateLimitStrategy strategy = strategyMap.get(type);
        if (strategy == null) {
            return getDefaultStrategy();
        }
        return strategy;
    }

    /**
     * 获取默认策略（滑动窗口）
     */
    private RateLimitStrategy getDefaultStrategy() {
        return strategyMap.get(RateLimitType.SLIDING_WINDOW.getValue());
    }
}