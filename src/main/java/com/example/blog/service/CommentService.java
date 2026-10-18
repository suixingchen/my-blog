package com.example.blog.service;

import com.example.blog.entity.Comment;
import com.example.blog.repository.CommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 评论业务逻辑。
 *
 * <p>所有写操作都标了 {@code @Transactional}：要么全部成功，要么全部回滚。
 * 读操作标 {@code readOnly = true}，性能更好。</p>
 */
@Service
public class CommentService {

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    /** 发表评论 */
    @Transactional
    public Comment add(Long articleId, String author, String content) {
        Comment comment = new Comment();
        comment.setArticleId(articleId);
        comment.setAuthor(author);
        comment.setContent(content);
        return commentRepository.save(comment);
    }

    /** 某篇文章的评论列表（最新在前） */
    @Transactional(readOnly = true)
    public List<Comment> listByArticle(Long articleId) {
        return commentRepository.findByArticleIdOrderByCreatedAtDesc(articleId);
    }

    @Transactional(readOnly = true)
    public long countByArticle(Long articleId) {
        return commentRepository.countByArticleId(articleId);
    }

    /** 按 id 查评论，找不到返回 null（调用处负责提示） */
    @Transactional(readOnly = true)
    public Comment findByIdOrNull(Long id) {
        return commentRepository.findById(id).orElse(null);
    }

    /** 删除单条评论（管理员使用） */
    @Transactional
    public void delete(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    /** 删除某篇文章的全部评论（删文章时调用） */
    @Transactional
    public void deleteByArticle(Long articleId) {
        commentRepository.deleteByArticleId(articleId);
    }
}
