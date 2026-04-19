package com.mok.framework.auth.utils;

import com.mok.framework.common.utils.LogUtils;
import org.slf4j.Logger;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator  {
       private static final Logger log = LogUtils.getLogger(PasswordGenerator.class);
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        System.out.println("=== 生成BCrypt加密密码 ===");
        
        // 为admin用户生成密码（123456）
        String adminPassword = encoder.encode("123456");
        System.out.println("admin 加密密码: " + adminPassword);
        System.out.println("长度: " + adminPassword.length());
        
        // 验证密码
        boolean adminMatch = encoder.matches("123456", adminPassword);
        System.out.println("验证admin密码: " + adminMatch);
        
        System.out.println();
        
        // 为user1用户生成密码（123456）
        String user1Password = encoder.encode("123456");
        System.out.println("user1 加密密码: " + user1Password);
        System.out.println("长度: " + user1Password.length());
        
        // 验证密码
        boolean user1Match = encoder.matches("123456", user1Password);
        System.out.println("验证user1密码: " + user1Match);
        
        System.out.println("\n=== SQL更新语句 ===");
        System.out.println("-- 更新admin用户密码");
        System.out.println("UPDATE sys_user SET password = '" + adminPassword + "' WHERE username = 'admin';");
        System.out.println();
        System.out.println("-- 更新user1用户密码");
        System.out.println("UPDATE sys_user SET password = '" + user1Password + "' WHERE username = 'user1';");
    }
}