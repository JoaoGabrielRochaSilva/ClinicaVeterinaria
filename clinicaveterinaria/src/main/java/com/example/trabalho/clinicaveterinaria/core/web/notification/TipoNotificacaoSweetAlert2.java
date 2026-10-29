package com.example.trabalho.clinicaveterinaria.core.web.notification;

public enum TipoNotificacaoSweetAlert2 {
	
	SUCCESS("success"),
	ERROR("error"),
	WARNING("warning"),
	INFO("info"),
	QUESTION("question");
	
	private String tipo;
	
	private TipoNotificacaoSweetAlert2(String tipo) {
		this.tipo = tipo;
	}
	
	public String getTipo() {
		return tipo;
	}	
}
