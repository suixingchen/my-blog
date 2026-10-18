package com.example.blog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 评论实体（对应数据库的 comment 表）。
 *
 * <p>设计说明（面试常问）：这里用 {@code articleId} 这个普通字段来记录"属于哪篇文章"，
 * 而不是用 {@code @ManyToOne Article} 对象关联。原因有两点：</p>
 * <ol>
 *   <li>模板里只需要显示评论内容和作者，不需要从评论反向导航到整篇文章对象；</li>
 *   <li>避免了懒加载（LazyInitializationException）和 N+1 查询这类坑。</li>
 * </ol>
 * <p>代价是删除文章时要手动删掉它的评论——这件事在 {@code ArticleService#delete} 里做了。</p>
 */
@Entity
@Table(name = "comment")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属文章 id */
    @Column(name = "article_id", nullable = false)
    private Long articleId;

    /** 评论人（就是登录用户名） */
    @Column(nullable = false, length = 50)
    private String author;

    /** 评论内容 */
    @Column(nullable = false, length = 500)
    private String content;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
