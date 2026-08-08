package com.unisal.processoseletivoapi.mensageria.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailSendRequest(
    String token, 
    @Email(message = "email invalido") @NotBlank(message = "campo email precisa estar preenchido")
    String email){

}
