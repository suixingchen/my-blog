-- MySQL 建表脚本（与 JPA 实体 Article 对应）
-- 使用方式：
--   mysql -uroot -p myblog < db/schema-mysql.sql
-- 若使用 application-mysql.yml 中的 ddl-auto=update，本脚本可省略。

CREATE TABLE IF NOT EXISTS `article` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `title`      VARCHAR(150) NOT NULL COMMENT '标题',
    `summary`    VARCHAR(300) DEFAULT NULL COMMENT '摘要',
    `content`    LONGTEXT     NOT NULL COMMENT '正文',
    `category`   VARCHAR(50)  DEFAULT NULL COMMENT '分类',
    `tags`       VARCHAR(200) DEFAULT NULL COMMENT '标签，逗号分隔',
    `author`     VARCHAR(50)  DEFAULT NULL COMMENT '作者',
    `views`      INT          NOT NULL DEFAULT 0 COMMENT '阅读量',
    `published`  BIT(1)       NOT NULL DEFAULT b'1' COMMENT '是否已发布',
    `created_at` DATETIME(6)  NOT NULL COMMENT '创建时间',
    `updated_at` DATETIME(6)  NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_article_published_created` (`published`, `created_at`),
    KEY `idx_article_category` (`category`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '博客文章';
