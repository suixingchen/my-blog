package com.example.blog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 站点外观设置（只有一行数据，id 固定为 1）。
 *
 * <p>为什么单独建一张表，而不是写死在配置文件里？
 * 因为壁纸是"用户/管理员在网页上改的数据"，属于<b>运行时数据</b>，
 * 应该存数据库；写在 application.yml 里就得改配置、重启，那就没法"自主更换"了。</p>
 *
 * <p>这种"单行配置表"是很常见的做法（key-value 表也行），面试可以说：
 * 把系统设置当作数据管理，避免为了改一个背景图就动配置文件和重启服务。</p>
 */
@Entity
@Table(name = "site_setting")
public class SiteSetting {

    /** 固定主键：永远只有 1 行 */
    public static final Long SINGLETON_ID = 1L;

    @Id
    private Long id = SINGLETON_ID;

    /** 背景壁纸的访问路径，例如 /uploads/ab12cd34.jpg；为 null 表示用默认背景 */
    @Column(name = "bg_image", length = 255)
    private String bgImage;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBgImage() {
        return bgImage;
    }

    public void setBgImage(String bgImage) {
        this.bgImage = bgImage;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
