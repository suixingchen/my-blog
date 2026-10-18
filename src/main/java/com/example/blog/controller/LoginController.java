package com.example.blog.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 登录页控制器。
 *
 * <p>只负责"把登录页显示出来"，真正的账号密码校验由 Spring Security 的
 * 过滤器（UsernamePasswordAuthenticationFilter）完成，我们不需要写一行校验代码。</p>
 */
@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
