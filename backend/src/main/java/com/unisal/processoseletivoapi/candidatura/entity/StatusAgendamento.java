package com.unisal.processoseletivoapi.candidatura.entity;

public enum StatusAgendamento {

    PENDENTE("Pendente de confirmação"),
    CONFIRMADO("Confirmado"),
    RECUSADO("Recusado pelo candidato"),
    CANCELADO("Cancelado pela empresa"),
    REALIZADO("Realizado"),
    NAO_COMPARECEU("Não compareceu");

    private final String descricao;

    StatusAgendamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isFinalizado() {
        return this == CANCELADO || this == RECUSADO || this == REALIZADO || this == NAO_COMPARECEU;
    }
}