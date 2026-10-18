# my-blog · Spring Boot 个人博客系统

一个用 Spring Boot + Thymeleaf 搭建的个人博客：前台可以浏览文章列表与详情，后台可以发布、编辑、删除文章（按需求**暂不做登录验证**）。数据默认存放在本地 H2 文件数据库，也可切换到 MySQL。

> 📘 **想从零跟着做一遍？** 见 [`docs/教程.md`](docs/教程.md)：按本仓库真实代码分八步讲解（骨架 → 接数据库 → 前台 → 后台 → 完善 → MySQL → 打包验收 → Git 推送），并附「常见问题速查」收录了开发中实际踩过的 12 个坑。
>
> 🎤 **要拿去面试/答辩？** 见 [`docs/功能讲解-面试版.md`](docs/功能讲解-面试版.md)：逐个功能讲清「涉及哪些文件、代码为什么这么写、可能被问什么」。

## 功能清单

| 模块 | 功能 |
| --- | --- |
| 前台首页 `/` | 已发布文章分页列表、关键字搜索（标题 / 标签）、按分类筛选、侧边栏分类统计 |
| 文章详情 `/article/{id}` | 完整正文、阅读量自增、标签、相关文章推荐 |
| **登录验证** | Spring Security：`/admin/**` 需要 ADMIN 角色，前台公开浏览；BCrypt 存储密码；表单统一 CSRF 防护；支持退出登录 |
| 后台 `/admin` | 全部文章列表（含草稿）、统计卡片、关键字搜索、分页 |
| 后台写作 | 新建 / 编辑文章表单（标题、摘要、正文、分类、标签、作者、是否发布） |
| 后台删除 | POST 删除并二次确认，操作结果以 Flash 消息提示 |
| **评论** | 登录后可评论（未登录显示登录提示），评论存 MySQL；管理员可在评论区直接删除，删文章时评论级联清理 |
| **更换壁纸** | 后台 `/admin/settings` 上传壁纸（jpg/png/gif/webp，≤5MB），全站背景立即生效，可一键恢复默认 |
| 其他 | 表单校验（`@Valid` + 错误回显）、全局异常处理（404 / 500 友好页面）、首次启动写入演示数据 |

> 默认管理员账号：**`admin` / `admin123`**（写在 `SecurityConfig` 里，换成数据库用户只需要替换一个 Bean）。

## 技术栈

- **后端**：Spring Boot 3.2.5、Spring MVC、Spring Data JPA（Hibernate 6）、**Spring Security 6**、Jakarta Bean Validation
- **模板**：Thymeleaf 3（`th:fragment` 片段复用 + `thymeleaf-extras-springsecurity6` 判断登录状态）
- **数据库**：**MySQL 8**（默认，库名 `blog`，utf8mb4）；H2 作为备用 profile（`--spring.profiles.active=h2`）
- **构建**：Maven 3.9（`spring-boot-maven-plugin`）
- **前端**：手写 CSS（卡片式博客主题、响应式、`/theme.css` 动态生成壁纸样式），无前端构建步骤
- **JDK**：Java 17
- **测试**：JUnit 5 + AssertJ + `@DataJpaTest`（14 个用例）

## 目录结构

```
my-blog
├── pom.xml
├── db/schema-mysql.sql                 # MySQL 建表脚本（可选）
├── src/main/java/com/example/blog
│   ├── BlogApplication.java            # 启动类
│   ├── config/DataInitializer.java     # 首次启动写入演示文章
│   ├── controller/                     # HomeController / ArticleController / AdminController
│   │   ├── GlobalModelAdvice.java      # 全站公共模型（站点信息、分类统计）
│   │   └── GlobalExceptionHandler.java # 404 / 500 统一处理
│   ├── dto/ArticleForm.java            # 表单对象 + 校验注解
│   ├── entity/Article.java             # 文章实体
│   ├── repository/ArticleRepository.java
│   └── service/                        # ArticleService + ArticleNotFoundException
├── src/main/resources
│   ├── application.yml                 # 默认配置（H2）
│   ├── application-mysql.yml           # MySQL 配置
│   ├── static/css/style.css            # 博客主题样式
│   └── templates/                      # index / article / admin/* / error/* / fragments/layout.html
└── src/test/java/...                   # Repository 与 Service 测试
```

