package com.example.blog.controller;

import com.example.blog.model.Post;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;

/**
 * 首页控制器。
 * <p>项目初始化阶段先返回一组写死的文章，用来确认 Thymeleaf 模板与静态资源链路是通的。</p>
 */
@Controller
public class HomeController {

    @Value("${blog.title}")
    private String blogTitle;

    @Value("${blog.subtitle}")
    private String blogSubtitle;

    @Value("${blog.author}")
    private String blogAuthor;

    private static final List<Post> MOCK_POSTS = List.of(
            new Post(1L, "Hello, Spring Boot", "第一篇占位文章，用于验证模板渲染。", "后端", LocalDate.of(2024, 5, 1)),
            new Post(2L, "我的开发环境搭建记录", "记录 JDK、Maven、IDE 的安装与配置过程。", "工具", LocalDate.of(2024, 5, 3)),
            new Post(3L, "关于这个博客的规划", "先跑通列表页，再做详情页和后台管理。", "随笔", LocalDate.of(2024, 5, 6))
    );

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("blogTitle", blogTitle);
        model.addAttribute("blogSubtitle", blogSubtitle);
        model.addAttribute("blogAuthor", blogAuthor);
        model.addAttribute("posts", MOCK_POSTS);
        return "index";
    }
}
