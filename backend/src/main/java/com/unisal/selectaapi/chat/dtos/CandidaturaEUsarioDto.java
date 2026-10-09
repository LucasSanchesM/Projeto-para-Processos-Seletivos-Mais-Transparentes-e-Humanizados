package com.unisal.selectaapi.chat.dtos;

import com.unisal.selectaapi.candidatura.entity.Candidatura;
import com.unisal.selectaapi.user.entity.User;

public record CandidaturaEUsarioDto(Candidatura candidatura, User user) {
}
