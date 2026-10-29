package com.example.trabalho.clinicaveterinaria.animal;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.trabalho.clinicaveterinaria.animal.dto.AnimalCreateRequest;
import com.example.trabalho.clinicaveterinaria.animal.dto.AnimalResponse;
import com.example.trabalho.clinicaveterinaria.animal.dto.AnimalSearchRequest;
import com.example.trabalho.clinicaveterinaria.animal.dto.AnimalUpdateRequest;
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
public class AnimalController {

    private final AnimalService animalService;

    @GetMapping("/animais")
    public String pesquisar(
            AnimalSearchRequest form,
            Model model,
            @PageableDefault(size = 7)
            @SortDefault(sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable,
            HttpServletRequest request,
            HtmxRequest htmxRequest) {

        Page<AnimalResponse> pagina = animalService.pesquisar(form, pageable);

        log.info("Animais encontrados: {}", pagina.getNumberOfElements());

        PageWrapper<AnimalResponse> paginaWrapper =
                new PageWrapper<>(pagina, request);

        model.addAttribute("pagina", paginaWrapper);
        model.addAttribute("animalSearchRequest", form);

        return htmxRequest.isHtmxRequest()
                ? "animais/list :: conteudo"
                : "animais/list";
    }

    @GetMapping("/animais/nova")
    public String abrirFormularioCadastro(
            AnimalCreateRequest request,
            Model model,
            HtmxRequest htmxRequest) {

        model.addAttribute("animalCreateRequest", request);

        return htmxRequest.isHtmxRequest()
                ? "animais/create :: formulario"
                : "animais/create";
    }

    @PostMapping("/animais")
    public String cadastrar(
            @Valid AnimalCreateRequest request,
            BindingResult resultado,
            Model model,
            RedirectAttributes redirectAttributes,
            HtmxRequest htmxRequest) {

        if (resultado.hasErrors()) {
            log.info("O animal recebido para cadastro não é válido.");

            for (FieldError erro : resultado.getFieldErrors()) {
                log.info("{}", erro);
            }

            for (ObjectError erro : resultado.getGlobalErrors()) {
                log.info("{}", erro);
            }

            model.addAttribute("animalCreateRequest", request);

            return htmxRequest.isHtmxRequest()
                    ? "animais/create :: formulario"
                    : "animais/create";
        }

        animalService.salvar(request);

        redirectAttributes.addFlashAttribute(
                "notificacaoSA2",
                new NotificacaoSweetAlert2(
                        "Animal cadastrado com sucesso!",
                        TipoNotificacaoSweetAlert2.SUCCESS,
                        4000));

        redirectAttributes.addFlashAttribute(
                "mensagem",
                "Animal cadastrado com sucesso!");

        return htmxRequest.isHtmxRequest()
                ? "redirect:htmx:location:/animais"
                : "redirect:/animais";
    }

    @GetMapping("/animais/{id}/editar")
    public String abrirFormularioEdicao(
            @PathVariable Long id,
            Model model,
            HtmxRequest htmxRequest) {

        AnimalUpdateRequest request = animalService.buscarParaEdicao(id);

        model.addAttribute("animalUpdateRequest", request);
        model.addAttribute("animalId", id);

        return htmxRequest.isHtmxRequest()
                ? "animais/edit :: formulario"
                : "animais/edit";
    }

    @PutMapping("/animais/{id}")
    public String atualizar(
            @PathVariable Long id,
            @Valid AnimalUpdateRequest request,
            BindingResult resultado,
            Model model,
            RedirectAttributes redirectAttributes,
            HtmxRequest htmxRequest) {

        if (resultado.hasErrors()) {
            log.info("O animal recebido para atualização não é válido.");

            for (FieldError erro : resultado.getFieldErrors()) {
                log.info("{}", erro);
            }

            for (ObjectError erro : resultado.getGlobalErrors()) {
                log.info("{}", erro);
            }

            model.addAttribute("animalUpdateRequest", request);
            model.addAttribute("animalId", id);

            return htmxRequest.isHtmxRequest()
                    ? "animais/edit :: formulario"
                    : "animais/edit";
        }

        animalService.atualizar(id, request);

        redirectAttributes.addFlashAttribute(
                "notificacaoSA2",
                new NotificacaoSweetAlert2(
                        "Animal atualizado com sucesso!",
                        TipoNotificacaoSweetAlert2.SUCCESS,
                        4000));

        redirectAttributes.addFlashAttribute(
                "mensagem",
                "Animal atualizado com sucesso!");

        return htmxRequest.isHtmxRequest()
                ? "redirect:htmx:location:/animais"
                : "redirect:/animais";
    }

    @DeleteMapping("/animais/{id}")
    public String remover(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes,
            HtmxRequest htmxRequest) {

        animalService.remover(id);

        redirectAttributes.addFlashAttribute(
                "notificacaoSA2",
                new NotificacaoSweetAlert2(
                        "Animal removido com sucesso!",
                        TipoNotificacaoSweetAlert2.SUCCESS,
                        4000));

        redirectAttributes.addFlashAttribute(
                "mensagem",
                "Animal removido com sucesso!");

        return htmxRequest.isHtmxRequest()
                ? "redirect:htmx:location:/animais"
                : "redirect:/animais";
    }
}