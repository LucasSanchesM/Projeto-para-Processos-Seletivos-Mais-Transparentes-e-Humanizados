package com.unisal.processoseletivoapi.candidatura.entity;

/**
 * Etapas fixas do processo seletivo.
 * A "ordem" garante que a empresa só consiga avançar o candidato para frente.
 */
public enum EtapaProcesso {

    INSCRICAO(0, "Inscrição realizada"),
    TRIAGEM(1, "Triagem de currículo"),
    ENTREVISTA_RH(2, "Entrevista com RH"),
    TESTE_TECNICO(3, "Teste técnico"),
    ENTREVISTA_TECNICA(4, "Entrevista técnica"),
    PROPOSTA(5, "Proposta"),
    CONTRATACAO(6, "Contratação");

    private final int ordem;
    private final String descricao;

    EtapaProcesso(int ordem, String descricao) {
        this.ordem = ordem;
        this.descricao = descricao;
    }

    public int getOrdem() {
        return ordem;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isDepoisDe(EtapaProcesso outra) {
        return this.ordem > outra.ordem;
    }
}
