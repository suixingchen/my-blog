package com.example.blog.config;

import com.example.blog.entity.Article;
import com.example.blog.repository.ArticleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 首次启动时写入演示数据，方便直接看到列表页与详情页的效果。
 * <p>数据库里已经有文章时不会重复插入。</p>
 */
@Configuration
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final ArticleRepository articleRepository;

    public DataInitializer(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    @Override
    public void run(String... args) {
        if (articleRepository.count() > 0) {
            log.info("数据库中已有 {} 篇文章，跳过初始化数据", articleRepository.count());
            return;
        }
        articleRepository.saveAll(sampleArticles());
        log.info("已写入 {} 篇演示文章", articleRepository.count());
    }

    private List<Article> sampleArticles() {
        return List.of(
                new Article(
                        "从零搭一个 Spring Boot 博客：整体设计",
                        "记录这个博客系统的分层设计：Controller / Service / Repository，以及为什么先跑通页面再接入数据库。",
                        """
                        做这个博客的出发点很简单：想要一个完全自己掌控的写作角落。

                        整体分成三层：
                        1. Controller 负责接收请求与返回视图；
                        2. Service 承载业务规则，例如发布、阅读量累加；
                        3. Repository 只关心数据读写，交给 Spring Data JPA。

                        先把首页用假数据渲染出来，确认 Thymeleaf 模板、静态资源路径都没问题，
                        之后再接数据库，这样一旦出错就能快速定位是模板问题还是数据问题。
                        """,
                        "后端", "Spring Boot,Thymeleaf,分层设计", "Stellara"),
                new Article(
                        "JPA 实体设计：一篇文章需要哪些字段",
                        "标题、摘要、正文、分类、标签、阅读量、发布状态与时间戳，逐个说明设计取舍。",
                        """
                        文章表最终的字段如下：

                        - title：标题，限长 150；
                        - summary：摘要，留空时由正文自动截取前 120 字；
                        - content：正文，用 @Lob 映射成大文本；
                        - category：单个分类，便于侧边栏归类；
                        - tags：多个标签用逗号分隔，轻量且够用；
                        - views：阅读量，详情页访问时自增；
                        - published：发布状态，false 即草稿，前台不展示；
                        - createdAt / updatedAt：由 @PrePersist、@PreUpdate 自动维护。

                        时间戳交给实体生命周期回调，而不是在每个 Service 方法里手写，
                        这样即使以后有别的入口写入数据，时间也不会漏掉。
                        """,
                        "后端", "JPA,Hibernate,实体设计", "Stellara"),
                new Article(
                        "Thymeleaf 模板复用：把页面拆成片段",
                        "用 th:fragment 抽出页头、页脚与侧边栏，避免每个页面都复制一遍结构。",
                        """
                        模板重复是维护成本最高的地方之一。这个项目把公共部分抽成 fragments：

                        - head：统一引入 CSS 与 meta，标题由页面传入；
                        - header：站点名与导航；
                        - footer：版权信息；
                        - sidebar：作者卡片、分类列表、关于本站。

                        页面里用 th:replace="~{fragments/layout :: header}" 引入即可。
                        需要传参的地方（比如 head 的标题）用片段参数，写法是 ~{... :: head('首页')}。
                        """,
                        "前端", "Thymeleaf,模板", "Stellara"),
                new Article(
                        "后台管理页面的最小可用做法",
                        "不做登录的前提下，后台只需要列表、表单、删除三个动作，先把流程跑顺。",
                        """
                        需求里明确说暂时不做登录验证，所以后台的核心就是让写文章的流程足够顺手：

                        1. /admin 列出全部文章，含草稿，显示阅读量与时间；
                        2. /admin/articles/new 打开新建表单；
                        3. 提交后重定向回列表，并用 Flash 属性提示结果；
                        4. 编辑与删除分别走 GET 表单页和 POST 动作。

                        删除用 POST 而不是 GET 链接，避免浏览器预取或爬虫误删数据。
                        """,
                        "后端", "后台管理,CRUD", "Stellara"),
                new Article(
                        "本地开发环境搭建记录（JDK 与 Maven）",
                        "记录 JDK 17 与 Maven 的便携式安装方式，不改系统环境变量也能跑起来。",
                        """
                        为了不污染系统环境，这里用的是便携版方案：把 JDK 与 Maven 解压到固定目录，
                        需要时用绝对路径调用，或者只在当前会话里临时设置 JAVA_HOME。

                        关键点有两个：
                        1. Maven 的 settings.xml 里配置国内镜像，依赖下载速度快很多；
                        2. localRepository 指到数据盘，避免 C 盘被依赖包撑满。

                        验证方式：java -version 与 mvn -v 都能正常输出即可。
                        """,
                        "工具", "环境搭建,Maven,JDK", "Stellara"),
                new Article(
                        "写博客这件事，先跑起来再说",
                        "关于为什么先做一个功能最少但能用的版本，而不是一开始就追求完整。",
                        """
                        很多人写博客卡在第一步：主题要好看、功能要齐全、部署要优雅。

                        结果往往是项目停留在本地的一个空目录里。

                        这次换了个顺序：先让首页能显示三篇假文章，再接数据库，
                        再做后台，最后才补分页、搜索和异常处理。

                        每一步都是可以运行、可以提交的状态，进度看得见，也就更容易坚持下去。
                        """,
                        "随笔", "随笔,方法论", "Stellara")
        );
    }
}
