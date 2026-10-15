package com.example.blog.repository;

import com.example.blog.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 文章数据访问层。
 */
public interface ArticleRepository extends JpaRepository<Article, Long> {

    /** 前台列表：只查已发布的，按创建时间倒序 */
    List<Article> findByPublishedTrueOrderByCreatedAtDesc();

    /** 后台列表：所有文章，按创建时间倒序 */
    List<Article> findAllByOrderByCreatedAtDesc();

    long countByPublishedTrue();

    long countByPublishedFalse();
}
