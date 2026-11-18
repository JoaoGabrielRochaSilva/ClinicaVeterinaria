package com.example.trabalho.clinicaveterinaria.agendamento;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.trabalho.clinicaveterinaria.agendamento.dto.AgendamentoCreateRequest;
import com.example.trabalho.clinicaveterinaria.agendamento.dto.AgendamentoRemarcarRequest;
import com.example.trabalho.clinicaveterinaria.agendamento.dto.AgendamentoResponse;
import com.example.trabalho.clinicaveterinaria.agendamento.dto.AgendamentoSearchRequest;
import com.example.trabalho.clinicaveterinaria.common.enums.StatusAgendamento;
import com.example.trabalho.clinicaveterinaria.common.exception.RegraDeNegocioException;
import com.example.trabalho.clinicaveterinaria.core.web.notification.NotificacaoSweetAlert2;
import com.example.trabalho.clinicaveterinaria.core.web.notification.TipoNotificacaoSweetAlert2;
import com.example.trabalho.clinicaveterinaria.core.web.pagination.PageWrapper;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@AllArgsConstructor
public class AgendamentoController {

    private static final List<StatusAgendamento> SITUACOES_FILTRO = List.of(
        StatusAgendamento.PENDENTE,
        StatusAgendamento.REALIZADO,
        StatusAgendamento.CANCELADO);

    private final AgendamentoService agendamentoService;

    @GetMapping("/agendamentos")
    public String pesquisar(
            AgendamentoSearchRequest form,
            Model model,
            @PageableDefault(size = 7)
            @SortDefault(sort = "dataHora", direction = Sort.Direction.DESC)
            Pageable pageable,
            HttpServletRequest request,
            HtmxRequest htmxRequest) {

        Page<AgendamentoResponse> pagina = agendamentoService.pesquisar(form, pageable);

        log.info("Agendamentos encontrados: {}", pagina.getNumberOfElements());

        model.addAttribute("pagina", new PageWrapper<>(pagina, request));
        model.addAttribute("agendamentoSearchRequest", form);
        model.addAttribute("situacoes", SITUACOES_FILTRO);

        return htmxRequest.isHtmxRequest()
                ? "agendamentos/list :: conteudo"
                : "agendamentos/list";
    }

    @GetMapping("/agendamentos/nova")
    public String abrirFormularioCadastro(
            AgendamentoCreateRequest request,
            Model model,
            HtmxRequest htmxRequest) {

        model.addAttribute("agendamentoCreateRequest", request);

        return htmxRequest.isHtmxRequest()
                ? "agendamentos/create :: formulario"
                : "agendamentos/create";
    }

    @PostMapping("/agendamentos")
    public String cadastrar(
            @Valid AgendamentoCreateRequest request,
            BindingResult resultado,
            RedirectAttributes redirectAttributes,
            HtmxRequest htmxRequest) {

        if (resultado.hasErrors()) {
            log.info("O agendamento recebido para cadastro não é válido: {}", resultado.getAllErrors());
        } else {
            try {
                agendamentoService.criar(request);

                notificarSucesso(redirectAttributes, "Agendamento cadastrado com sucesso!");

                return voltarParaLista(htmxRequest);
            } catch (RegraDeNegocioException e) {
                // criar() sempre informa o campo
                log.info("Regra de negócio violada ao cadastrar agendamento: {}", e.getMessage());
                resultado.rejectValue(e.getCampo(), "regraDeNegocio", e.getMessage());
            }
        }

        return htmxRequest.isHtmxRequest()
                ? "agendamentos/create :: formulario"
                : "agendamentos/create";
    }

    @GetMapping("/agendamentos/{id}/remarcar")
    public String abrirFormularioRemarcacao(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes,
            HtmxRequest htmxRequest) {

        AgendamentoResponse agendamento;

        try {
            agendamento = agendamentoService.buscarParaRemarcacao(id);
        } catch (RegraDeNegocioException e) {
            return voltarParaListaComErro(e.getMessage(), redirectAttributes, htmxRequest);
        }

        model.addAttribute("agendamento", agendamento);
        model.addAttribute("agendamentoRemarcarRequest",
                new AgendamentoRemarcarRequest(
                        agendamento.dataHora().toLocalDate(),
                        agendamento.dataHora().toLocalTime()));

        return htmxRequest.isHtmxRequest()
                ? "agendamentos/remarcar :: formulario"
                : "agendamentos/remarcar";
    }

    @PutMapping("/agendamentos/{id}")
    public String remarcar(
            @PathVariable Long id,
            @Valid AgendamentoRemarcarRequest request,
            BindingResult resultado,
            Model model,
            RedirectAttributes redirectAttributes,
            HtmxRequest htmxRequest) {

        if (resultado.hasErrors()) {
            log.info("A remarcação recebida não é válida: {}", resultado.getAllErrors());
        } else {
            try {
                agendamentoService.remarcar(id, request);

                notificarSucesso(redirectAttributes, "Agendamento remarcado com sucesso!");

                return voltarParaLista(htmxRequest);
            } catch (RegraDeNegocioException e) {
                log.info("Regra de negócio violada ao remarcar agendamento: {}", e.getMessage());

                // Sem campo: o agendamento deixou de ser PENDENTE (tela desatualizada)
                if (e.getCampo() == null) {
                    return voltarParaListaComErro(e.getMessage(), redirectAttributes, htmxRequest);
                }

                resultado.rejectValue(e.getCampo(), "regraDeNegocio", e.getMessage());
            }
        }

        model.addAttribute("agendamento", agendamentoService.buscarPorId(id));

        return htmxRequest.isHtmxRequest()
                ? "agendamentos/remarcar :: formulario"
                : "agendamentos/remarcar";
    }

    @PutMapping("/agendamentos/{id}/cancelar")
    public String cancelar(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes,
            HtmxRequest htmxRequest) {

        try {
            agendamentoService.cancelar(id);
        } catch (RegraDeNegocioException e) {
            return voltarParaListaComErro(e.getMessage(), redirectAttributes, htmxRequest);
        }

        notificarSucesso(redirectAttributes, "Agendamento cancelado com sucesso!");

        return voltarParaLista(htmxRequest);
    }

    private void notificarSucesso(RedirectAttributes redirectAttributes, String mensagem) {
        redirectAttributes.addFlashAttribute(
                "notificacaoSA2",
                new NotificacaoSweetAlert2(
                        mensagem,
                        TipoNotificacaoSweetAlert2.SUCCESS,
                        4000));

        redirectAttributes.addFlashAttribute("mensagem", mensagem);
    }

    private String voltarParaListaComErro(
            String mensagem,
            RedirectAttributes redirectAttributes,
            HtmxRequest htmxRequest) {

        redirectAttributes.addFlashAttribute(
                "notificacaoSA2",
                new NotificacaoSweetAlert2(
                        mensagem,
                        TipoNotificacaoSweetAlert2.ERROR,
                        4000));

        return voltarParaLista(htmxRequest);
    }

    private String voltarParaLista(HtmxRequest htmxRequest) {
        return htmxRequest.isHtmxRequest()
                ? "redirect:htmx:location:/agendamentos"
                : "redirect:/agendamentos";
    }
}