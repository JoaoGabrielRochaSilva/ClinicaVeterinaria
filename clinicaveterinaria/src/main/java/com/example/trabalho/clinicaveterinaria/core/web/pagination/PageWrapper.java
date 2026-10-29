package com.example.trabalho.clinicaveterinaria.core.web.pagination;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;

public class PageWrapper<T> {

    private Page<T> pagina;
    private UriComponentsBuilder uriBuilder;
    private int maximoPaginasMostrar = 5;
    private int inicio;
    private int fim;

    public PageWrapper(Page<T> pagina, HttpServletRequest request) {
        this.pagina = pagina;
        String queryString = removeEmptyQueryParams(request.getQueryString());
        String httpURL = request.getRequestURL().toString() + (queryString.isEmpty() ? "" : "?" + queryString);
        this.uriBuilder = UriComponentsBuilder.fromUriString(httpURL);
        definirInicioFimPaginacao();
    }

    private String removeEmptyQueryParams(String queryString) {
        if (queryString == null || queryString.isBlank()) {
            return "";
        }
        return Arrays.stream(queryString.split("&"))
                .filter(parameter -> parameter.contains("=") && !parameter.endsWith("="))
                .collect(Collectors.joining("&"));
    }

    public List<T> getConteudo() {
        return pagina.getContent();
    }

    public boolean isVazia() {
        return pagina.getContent().isEmpty();
    }

    public int getAtual() {
        return pagina.getNumber();
    }

    public boolean isPrimeira() {
        return pagina.isFirst();
    }

    public boolean isUltima() {
        return pagina.isLast();
    }

    public int getNumeroPaginas() {
        return pagina.getTotalPages();
    }

    public int getInicio() {
        return inicio;
    }

    public int getFim() {
        return fim;
    }

    public String urlAtual() {
        return uriBuilder.build(true).encode().toUriString();
    }

    public String urlParaPagina(int pagina) {
        return uriBuilder.replaceQueryParam("page", pagina).build(true).encode().toUriString();
    }

	// Se a propriedade nao tiver uma ordenacao na URL atual, insere uma com a ordem
	// ASC na URL.
	// Se a propriedade tiver uma ordenacao na URL atual, inverte essa ordem na URL.
	// Nos dois casos se existir uma ordenacao anterior ela sera substituida por
	// essa.
	// Do jeito que esta nao permite ordenacao por mais de uma coluna.
    public String urlInvertendoDirecaoOrdem(String propriedade) {
		// Nao podemos usar o mesmo uriBuilder ou entao ao carregar uma pagina o sort
		// inserido em
		// uma coluna alteraria o uriBuilder que depois sera usado nos numeros da
		// paginacao o que
		// colocaria a ordenacao nesses numeros mesmo que nao tenhamos clicado para
		// ordenar ainda
        UriComponentsBuilder uriBuilderOrder = UriComponentsBuilder
                .fromUriString(uriBuilder.build(true).encode().toUriString());
        String valorSort = String.format("%s,%s", propriedade, inverterDirecaoOrdem(propriedade));
        return uriBuilderOrder.replaceQueryParam("sort", valorSort).build(true).encode().toUriString();
    }

	// Caso a propriedade tenha uma ordem o metodo retorna o seu inverso. ASC ->
	// DESC, DESC -> ASC.
	// Caso a propriedade nao tenha uma ordem o metodo retorna ASC.
    private String inverterDirecaoOrdem(String propriedade) {
        String direcao = "asc";
        Sort.Order order = (pagina.getSort() != null) ? pagina.getSort().getOrderFor(propriedade) : null;
        if (order != null) {
            direcao = Sort.Direction.ASC.equals(order.getDirection()) ? "desc" : "asc";
        }
        return direcao;
    }

    public boolean isDescendente(String propriedade) {
        Sort.Order order = (pagina.getSort() != null) ? pagina.getSort().getOrderFor(propriedade) : null;
        return order != null && Sort.Direction.DESC.equals(order.getDirection());
    }

    public boolean isAscendente(String propriedade) {
        Sort.Order order = (pagina.getSort() != null) ? pagina.getSort().getOrderFor(propriedade) : null;
        return order != null && Sort.Direction.ASC.equals(order.getDirection());
    }

    public boolean ordenada(String propriedade) {
        Sort.Order order = (pagina.getSort() != null) ? pagina.getSort().getOrderFor(propriedade) : null;
        return order != null;
    }

    public int getMaximoPaginasMostrar() {
        return maximoPaginasMostrar;
    }

    public void setMaximoPaginasMostrar(int maximoPaginasMostrar) {
        this.maximoPaginasMostrar = maximoPaginasMostrar;
    }

    private void definirInicioFimPaginacao() {
        int metadeMaximoPaginasMostrar = maximoPaginasMostrar / 2;
        int totalDePaginas = pagina.getTotalPages();
        int paginaAtual = pagina.getNumber() + 1;

        inicio = 1;
        if (totalDePaginas == 0) {
            fim = 1;
        } else if (totalDePaginas <= maximoPaginasMostrar) {
            fim = totalDePaginas;
        } else {
            if (paginaAtual <= metadeMaximoPaginasMostrar + 1) {
                fim = maximoPaginasMostrar;
            } else {
                inicio = paginaAtual - metadeMaximoPaginasMostrar;
                if (paginaAtual + metadeMaximoPaginasMostrar <= totalDePaginas) {
                    fim = paginaAtual + metadeMaximoPaginasMostrar;
                } else {
                    fim = totalDePaginas;
                    inicio = totalDePaginas - maximoPaginasMostrar + 1;
                }
            }
        }
    }
}
