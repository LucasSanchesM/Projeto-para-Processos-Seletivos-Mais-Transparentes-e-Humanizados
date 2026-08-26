package com.unisal.processoseletivoapi.candidatura.entity;

/**
 * Situação geral da candidatura.
 * A etapa diz "onde" o candidato está, o status diz "como" ele está.
 */
public enum StatusCandidatura {

    EM_ANDAMENTO("Em andamento"),
    APROVADA("Aprovada"),
    REPROVADA("Reprovada"),
    CANCELADA("Cancelada pelo candidato");

    private final String descricao;

    StatusCandidatura(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isFinalizada() {
        return this != EM_ANDAMENTO;
    }
}
