package com.unisal.selectaapi.config;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.unisal.selectaapi.company.entity.CompanyProfile;
import com.unisal.selectaapi.company.repository.CompanyProfileRepository;
import com.unisal.selectaapi.candidate.entity.CandidateProfile;
import com.unisal.selectaapi.candidate.repository.CandidateProfileRepository;
import com.unisal.selectaapi.candidatura.entity.EtapaProcesso;
import com.unisal.selectaapi.user.UserRepository;
import com.unisal.selectaapi.user.UserRole;
import com.unisal.selectaapi.user.entity.User;
import com.unisal.selectaapi.vagas.VagaRepository;
import com.unisal.selectaapi.vagas.VagaRole;
import com.unisal.selectaapi.vagas.entity.ModalidadeVaga;
import com.unisal.selectaapi.vagas.entity.NivelExperiencia;
import com.unisal.selectaapi.vagas.entity.Vaga;
import com.unisal.selectaapi.vagas.entity.VagaEtapa;

@Configuration
public class DevelopmentSeedConfig {

    @Bean
    CommandLineRunner developmentSeed(
            @Value("${app.seed.enabled:false}") boolean seedEnabled,
            UserRepository userRepository,
            CompanyProfileRepository companyProfileRepository,
            CandidateProfileRepository candidateProfileRepository,
            VagaRepository vagaRepository,
            BCryptPasswordEncoder passwordEncoder) {

        return args -> {
            if (!seedEnabled) {
                return;
            }

            CompanyProfile techNova = company(
                    userRepository, companyProfileRepository, passwordEncoder,
                    "TechNova Solucoes", "rh@technova.dev", "29999999000101",
                    "Tecnologia", "Sao Paulo", "SP",
                    "Empresa de produtos digitais e plataformas web.",
                    -23.5505, -46.6333);

            CompanyProfile ponteVerde = company(
                    userRepository, companyProfileRepository, passwordEncoder,
                    "Ponte Verde Consultoria", "talentos@ponteverde.com.br", "28888888000102",
                    "Consultoria empresarial", "Campinas", "SP",
                    "Consultoria focada em pessoas, processos e crescimento sustentavel.",
                    -22.9056, -47.0608);

            candidate(candidateProfileRepository, userRepository, passwordEncoder,
                    "Ana Souza", "ana.souza@candidatos.dev", "Desenvolvedora Java", "Sao Paulo", "SP");
            candidate(candidateProfileRepository, userRepository, passwordEncoder,
                    "Bruno Lima", "bruno.lima@candidatos.dev", "Analista de projetos", "Campinas", "SP");
            candidate(candidateProfileRepository, userRepository, passwordEncoder,
                    "Carla Mendes", "carla.mendes@candidatos.dev", "Desenvolvedora frontend", "Santos", "SP");

            createJobIfAbsent(vagaRepository, techNova, "Desenvolvedor(a) Backend Java",
                    new BigDecimal("8500.00"),
                    "Desenvolver APIs REST com Spring Boot, PostgreSQL e testes automatizados.",
                    "Vale refeicao, plano de saude, trabalho remoto e horario flexivel.",
                    "Sao Paulo", "SP", -23.5505, -46.6333,
                    VagaRole.DESENVOLVIMENTO_SOFTWARE, ModalidadeVaga.REMOTO, NivelExperiencia.PLENO,
                    List.of(EtapaProcesso.TRIAGEM, EtapaProcesso.TESTE_TECNICO,
                            EtapaProcesso.ENTREVISTA_TECNICA, EtapaProcesso.PROPOSTA));

            createJobIfAbsent(vagaRepository, techNova, "Pessoa Desenvolvedora Frontend",
                    new BigDecimal("6500.00"),
                    "Construir interfaces acessiveis com React, TypeScript e integracao com APIs.",
                    "Vale refeicao, auxilio home office e plano odontologico.",
                    "Sao Paulo", "SP", -23.5505, -46.6333,
                    VagaRole.DESENVOLVIMENTO_SOFTWARE, ModalidadeVaga.HIBRIDO, NivelExperiencia.JUNIOR,
                    List.of(EtapaProcesso.TRIAGEM, EtapaProcesso.ENTREVISTA_RH, EtapaProcesso.TESTE_TECNICO,
                            EtapaProcesso.PROPOSTA));

            createJobIfAbsent(vagaRepository, ponteVerde, "Analista de Projetos",
                    new BigDecimal("5800.00"),
                    "Acompanhar cronogramas, riscos, indicadores e comunicacao com clientes.",
                    "Vale alimentacao, plano de saude e programa de desenvolvimento.",
                    "Campinas", "SP", -22.9056, -47.0608,
                    VagaRole.GESTAO_PROJETOS, ModalidadeVaga.HIBRIDO, NivelExperiencia.PLENO,
                    List.of(EtapaProcesso.TRIAGEM, EtapaProcesso.ENTREVISTA_RH, EtapaProcesso.ENTREVISTA_TECNICA,
                            EtapaProcesso.PROPOSTA));

            createJobIfAbsent(vagaRepository, ponteVerde, "Estagio em Recursos Humanos",
                    new BigDecimal("1800.00"),
                    "Apoiar processos de recrutamento, entrevistas e organizacao de documentos.",
                    "Bolsa auxilio, vale transporte e mentoria.",
                    "Campinas", "SP", -22.9056, -47.0608,
                    VagaRole.RECURSOS_HUMANOS, ModalidadeVaga.PRESENCIAL, NivelExperiencia.ESTAGIO,
                    List.of(EtapaProcesso.TRIAGEM, EtapaProcesso.ENTREVISTA_RH, EtapaProcesso.PROPOSTA));
        };
    }

