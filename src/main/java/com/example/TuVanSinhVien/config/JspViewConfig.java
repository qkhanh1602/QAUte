package com.example.TuVanSinhVien.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@Configuration
public class JspViewConfig {

    @Bean
    InternalResourceViewResolver jspViewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/WEB-INF/jsp/");
        resolver.setSuffix(".jsp");
        // Tomcat 11 requires include dispatching for JSP views resolved by Spring MVC.
        resolver.setAlwaysInclude(true);
        resolver.setOrder(100);
        return resolver;
    }
}
