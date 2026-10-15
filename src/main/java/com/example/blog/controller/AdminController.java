package com.example.blog.controller;

import com.example.blog.dto.ArticleForm;
import com.example.blog.entity.Article;
import com.example.blog.service.ArticleNotFoundException;
import com.example.blog.service.ArticleService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 后台管理：文章列表、新建、编辑、删除。
 * <p>按需求本阶段不做登录校验，直接访问 /admin 即可管理文章。</p>
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final ArticleService articleService;

    @Value("${blog.page-size:5}")
    private int pageSize;

    public AdminController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public String dashboard(@RequestParam(required = false) String keyword,
                            @RequestParam(defaultValue = "1") int page,
                            Model model) {
        Page<Article> articlePage = articleService.searchAll(keyword, page, pageSize);
        model.addAttribute("articlePage", articlePage);
        model.addAttribute("articles", articlePage.getContent());
        model.addAttribute("keyword", keyword);
        model.addAttribute("totalCount", articleService.countAll());
        model.addAttribute("publishedCount", articleService.countPublished());
        model.addAttribute("draftCount", articleService.countDraft());
        return "admin/list";
    }

    @GetMapping("/articles/new")
    public String createForm(Model model) {
        model.addAttribute("articleForm", new ArticleForm());
        model.addAttribute("mode", "create");
        return "admin/form";
    }

    @PostMapping("/articles")
    public String create(@Valid @ModelAttribute("articleForm") ArticleForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("mode", "create");
            return "admin/form";
        }
        Article saved = articleService.create(form);
        redirectAttributes.addFlashAttribute("message", "文章《" + saved.getTitle() + "》已保存"
                + (Boolean.TRUE.equals(saved.getPublished()) ? "并发布" : "为草稿"));
        return "redirect:/admin";
    }

    @GetMapping("/articles/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Article article = articleService.getById(id);
        model.addAttribute("articleForm", ArticleForm.from(article));
        model.addAttribute("mode", "edit");
        return "admin/form";
    }

    @PostMapping("/articles/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("articleForm") ArticleForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("mode", "edit");
            form.setId(id);
            return "admin/form";
        }
        Article saved = articleService.update(id, form);
        redirectAttributes.addFlashAttribute("message", "文章《" + saved.getTitle() + "》已更新");
        return "redirect:/admin";
    }

    @PostMapping("/articles/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Article article;
        try {
            article = articleService.getById(id);
        } catch (ArticleNotFoundException ex) {
            log.warn("尝试删除不存在的文章: id={}", id);
            redirectAttributes.addFlashAttribute("errorMessage", "文章不存在或已被删除");
            return "redirect:/admin";
        }
        articleService.delete(id);
        redirectAttributes.addFlashAttribute("message", "文章《" + article.getTitle() + "》已删除");
        return "redirect:/admin";
    }
}
