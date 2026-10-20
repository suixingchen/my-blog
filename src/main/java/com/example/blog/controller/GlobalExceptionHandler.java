package com.example.blog.controller;

import com.example.blog.service.ArticleNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * 全局异常处理：把异常翻译成友好的错误页面，而不是直接抛出堆栈。
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 文章不存在 -> 404 页面 */
    @ExceptionHandler(ArticleNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleArticleNotFound(ArticleNotFoundException ex, Model model) {
        log.warn("文章不存在: {}", ex.getMessage());
        model.addAttribute("status", 404);
        model.addAttribute("errorTitle", "文章走丢了");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/404";
    }

    /**
     * 上传文件超过大小上限。
     *
     * <p>这个异常是 Tomcat 在读请求体时抛的，发生在我们 Controller 的方法执行之前，
     * 所以没法在 Controller 里 try-catch，只能在这里统一接住，
     * 再重定向回设置页，用 ?tooLarge=1 让页面显示一句人话提示。</p>
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleUploadTooLarge(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        log.warn("上传文件超过大小上限，请求 {} 被拒绝: {}", request.getRequestURI(), ex.getMessage());
        return "redirect:/admin/settings?tooLarge=1";
    }

    /** 兜底：其它未预期异常 -> 500 页面 */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(Exception ex, HttpServletRequest request, Model model) {
        log.error("处理请求 {} 时发生异常", request.getRequestURI(), ex);
        model.addAttribute("status", 500);
        model.addAttribute("errorTitle", "服务器内部错误");
        model.addAttribute("errorMessage", ex.getClass().getSimpleName() + ": " + ex.getMessage());
        return "error/500";
    }
}
