package com.unisal.selectaapi.company.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unisal.selectaapi.company.entity.CompanyMember;
import com.unisal.selectaapi.company.entity.CompanyProfile;
import com.unisal.selectaapi.company.repository.CompanyMemberRepository;
import com.unisal.selectaapi.company.repository.CompanyProfileRepository;
import com.unisal.selectaapi.exception.BusinessException;
import com.unisal.selectaapi.user.UserRepository;
import com.unisal.selectaapi.user.UserRole;

/**
 * Ponto único que traduz "usuário logado" em "empresa que ele opera".
 * Todo acesso a dados da empresa deve passar por aqui, para que proprietário
 * e membros convidados enxerguem exatamente os mesmos dados.
 */
@Service
public class CompanyAccessService {

    private final CompanyProfileRepository companyProfileRepository;
    private final CompanyMemberRepository memberRepository;
    private final UserRepository userRepository;

    public CompanyAccessService(CompanyProfileRepository companyProfileRepository,
            CompanyMemberRepository memberRepository,
            UserRepository userRepository) {
        this.companyProfileRepository = companyProfileRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public CompanyAccess resolve(UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        if (!UserRole.COMPANY.name().equals(user.getRole())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Acesso restrito a contas empresariais");
        }
        // O JWT vale 15 minutos; checar aqui corta o acesso de quem foi desativado na hora.
        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Seu acesso a esta empresa está desativado");
        }

        var owned = companyProfileRepository.findByUserId(userId);
        if (owned.isPresent()) {
            return new CompanyAccess(owned.get(), user, null);
        }

        CompanyMember member = memberRepository.findWithCompanyByUserId(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "Não existe um perfil de empresa associado a este usuário"));

        return new CompanyAccess(member.getCompany(), user, member.getRole());
    }

    public CompanyProfile resolveCompany(UUID userId) {
        return resolve(userId).company();
    }

    /** Proprietário ou ADMIN: editar perfil da empresa e gerenciar a equipe. */
    public CompanyAccess requireManager(UUID userId) {
        var access = resolve(userId);
        if (!access.canManageCompany()) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "Apenas o proprietário ou administradores da empresa podem fazer isso");
        }
        return access;
    }

    /** Todos os usuários com acesso ativo à empresa (para notificações em tempo real). */
    @Transactional(readOnly = true)
    public List<UUID> activeUserIdsOf(CompanyProfile company) {
        var ids = new ArrayList<UUID>();
        ids.add(company.getUser().getId());
        memberRepository.findAllByCompanyId(company.getId()).stream()
                .filter(member -> Boolean.TRUE.equals(member.getUser().getActive()))
                .forEach(member -> ids.add(member.getUser().getId()));
        return ids;
    }
}
