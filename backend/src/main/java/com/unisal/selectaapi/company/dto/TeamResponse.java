package com.unisal.selectaapi.company.dto;

import java.util.List;

/** {@code meuPapel} e {@code podeGerenciar} permitem ao front esconder ações sem permissão. */
public record TeamResponse(
    String meuPapel,
    boolean podeGerenciar,
    List<TeamMemberResponse> membros
) {}