    private CompanyProfile company(
            UserRepository userRepository,
            CompanyProfileRepository companyProfileRepository,
            BCryptPasswordEncoder passwordEncoder,
            String responsibleName,
            String email,
            String cnpj,
            String industry,
            String city,
            String state,
            String description,
            Double latitude,
            Double longitude) {

        User user = userRepository.findByEmail(email).orElseGet(() -> userRepository.save(new User(
                responsibleName,
                email,
                passwordEncoder.encode("Teste@123"),
                UserRole.COMPANY.name(),
                true)));
        user.setActive(true);
        userRepository.save(user);

                CompanyProfile existingProfile = companyProfileRepository.findByUserId(user.getId()).orElse(null);
                if (existingProfile != null) {
                        if (existingProfile.getLatitude() == null || existingProfile.getLongitude() == null) {
                                existingProfile.setLatitude(latitude);
                                existingProfile.setLongitude(longitude);
                                companyProfileRepository.save(existingProfile);
                        }
                        return existingProfile;
                }

                return companyProfileRepository.save(newCompanyProfile(user, responsibleName, cnpj, industry,
                                city, state, description, latitude, longitude));
        }

        private CompanyProfile newCompanyProfile(
                        User user,
                        String companyName,
                        String cnpj,
                        String industry,
                        String city,
                        String state,
                        String description,
                        Double latitude,
                        Double longitude) {

            CompanyProfile profile = new CompanyProfile(user, companyName);
            profile.setCnpj(cnpj);
            profile.setIndustry(industry);
            profile.setCity(city);
            profile.setState(state);
            profile.setDescription(description);
                        profile.setLatitude(latitude);
                        profile.setLongitude(longitude);
                        return profile;
    }

        private void candidate(
                        CandidateProfileRepository candidateProfileRepository,
                        UserRepository userRepository,
                        BCryptPasswordEncoder passwordEncoder,
                        String name,
                        String email,
                        String headline,
                        String city,
                        String state) {

                User user = userRepository.findByEmail(email).orElseGet(() -> userRepository.save(new User(
                                name,
                                email,
                                passwordEncoder.encode("Candidato@123"),
                                UserRole.CANDIDATE.name(),
                                true)));
                user.setActive(true);
                userRepository.save(user);

                candidateProfileRepository.findByUserId(user.getId()).orElseGet(() -> {
                        CandidateProfile profile = new CandidateProfile(user);
                        profile.setHeadline(headline);
                        profile.setCity(city);
                        profile.setState(state);
                        return candidateProfileRepository.save(profile);
                });
        }

    private void createJobIfAbsent(
            VagaRepository vagaRepository,
            CompanyProfile company,
            String title,
            BigDecimal salary,
            String description,
            String benefits,
            String city,
            String state,
            Double latitude,
            Double longitude,
            VagaRole category,
            ModalidadeVaga modality,
            NivelExperiencia experienceLevel,
            List<EtapaProcesso> etapas) {

        Vaga existingJob = vagaRepository.findByEmpresaId(company.getId()).stream()
                .filter(job -> job.getTitulo().equals(title))
                .findFirst()
                .orElse(null);
        if (existingJob != null) {
            if (existingJob.getEtapas().isEmpty()) {
                existingJob.setEtapas(etapas.stream()
                        .map((etapa) -> new VagaEtapa(existingJob, etapa, etapas.indexOf(etapa) + 1))
                        .toList());
                vagaRepository.save(existingJob);
            }
            return;
        }

        Vaga job = new Vaga();
        job.setTitulo(title);
        job.setSalario(salary);
        job.setDescricao(description);
        job.setBeneficios(benefits);
        job.setEmpresa(company);
        job.setCidade(city);
        job.setEstado(state);
        job.setLatitude(latitude);
        job.setLongitude(longitude);
        job.setCategoria(category);
        job.setModalidade(modality);
        job.setNivelExperiencia(experienceLevel);
        job.setAtiva(true);
        job.setEtapas(etapas.stream()
                .map((etapa) -> new VagaEtapa(job, etapa, etapas.indexOf(etapa) + 1))
                .toList());
        vagaRepository.save(job);
    }
}