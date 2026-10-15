package com.example.blog.service;

/**
 * 文章不存在时抛出。
 */
public class ArticleNotFoundException extends RuntimeException {

    private final Long id;

    public ArticleNotFoundException(Long id) {
        super("文章不存在，id=" + id);
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
