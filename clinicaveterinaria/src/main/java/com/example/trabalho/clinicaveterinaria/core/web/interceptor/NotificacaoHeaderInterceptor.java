package com.example.trabalho.clinicaveterinaria.core.web.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;
import com.example.trabalho.clinicaveterinaria.core.web.notification.NotificacaoSweetAlert2;

@Component
public class NotificacaoHeaderInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper;

    public NotificacaoHeaderInterceptor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {

        if (modelAndView != null && modelAndView.getModel() != null) {
            Object notificacaoObj = modelAndView.getModel().get("notificacaoSA2");

            if (notificacaoObj instanceof NotificacaoSweetAlert2) {
                NotificacaoSweetAlert2 notificacao = (NotificacaoSweetAlert2) notificacaoObj;

                // Estrutura do evento: {"exibirAlerta": { ... dados ... }}
                String jsonDados = objectMapper.writeValueAsString(notificacao);
                String headerValue = "{\"exibirAlerta\": " + jsonDados + "}";

                response.addHeader("HX-Trigger", headerValue);
            }
        }
    }
}
