package com.example.trabalho.clinicaveterinaria.common.exception;

public class RegraDeNegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String campo;

    public RegraDeNegocioException(String campo, String mensagem) {
        super(mensagem);
        this.campo = campo;
    }

    public RegraDeNegocioException(String mensagem) {
        this(null, mensagem);
    }

    public String getCampo() {
        return campo;
    }
}