package com.example.blog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 文章实体。
 */
@Entity
@Table(name = "article")
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 标题 */
    @Column(nullable = false, length = 150)
    private String title;

    /** 摘要，留空时由正文自动截取 */
    @Column(length = 300)
    private String summary;

    /** 正文（长文本：MySQL 上是 text，H2 上是 CLOB） */
    @Lob
    @Column(nullable = false, length = 65535)
    private String content;

    /** 分类 */
    @Column(length = 50)
    private String category;

    /** 标签，多个用英文逗号分隔 */
    @Column(length = 200)
    private String tags;

    /** 作者 */
    @Column(length = 50)
    private String author;

    /** 阅读量 */
    @Column(nullable = false)
    private Integer views = 0;

    /** 是否已发布，false 表示草稿，前台列表不展示 */
    @Column(nullable = false)
    private Boolean published = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Article() {
    }

    public Article(String title, String summary, String content, String category, String tags, String author) {
        this.title = title;
        this.summary = summary;
        this.content = content;
        this.category = category;
        this.tags = tags;
        this.author = author;
    }

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.views == null) {
            this.views = 0;
        }
        if (this.published == null) {
            this.published = true;
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /** 摘要为空时用正文前 120 个字符兜底 */
    public String getDisplaySummary() {
        if (summary != null && !summary.isBlank()) {
            return summary;
        }
        if (content == null) {
            return "";
        }
        String plain = content.replaceAll("\\s+", " ").trim();
        return plain.length() <= 120 ? plain : plain.substring(0, 120) + "…";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Integer getViews() {
        return views;
    }

    public void setViews(Integer views) {
        this.views = views;
    }

    public Boolean getPublished() {
        return published;
    }

    public void setPublished(Boolean published) {
        this.published = published;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
