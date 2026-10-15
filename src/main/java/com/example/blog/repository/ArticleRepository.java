package com.example.blog.repository;

import com.example.blog.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * 文章数据访问层。
 * <p>列表页需要「已发布 + 关键字 + 分类」的任意组合查询，交给 Specification 动态拼条件；
 * 简单的统计查询仍用方法名派生。</p>
 */
public interface ArticleRepository extends JpaRepository<Article, Long>, JpaSpecificationExecutor<Article> {

    long countByPublishedTrue();

    long countByPublishedFalse();

    /** 按分类统计已发布文章数量，用于侧边栏 */
    @Query("""
            select a.category, count(a)
            from Article a
            where a.published = true and a.category is not null and a.category <> ''
            group by a.category
            order by count(a) desc, a.category asc
            """)
    List<Object[]> countGroupByCategory();
}
