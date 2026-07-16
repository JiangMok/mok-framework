package com.mok.framework.ratelimiter.expression;

import org.aspectj.lang.JoinPoint;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * SpEL 表达式解析器
 * 作用：解析注解中的SpEL表达式，用于动态生成key
 * @author mok
 */
// 注册为 spring bean
@Component
public class SpelExpressionEvaluator {
    
    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * 解析 SpEL 表达式
     * @param expression 表达式字符串，例如 #user.id
     * @param joinPoint 连接点，可以获取方法参数等信息
     * @return 解析后的字符串结果
     */
    public String evaluate(String expression, JoinPoint joinPoint) {
        try {
            // 创建SpEL 表达式
            StandardEvaluationContext context = new StandardEvaluationContext();
            
            // 设置参数
            Object[] args = joinPoint.getArgs();
            // 获取方法签名,进而获取参数名
            Method method = ((org.aspectj.lang.reflect.MethodSignature) joinPoint.getSignature()).getMethod();
            String[] paramNames = ((org.aspectj.lang.reflect.MethodSignature) joinPoint.getSignature()).getParameterNames();

            // 将参数放入上下文中,变量名为参数名,变量值为参数值
            for (int i = 0; i < args.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }

            // 解析表达式
            Expression exp = parser.parseExpression(expression);
            //在上下文中求值
            Object value = exp.getValue(context);

            // 返回字符串形式
            return value != null ? value.toString() : "";
        } catch (Exception e) {
            // 解析失败时，直接返回原表达式字符串（避免影响业务）
            return expression;
        }
    }
}