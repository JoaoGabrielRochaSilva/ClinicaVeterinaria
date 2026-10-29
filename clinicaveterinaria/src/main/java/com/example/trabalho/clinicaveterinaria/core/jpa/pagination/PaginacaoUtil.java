package com.example.trabalho.clinicaveterinaria.core.jpa.pagination;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;

public class PaginacaoUtil {

    public static void prepararIntervalo(TypedQuery<?> typedQuery, Pageable pageable) {
        int paginaAtual = pageable.getPageNumber();
        int totalRegistrosPorPagina = pageable.getPageSize();
        int primeiroRegistro = paginaAtual * totalRegistrosPorPagina;
        
        typedQuery.setFirstResult(primeiroRegistro);
        typedQuery.setMaxResults(totalRegistrosPorPagina);
    }
    
    public static void prepararOrdemCriteria(Root<?> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder builder, Pageable pageable) {
        Sort sort = pageable.getSort();
        List<Order> ordenacoes = new ArrayList<>();
        
        if (sort != null && !sort.isEmpty()) {
            for (Sort.Order o : sort) {
                String atributo = o.getProperty();
                Order order = o.isAscending() ? builder.asc(root.get(atributo)) : builder.desc(root.get(atributo));
                ordenacoes.add(order);
            }
        }
        criteriaQuery.orderBy(ordenacoes);
    }

    public static void preencherParametros(Map<String, Object> parametros, TypedQuery<?> typedQuery) {
        for (Map.Entry<String, Object> entry : parametros.entrySet()) {
            typedQuery.setParameter(entry.getKey(), entry.getValue());
        }
    }

    public static long getTotalRegistros(String entidade, String alias, StringBuilder condicoes, Map<String, Object> parametros, EntityManager manager) {
        StringBuilder queryTotal = new StringBuilder("select count(");
        queryTotal.append(alias);
        queryTotal.append(") from ");
        queryTotal.append(entidade);
        queryTotal.append(" ");
        queryTotal.append(alias);
        queryTotal.append(condicoes);

        TypedQuery<Long> typedQueryTotal = manager.createQuery(queryTotal.toString(), Long.class);
        preencherParametros(parametros, typedQueryTotal);

        return typedQueryTotal.getSingleResult();		
    }

    public static void fazerLigacaoCondicoes(StringBuilder condicoes, boolean condicao) {
        if (!condicao) {
            condicoes.append(" where ");
        } else {
            condicoes.append(" and ");
        }
    }
}