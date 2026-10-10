package com.unisal.selectaapi.company.service;

import com.unisal.selectaapi.company.entity.CompanyMemberRole;
import com.unisal.selectaapi.company.entity.CompanyProfile;
import com.unisal.selectaapi.user.entity.User;

/**
 * Empresa que o usuário logado pode operar e com qual papel.
 * {@code memberRole == null} significa que ele é o proprietário.
 */
public record CompanyAccess(CompanyProfile company, User user, CompanyMemberRole memberRole) {

    public static final String PROPRIETARIO = "PROPRIETARIO";

    public boolean isOwner() {
        return memberRole == null;
    }

    public boolean canManageCompany() {
        return isOwner() || memberRole == CompanyMemberRole.ADMIN;
    }

    public String roleName() {
        return isOwner() ? PROPRIETARIO : memberRole.name();
    }
}
