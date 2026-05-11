package com.itheima.reggie.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        //1. 配置CORS
        CorsConfiguration config = new CorsConfiguration();
        //允许的域,生产环境应该配置具体域名,这里允许所有
        config.addAllowedOriginPattern("*");
        //允许携带凭证(cookies)
        config.setAllowCredentials(true);
        //允许哪些请求方法
        config.addAllowedMethod("*");
        //允许哪些请求头
        config.addAllowedHeader("*");
        //暴露哪些响应头
        config.addExposedHeader("*");
        //预检请求的缓存时间
        config.setMaxAge(3600L);

        //2. 配置源
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        //3. 返回过滤器
        log.info("CORS配置已启用，允许跨域请求");
        return new CorsFilter(source);
    }
}