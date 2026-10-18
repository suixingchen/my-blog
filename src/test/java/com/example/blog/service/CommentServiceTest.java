package com.example.blog.service;

import com.example.blog.entity.Article;
import com.example.blog.entity.Comment;
import com.example.blog.repository.ArticleRepository;
import com.example.blog.repository.CommentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 评论功能测试。
 *
 * <p>用的还是 {@code @DataJpaTest}：只加载 JPA 相关组件、用内嵌 H2，
 * 不启动 Web 容器，所以跑得很快。这些测试能保证"评论存得进、查得出、
 * 按文章隔离、删文章时被一起清理"这四条关键规则不被以后改坏。</p>
 */
@DataJpaTest
class CommentServiceTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ArticleRepository articleRepository;

    private CommentService commentService() {
        return new CommentService(commentRepository);
    }

    @Test
    @DisplayName("发表评论后能按文章查出来，且最新的排在前面")
    void addAndListComments() {
        CommentService service = commentService();

        service.add(100L, "admin", "第一条评论");
        service.add(100L, "admin", "第二条评论");

        List<Comment> comments = service.listByArticle(100L);

        assertThat(comments).hasSize(2);
        assertThat(comments.get(0).getContent()).isEqualTo("第二条评论");   // 最新在前
        assertThat(comments.get(0).getCreatedAt()).isNotNull();            // 创建时间由 @PrePersist 填
        assertThat(service.countByArticle(100L)).isEqualTo(2L);
    }

    @Test
    @DisplayName("评论按文章隔离，不会串到别的文章下")
    void commentsAreIsolatedByArticle() {
        CommentService service = commentService();

        service.add(1L, "admin", "文章1的评论");
        service.add(2L, "admin", "文章2的评论");

        assertThat(service.listByArticle(1L)).hasSize(1);
        assertThat(service.listByArticle(2L)).hasSize(1);
        assertThat(service.listByArticle(1L).get(0).getContent()).isEqualTo("文章1的评论");
    }

    @Test
    @DisplayName("管理员可以删除单条评论")
    void deleteSingleComment() {
        CommentService service = commentService();
        Comment saved = service.add(1L, "admin", "待删除的评论");

        service.delete(saved.getId());

        assertThat(service.listByArticle(1L)).isEmpty();
        assertThat(service.findByIdOrNull(saved.getId())).isNull();
    }

    @Test
    @DisplayName("删除文章时，它的评论会被一起清理（不留孤儿数据）")
    void deletingArticleAlsoDeletesItsComments() {
        Article article = new Article();
        article.setTitle("带评论的文章");
        article.setContent("正文内容");
        article.setPublished(true);
        article.setAuthor("admin");
        Article saved = articleRepository.save(article);

        CommentService commentService = commentService();
        commentService.add(saved.getId(), "admin", "这条评论应该跟着文章一起消失");
        assertThat(commentService.countByArticle(saved.getId())).isEqualTo(1L);

        // ArticleService.delete 内部会调用 commentRepository.deleteByArticleId
        ArticleService articleService = new ArticleService(articleRepository, commentRepository);
        articleService.delete(saved.getId());

        assertThat(commentService.listByArticle(saved.getId())).isEmpty();
        assertThat(articleRepository.count()).isZero();
    }
}
