package com.example.blog.service;

import com.example.blog.dto.ArticleForm;
import com.example.blog.entity.Article;
import com.example.blog.repository.ArticleRepository;
import com.example.blog.repository.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Service 层测试：发布状态、阅读量自增、搜索与分页。
 */
@DataJpaTest
class ArticleServiceTest {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private CommentRepository commentRepository;

    private ArticleService articleService;

    @BeforeEach
    void setUp() {
        articleService = new ArticleService(articleRepository, commentRepository);
    }

    @Test
    @DisplayName("未勾选发布时保存为草稿，前台查不到")
    void createDraftWhenNotPublished() {
        ArticleForm form = form("草稿文章", "只有自己能看到");
        form.setPublished(false);

        Article saved = articleService.create(form);

        assertThat(saved.getPublished()).isFalse();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(articleService.searchPublished(null, null, 1, 10).getTotalElements()).isZero();
        assertThat(articleService.searchAll(null, 1, 10).getTotalElements()).isEqualTo(1L);
    }

    @Test
    @DisplayName("勾选发布后前台可见，并按关键字与分类过滤")
    void searchByKeywordAndCategory() {
        ArticleForm backend = publishedForm("Spring Boot 入门", "讲讲自动配置", "后端");
        backend.setTags("spring,自动配置");
        articleService.create(backend);
        ArticleForm frontend = publishedForm("Thymeleaf 模板", "片段复用技巧", "前端");
        frontend.setTags("thymeleaf,模板");
        articleService.create(frontend);

        assertThat(articleService.searchPublished(null, null, 1, 10).getTotalElements()).isEqualTo(2L);
        // 关键字命中标签
        assertThat(articleService.searchPublished("自动配置", null, 1, 10).getTotalElements()).isEqualTo(1L);
        // 关键字命中标题，且不区分大小写
        assertThat(articleService.searchPublished("thymeleaf", null, 1, 10).getTotalElements()).isEqualTo(1L);
        assertThat(articleService.searchPublished(null, "前端", 1, 10).getTotalElements()).isEqualTo(1L);
        assertThat(articleService.searchPublished("不存在的关键字", null, 1, 10).getTotalElements()).isZero();
    }

    @Test
    @DisplayName("详情页访问会累加阅读量")
    void viewIncreasesCounter() {
        Article saved = articleService.create(publishedForm("阅读量", "正文", "随笔"));
        assertThat(saved.getViews()).isZero();

        articleService.viewAndIncrease(saved.getId());
        Article reloaded = articleService.viewAndIncrease(saved.getId());

        assertThat(reloaded.getViews()).isEqualTo(2);
    }

    @Test
    @DisplayName("更新不会改变创建时间与阅读量")
    void updateKeepsCreatedAtAndViews() {
        Article saved = articleService.create(publishedForm("原标题", "原正文", "随笔"));
        articleService.viewAndIncrease(saved.getId());

        ArticleForm form = ArticleForm.from(articleService.getById(saved.getId()));
        form.setTitle("新标题");
        form.setContent("新正文");
        Article updated = articleService.update(saved.getId(), form);

        assertThat(updated.getTitle()).isEqualTo("新标题");
        assertThat(updated.getViews()).isEqualTo(1);
        assertThat(updated.getCreatedAt()).isEqualTo(saved.getCreatedAt());
    }

    @Test
    @DisplayName("查询不存在的文章抛出 ArticleNotFoundException")
    void missingArticleThrows() {
        assertThatThrownBy(() -> articleService.getById(9999L))
                .isInstanceOf(ArticleNotFoundException.class);
    }

    @Test
    @DisplayName("分页参数非法时被收敛到安全范围")
    void pageParamsAreSanitized() {
        for (int i = 0; i < 7; i++) {
            articleService.create(publishedForm("文章 " + i, "正文 " + i, "随笔"));
        }
        Page<Article> page = articleService.searchPublished(null, null, 0, 100);
        assertThat(page.getSize()).isEqualTo(50);
        assertThat(page.getNumber()).isZero();
    }

    private ArticleForm form(String title, String content) {
        ArticleForm form = new ArticleForm();
        form.setTitle(title);
        form.setContent(content);
        form.setAuthor("suixingchen");
        return form;
    }

    private ArticleForm publishedForm(String title, String content, String category) {
        ArticleForm form = form(title, content);
        form.setCategory(category);
        form.setPublished(true);
        return form;
    }
}
