package com.example.blog.repository;

import com.example.blog.entity.SiteSetting;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 站点设置（单行）数据访问层。
 */
public interface SiteSettingRepository extends JpaRepository<SiteSetting, Long> {
}
