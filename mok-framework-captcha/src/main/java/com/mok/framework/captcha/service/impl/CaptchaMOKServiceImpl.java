package com.mok.framework.captcha.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.mok.framework.captcha.config.CaptchaConfig;
import com.mok.framework.captcha.service.CaptchaService;
import com.mok.framework.common.utils.LogUtils;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: JN
 * @date: 2026/1/6
 */

@Service
@ConditionalOnProperty(name = "captchaImpl.generate.type", havingValue = "mok-framework")
public class CaptchaMOKServiceImpl implements CaptchaService {
    private static final Logger log = LogUtils.getLogger(CaptchaMOKServiceImpl.class);
    //验证码字符集常量
    //  作用 : 定义字符串验证码可用的字符集合
    //  注意 : 去掉了容易混淆的字符（0, O, 1, l, I等）
    private static final String CHAR_SET =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
    //数学运算符常量
    //  作用 : 定义数学验证码可用的运算符
    private static final char[] OPERATORS = {'+', '-', '*'};
    //注入 redisTemplate
    //  作用 : 用于 存储 和 获取 验证码,支持分布式部署
    //  泛型 : <String,String>表示 键值对 都是 String 类型
    private final RedisTemplate<String, String> redisTemplate;
    //注入验证码配置类
    //  作用 : 用于获验证码的配置参数
    private final CaptchaConfig captchaConfig;

    public CaptchaMOKServiceImpl(RedisTemplate<String, String> redisTemplate,
                                 CaptchaConfig captchaConfig) {
        this.redisTemplate = redisTemplate;
        this.captchaConfig = captchaConfig;
    }

    /**
     * @description: 生成验证码
     * 作用 : 生成验证码图片和相关信息
     * @author: JN
     * @date: 2026/1/1 15:27
     * @param: []
     * @return: java.util.Map<java.lang.String, java.lang.Object>
     **/
    @Override
    public Map<String, Object> generateCaptcha() {
        //创建返回结果 map
        Map<String, Object> result = new HashMap<>();

        //生成验证码 key
        //  格式 : "captcha:"+32位字符串
        //  这个 key 会返回给前端,用于后续的验证
        String key = "captcha:" + RandomUtil.randomString(32);

        //根据配置类型生成验证码值
        String code;
        if ("math".equals(captchaConfig.getType())) {
            //生成数学计算验证码
            code = generateMathCode();
        } else {
            //生成字符验证码
            code = generateCharCode();
        }

        //存储验证码到redis
        //  第一个参数 : 键 : "captcha:"+32位字符串
        //  第二个参数 : 值 : 验证码(字符验证码是字符,数学验证码是表达式)
        //  第三个参数 : 过期时间 : 从配置中获取
        //  第四个参数 : 时间单位 : 秒
        redisTemplate.opsForValue().set(key, code, captchaConfig.getExpire(), TimeUnit.SECONDS);

        //生成验证码图片
        BufferedImage image = createImage(code);

        //将图片转换为base64字符串
        String base64Image = imageToBase64(image);

        //构建返回结果
        //验证码 key
        result.put("key", key);
        //图片base64，包含MIME类型
        result.put("image", "data:image/png;base64," + base64Image);
        //过期时间
        result.put("expire", captchaConfig.getExpire());

        //记录调试日志
        //  注意：生产环境不应该记录验证码值，这里用debug级别
        log.debug("生成验证码，key: {}, code: {}", key, code);

        return result;
    }

    /**
     * @description: 验证验证码
     * @author: JN
     * @date: 2026/1/1 15:52
     * @param: [key, code]
     * @return: boolean
     **/
    @Override
    public boolean validateCaptcha(String key, String code) {
        //检查参数是否为空
        if (key == null || code == null) {
            return false;
        }

        //从 redis 获取存储的验证码
        String storedCode = redisTemplate.opsForValue().get(key);

        //检查验证码是否存在
        if (storedCode == null) {
            //验证码不存在或者已过期
            return false;
        }

        //验证后删除验证码
        //  防止重复使用,提高安全性
        redisTemplate.delete(key);

        //根据验证码类型进行验证
        if ("math".equals(captchaConfig.getType())) {
            try {
                //将用户输入转换为整数
                int inputResult = Integer.parseInt(code.trim());
                //计算表达式的结果,并与用户输入计较
                return calculateMathResult(storedCode) == inputResult;
            } catch (NumberFormatException e) {
                //输入的不是数字
                return false;
            }
        }

        //字符验证码验证
        //  不区分大小写,因为图片中的字母可能大小写混合
        return storedCode.equalsIgnoreCase(code);
    }

    /**
     * @description: 生成字符串验证码
     * @author: JN
     * @date: 2026/1/1 16:02
     * @param: []
     * @return: java.lang.String
     **/
    private String generateCharCode() {
        //使用 Hutool 的 RandomUtil 从字符集中随机选择字符
        return RandomUtil.randomString(CHAR_SET, captchaConfig.getLength());
    }

