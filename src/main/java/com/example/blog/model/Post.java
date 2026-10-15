package com.example.blog.model;

import java.time.LocalDate;

/**
 * 首页占位用的文章模型。
 * <p>当前阶段只用于渲染静态假数据，等接入数据库后会被真正的实体取代。</p>
 */
public class Post {

    private final Long id;
    private final String title;
    private final String summary;
    private final String category;
    private final LocalDate createdAt;

    public Post(Long id, String title, String summary, String category, LocalDate createdAt) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.category = category;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public String getCategory() {
        return category;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }
}
