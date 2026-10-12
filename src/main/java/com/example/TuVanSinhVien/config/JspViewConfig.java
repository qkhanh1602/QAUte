package com.example.TuVanSinhVien.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@Configuration
public class JspViewConfig implements WebMvcConfigurer {
    private static final String HTML_UTF8 = "text/html; charset=UTF-8";

    @Bean
    InternalResourceViewResolver jspViewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/WEB-INF/jsp/");
        resolver.setSuffix(".jsp");
        resolver.setContentType(HTML_UTF8);
        // Tomcat 11 requires include dispatching for JSP views resolved by Spring MVC.
        resolver.setAlwaysInclude(true);
        resolver.setOrder(100);
        return resolver;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public void postHandle(HttpServletRequest request, HttpServletResponse response,
                                   Object handler, ModelAndView modelAndView) {
                if (modelAndView == null || modelAndView.getViewName() == null) return;
                String viewName = modelAndView.getViewName();
                if (viewName.startsWith("redirect:") || viewName.startsWith("forward:")) return;
                // SiteMesh creates its byte buffer before the JSP include sets a charset.
                // Set UTF-8 on the real response first, so both buffer decoding and HTTP agree.
                response.setContentType(HTML_UTF8);
            }
        });
    }
}