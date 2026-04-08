package com.drystorm.api.service;

import com.drystorm.api.dto.request.AppointmentRequest;
import com.drystorm.api.dto.request.AppointmentStatusRequest;
import com.drystorm.api.dto.response.AppointmentResponse;
import com.drystorm.api.dto.response.AvailableSlotsResponse;
import com.drystorm.api.entity.Appointment;
import com.drystorm.api.entity.Service;
import com.drystorm.api.exception.BusinessException;
import com.drystorm.api.exception.ResourceNotFoundException;
import com.drystorm.api.repository.AppointmentRepository;
import com.drystorm.api.repository.ServiceRepository;
import com.drystorm.api.util.TokenGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ServiceRepository serviceRepository;
    private final EmailService emailService;
    private final TokenGenerator tokenGenerator;

    @Value("${app.business.opening-hour:8}")
    private int openingHour;

    @Value("${app.business.closing-hour:18}")
    private int closingHour;

    @Value("${app.business.slot-interval-minutes:30}")
    private int slotInterval;

    // ─── Criação de agendamento ───────────────────────────────────────────────
    @Transactional
    public AppointmentResponse create(AppointmentRequest req) {
        // 1. Validar serviço
        Service service = serviceRepository.findByIdAndActiveTrue(req.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Serviço", req.getServiceId()));

        // 2. Validar que não é domingo
        validateBusinessDay(req.getAppointmentDate());

        // 3. Calcular horário de término
        LocalTime endTime = req.getAppointmentTime().plusMinutes(service.getDurationMinutes());

        // 4. Validar horário de funcionamento
        validateBusinessHours(req.getAppointmentTime(), endTime);

        // 5. Verificar conflito de horários
        boolean conflict = appointmentRepository.hasConflict(
                req.getAppointmentDate(),
                req.getAppointmentTime(),
                endTime,
                null);

        if (conflict) {
            throw new BusinessException(
                    "Horário indisponível. Já existe um agendamento nesse período.");
        }

        // 6. Criar agendamento
        Appointment appointment = Appointment.builder()
                .clientName(req.getClientName())
                .clientEmail(req.getClientEmail())
                .clientPhone(req.getClientPhone())
                .clientProductNote(req.getClientProductNote())
                .service(service)
                .appointmentDate(req.getAppointmentDate())
                .appointmentTime(req.getAppointmentTime())
                .endTime(endTime)
                .notes(req.getNotes())
                .confirmationToken(tokenGenerator.generate())
                .status(Appointment.AppointmentStatus.PENDING)
                .build();

        Appointment saved = appointmentRepository.save(appointment);

        // 7. Enviar e-mail de confirmação (assíncrono)
        emailService.sendAppointmentConfirmation(saved);

        log.info("Agendamento criado: id={}, cliente={}, data={} {}",
                saved.getId(), saved.getClientName(),
                saved.getAppointmentDate(), saved.getAppointmentTime());

        return AppointmentResponse.from(saved);
    }

    // ─── Horários disponíveis ─────────────────────────────────────────────────
    public AvailableSlotsResponse getAvailableSlots(LocalDate date, Long serviceId) {
        validateBusinessDay(date);

        Service service = serviceRepository.findByIdAndActiveTrue(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço", serviceId));

        List<AvailableSlotsResponse.TimeSlot> slots = new ArrayList<>();
        LocalTime cursor = LocalTime.of(openingHour, 0);
        LocalTime closing = LocalTime.of(closingHour, 0);

        while (cursor.plusMinutes(service.getDurationMinutes()).compareTo(closing) <= 0) {
            LocalTime slotEnd = cursor.plusMinutes(service.getDurationMinutes());
            boolean available = !appointmentRepository.hasConflict(date, cursor, slotEnd, null);

            slots.add(AvailableSlotsResponse.TimeSlot.builder()
                    .startTime(cursor)
                    .endTime(slotEnd)
                    .available(available)
                    .build());

            cursor = cursor.plusMinutes(slotInterval);
        }

        return AvailableSlotsResponse.builder()
                .date(date)
                .serviceId(serviceId)
                .serviceDurationMinutes(service.getDurationMinutes())
                .availableSlots(slots)
                .build();
    }

    // ─── Busca ────────────────────────────────────────────────────────────────
    public AppointmentResponse findById(Long id) {
        return appointmentRepository.findById(id)
                .map(AppointmentResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento", id));
    }

    public List<AppointmentResponse> findByDate(LocalDate date) {
        return appointmentRepository.findByAppointmentDateOrderByAppointmentTime(date)
                .stream().map(AppointmentResponse::from).toList();
    }

    public List<AppointmentResponse> findByPeriod(LocalDate start, LocalDate end,
                                                    Appointment.AppointmentStatus status) {
        if (start.isAfter(end)) throw new BusinessException("Data inicial deve ser antes da data final");
        return appointmentRepository.findByPeriodAndStatus(start, end, status)
                .stream().map(AppointmentResponse::from).toList();
    }

    public List<AppointmentResponse> findByClientEmail(String email) {
        return appointmentRepository
                .findByClientEmailOrderByAppointmentDateDescAppointmentTimeDesc(email)
                .stream().map(AppointmentResponse::from).toList();
    }

    // ─── Atualização de status ────────────────────────────────────────────────
    @Transactional
    public AppointmentResponse updateStatus(Long id, AppointmentStatusRequest req) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento", id));

        validateStatusTransition(appointment.getStatus(), req.getStatus());

        appointment.setStatus(req.getStatus());
        if (req.getNotes() != null) appointment.setNotes(req.getNotes());

        Appointment saved = appointmentRepository.save(appointment);

        if (req.getStatus() == Appointment.AppointmentStatus.CANCELLED) {
            emailService.sendAppointmentCancellation(saved);
        }

        log.info("Status do agendamento {} alterado para {}", id, req.getStatus());
        return AppointmentResponse.from(saved);
    }

    // ─── Confirmação via token ────────────────────────────────────────────────
    @Transactional
    public AppointmentResponse confirmByToken(String token) {
        Appointment appointment = appointmentRepository.findByConfirmationToken(token)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Agendamento não encontrado ou token inválido"));

        if (appointment.getStatus() != Appointment.AppointmentStatus.PENDING) {
            throw new BusinessException("Este agendamento já foi processado.");
        }

        appointment.setStatus(Appointment.AppointmentStatus.CONFIRMED);
        appointment.setConfirmationToken(null);

        log.info("Agendamento {} confirmado pelo cliente", appointment.getId());
        return AppointmentResponse.from(appointmentRepository.save(appointment));
    }

    // ─── Reagendamento ────────────────────────────────────────────────────────
    @Transactional
    public AppointmentResponse reschedule(Long id, AppointmentRequest req) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento", id));

        if (appointment.getStatus() == Appointment.AppointmentStatus.COMPLETED
                || appointment.getStatus() == Appointment.AppointmentStatus.CANCELLED) {
            throw new BusinessException("Não é possível reagendar um agendamento " +
                    appointment.getStatus().name().toLowerCase());
        }

        Service service = serviceRepository.findByIdAndActiveTrue(req.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Serviço", req.getServiceId()));

        validateBusinessDay(req.getAppointmentDate());
        LocalTime endTime = req.getAppointmentTime().plusMinutes(service.getDurationMinutes());
        validateBusinessHours(req.getAppointmentTime(), endTime);

        boolean conflict = appointmentRepository.hasConflict(
                req.getAppointmentDate(), req.getAppointmentTime(), endTime, id);
        if (conflict) {
            throw new BusinessException("Horário indisponível para reagendamento.");
        }

        appointment.setService(service);
        appointment.setAppointmentDate(req.getAppointmentDate());
        appointment.setAppointmentTime(req.getAppointmentTime());
        appointment.setEndTime(endTime);
        appointment.setStatus(Appointment.AppointmentStatus.PENDING);
        if (req.getNotes() != null) appointment.setNotes(req.getNotes());

        Appointment saved = appointmentRepository.save(appointment);
        emailService.sendAppointmentConfirmation(saved);

        log.info("Agendamento {} reagendado para {} {}", id,
                saved.getAppointmentDate(), saved.getAppointmentTime());

        return AppointmentResponse.from(saved);
    }

    // ─── Validações ───────────────────────────────────────────────────────────
    private void validateBusinessDay(LocalDate date) {
        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new BusinessException("Não realizamos atendimentos aos domingos.");
        }
    }

    private void validateBusinessHours(LocalTime start, LocalTime end) {
        LocalTime opening = LocalTime.of(openingHour, 0);
        LocalTime closing = LocalTime.of(closingHour, 0);

        if (start.isBefore(opening)) {
            throw new BusinessException("Horário de início antes da abertura (" + opening + ").");
        }
        if (end.isAfter(closing)) {
            throw new BusinessException(
                    "O serviço ultrapassaria o horário de fechamento (" + closing + ").");
        }
    }

    private void validateStatusTransition(Appointment.AppointmentStatus current,
                                          Appointment.AppointmentStatus next) {
        boolean invalid = switch (current) {
            case COMPLETED, CANCELLED, NO_SHOW -> true;
            case CONFIRMED -> next == Appointment.AppointmentStatus.PENDING;
            default -> false;
        };

        if (invalid) {
            throw new BusinessException(
                    "Transição de status inválida: " + current + " → " + next);
        }
    }
}
