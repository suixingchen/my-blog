package com.example.blog.controller;

import com.example.blog.entity.Article;
import com.example.blog.service.ArticleService;
import com.example.blog.service.CommentService;
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
    private final CommentService commentService;

    public ArticleController(ArticleService articleService, CommentService commentService) {
        this.articleService = articleService;
        this.commentService = commentService;
    }

    @GetMapping("/article/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Article article = articleService.viewAndIncrease(id);
        model.addAttribute("article", article);
        model.addAttribute("related", articleService.findRelated(article, 5));
        // 详情页要显示评论，所以把这篇的评论一起查出来放进 Model
        model.addAttribute("comments", commentService.listByArticle(id));
        return "article";
    }
}
