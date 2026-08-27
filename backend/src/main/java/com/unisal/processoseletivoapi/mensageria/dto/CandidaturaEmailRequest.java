package com.unisal.processoseletivoapi.mensageria.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Payload publicado na fila "candidatura.atualizacao" sempre que a etapa ou o
 * status de uma candidatura muda.
 */
public record CandidaturaEmailRequest(

        @Email(message = "email invalido") @NotBlank(message = "campo email precisa estar preenchido") String email,

        @NotBlank(message = "campo candidatoNome precisa estar preenchido") String candidatoNome,

        @NotBlank(message = "campo vagaTitulo precisa estar preenchido") String vagaTitulo,

        String empresaNome,

        String etapaDescricao,

        String statusDescricao,

        String mensagem) {
}
