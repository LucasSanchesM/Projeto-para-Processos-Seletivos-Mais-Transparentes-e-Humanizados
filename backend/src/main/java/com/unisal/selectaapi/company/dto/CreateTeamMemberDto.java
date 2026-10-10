package com.unisal.selectaapi.company.dto;

import com.unisal.selectaapi.company.entity.CompanyMemberRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTeamMemberDto(
    @NotBlank(message = "O campo 'name' é obrigatório")
    @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres")
    String name,

    @NotBlank(message = "O campo 'email' é obrigatório")
    @Email(message = "O campo 'email' deve ser um e-mail válido")
    String email,

    @NotBlank(message = "O campo 'password' é obrigatório")
    @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres")
    String password,

    @NotNull(message = "O campo 'role' é obrigatório")
    CompanyMemberRole role
) {}
