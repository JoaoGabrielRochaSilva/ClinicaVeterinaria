package com.example.trabalho.clinicaveterinaria.usuario;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.trabalho.clinicaveterinaria.core.web.notification.NotificacaoSweetAlert2;
import com.example.trabalho.clinicaveterinaria.core.web.notification.TipoNotificacaoSweetAlert2;
import com.example.trabalho.clinicaveterinaria.core.web.pagination.PageWrapper;
import com.example.trabalho.clinicaveterinaria.usuario.dto.UsuarioCreateRequest;
import com.example.trabalho.clinicaveterinaria.usuario.dto.UsuarioResponse;
import com.example.trabalho.clinicaveterinaria.usuario.dto.UsuarioSearchRequest;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@AllArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping("/usuarios")
    public String pesquisar(
            UsuarioSearchRequest form,
            Model model,
            @PageableDefault(size = 7)
            @SortDefault(
                sort = "id",
                direction = Sort.Direction.ASC
            )
            Pageable pageable,
            HttpServletRequest request,
            HtmxRequest htmxRequest) {

        Page<UsuarioResponse> pagina =
                usuarioService.pesquisar(form, pageable);

        log.info("Usuários pesquisados: {}", pagina.getContent());

        PageWrapper<UsuarioResponse> paginaWrapper =
                new PageWrapper<>(pagina, request);

        model.addAttribute("pagina", paginaWrapper);

        return htmxRequest.isHtmxRequest()
                ? "usuarios/lista :: conteudo"
                : "usuarios/lista";
    }

    @GetMapping("/usuarios/nova")
    public String abrirFormulario(
            UsuarioCreateRequest request,
            Model model,
            HtmxRequest htmxRequest) {

        model.addAttribute("usuarioCreateRequest", request);

        return htmxRequest.isHtmxRequest()
                ? "usuarios/create :: formulario"
                : "usuarios/create";
    }

    @PostMapping("/usuarios")
    public String cadastrar(
            @Valid UsuarioCreateRequest request,
            BindingResult resultado,
            RedirectAttributes atributos,
            HtmxRequest htmxRequest) {

        if (resultado.hasErrors()) {
            log.info("O usuário recebido para cadastrar não é válido.");
            log.info("Erros encontrados:");

            for (FieldError erro : resultado.getFieldErrors()) {
                log.info("{}", erro);
            }

            for (ObjectError erro : resultado.getGlobalErrors()) {
                log.info("{}", erro);
            }

            return htmxRequest.isHtmxRequest()
                    ? "usuarios/create :: formulario"
                    : "usuarios/create";
        }

        usuarioService.salvar(request);

        atributos.addFlashAttribute(
                "notificacaoSA2",
                new NotificacaoSweetAlert2(
                        "Usuário cadastrado com sucesso!",
                        TipoNotificacaoSweetAlert2.SUCCESS,
                        4000
                )
        );

        atributos.addFlashAttribute(
                "mensagem",
                "Usuário cadastrado com sucesso"
        );

        return htmxRequest.isHtmxRequest()
                ? "redirect:htmx:location:/usuarios"
                : "redirect:/usuarios";
    }
}