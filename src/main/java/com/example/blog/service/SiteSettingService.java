package com.example.blog.service;

import com.example.blog.entity.SiteSetting;
import com.example.blog.repository.SiteSettingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 站点外观设置：提供"读取当前壁纸"和"更换/恢复壁纸"两个能力。
 */
@Service
public class SiteSettingService {

    private static final Logger log = LoggerFactory.getLogger(SiteSettingService.class);

    /** 允许上传的图片后缀 */
    private static final List<String> ALLOWED_EXT = List.of("jpg", "jpeg", "png", "gif", "webp");
    /** 壁纸在网页上的访问前缀，对应 WebMvcConfig 里的静态资源映射 */
    private static final String WEB_PREFIX = "/uploads/";

    private final SiteSettingRepository siteSettingRepository;

    /** 单个文件大小上限（字节）。直接读 application.yml 的 multipart 配置，一处配置两边生效 */
    private final long maxSize;
    /** 上限的可读形式，如 "40MB"，用来拼提示语和页面上显示 */
    private final String maxSizeLabel;

    public SiteSettingService(SiteSettingRepository siteSettingRepository,
                              @Value("${spring.servlet.multipart.max-file-size:40MB}") String maxFileSize) {
        this.siteSettingRepository = siteSettingRepository;
        DataSize limit = DataSize.parse(maxFileSize);   // "40MB" -> 41943040 字节
        this.maxSize = limit.toBytes();
        this.maxSizeLabel = formatSize(limit);
    }

    /** 把 DataSize 变成人看的大小：41943040 -> "40MB"，2048 -> "2KB" */
    private static String formatSize(DataSize size) {
        long bytes = size.toBytes();
        if (bytes >= 1024 * 1024 && bytes % (1024 * 1024) == 0) {
            return (bytes / (1024 * 1024)) + "MB";
        }
        if (bytes >= 1024 && bytes % 1024 == 0) {
            return (bytes / 1024) + "KB";
        }
        return bytes + "B";
    }

    /** 给页面显示的"不超过 40MB"里的那个 40MB，保证和上面读的是同一个配置 */
    public String getMaxSizeLabel() {
        return maxSizeLabel;
    }

    /** 只读地取当前壁纸 URL，没有就返回 null（用于每个页面渲染，不写库） */
    @Transactional(readOnly = true)
    public String getBackgroundUrl() {
        return siteSettingRepository.findById(SiteSetting.SINGLETON_ID)
                .map(SiteSetting::getBgImage)
                .orElse(null);
    }

    /** 取设置行，不存在就创建一行（后台设置页使用） */
    @Transactional
    public SiteSetting getOrCreate() {
        return siteSettingRepository.findById(SiteSetting.SINGLETON_ID)
                .orElseGet(() -> {
                    SiteSetting setting = new SiteSetting();
                    return siteSettingRepository.save(setting);
                });
    }

    /**
     * 更换背景壁纸。
     *
     * <p>注意三个安全点（面试常问"上传功能要注意什么"）：</p>
     * <ol>
     *   <li><b>不能信任原文件名</b>：用 UUID 重新命名，避免 ../../ 之类的路径穿越；</li>
     *   <li><b>校验类型和大小</b>：只允许图片后缀，且不超过配置的大小上限（现在是 40MB）；</li>
     *   <li><b>只存相对路径</b>：数据库里存 /uploads/xxx.jpg，换了机器也能用。</li>
     * </ol>
     */
    @Transactional
    public void updateBackground(MultipartFile file, String uploadDir) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请先选择一张图片");
        }
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("图片不能超过 " + maxSizeLabel);
        }

        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0) {
            ext = original.substring(dot + 1).toLowerCase();
        }
        if (!ALLOWED_EXT.contains(ext)) {
            throw new IllegalArgumentException("只支持 " + String.join(" / ", ALLOWED_EXT) + " 格式的图片");
        }

        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(dir);

        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path target = dir.resolve(filename);
        file.transferTo(target.toFile());
        log.info("壁纸已保存到 {}", target);

        SiteSetting setting = getOrCreate();
        deleteOldFile(setting.getBgImage(), dir);   // 顺手删掉上一张，避免文件越攒越多

        setting.setBgImage(WEB_PREFIX + filename);
        setting.setUpdatedAt(LocalDateTime.now());
        siteSettingRepository.save(setting);
    }

    /** 恢复默认背景：清掉数据库里的壁纸配置，同时删除已上传的文件 */
    @Transactional
    public void resetBackground(String uploadDir) {
        SiteSetting setting = getOrCreate();
        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        deleteOldFile(setting.getBgImage(), dir);
        setting.setBgImage(null);
        setting.setUpdatedAt(LocalDateTime.now());
        siteSettingRepository.save(setting);
    }

    /** 删除 /uploads/ 下的旧壁纸文件；只处理本功能管理的路径，防止误删 */
    private void deleteOldFile(String webPath, Path dir) {
        if (webPath == null || !webPath.startsWith(WEB_PREFIX)) {
            return;
        }
        String name = webPath.substring(WEB_PREFIX.length());
        if (name.contains("..") || name.contains("/") || name.contains("\\")) {
            return;
        }
        try {
            Files.deleteIfExists(dir.resolve(name));
        } catch (IOException e) {
            log.warn("旧壁纸删除失败: {}", name, e);
        }
    }
}
