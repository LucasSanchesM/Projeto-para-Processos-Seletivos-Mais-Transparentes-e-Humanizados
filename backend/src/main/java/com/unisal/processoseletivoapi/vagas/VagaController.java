package com.unisal.processoseletivoapi.vagas;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import com.unisal.processoseletivoapi.vagas.dto.CreateVaga;
import com.unisal.processoseletivoapi.vagas.dto.UpdateVaga;
import com.unisal.processoseletivoapi.vagas.dto.VagaResponse;
import com.unisal.processoseletivoapi.vagas.dto.SearchMetadataRequest;
import com.unisal.processoseletivoapi.vagas.entity.SearchMetadata;
import com.unisal.processoseletivoapi.vagas.repository.SearchMetadataRepository;
import com.unisal.processoseletivoapi.vagas.entity.ModalidadeVaga;
import com.unisal.processoseletivoapi.vagas.services.VagaService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/vagas")
public class VagaController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VagaController.class);
    @org.springframework.beans.factory.annotation.Value("${recommendation.log-details:false}")
    private boolean logDetails;

    private final VagaService vagaService;
    private final SearchMetadataRepository searchMetadataRepository;

    public VagaController(VagaService vagaService, SearchMetadataRepository searchMetadataRepository) {
        this.vagaService = vagaService;
        this.searchMetadataRepository = searchMetadataRepository;
    }

    // CRIAR UMA VAGA
    @PostMapping
    public ResponseEntity<VagaResponse> createVaga(
            @Valid @RequestBody CreateVaga dto) {

        var vaga = vagaService.createVaga(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(vaga);
    }

    // LISTAR TODAS AS VAGAS COM FILTROS OPCIONAIS
    @GetMapping
    public ResponseEntity<List<VagaResponse>> findAllVagas(
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) VagaRole categoria,
            @RequestParam(required = false) ModalidadeVaga modalidade,
            @RequestParam(required = false) BigDecimal salarioMin,
            @RequestParam(required = false) BigDecimal salarioMax,
            @RequestParam(required = false) String cidade,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Boolean ativa,
            @RequestParam(required = false) UUID companyProfileId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @AuthenticationPrincipal Jwt jwt) {

        var vagas = vagaService.findAllWithFilters(
                titulo, categoria, modalidade,
                salarioMin, salarioMax, cidade, estado, ativa, companyProfileId,
                jwt == null ? null : UUID.fromString(jwt.getSubject()), Math.max(0, page), Math.min(Math.max(size, 1), 50)
        );

        log.info("GET /vagas resultado quantidade={} vagasIds={}", vagas.size(),
            vagas.stream().map(vaga -> vaga.id()).toList());
        if (logDetails) log.info("GET /vagas corpoResposta={}",
            com.unisal.processoseletivoapi.vagas.services.RecommendationLog.json(vagas));
        return ResponseEntity.ok(vagas);
    }

    @PostMapping("/search-metadata")
    public ResponseEntity<Void> recordSearchMetadata(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody SearchMetadataRequest request) {
        SearchMetadata metadata = new SearchMetadata();
        metadata.setUserId(jwt == null ? null : UUID.fromString(jwt.getSubject()));
        metadata.setQuery(request.query());
        metadata.setSearchTag(request.searchTag().trim().toLowerCase());
        metadata.setMetadataJson(request.metadataJson());
        searchMetadataRepository.save(metadata);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // LISTAR VAGAS DE UMA EMPRESA ESPECÍFICA
    @GetMapping("/empresa/{companyProfileId}")
    public ResponseEntity<List<VagaResponse>> findVagasByCompany(
            @PathVariable UUID companyProfileId) {

        var vagas = vagaService.findAllByCompany(companyProfileId);

        return ResponseEntity.ok(vagas);
    }

    // BUSCAR UMA VAGA PELO ID
    @GetMapping("/{id}")
    public ResponseEntity<VagaResponse> findVagaById(
            @PathVariable UUID id) {

        var vaga = vagaService.findVagaById(id);

        return ResponseEntity.ok(vaga);
    }

    // ATUALIZAR UMA VAGA
    @PutMapping("/{id}")
    public ResponseEntity<VagaResponse> updateVaga(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateVaga dto) {

        var vaga = vagaService.updateVaga(id, dto);

        return ResponseEntity.ok(vaga);
    }

    // DELETAR UMA VAGA
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVaga(
            @PathVariable UUID id) {

        vagaService.deleteVaga(id);

        return ResponseEntity.noContent().build();
    }
    //ver vags de acordo com distancia
    @GetMapping("/proximas")
    public ResponseEntity<List<VagaResponse>> listarvagasPorKm( @RequestParam @NotNull Double latitude,
                                                                @RequestParam @NotNull Double longitude,
                                                                @RequestParam @NotNull Double raioKm) {
        return ResponseEntity.ok(vagaService.pegarVagasPorRaio(latitude, longitude, raioKm));
    }
    
}
