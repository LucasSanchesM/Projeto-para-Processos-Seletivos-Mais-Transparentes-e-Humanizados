package com.unisal.processoseletivoapi.vagas;

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
import org.springframework.web.bind.annotation.RestController;

import com.unisal.processoseletivoapi.vagas.dto.CreateVaga;
import com.unisal.processoseletivoapi.vagas.dto.UpdateVaga;
import com.unisal.processoseletivoapi.vagas.dto.VagaResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/vagas")
public class VagaController {

    private final VagaService vagaService;

    // CONSTRUTOR
    public VagaController(VagaService vagaService) {
        this.vagaService = vagaService;
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

    // LISTAR TODAS AS VAGAS
    @GetMapping
    public ResponseEntity<List<VagaResponse>> findAllVagas() {

        var vagas = vagaService.findAllVagas();

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
}