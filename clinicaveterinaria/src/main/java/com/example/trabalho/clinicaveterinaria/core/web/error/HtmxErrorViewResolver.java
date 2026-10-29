package com.example.trabalho.clinicaveterinaria.core.web.error;

import java.util.Map;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.webmvc.autoconfigure.error.DefaultErrorViewResolver;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class HtmxErrorViewResolver extends DefaultErrorViewResolver {

    public HtmxErrorViewResolver(ApplicationContext applicationContext, WebProperties webProperties) {
        super(applicationContext, webProperties.getResources());
    }

    @Override
    public ModelAndView resolveErrorView(HttpServletRequest request, HttpStatus status, Map<String, Object> model) {
        ModelAndView modelAndView = super.resolveErrorView(request, status, model);
        boolean isHtmx = request.getHeader("HX-Request") != null;

        if (isHtmx) {
            if (modelAndView != null) {
                modelAndView.setViewName(modelAndView.getViewName() + " :: conteudo");
                return modelAndView;
            } else {
                return new ModelAndView("error :: conteudo", model);
            }
        }

        return modelAndView;
    }
}

