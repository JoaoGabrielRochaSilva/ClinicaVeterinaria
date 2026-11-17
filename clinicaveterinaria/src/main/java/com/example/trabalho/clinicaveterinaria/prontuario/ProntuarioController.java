package com.example.trabalho.clinicaveterinaria.prontuario;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.trabalho.clinicaveterinaria.core.web.notification.NotificacaoSweetAlert2;
import com.example.trabalho.clinicaveterinaria.core.web.notification.TipoNotificacaoSweetAlert2;
import com.example.trabalho.clinicaveterinaria.core.web.pagination.PageWrapper;
import com.example.trabalho.clinicaveterinaria.prontuario.dto.ProntuarioResponse;
import com.example.trabalho.clinicaveterinaria.prontuario.dto.ProntuarioSearchRequest;
import com.example.trabalho.clinicaveterinaria.prontuario.dto.ProntuarioUpdateRequest;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j 
@Controller 
@AllArgsConstructor 
public class ProntuarioController {

    private final ProntuarioService prontuarioService;

    @GetMapping("/prontuarios")
    public String pesquisar(
            ProntuarioSearchRequest form,
            Model model,
            @PageableDefault(size = 7)
            @SortDefault(sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable,
            HttpServletRequest request,
            HtmxRequest htmxRequest) {

        Page<ProntuarioResponse> pagina = prontuarioService.pesquisar(form, pageable);

        log.info("Prontuários encontrados: {}", pagina.getNumberOfElements());

        PageWrapper<ProntuarioResponse> paginaWrapper =
                new PageWrapper<>(pagina, request);

        model.addAttribute("pagina", paginaWrapper);
        model.addAttribute("prontuarioSearchRequest", form);

        return htmxRequest.isHtmxRequest()
                ? "prontuarios/list :: conteudo"
                : "prontuarios/list";
    }

    @GetMapping("/prontuarios/{id}/editar")
    public String abrirFormularioEdicao(
            @PathVariable Long id,
            Model model,
            HtmxRequest htmxRequest) {

        popularFormulario(model, prontuarioService.buscarPorId(id));

        return htmxRequest.isHtmxRequest()
                ? "prontuarios/edit :: formulario"
                : "prontuarios/edit";
    }

    // Usado pelo link "Prontuário" da lista de animais
    @GetMapping("/prontuarios/animal/{idAnimal}")
    public String abrirFormularioPeloAnimal(
            @PathVariable Long idAnimal,
            Model model,
            HtmxRequest htmxRequest) {

        popularFormulario(model, prontuarioService.buscarPorAnimal(idAnimal));

        return htmxRequest.isHtmxRequest()
                ? "prontuarios/edit :: formulario"
                : "prontuarios/edit";
    }

    @PutMapping("/prontuarios/{id}")
    public String atualizar(
            @PathVariable Long id,
            ProntuarioUpdateRequest request,
            RedirectAttributes redirectAttributes,
            HtmxRequest htmxRequest) {

        prontuarioService.atualizar(id, request);

        redirectAttributes.addFlashAttribute(
                "notificacaoSA2",
                new NotificacaoSweetAlert2(
                        "Prontuário atualizado com sucesso!",
                        TipoNotificacaoSweetAlert2.SUCCESS,
                        4000));

        redirectAttributes.addFlashAttribute(
                "mensagem",
                "Prontuário atualizado com sucesso!");

        return htmxRequest.isHtmxRequest()
                ? "redirect:htmx:location:/prontuarios"
                : "redirect:/prontuarios";
    }

    private void popularFormulario(Model model, ProntuarioResponse prontuario) {
        model.addAttribute("prontuario", prontuario);
        model.addAttribute("prontuarioUpdateRequest",
                new ProntuarioUpdateRequest(
                        prontuario.alergias(),
                        prontuario.doencasPreExistentes(),
                        prontuario.observacoes()));
    }
}