    /**
     * @description: 生成数学计算验证码
     * @author: JN
     * @date: 2026/1/1 16:04
     * @param: []
     * @return: java.lang.String
     **/
    private String generateMathCode() {
        //生成两个随机数(1~9)
        int num1 = RandomUtil.randomInt(1, 10);
        int num2 = RandomUtil.randomInt(1, 10);

        //随机选择一个运算符
        char operator = OPERATORS[RandomUtil.randomInt(0, OPERATORS.length)];

        //构建数学表达式

        //存储的是表达式,不是计算结果
        return num1 + " " + operator + " " + num2 + " = ?";

    }

    /**
     * @description: 计算数学表达式结果
     * @author: JN
     * @date: 2026/1/1 16:09
     * @param: [expression]
     * @return: int
     **/
    private int calculateMathResult(String expression) {
        //分割表达式字符串
        //  格式 : "3 + 5 =?",分割为["3"、"+"、"5"、"= ?"]
        String[] parts = expression.split(" ");
        if (parts.length != 5) {
            //表达式格式错误
            return 0;
        }

        //解析数字和运算符
        int num1 = Integer.parseInt(parts[0]);
        int num2 = Integer.parseInt(parts[2]);
        char operator = parts[1].charAt(0);

        return switch (operator) {
            case '+' -> num1 + num2;
            case '-' -> num1 - num2;
            case '*' -> num1 * num2;
            //未知运算符
            default -> 0;
        };
    }

    /**
     * @description: 创建验证码图片
     **/
    private BufferedImage createImage(String code) {
        //创建 BufferedImage 对象
        //  参数 : 宽度、高度、图片类型
        BufferedImage image = new BufferedImage(
                captchaConfig.getWidth(),
                captchaConfig.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );

        //获取 Graphics2D 对象,用于绘制
        Graphics2D g = image.createGraphics();
        //设置背景位白色
        g.setColor(Color.WHITE);
        //第一个参数 : 矩形起始点的 x 轴坐标。
        //第二个参数 : 矩形起始点的 y 轴坐标。
        //第三个参数 : 矩形的宽度。正值向右延伸，负值向左延伸。
        //第四个参数 : 矩形的高度。正值向下延伸，负值向上延伸。
        g.fillRect(0, 0, captchaConfig.getWidth(), captchaConfig.getHeight());

        //设置字体
        Font font = new Font("Arial", Font.BOLD, 24);
        g.setFont(font);

        //绘制验证码字符
        for (int i = 0; i < code.length(); i++) {
            //为每个字符设置随机颜色
            g.setColor(new Color(
                    RandomUtil.randomInt(0, 200),
                    RandomUtil.randomInt(0, 200),
                    RandomUtil.randomInt(0, 200)
            ));
            //绘制字符
            //  位置 : 20 + i * 25 >>> 为水平位置,
            //         30 + 随机偏移 >>> 为垂直位置
            g.drawString(String.valueOf(code.charAt(i)),
                    10 + i * 11,
                    30 + RandomUtil.randomInt(-5, 5)
            );

        }
        // 56. 绘制干扰线
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < 5; i++) {
            int x1 = RandomUtil.randomInt(0, captchaConfig.getWidth());
            int y1 = RandomUtil.randomInt(0, captchaConfig.getHeight());
            int x2 = RandomUtil.randomInt(0, captchaConfig.getWidth());
            int y2 = RandomUtil.randomInt(0, captchaConfig.getHeight());
            g.drawLine(x1, y1, x2, y2);
        }

        // 57. 绘制干扰点
        for (int i = 0; i < 50; i++) {
            int x = RandomUtil.randomInt(0, captchaConfig.getWidth());
            int y = RandomUtil.randomInt(0, captchaConfig.getHeight());
            g.setColor(new Color(
                    RandomUtil.randomInt(0, 255),
                    RandomUtil.randomInt(0, 255),
                    RandomUtil.randomInt(0, 255)));
            // 绘制一个点
            g.drawLine(x, y, x, y);
        }
        //释放图形资源
        g.dispose();
        return image;
    }

    /**
     * @description: 图片转Base64
     **/
    private String imageToBase64(BufferedImage image) {
        //使用 try - with - resources 确保流关闭
        try (ByteArrayOutputStream byteArrayOutputStream
                     = new ByteArrayOutputStream()) {
            //将图片写入字节流，格式为PNG
            ImageIO.write(image, "png", byteArrayOutputStream);
            //将字节数组转换为Base64字符串
            return Base64.getEncoder()
                    .encodeToString(byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            //记录错误日志
            log.error("验证码图片转换失败", e);
            return "";
        }
    }
}
