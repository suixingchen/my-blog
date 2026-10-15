package com.example.blog.controller;

import com.example.blog.entity.Article;
import com.example.blog.service.ArticleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 后台管理：文章列表、新建、编辑、删除。
 * <p>按需求本阶段不做登录校验，直接访问 /admin 即可管理文章。</p>
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ArticleService articleService;

    public AdminController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("articles", articleService.listAll());
        model.addAttribute("totalCount", articleService.countAll());
        model.addAttribute("publishedCount", articleService.countPublished());
        model.addAttribute("draftCount", articleService.countDraft());
        return "admin/list";
    }

    @GetMapping("/articles/new")
    public String createForm(Model model) {
        model.addAttribute("article", new Article());
        model.addAttribute("mode", "create");
        return "admin/form";
    }

    @PostMapping("/articles")
    public String create(@ModelAttribute("article") Article article, RedirectAttributes redirectAttributes) {
        Article saved = articleService.create(article);
        redirectAttributes.addFlashAttribute("message", "文章《" + saved.getTitle() + "》已保存");
        return "redirect:/admin";
    }

    @GetMapping("/articles/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("article", articleService.getById(id));
        model.addAttribute("mode", "edit");
        return "admin/form";
    }

    @PostMapping("/articles/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute("article") Article article,
                         RedirectAttributes redirectAttributes) {
        Article saved = articleService.update(id, article);
        redirectAttributes.addFlashAttribute("message", "文章《" + saved.getTitle() + "》已更新");
        return "redirect:/admin";
    }

    @PostMapping("/articles/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Article article = articleService.getById(id);
        articleService.delete(id);
        redirectAttributes.addFlashAttribute("message", "文章《" + article.getTitle() + "》已删除");
        return "redirect:/admin";
    }
}
