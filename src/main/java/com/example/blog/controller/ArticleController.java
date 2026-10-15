package com.example.blog.controller;

import com.example.blog.entity.Article;
import com.example.blog.service.ArticleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 前台文章详情页。
 */
@Controller
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping("/article/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Article article = articleService.viewAndIncrease(id);
        model.addAttribute("article", article);
        model.addAttribute("related", articleService.findRelated(article, 5));
        return "article";
    }
}
