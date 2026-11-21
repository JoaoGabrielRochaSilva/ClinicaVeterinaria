package com.example.trabalho.clinicaveterinaria.usuario;

import java.util.List;

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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.trabalho.clinicaveterinaria.common.enums.Permissao;
import com.example.trabalho.clinicaveterinaria.common.exception.RegraDeNegocioException;
import com.example.trabalho.clinicaveterinaria.core.web.notification.NotificacaoSweetAlert2;
import com.example.trabalho.clinicaveterinaria.core.web.notification.TipoNotificacaoSweetAlert2;
import com.example.trabalho.clinicaveterinaria.core.web.pagination.PageWrapper;
import com.example.trabalho.clinicaveterinaria.usuario.dto.UsuarioCreateRequest;
import com.example.trabalho.clinicaveterinaria.usuario.dto.UsuarioResponse;
import com.example.trabalho.clinicaveterinaria.usuario.dto.UsuarioSearchRequest;
import com.example.trabalho.clinicaveterinaria.usuario.dto.UsuarioUpdateRequest;

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
                        @PageableDefault(size = 7) @SortDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
                        HttpServletRequest request,
                        HtmxRequest htmxRequest) {

                Page<UsuarioResponse> pagina = usuarioService.pesquisar(form, pageable);

                log.info("Usuários encontrados: {}", pagina.getNumberOfElements());

                PageWrapper<UsuarioResponse> paginaWrapper = new PageWrapper<>(pagina, request);

                model.addAttribute("pagina", paginaWrapper);

                return htmxRequest.isHtmxRequest()
                                ? "usuarios/list :: conteudo"
                                : "usuarios/list";
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

        @GetMapping("/usuarios/pesquisar")
        public String pesquisarPorPermissao(
                        @RequestParam(defaultValue = "") String termoBusca,
                        @RequestParam Permissao permissao,
                        Model model) {

                log.debug("Termo recebido na busca de usuários ({}): [{}]", permissao, termoBusca);

                List<UsuarioResponse> usuarios = usuarioService.buscarPeloNome(termoBusca, permissao);

                log.debug("Quantidade de usuários encontrados: {}", usuarios.size());

                model.addAttribute("usuarios", usuarios);

                return "usuarios/fragments/list_li :: lista";
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

                notificarSucesso(atributos, "Usuário cadastrado com sucesso!");

                return voltarParaLista(htmxRequest);
        }

        @GetMapping("/usuarios/{id}/editar")
        public String abrirFormularioEdicao(
                        @PathVariable Long id,
                        Model model,
                        HtmxRequest htmxRequest) {

                UsuarioResponse usuario = usuarioService.buscarPorId(id);

                model.addAttribute("usuario", usuario);
                model.addAttribute("usuarioUpdateRequest",
                                new UsuarioUpdateRequest(usuario.nome(), usuario.email()));

                return htmxRequest.isHtmxRequest()
                                ? "usuarios/edit :: formulario"
                                : "usuarios/edit";
        }

        @PutMapping("/usuarios/{id}")
        public String atualizar(
                        @PathVariable Long id,
                        @Valid UsuarioUpdateRequest request,
                        BindingResult resultado,
                        Model model,
                        RedirectAttributes atributos,
                        HtmxRequest htmxRequest) {

                if (resultado.hasErrors()) {
                        log.info("O usuário recebido para atualização não é válido: {}", resultado.getAllErrors());
                } else {
                        try {
                                usuarioService.atualizar(id, request);

                                notificarSucesso(atributos, "Usuário atualizado com sucesso!");

                                return voltarParaLista(htmxRequest);
                        } catch (RegraDeNegocioException e) {
                                log.info("Regra de negócio violada ao atualizar usuário: {}", e.getMessage());
                                resultado.rejectValue(e.getCampo(), "regraDeNegocio", e.getMessage());
                        }
                }

                model.addAttribute("usuario", usuarioService.buscarPorId(id));

                return htmxRequest.isHtmxRequest()
                                ? "usuarios/edit :: formulario"
                                : "usuarios/edit";
        }

        @DeleteMapping("/usuarios/{id}")
        public String remover(
                        @PathVariable Long id,
                        RedirectAttributes atributos,
                        HtmxRequest htmxRequest) {

                usuarioService.remover(id);

                notificarSucesso(atributos, "Usuário removido com sucesso!");

                return voltarParaLista(htmxRequest);
        }

        private void notificarSucesso(RedirectAttributes atributos, String mensagem) {
                atributos.addFlashAttribute(
                                "notificacaoSA2",
                                new NotificacaoSweetAlert2(
                                                mensagem,
                                                TipoNotificacaoSweetAlert2.SUCCESS,
                                                4000));

                atributos.addFlashAttribute("mensagem", mensagem);
        }

        private String voltarParaLista(HtmxRequest htmxRequest) {
                return htmxRequest.isHtmxRequest()
                                ? "redirect:htmx:location:/usuarios"
                                : "redirect:/usuarios";
        }
}