package com.drystorm.api.controller;

import com.drystorm.api.dto.request.ServiceRequest;
import com.drystorm.api.dto.response.ApiResponse;
import com.drystorm.api.dto.response.ServiceResponse;
import com.drystorm.api.service.ServiceManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/services")
@RequiredArgsConstructor
@Tag(name = "Serviços", description = "Catálogo de serviços da DryStorm")
public class ServiceController {

    private final ServiceManagementService service;

    @GetMapping
    @Operation(summary = "Lista todos os serviços ativos")
    public ResponseEntity<ApiResponse<List<ServiceResponse>>> listAll() {
        return ResponseEntity.ok(ApiResponse.ok(service.findAllActive()));
    }

    @GetMapping("/grouped")
    @Operation(summary = "Lista serviços agrupados por categoria")
    public ResponseEntity<ApiResponse<Map<String, List<ServiceResponse>>>> listGrouped() {
        return ResponseEntity.ok(ApiResponse.ok(service.findGroupedByCategory()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca serviço por ID")
    public ResponseEntity<ApiResponse<ServiceResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(service.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cria um novo serviço")
    public ResponseEntity<ApiResponse<ServiceResponse>> create(
            @Valid @RequestBody ServiceRequest req) {
        ServiceResponse created = service.create(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Serviço criado com sucesso", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Atualiza um serviço")
    public ResponseEntity<ApiResponse<ServiceResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ServiceRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Serviço atualizado", service.update(id, req)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Desativa um serviço (soft delete)")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        service.deactivate(id);
        return ResponseEntity.ok(ApiResponse.ok("Serviço desativado com sucesso", null));
    }

    @PatchMapping("/{id}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Reativa um serviço")
    public ResponseEntity<ApiResponse<ServiceResponse>> reactivate(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Serviço reativado", service.reactivate(id)));
    }
}
