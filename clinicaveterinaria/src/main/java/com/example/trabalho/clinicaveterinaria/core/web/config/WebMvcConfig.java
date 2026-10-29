package com.example.trabalho.clinicaveterinaria.core.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxViewResolver;
import com.example.trabalho.clinicaveterinaria.core.web.interceptor.NotificacaoHeaderInterceptor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private NotificacaoHeaderInterceptor notificacaoInterceptor;

    public WebMvcConfig(NotificacaoHeaderInterceptor notificacaoInterceptor) {
        this.notificacaoInterceptor = notificacaoInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(notificacaoInterceptor);
    }

    @Bean
    public HtmxViewResolver htmxViewResolver() {
        HtmxViewResolver resolver = new HtmxViewResolver();
        resolver.setOrder(Ordered.LOWEST_PRECEDENCE - 10);
        return resolver;
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("index");
    }
        
}