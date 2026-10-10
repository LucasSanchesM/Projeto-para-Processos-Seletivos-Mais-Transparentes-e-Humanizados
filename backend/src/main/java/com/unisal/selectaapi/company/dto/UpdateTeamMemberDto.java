package com.unisal.selectaapi.company.dto;

import com.unisal.selectaapi.company.entity.CompanyMemberRole;

import jakarta.validation.constraints.Size;

/** Todos os campos são opcionais: só o que vier preenchido é alterado. */
public record UpdateTeamMemberDto(
    CompanyMemberRole role,
    Boolean active,

    @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres")
    String password
) {}
