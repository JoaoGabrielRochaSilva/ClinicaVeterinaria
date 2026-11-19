package com.example.trabalho.clinicaveterinaria.consulta;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.trabalho.clinicaveterinaria.agendamento.AgendamentoService;
import com.example.trabalho.clinicaveterinaria.agendamento.dto.AgendamentoResponse;
import com.example.trabalho.clinicaveterinaria.agendamento.dto.AgendamentoSearchRequest;
import com.example.trabalho.clinicaveterinaria.common.enums.StatusAgendamento;
import com.example.trabalho.clinicaveterinaria.common.exception.RegraDeNegocioException;
import com.example.trabalho.clinicaveterinaria.consulta.dto.ConsultaCreateRequest;
import com.example.trabalho.clinicaveterinaria.consulta.dto.ConsultaResponse;
import com.example.trabalho.clinicaveterinaria.consulta.dto.ConsultaSearchRequest;
import com.example.trabalho.clinicaveterinaria.consulta.dto.ConsultaUpdateRequest;
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
public class ConsultaController {

    private final ConsultaService consultaService;
    private final AgendamentoService agendamentoService;

    @GetMapping("/consultas")
    public String pesquisar(
            ConsultaSearchRequest form,
            Model model,
            @PageableDefault(size = 7)
            @SortDefault(sort = "dataHora", direction = Sort.Direction.DESC)
            Pageable pageable,
            HttpServletRequest request,
            HtmxRequest htmxRequest) {

        Page<ConsultaResponse> pagina = consultaService.pesquisar(form, pageable);

        log.info("Consultas encontradas: {}", pagina.getNumberOfElements());

        model.addAttribute("pagina", new PageWrapper<>(pagina, request));
        model.addAttribute("consultaSearchRequest", form);

        return htmxRequest.isHtmxRequest()
                ? "consultas/list :: conteudo"
                : "consultas/list";
    }

    @GetMapping("/consultas/realizar")
    public String listarParaAtendimento(
            AgendamentoSearchRequest form,
            Model model,
            @PageableDefault(size = 7)
            @SortDefault(sort = "dataHora", direction = Sort.Direction.ASC)
            Pageable pageable,
            HttpServletRequest request,
            HtmxRequest htmxRequest) {

        AgendamentoSearchRequest filtro = new AgendamentoSearchRequest(
            null, null,
            form.idVeterinario(), form.nomeVeterinario(),
            null, null,
            StatusAgendamento.PENDENTE);

        Page<AgendamentoResponse> pagina = agendamentoService.pesquisar(filtro, pageable);

        model.addAttribute("pagina", new PageWrapper<>(pagina, request));
        model.addAttribute("agendamentoSearchRequest", form);

        return htmxRequest.isHtmxRequest()
                ? "consultas/realizar :: conteudo"
                : "consultas/realizar";
    }

    @GetMapping("/consultas/realizar/{idAgendamento}")
    public String abrirFormularioAtendimento(
            @PathVariable Long idAgendamento,
            Model model,
            RedirectAttributes redirectAttributes,
            HtmxRequest htmxRequest) {

        AgendamentoResponse agendamento;

        try {
            agendamento = agendamentoService.buscarParaAtendimento(idAgendamento);
        } catch (RegraDeNegocioException e) {
            return voltarParaAtendimentoComErro(e.getMessage(), redirectAttributes, htmxRequest);
        }

        model.addAttribute("agendamento", agendamento);
        model.addAttribute("consultaCreateRequest",
                new ConsultaCreateRequest(idAgendamento, null, null));

        return htmxRequest.isHtmxRequest()
                ? "consultas/atender :: formulario"
                : "consultas/atender";
    }

    @PostMapping("/consultas")
    public String registrar(
            @Valid ConsultaCreateRequest request,
            BindingResult resultado,
            Model model,
            RedirectAttributes redirectAttributes,
            HtmxRequest htmxRequest) {

        if (resultado.hasErrors()) {
            log.info("A consulta recebida para registro não é válida: {}", resultado.getAllErrors());
        } else {
            try {
                consultaService.registrar(request);

                notificarSucesso(redirectAttributes, "Atendimento registrado com sucesso!");

                return voltarParaLista(htmxRequest);
            } catch (RegraDeNegocioException e) {
                // O agendamento deixou de ser PENDENTE
                log.info("Regra de negócio violada ao registrar consulta: {}", e.getMessage());
                return voltarParaAtendimentoComErro(e.getMessage(), redirectAttributes, htmxRequest);
            }
        }

        // Erro de validação reexibe o formulário com os dados do agendamento
        try {
            model.addAttribute("agendamento",
                    agendamentoService.buscarParaAtendimento(request.idAgendamento()));
        } catch (RegraDeNegocioException e) {
            return voltarParaAtendimentoComErro(e.getMessage(), redirectAttributes, htmxRequest);
        }

        return htmxRequest.isHtmxRequest()
                ? "consultas/atender :: formulario"
                : "consultas/atender";
    }

    @GetMapping("/consultas/{id}/editar")
    public String abrirFormularioEdicao(
            @PathVariable Long id,
            Model model,
            HtmxRequest htmxRequest) {

        ConsultaResponse consulta = consultaService.buscarPorId(id);

        model.addAttribute("consulta", consulta);
        model.addAttribute("consultaUpdateRequest",
                new ConsultaUpdateRequest(consulta.diagnostico(), consulta.tratamento()));

        return htmxRequest.isHtmxRequest()
                ? "consultas/edit :: formulario"
                : "consultas/edit";
    }

    @PutMapping("/consultas/{id}")
    public String atualizar(
            @PathVariable Long id,
            @Valid ConsultaUpdateRequest request,
            BindingResult resultado,
            Model model,
            RedirectAttributes redirectAttributes,
            HtmxRequest htmxRequest) {

        if (resultado.hasErrors()) {
            log.info("A consulta recebida para atualização não é válida: {}", resultado.getAllErrors());

            model.addAttribute("consulta", consultaService.buscarPorId(id));

            return htmxRequest.isHtmxRequest()
                    ? "consultas/edit :: formulario"
                    : "consultas/edit";
        }

        consultaService.atualizar(id, request);

        notificarSucesso(redirectAttributes, "Consulta atualizada com sucesso!");

        return voltarParaLista(htmxRequest);
    }

    @DeleteMapping("/consultas/{id}")
    public String excluir(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes,
            HtmxRequest htmxRequest) {

        consultaService.excluir(id);

        notificarSucesso(redirectAttributes, "Consulta excluída com sucesso!");

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

    private String voltarParaAtendimentoComErro(
            String mensagem,
            RedirectAttributes redirectAttributes,
            HtmxRequest htmxRequest) {

        redirectAttributes.addFlashAttribute(
                "notificacaoSA2",
                new NotificacaoSweetAlert2(
                        mensagem,
                        TipoNotificacaoSweetAlert2.ERROR,
                        4000));

        return htmxRequest.isHtmxRequest()
                ? "redirect:htmx:location:/consultas/realizar"
                : "redirect:/consultas/realizar";
    }

    private String voltarParaLista(HtmxRequest htmxRequest) {
        return htmxRequest.isHtmxRequest()
                ? "redirect:htmx:location:/consultas"
                : "redirect:/consultas";
    }
}