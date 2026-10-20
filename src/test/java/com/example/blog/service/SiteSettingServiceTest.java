package com.example.blog.service;

import com.example.blog.entity.SiteSetting;
import com.example.blog.repository.SiteSettingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 壁纸上传的规则测试。
 *
 * <p>这里不用 {@code @DataJpaTest}，因为被测的是"校验规则"而不是数据库：
 * Repository 用 Mockito 假装一下，文件写到临时目录 {@code @TempDir}。</p>
 *
 * <p>关键技巧：大小上限是从构造参数传进来的，所以测试里可以直接传一个
 * "1KB" 来验证超限逻辑，不用真的造一个 40MB 的文件——跑得快，也不会占内存。</p>
 */
class SiteSettingServiceTest {

    /** 假装出来的仓库：用 AtomicReference 当"那一行数据"，保存后再查能查到同一行 */
    private SiteSettingRepository fakeRepository() {
        AtomicReference<SiteSetting> row = new AtomicReference<>();
        SiteSettingRepository repository = mock(SiteSettingRepository.class);
        when(repository.findById(any())).thenAnswer(invocation -> Optional.ofNullable(row.get()));
        when(repository.save(any(SiteSetting.class))).thenAnswer(invocation -> {
            SiteSetting setting = invocation.getArgument(0);
            row.set(setting);
            return setting;
        });
        return repository;
    }

    private SiteSettingService serviceWithLimit(String limit) {
        return new SiteSettingService(fakeRepository(), limit, "默认站名", "默认作者");
    }

    @Test
    @DisplayName("超过上限的图片会被拒绝，提示里带上配置的上限")
    void rejectsOversizedFile(@TempDir Path tempDir) {
        SiteSettingService service = serviceWithLimit("1KB");
        MockMultipartFile big = new MockMultipartFile(
                "file", "wallpaper.png", "image/png", new byte[2 * 1024]);   // 2KB > 1KB

        assertThatThrownBy(() -> service.updateBackground(big, tempDir.toString()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("1KB");

        assertThat(service.getMaxSizeLabel()).isEqualTo("1KB");
        assertThat(tempDir.toFile().list()).isEmpty();     // 被拒绝的文件不该落盘
    }

    @Test
    @DisplayName("后缀不在白名单里的文件会被拒绝（防止传 .jsp/.sh 之类）")
    void rejectsWrongExtension(@TempDir Path tempDir) {
        SiteSettingService service = serviceWithLimit("40MB");
        MockMultipartFile evil = new MockMultipartFile(
                "file", "hack.jsp", "text/plain", "not an image".getBytes());

        assertThatThrownBy(() -> service.updateBackground(evil, tempDir.toString()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("只支持");
    }

    @Test
    @DisplayName("正常图片会被存下：文件名是 UUID，数据库只存 /uploads/ 开头的相对路径")
    void savesImageWithUuidName(@TempDir Path tempDir) throws Exception {
        SiteSettingService service = serviceWithLimit("40MB");
        MockMultipartFile image = new MockMultipartFile(
                "file", "我的壁纸.png", "image/png", new byte[1024]);

        service.updateBackground(image, tempDir.toString());

        String bgImage = service.getBackgroundUrl();
        assertThat(bgImage).startsWith("/uploads/");
        assertThat(bgImage).endsWith(".png");
        assertThat(bgImage).doesNotContain("我的壁纸");      // 原名不落地，杜绝路径穿越

        String savedName = bgImage.substring("/uploads/".length());
        assertThat(Files.exists(tempDir.resolve(savedName))).isTrue();
        assertThat(Files.size(tempDir.resolve(savedName))).isEqualTo(1024);
    }

    @Test
    @DisplayName("换新壁纸时旧文件会被删掉，不会越攒越多")
    void deletesPreviousFileWhenReplacing(@TempDir Path tempDir) throws Exception {
        SiteSettingService service = serviceWithLimit("40MB");

        service.updateBackground(new MockMultipartFile("file", "a.png", "image/png", new byte[10]), tempDir.toString());
        Path first = tempDir.resolve(service.getBackgroundUrl().substring("/uploads/".length()));

        service.updateBackground(new MockMultipartFile("file", "b.png", "image/png", new byte[10]), tempDir.toString());
        Path second = tempDir.resolve(service.getBackgroundUrl().substring("/uploads/".length()));

        assertThat(first).isNotEqualTo(second);
        assertThat(Files.exists(first)).isFalse();     // 旧的删了
        assertThat(Files.exists(second)).isTrue();     // 新的在
    }

    @Test
    @DisplayName("没改过名字时，站名/作者名用配置文件里的默认值")
    void namesFallBackToConfiguredDefaults() {
        SiteSettingService service = serviceWithLimit("40MB");

        assertThat(service.getSiteTitle()).isEqualTo("默认站名");
        assertThat(service.getSiteAuthor()).isEqualTo("默认作者");
    }

    @Test
    @DisplayName("改名后立刻生效，并且会去掉首尾空格")
    void updateNamesTrimsAndTakesEffect() {
        SiteSettingService service = serviceWithLimit("40MB");

        service.updateNames("  Stellara的个人博客  ", " Stellara ");

        assertThat(service.getSiteTitle()).isEqualTo("Stellara的个人博客");
        assertThat(service.getSiteAuthor()).isEqualTo("Stellara");
    }

    @Test
    @DisplayName("留空保存 = 恢复配置文件里的默认值")
    void blankNamesFallBackToDefaults() {
        SiteSettingService service = serviceWithLimit("40MB");
        service.updateNames("临时站名", "临时作者");

        service.updateNames("   ", "");

        assertThat(service.getSiteTitle()).isEqualTo("默认站名");
        assertThat(service.getSiteAuthor()).isEqualTo("默认作者");
    }

    @Test
    @DisplayName("名字太长会被截断到 40 个字符，不会把页面撑坏")
    void tooLongNameIsTruncated() {
        SiteSettingService service = serviceWithLimit("40MB");
        String tooLong = "长".repeat(60);

        service.updateNames(tooLong, "作者");

        assertThat(service.getSiteTitle()).hasSize(40);
    }
}
