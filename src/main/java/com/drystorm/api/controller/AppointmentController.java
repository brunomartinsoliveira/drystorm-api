package com.drystorm.api.controller;

import com.drystorm.api.dto.request.AppointmentRequest;
import com.drystorm.api.dto.request.AppointmentStatusRequest;
import com.drystorm.api.dto.response.ApiResponse;
import com.drystorm.api.dto.response.AppointmentResponse;
import com.drystorm.api.dto.response.AvailableSlotsResponse;
import com.drystorm.api.entity.Appointment;
import com.drystorm.api.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@Tag(name = "Agendamentos", description = "Gestão de agendamentos de serviços")
public class AppointmentController {

    private final AppointmentService appointmentService;

    // ─── Público ──────────────────────────────────────────────────────────────
    @PostMapping
    @Operation(summary = "Cria um novo agendamento (público)")
    public ResponseEntity<ApiResponse<AppointmentResponse>> create(
            @Valid @RequestBody AppointmentRequest req) {
        AppointmentResponse response = appointmentService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(
                        "Agendamento realizado! Verifique seu e-mail para confirmar.", response));
    }

    @GetMapping("/available-slots")
    @Operation(summary = "Lista horários disponíveis para uma data e serviço (público)")
    public ResponseEntity<ApiResponse<AvailableSlotsResponse>> availableSlots(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Long serviceId) {
        return ResponseEntity.ok(
                ApiResponse.ok(appointmentService.getAvailableSlots(date, serviceId)));
    }

    @GetMapping("/confirm/{token}")
    @Operation(summary = "Confirma agendamento via token enviado por e-mail (público)")
    public ResponseEntity<ApiResponse<AppointmentResponse>> confirm(@PathVariable String token) {
        return ResponseEntity.ok(
                ApiResponse.ok("Agendamento confirmado com sucesso!",
                        appointmentService.confirmByToken(token)));
    }

    // ─── Protegido (Admin/Operator) ────────────────────────────────────────────
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Busca agendamento por ID")
    public ResponseEntity<ApiResponse<AppointmentResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(appointmentService.findById(id)));
    }

    @GetMapping("/by-date")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Lista agendamentos por data")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> byDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.ok(appointmentService.findByDate(date)));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Lista agendamentos por período e status")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> byPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false) Appointment.AppointmentStatus status) {
        return ResponseEntity.ok(
                ApiResponse.ok(appointmentService.findByPeriod(start, end, status)));
    }

    @GetMapping("/by-client")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Lista agendamentos de um cliente por e-mail")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> byClient(
            @RequestParam String email) {
        return ResponseEntity.ok(ApiResponse.ok(appointmentService.findByClientEmail(email)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Atualiza o status de um agendamento")
    public ResponseEntity<ApiResponse<AppointmentResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentStatusRequest req) {
        return ResponseEntity.ok(
                ApiResponse.ok("Status atualizado", appointmentService.updateStatus(id, req)));
    }

    @PutMapping("/{id}/reschedule")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Reagenda um agendamento")
    public ResponseEntity<ApiResponse<AppointmentResponse>> reschedule(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentRequest req) {
        return ResponseEntity.ok(
                ApiResponse.ok("Agendamento reagendado com sucesso",
                        appointmentService.reschedule(id, req)));
    }
}
