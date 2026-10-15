package com.example.blog.repository;

import com.example.blog.entity.Article;
import com.example.blog.dto.ArticleForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository 层测试：确认「草稿不出现在前台」「关键字与分类过滤」这两条关键规则。
 */
@DataJpaTest
class ArticleRepositoryTest {

    @Autowired
    private ArticleRepository articleRepository;

    @Test
    @DisplayName("按分类统计只包含已发布文章")
    void countGroupByCategoryIgnoresDraft() {
        articleRepository.save(article("A", "正文 A", "后端", true));
        articleRepository.save(article("B", "正文 B", "后端", true));
        articleRepository.save(article("C", "正文 C", "工具", true));
        articleRepository.save(article("D", "正文 D", "工具", false));

        List<Object[]> rows = articleRepository.countGroupByCategory();
        assertThat(rows).hasSize(2);
        assertThat(rows.get(0)[0]).isEqualTo("后端");
        assertThat(((Number) rows.get(0)[1]).longValue()).isEqualTo(2L);
        assertThat(rows.get(1)[0]).isEqualTo("工具");
        assertThat(((Number) rows.get(1)[1]).longValue()).isEqualTo(1L);
    }

    @Test
    @DisplayName("统计方法区分已发布与草稿")
    void countPublishedAndDraft() {
        articleRepository.save(article("A", "正文 A", "后端", true));
        articleRepository.save(article("B", "正文 B", "后端", false));

        assertThat(articleRepository.countByPublishedTrue()).isEqualTo(1L);
        assertThat(articleRepository.countByPublishedFalse()).isEqualTo(1L);
        assertThat(articleRepository.count()).isEqualTo(2L);
    }

    @Test
    @DisplayName("分页查询按创建时间倒序返回")
    void findLatestFirst() {
        articleRepository.save(article("先写的", "正文 1", "随笔", true));
        articleRepository.save(article("后写的", "正文 2", "随笔", true));

        Page<Article> page = articleRepository.findAll(
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt", "id")));
        assertThat(page.getTotalElements()).isEqualTo(2L);
        assertThat(page.getContent().get(0).getTitle()).isEqualTo("后写的");
    }

    private Article article(String title, String content, String category, boolean published) {
        Article article = new Article();
        article.setTitle(title);
        article.setContent(content);
        article.setCategory(category);
        article.setPublished(published);
        return article;
    }

    /** 保证 ArticleForm 与实体字段名一致，避免重构后测试静默失效 */
    @Test
    @DisplayName("表单 DTO 能完整回填实体字段")
    void articleFormRoundTrip() {
        Article source = article("标题", "正文", "后端", false);
        source.setSummary("摘要");
        source.setTags("a,b");
        source.setAuthor("someone");

        ArticleForm form = ArticleForm.from(source);
        assertThat(form.getTitle()).isEqualTo("标题");
        assertThat(form.getSummary()).isEqualTo("摘要");
        assertThat(form.getTags()).isEqualTo("a,b");
        assertThat(form.getAuthor()).isEqualTo("someone");
        assertThat(form.isPublished()).isFalse();
    }
}
