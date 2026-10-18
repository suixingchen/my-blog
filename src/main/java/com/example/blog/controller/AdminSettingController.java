package com.example.blog.controller;

import com.example.blog.service.SiteSettingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

/**
 * 后台"外观设置"：更换背景壁纸。
 *
 * <p>整个类都在 {@code /admin/**} 下，所以只有登录的管理员能访问
 * （规则在 SecurityConfig 里）。</p>
 */
@Controller
@RequestMapping("/admin/settings")
public class AdminSettingController {

    private final SiteSettingService siteSettingService;

    @Value("${blog.upload-dir:./uploads}")
    private String uploadDir;

    public AdminSettingController(SiteSettingService siteSettingService) {
        this.siteSettingService = siteSettingService;
    }

    /** 设置页：显示当前壁纸 + 上传表单 */
    @GetMapping
    public String page(Model model) {
        model.addAttribute("setting", siteSettingService.getOrCreate());
        return "admin/settings";
    }

    /** 上传新壁纸 */
    @PostMapping("/background")
    public String uploadBackground(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {
        try {
            siteSettingService.updateBackground(file, uploadDir);
            redirectAttributes.addFlashAttribute("message", "背景壁纸已更新");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "文件保存失败：" + e.getMessage());
        }
        return "redirect:/admin/settings";
    }

    /** 恢复默认背景 */
    @PostMapping("/background/reset")
    public String resetBackground(RedirectAttributes redirectAttributes) {
        siteSettingService.resetBackground(uploadDir);
        redirectAttributes.addFlashAttribute("message", "已恢复默认背景");
        return "redirect:/admin/settings";
    }
}
