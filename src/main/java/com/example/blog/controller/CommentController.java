package com.example.blog.controller;

import com.example.blog.entity.Comment;
import com.example.blog.service.CommentService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 评论控制器。
 *
 * <p>只做两件事：发表评论、删除评论。两个方法都是 POST，
 * 因为要"改变数据"的请求不应该用 GET（GET 应该是只读的）。</p>
 */
@Controller
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * 发表评论。
     *
     * <p>谁能访问到这里？SecurityConfig 里写了 {@code anyRequest().authenticated()}，
     * 而 /article/** 是放行"阅读"的——但 POST 到 /article/{id}/comments 这条路
     * 不在放行清单里，所以必须先登录。{@link Authentication} 参数由 Spring Security
     * 自动注入，里面就有当前登录用户名。</p>
     */
    @PostMapping("/article/{articleId}/comments")
    public String addComment(@PathVariable Long articleId,
                             @RequestParam("content") String content,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {

        String text = content == null ? "" : content.trim();

        if (text.isEmpty()) {
            redirectAttributes.addFlashAttribute("commentError", "评论内容不能为空");
            return "redirect:/article/" + articleId + "#comments";
        }
        if (text.length() > 500) {
            redirectAttributes.addFlashAttribute("commentError", "评论最多 500 个字");
            return "redirect:/article/" + articleId + "#comments";
        }

        // authentication.getName() 就是登录时用的用户名（这里是 admin）
        commentService.add(articleId, authentication.getName(), text);
        redirectAttributes.addFlashAttribute("commentOk", "评论已发布");
        return "redirect:/article/" + articleId + "#comments";
    }

    /**
     * 删除评论（只有管理员能进来，因为路径是 /admin/**）。
     * 删完回到那篇文章的评论区。
     */
    @PostMapping("/admin/comments/{id}/delete")
    public String deleteComment(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {

        Comment comment = commentService.findByIdOrNull(id);
        if (comment == null) {
            redirectAttributes.addFlashAttribute("commentError", "评论不存在或已被删除");
            return "redirect:/";
        }

        Long articleId = comment.getArticleId();
        commentService.delete(id);
        redirectAttributes.addFlashAttribute("commentOk", "评论已删除");
        return "redirect:/article/" + articleId + "#comments";
    }
}
