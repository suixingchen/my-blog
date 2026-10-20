package com.example.blog.controller;

import com.example.blog.service.SiteSettingService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 动态样式表：把"当前壁纸"生成成一段 CSS 返回给浏览器。
 *
 * <p>为什么不用 Thymeleaf 在模板里直接拼 CSS？
 * 因为模板表达式里混 CSS 很容易踩坑（类选择器的点会被当成 SpEL 属性访问、
 * CSS 内联语法要用 /*[[...]]*&#47; 注释形式等），一旦写错整个页面都渲染失败。
 * 改成"后端产出 CSS 文件"后：模板里只留一句普通的
 * {@code <link rel="stylesheet" href="/theme.css">}，逻辑全在 Java 里，清楚又好测。</p>
 *
 * <p>面试可以这么说：壁纸属于运行时数据，所以样式表也做成动态生成的；
 * 换壁纸不需要重启，也不需要改模板。</p>
 */
@Controller
public class ThemeCssController {

    private final SiteSettingService siteSettingService;

    public ThemeCssController(SiteSettingService siteSettingService) {
        this.siteSettingService = siteSettingService;
    }

    @GetMapping(value = "/theme.css", produces = "text/css;charset=UTF-8")
    @ResponseBody
    public String themeCss() {
        String bgImage = siteSettingService.getBackgroundUrl();

        // 没有设置壁纸：返回一段空注释，浏览器沿用 style.css 里的默认背景
        if (bgImage == null || bgImage.isBlank()) {
            return "/* 使用默认背景 */\n";
        }

        // 壁纸不直接画在 body 上，而是画在一个固定定位的 ::before 图层上：
        //   ① 用 background-attachment: fixed 在 iOS Safari 上会失效/抖动，改成固定图层更稳；
        //   ② z-index: -1 让它躺在所有内容底下，卡片半透明就能透出壁纸；
        //   ③ 滚动时壁纸不动，长页面也有"沉浸"感。
        return "body::before{content:'';position:fixed;inset:0;z-index:-1;"
                + "background-image:url('" + bgImage + "');"
                + "background-size:cover;"
                + "background-position:center;"
                + "background-repeat:no-repeat;"
                + "}"
                // 卡片的颜色/透明度不在这里决定：
                // 交给 style.css 的 --card-bg 变量，用户在侧边栏"外观"里能自己选配色。
                + "\n";
    }
}
