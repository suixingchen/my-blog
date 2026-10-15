package com.example.blog.controller;

import com.example.blog.service.ArticleService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 给所有页面注入公共数据（站点信息、侧边栏分类统计）。
 */
@ControllerAdvice
public class GlobalModelAdvice {

    private final ArticleService articleService;

    @Value("${blog.title}")
    private String blogTitle;

    @Value("${blog.subtitle}")
    private String blogSubtitle;

    @Value("${blog.author}")
    private String blogAuthor;

    public GlobalModelAdvice(ArticleService articleService) {
        this.articleService = articleService;
    }

    @ModelAttribute
    public void commonAttributes(Model model) {
        model.addAttribute("blogTitle", blogTitle);
        model.addAttribute("blogSubtitle", blogSubtitle);
        model.addAttribute("blogAuthor", blogAuthor);
        model.addAttribute("categoryCounts", articleService.categoryCounts());
    }
}
