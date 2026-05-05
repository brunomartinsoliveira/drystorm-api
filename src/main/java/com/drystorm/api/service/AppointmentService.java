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

    @Value("${app.business.opening-hour:8}")
    private int openingHour;

    @Value("${app.business.closing-hour:18}")
    private int closingHour;

    @Value("${app.business.slot-interval-minutes:30}")
    private int slotInterval;

    // Cria um novo agendamento
    @Transactional
    public AppointmentResponse create(AppointmentRequest req) {
        // 1. Verifica se o serviço existe e está ativo
        Service service = serviceRepository.findByIdAndActiveTrue(req.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Serviço", req.getServiceId()));

        // 2. Valida que não é domingo
        validateBusinessDay(req.getAppointmentDate());

        // 3. Calcula o horário de término com base na duração do serviço
        LocalTime endTime = req.getAppointmentTime().plusMinutes(service.getDurationMinutes());

        // 4. Valida se o horário está dentro do funcionamento da loja
        validateBusinessHours(req.getAppointmentTime(), endTime);

        // 5. Verifica se já existe agendamento nesse horário
        boolean conflict = appointmentRepository.hasConflict(
                req.getAppointmentDate(),
                req.getAppointmentTime(),
                endTime,
                null);

        if (conflict) {
            throw new BusinessException("Horário indisponível. Já existe um agendamento nesse período.");
        }

        // 6. Salva o agendamento já como CONFIRMED
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
                .status(Appointment.AppointmentStatus.CONFIRMED)
                .build();

        Appointment saved = appointmentRepository.save(appointment);

        // 7. Envia e-mail de confirmação para o cliente
        emailService.sendAppointmentConfirmation(saved);

        log.info("Agendamento criado: id={}, cliente={}, data={} {}",
                saved.getId(), saved.getClientName(),
                saved.getAppointmentDate(), saved.getAppointmentTime());

        return AppointmentResponse.from(saved);
    }

    // Retorna os horários disponíveis para uma data e serviço
    public AvailableSlotsResponse getAvailableSlots(LocalDate date, Long serviceId) {
        validateBusinessDay(date);

        Service service = serviceRepository.findByIdAndActiveTrue(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço", serviceId));

        List<AvailableSlotsResponse.TimeSlot> slots = new ArrayList<>();
        LocalTime cursor = LocalTime.of(openingHour, 0);
        LocalTime closing = LocalTime.of(closingHour, 0);

        // Percorre os horários do dia em intervalos fixos e verifica disponibilidade
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

    // Busca agendamento por ID
    public AppointmentResponse findById(Long id) {
        return appointmentRepository.findById(id)
                .map(AppointmentResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento", id));
    }

    // Lista agendamentos de uma data específica
    public List<AppointmentResponse> findByDate(LocalDate date) {
        return appointmentRepository.findByAppointmentDateOrderByAppointmentTime(date)
                .stream().map(AppointmentResponse::from).toList();
    }

    // Lista agendamentos de um período com filtro opcional de status
    public List<AppointmentResponse> findByPeriod(LocalDate start, LocalDate end,
                                                    Appointment.AppointmentStatus status) {
        if (start.isAfter(end)) {
            throw new BusinessException("Data inicial deve ser antes da data final.");
        }
        return appointmentRepository.findByPeriodAndStatus(start, end, status)
                .stream().map(AppointmentResponse::from).toList();
    }

    // Lista todos os agendamentos de um cliente pelo e-mail
    public List<AppointmentResponse> findByClientEmail(String email) {
        return appointmentRepository
                .findByClientEmailOrderByAppointmentDateDescAppointmentTimeDesc(email)
                .stream().map(AppointmentResponse::from).toList();
    }

    // Atualiza o status de um agendamento
    @Transactional
    public AppointmentResponse updateStatus(Long id, AppointmentStatusRequest req) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento", id));

        // Não permite alterar agendamentos que já foram finalizados
        if (appointment.getStatus() == Appointment.AppointmentStatus.COMPLETED
                || appointment.getStatus() == Appointment.AppointmentStatus.CANCELLED) {
            throw new BusinessException(
                    "Não é possível alterar um agendamento com status: "
                    + appointment.getStatus());
        }

        appointment.setStatus(req.getStatus());
        if (req.getNotes() != null) {
            appointment.setNotes(req.getNotes());
        }

        Appointment saved = appointmentRepository.save(appointment);

        // Envia e-mail de cancelamento se o status for CANCELLED
        if (req.getStatus() == Appointment.AppointmentStatus.CANCELLED) {
            emailService.sendAppointmentCancellation(saved);
        }

        log.info("Status do agendamento {} alterado para {}", id, req.getStatus());
        return AppointmentResponse.from(saved);
    }

    // Reagenda um agendamento para nova data e horário
    @Transactional
    public AppointmentResponse reschedule(Long id, AppointmentRequest req) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento", id));

        // Não permite reagendar agendamentos finalizados ou cancelados
        if (appointment.getStatus() == Appointment.AppointmentStatus.COMPLETED
                || appointment.getStatus() == Appointment.AppointmentStatus.CANCELLED) {
            throw new BusinessException(
                    "Não é possível reagendar um agendamento com status: "
                    + appointment.getStatus());
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
        if (req.getNotes() != null) {
            appointment.setNotes(req.getNotes());
        }

        Appointment saved = appointmentRepository.save(appointment);
        emailService.sendAppointmentConfirmation(saved);

        log.info("Agendamento {} reagendado para {} {}",
                id, saved.getAppointmentDate(), saved.getAppointmentTime());

        return AppointmentResponse.from(saved);
    }

    // Valida se a data não é domingo
    private void validateBusinessDay(LocalDate date) {
        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new BusinessException("Não realizamos atendimentos aos domingos.");
        }
    }

    // Valida se o horário está dentro do funcionamento da loja
    private void validateBusinessHours(LocalTime start, LocalTime end) {
        LocalTime opening = LocalTime.of(openingHour, 0);
        LocalTime closing = LocalTime.of(closingHour, 0);

        if (start.isBefore(opening)) {
            throw new BusinessException(
                    "Horário de início antes da abertura (" + opening + ").");
        }
        if (end.isAfter(closing)) {
            throw new BusinessException(
                    "O serviço ultrapassaria o horário de fechamento (" + closing + ").");
        }
    }
}
