package com.example.blog.service;

import com.example.blog.entity.Article;
import com.example.blog.repository.ArticleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 文章业务逻辑。
 */
@Service
public class ArticleService {

    private final ArticleRepository articleRepository;

    public ArticleService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    /** 前台文章列表（仅已发布） */
    @Transactional(readOnly = true)
    public List<Article> listPublished() {
        return articleRepository.findByPublishedTrueOrderByCreatedAtDesc();
    }

    /** 后台文章列表（含草稿） */
    @Transactional(readOnly = true)
    public List<Article> listAll() {
        return articleRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * 按 id 查询文章，找不到时抛出 {@link ArticleNotFoundException}。
     */
    @Transactional(readOnly = true)
    public Article getById(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new ArticleNotFoundException(id));
    }

    /**
     * 查询文章并累加阅读量，用于详情页。
     */
    @Transactional
    public Article viewAndIncrease(Long id) {
        Article article = getById(id);
        article.setViews(article.getViews() == null ? 1 : article.getViews() + 1);
        return articleRepository.save(article);
    }

    /** 新增文章 */
    @Transactional
    public Article create(Article form) {
        Article article = new Article();
        applyForm(article, form);
        return articleRepository.save(article);
    }

    /**
     * 更新文章：只覆盖允许编辑的字段，阅读量与创建时间保持不变。
     */
    @Transactional
    public Article update(Long id, Article form) {
        Article article = getById(id);
        applyForm(article, form);
        return articleRepository.save(article);
    }

    /**
     * 把表单字段复制到实体。
     * <p>注意 published 的语义：复选框未勾选时 Spring 不会绑定该字段（保持 null），
     * 这里统一按「未勾选 = 草稿」处理。</p>
     */
    private void applyForm(Article article, Article form) {
        article.setTitle(form.getTitle());
        article.setSummary(form.getSummary());
        article.setContent(form.getContent());
        article.setCategory(form.getCategory());
        article.setTags(form.getTags());
        article.setAuthor(form.getAuthor());
        article.setPublished(Boolean.TRUE.equals(form.getPublished()));
    }

    /** 删除文章 */
    @Transactional
    public void delete(Long id) {
        Article article = getById(id);
        articleRepository.delete(article);
    }

    @Transactional(readOnly = true)
    public long countPublished() {
        return articleRepository.countByPublishedTrue();
    }

    @Transactional(readOnly = true)
    public long countDraft() {
        return articleRepository.countByPublishedFalse();
    }

    @Transactional(readOnly = true)
    public long countAll() {
        return articleRepository.count();
    }

    /** 汇总所有分类，用于侧边栏 */
    @Transactional(readOnly = true)
    public List<String> listCategories() {
        Set<String> categories = new LinkedHashSet<>();
        for (Article article : articleRepository.findByPublishedTrueOrderByCreatedAtDesc()) {
            if (article.getCategory() != null && !article.getCategory().isBlank()) {
                categories.add(article.getCategory().trim());
            }
        }
        return List.copyOf(categories);
    }
}
