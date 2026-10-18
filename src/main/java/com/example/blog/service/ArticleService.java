package com.example.blog.service;

import com.example.blog.dto.ArticleForm;
import com.example.blog.entity.Article;
import com.example.blog.repository.ArticleRepository;
import com.example.blog.repository.CommentRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 文章业务逻辑。
 */
@Service
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;

    public ArticleService(ArticleRepository articleRepository, CommentRepository commentRepository) {
        this.articleRepository = articleRepository;
        this.commentRepository = commentRepository;
    }

    /**
     * 前台分页查询：只查已发布文章，可选关键字（标题或正文）与分类过滤。
     */
    @Transactional(readOnly = true)
    public Page<Article> searchPublished(String keyword, String category, int page, int size) {
        return articleRepository.findAll(publishedSpec(keyword, category, true), pageable(page, size));
    }

    /**
     * 后台分页查询：包含草稿，仅支持关键字过滤。
     */
    @Transactional(readOnly = true)
    public Page<Article> searchAll(String keyword, int page, int size) {
        return articleRepository.findAll(publishedSpec(keyword, null, false), pageable(page, size));
    }

    /**
     * 详情页用的相关文章：同分类优先，不足时用最新文章补齐。
     */
    @Transactional(readOnly = true)
    public List<Article> findRelated(Article current, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return articleRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("published")));
            predicates.add(cb.notEqual(root.get("id"), current.getId()));
            if (StringUtils.hasText(current.getCategory())) {
                predicates.add(cb.equal(root.get("category"), current.getCategory()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable).getContent().stream().filter(item -> !item.getId().equals(current.getId())).toList();
    }

    private Pageable pageable(int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 50);
        return PageRequest.of(safePage - 1, safeSize, Sort.by(Sort.Direction.DESC, "createdAt", "id"));
    }

    private Specification<Article> publishedSpec(String keyword, String category, boolean onlyPublished) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (onlyPublished) {
                predicates.add(cb.isTrue(root.get("published")));
            }
            if (StringUtils.hasText(category)) {
                predicates.add(cb.equal(root.get("category"), category.trim()));
            }
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.trim().toLowerCase() + "%";
                // 只对标题与标签做不区分大小写的匹配。
                // 正文映射成 @Lob（CLOB），Hibernate 不允许对 CLOB 调用 lower()，
                // 强行匹配会在运行期抛 "Parameter 1 of function 'lower()' has type 'STRING'"。
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("tags")), like)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
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
    public Article create(ArticleForm form) {
        Article article = new Article();
        applyForm(article, form);
        return articleRepository.save(article);
    }

    /**
     * 更新文章：只覆盖允许编辑的字段，阅读量与创建时间保持不变。
     */
    @Transactional
    public Article update(Long id, ArticleForm form) {
        Article article = getById(id);
        applyForm(article, form);
        return articleRepository.save(article);
    }

    private void applyForm(Article article, ArticleForm form) {
        article.setTitle(form.getTitle() == null ? null : form.getTitle().trim());
        article.setSummary(form.getSummary() == null ? null : form.getSummary().trim());
        article.setContent(form.getContent());
        article.setCategory(blankToNull(form.getCategory()));
        article.setTags(blankToNull(form.getTags()));
        article.setAuthor(form.getAuthor() == null ? null : form.getAuthor().trim());
        article.setPublished(form.isPublished());
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    /** 删除文章（同时删掉它的评论，避免留下"孤儿"评论） */
    @Transactional
    public void delete(Long id) {
        Article article = getById(id);
        commentRepository.deleteByArticleId(id);
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

    /** 分类 -> 已发布文章数，用于侧边栏按热度排列 */
    @Transactional(readOnly = true)
    public Map<String, Long> categoryCounts() {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (Object[] row : articleRepository.countGroupByCategory()) {
            counts.put((String) row[0], ((Number) row[1]).longValue());
        }
        return counts;
    }
}