## 本地运行

### 1. 环境要求

- JDK 17 或更高（`java -version` 能看到 17+）
- Maven 3.6+（或使用 IDE 内置的 Maven）

没有现成环境也可以像本项目开发时那样用便携版：把 JDK 与 Maven 解压到任意目录，用绝对路径调用即可，无需改系统环境变量：

```powershell
# 本次开发实际使用的路径，仅作示例
$env:JAVA_HOME = 'D:\tools\jdk-17.0.20.1+1'
& 'D:\tools\apache-maven-3.9.16\bin\mvn.cmd' -v
```

国内网络建议在 Maven 的 `settings.xml` 里配置镜像与本地仓库位置，依赖下载会快很多：

```xml
<settings>
  <localRepository>D:\m2\repository</localRepository>
  <mirrors>
    <mirror>
      <id>aliyun-public</id>
      <url>https://maven.aliyun.com/repository/public</url>
      <mirrorOf>*</mirrorOf>
    </mirror>
  </mirrors>
</settings>
```

### 2. 启动（默认 MySQL）

先确保 MySQL 8 在运行，并建好数据库：

```sql
CREATE DATABASE IF NOT EXISTS blog DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

然后打包运行（**注意要在项目根目录执行**，因为上传目录 `uploads/` 是相对路径）：

```powershell
cd my-blog
mvn clean package
java -jar target/my-blog-0.0.1-SNAPSHOT.jar
```

浏览器打开 **http://localhost:8080**。表结构由 JPA 自动创建（`ddl-auto=update`），首次启动会写入 6 篇演示文章。

> Windows 上还有两个现成脚本（双击即可）：
> - `start-blog.cmd` —— 自动检查/启动 MySQL，再启动博客（默认 8081 端口）
> - `stop-blog.cmd` —— 停止博客

### 3. 没有 MySQL？用 H2 备用配置

```powershell
java -jar target/my-blog-0.0.1-SNAPSHOT.jar --spring.profiles.active=h2
```

数据文件会生成在项目根目录 `data/blogdb.mv.db`（删除该目录即可重置）；
H2 控制台：<http://localhost:8080/h2-console>（JDBC URL `jdbc:h2:file:./data/blogdb`，用户 `sa`，密码为空）。

### 4. 运行测试

```powershell
mvn test
```

单元测试用 `@DataJpaTest` + 内嵌 H2，**不依赖本机 MySQL**，共 14 个用例。

## 页面与路由

| 路由 | 说明 |
| --- | --- |
| `GET /` | 首页列表，支持 `?keyword=&category=&page=` |
| `GET /article/{id}` | 文章详情（阅读量 +1） |
| `GET /admin` | 后台文章列表，支持 `?keyword=&page=` |
| `GET /admin/articles/new` | 新建文章表单 |
| `POST /admin/articles` | 保存新文章 |
| `GET /admin/articles/{id}/edit` | 编辑文章表单 |
| `POST /admin/articles/{id}` | 保存修改 |
| `POST /admin/articles/{id}/delete` | 删除文章 |

## 数据模型

`Article`：`id`、`title`、`summary`（留空自动截取正文前 120 字）、`content`、`category`、`tags`（逗号分隔）、`author`、`views`、`published`、`createdAt`、`updatedAt`。

时间戳由 `@PrePersist` / `@PreUpdate` 生命周期回调维护，业务代码不需要手写。

## 开发过程与提交历史

项目按阶段提交，便于回溯每一步做了什么：

| 提交 | 内容 |
| --- | --- |
| 1. `chore: 初始化 Spring Boot 博客项目骨架` | Maven + Spring Boot 骨架、Thymeleaf 首页先用假数据跑通模板链路、`.gitignore`、README |
| 2. `feat: 实现文章发布、列表、详情与后台管理` | 引入 JPA + H2、`Article` 实体与 Repository/Service、首页列表、详情页、后台增删改查、博客主题样式、演示数据 |
| 3. `refactor: 完善校验、分页搜索、异常处理与 MySQL 支持` | 表单 DTO + 参数校验、Specification 动态查询与分页、全局异常处理与错误页、MySQL 配置与建表脚本、单元测试、README 完善 |

## 遇到的困难与解决

**1. 本机没有 JDK / Maven，且不想污染系统环境**
下载 Temurin JDK 17 与 Maven 的 zip 包解压到 `D:\tools`，用绝对路径调用；Maven 通过自定义 `settings.xml` 指定国内镜像和本地仓库（放在数据盘），既不写系统环境变量，也不占用 C 盘空间。

**2. Tomcat 启动直接失败：`Unable to create tempDir ... AccessDeniedException`**
报错栈指向 `AbstractConfigurableWebServerFactory.createTempDir`：内嵌 Tomcat 需要在 `java.io.tmpdir` 下创建临时目录，而本机这个变量指向了一个受限目录，Java 进程没有创建权限。解决办法是启动时显式指定临时目录：

```powershell
java -Djava.io.tmpdir=D:\c++\my-blog\.run-tmp -jar target/my-blog-0.0.1-SNAPSHOT.jar
```

该目录已加入 `.gitignore`。正常开发机上不会遇到这个问题，但排查思路（先看内嵌容器需要的临时目录/端口，再看数据库）值得记一笔。

**3. 复选框绑定陷阱：没勾「立即发布」却发布了出去**
一开始后台表单直接绑定 `Article` 实体，而实体字段声明成了 `private Boolean published = true;`。这样当请求里**完全没有** `published` 参数时（例如用脚本或接口直接 POST），Spring 不会覆盖这个初始值，文章就被当成已发布。修复方式是引入 `ArticleForm` DTO：把发布状态声明为原始类型 `boolean`，并在 DTO 上集中处理语义与校验注解。同时确认了浏览器表单的行为——Thymeleaf 的 `th:field` 渲染复选框时会额外生成一个 `_published` 隐藏字段，Spring 据此把「未勾选」正确绑定为 `false`。这条经验也写进了 `ArticleForm` 的类注释。

**4. 多条件动态查询怎么拼**
「只看已发布 + 关键字 + 分类」三个条件两两组合，用方法名派生要写 8 个 `findBy...And...Or...`。改用 `JpaSpecificationExecutor` 动态拼 `Predicate`，配合 `PageRequest` 统一处理分页与排序，同时在 Service 里把 `page`、`size` 收敛到安全范围（`size` 上限 50），避免非法参数打穿。

**5. 关键字搜索踩到 CLOB 的坑**
最初把正文也纳入关键字匹配，写成 `cb.like(cb.lower(root.get("content")), like)`，运行时报错：
`Parameter 1 of function 'lower()' has type 'STRING', but argument is of type 'java.lang.String'`。
原因是正文用 `@Lob` 映射成了 CLOB，Hibernate 不允许对 CLOB 调用 `lower()`。
由于正文必须保留大文本映射（H2 是 CLOB、MySQL 是 LONGTEXT），最终把搜索范围收敛为标题与标签，
并在 `ArticleService` 里写下注释说明原因——中文正文本身也没有大小写问题，这个取舍是合理的。

**6. H2 与 MySQL 的差异**
`@Lob` 映射大文本时两种数据库的列类型不同（H2 为 CLOB，MySQL 为 LONGTEXT），开发用 H2、生产用 MySQL 时要注意方言；本项目通过 profile 分离配置（`application.yml` 用 H2、`application-mysql.yml` 用 MySQL）并额外提供 `db/schema-mysql.sql`，切换时只改启动参数。

**7. Windows 下提交中文信息**
命令行直接传中文有时会乱码，改用 `git commit -F 提交信息.txt` 从 UTF-8 文件读取，保证 `git log` 显示正常。

**8. 推送 GitHub 时连接超时**
`api.github.com` 正常但 `github.com:443` 超时，排查发现本机 DNS 把 `github.com` 解析到了一个不可达的 IP。临时绕过方式（无需管理员权限改 hosts）：

```powershell
git -c http.curloptResolve=github.com:443:140.82.114.3 push
```

这也是"能连上 API 却推不动代码"这类问题的通用排查思路：先分开测 API 域与 git 域，再确认 DNS 解析结果。

## 已知限制与后续计划

- 无登录验证（按需求），后台接口对任何人开放
- 正文按纯文本渲染（保留换行），暂不支持 Markdown
- 未实现评论、标签页、RSS、图片上传
- 未做打包部署（Docker / Nginx）与生产级配置（连接池、日志切割）
