package com.example.blog.repository;

import com.example.blog.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 评论数据访问层。
 *
 * <p>注意 {@code deleteByArticleId}：Spring Data 会根据方法名自动生成
 * {@code delete from comment where article_id = ?}，不用写 SQL。
 * 这类"按条件删除"的方法必须在事务里调用（见 CommentService）。</p>
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /** 某篇文章的全部评论，最新的在前 */
    List<Comment> findByArticleIdOrderByCreatedAtDesc(Long articleId);

    /** 某篇文章的评论数 */
    long countByArticleId(Long articleId);

    /** 删除某篇文章的所有评论（删文章时级联清理） */
    void deleteByArticleId(Long articleId);
}
