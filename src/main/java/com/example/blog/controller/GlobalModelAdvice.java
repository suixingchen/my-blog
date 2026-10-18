package com.example.blog.controller;

import com.example.blog.service.ArticleService;
import com.example.blog.service.SiteSettingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 给所有页面注入公共数据（站点信息、侧边栏分类统计、背景壁纸）。
 *
 * <p>{@code @ControllerAdvice} + {@code @ModelAttribute} 的意思：
 * 每个被 Controller 返回的页面都会自动带上这些数据，
 * 所以模板里随处都能用 ${blogTitle}、${bgImage}，不用每个方法都写一遍。</p>
 */
@ControllerAdvice
public class GlobalModelAdvice {

    private final ArticleService articleService;
    private final SiteSettingService siteSettingService;

    @Value("${blog.title}")
    private String blogTitle;

    @Value("${blog.subtitle}")
    private String blogSubtitle;

    @Value("${blog.author}")
    private String blogAuthor;

    public GlobalModelAdvice(ArticleService articleService, SiteSettingService siteSettingService) {
        this.articleService = articleService;
        this.siteSettingService = siteSettingService;
    }

    @ModelAttribute
    public void commonAttributes(Model model) {
        model.addAttribute("blogTitle", blogTitle);
        model.addAttribute("blogSubtitle", blogSubtitle);
        model.addAttribute("blogAuthor", blogAuthor);
        model.addAttribute("categoryCounts", articleService.categoryCounts());
        // 管理员在后台设置的壁纸路径；没设置就是 null，模板里会走默认背景
        model.addAttribute("bgImage", siteSettingService.getBackgroundUrl());
    }
}
