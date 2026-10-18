package com.example.blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 配置：登录验证 + 权限控制。
 *
 * <p>分成三块，面试时可以照着讲：</p>
 * <ol>
 *   <li><b>密码怎么存</b>：{@link PasswordEncoder} 用 BCrypt，数据库/内存里都不存明文；</li>
 *   <li><b>账号从哪来</b>：{@link UserDetailsService} 用内存里的一个管理员账号，
 *       不建 user 表，够用且最简单；</li>
 *   <li><b>谁能访问什么</b>：{@link SecurityFilterChain} 里逐条声明放行规则。</li>
 * </ol>
 */
@Configuration
public class SecurityConfig {

    /**
     * 密码加密器：BCrypt 是"加盐哈希"，同一个密码每次加密结果都不同，
     * 所以即使数据泄露，也没法反推出原密码。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 管理员账号：admin / admin123。
     * 注意密码是用上面的编码器加密后再存进内存的，不是明文。
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails admin = User.withUsername("admin")
                .password(passwordEncoder.encode("admin123"))
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(admin);
    }

    /**
     * 访问控制规则。规则是<b>从上往下匹配，第一条命中就生效</b>，所以顺序很重要。
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // 1. 静态资源放行（不然登录页会没有样式）
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/uploads/**", "/favicon.ico").permitAll()
                        // 2. 前台页面放行：首页、文章详情、登录页
                        .requestMatchers("/", "/article/**", "/login").permitAll()
                        // 3. 后台必须登录，而且角色得是 ADMIN
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // 4. 其余一律要求登录（比如评论接口）
                        .anyRequest().authenticated()
                )
                // 使用我们自己写的登录页（templates/login.html）
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/admin", true)
                        .permitAll()
                )
                // 退出登录：Spring Security 6 默认要求 POST 请求
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll()
                )
                // 保留 H2 控制台时的兼容配置（H2 用的是 frame）
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }
}
