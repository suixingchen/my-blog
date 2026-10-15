package com.example.blog.controller;

import com.example.blog.entity.Article;
import com.example.blog.service.ArticleService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 前台首页：已发布文章的分页列表，支持关键字搜索与分类筛选。
 */
@Controller
public class HomeController {

    private final ArticleService articleService;

    @Value("${blog.page-size:5}")
    private int pageSize;

    public HomeController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping("/")
    public String index(@RequestParam(required = false) String keyword,
                        @RequestParam(required = false) String category,
                        @RequestParam(defaultValue = "1") int page,
                        Model model) {
        Page<Article> articlePage = articleService.searchPublished(keyword, category, page, pageSize);
        model.addAttribute("articlePage", articlePage);
        model.addAttribute("posts", articlePage.getContent());
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentCategory", category);
        return "index";
    }
}
