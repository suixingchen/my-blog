package com.example.blog.dto;

import com.example.blog.entity.Article;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 文章表单对象。
 * <p>后台表单不再直接绑定 {@link Article} 实体，原因有两点：
 * 一是实体的 published 字段带了「默认已发布」的初始值，直接绑定会把请求里没带的参数
 * 也当成已发布；二是校验注解属于入参边界，放在 DTO 上比放在实体上更清晰。</p>
 * <p>关于发布状态：Thymeleaf 的 th:field 渲染复选框时会额外生成一个 {@code _published}
 * 隐藏字段，因此浏览器提交「未勾选」时 Spring 会把该属性绑定为 false；
 * 若请求里完全没有这个参数，则按 DTO 的默认值处理。</p>
 */
public class ArticleForm {

    private Long id;

    @NotBlank(message = "标题不能为空")
    @Size(max = 150, message = "标题不能超过 150 个字符")
    private String title;

    @Size(max = 300, message = "摘要不能超过 300 个字符")
    private String summary;

    @NotBlank(message = "正文不能为空")
    private String content;

    @Size(max = 50, message = "分类不能超过 50 个字符")
    private String category;

    @Size(max = 200, message = "标签不能超过 200 个字符")
    private String tags;

    @Size(max = 50, message = "作者不能超过 50 个字符")
    private String author;

    /** 是否立即发布；未勾选即保存为草稿 */
    private boolean published = true;

    /** 把实体回填成表单，用于编辑页 */
    public static ArticleForm from(Article article) {
        ArticleForm form = new ArticleForm();
        form.setId(article.getId());
        form.setTitle(article.getTitle());
        form.setSummary(article.getSummary());
        form.setContent(article.getContent());
        form.setCategory(article.getCategory());
        form.setTags(article.getTags());
        form.setAuthor(article.getAuthor());
        form.setPublished(Boolean.TRUE.equals(article.getPublished()));
        return form;
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

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }
}
