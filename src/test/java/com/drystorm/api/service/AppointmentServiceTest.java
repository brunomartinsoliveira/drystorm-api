package com.drystorm.api.service;

import com.drystorm.api.dto.request.AppointmentRequest;
import com.drystorm.api.dto.response.AppointmentResponse;
import com.drystorm.api.entity.Appointment;
import com.drystorm.api.entity.Service;
import com.drystorm.api.exception.BusinessException;
import com.drystorm.api.repository.AppointmentRepository;
import com.drystorm.api.repository.ServiceRepository;
import com.drystorm.api.util.TokenGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentService - Testes Unitários")
class AppointmentServiceTest {

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private ServiceRepository serviceRepository;
    @Mock private EmailService emailService;
    @Mock private TokenGenerator tokenGenerator;

    @InjectMocks
    private AppointmentService appointmentService;

    private Service mockService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(appointmentService, "openingHour", 8);
        ReflectionTestUtils.setField(appointmentService, "closingHour", 18);
        ReflectionTestUtils.setField(appointmentService, "slotInterval", 30);

        mockService = Service.builder()
                .id(1L)
                .name("Camiseta Dryfit Training")
                .price(new BigDecimal("79.90"))
                .durationMinutes(30)
                .category(Service.ServiceCategory.CAMISETAS)
                .active(true)
                .build();
    }

    // ─── create() ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Deve criar agendamento com sucesso")
    void create_success() {
        AppointmentRequest req = buildRequest(
                LocalDate.now().plusDays(1), LocalTime.of(9, 0));

        given(serviceRepository.findByIdAndActiveTrue(1L))
                .willReturn(Optional.of(mockService));
        given(appointmentRepository.hasConflict(any(), any(), any(), any()))
                .willReturn(false);
        given(tokenGenerator.generate()).willReturn("token-abc");

        Appointment saved = buildSavedAppointment(req);
        given(appointmentRepository.save(any())).willReturn(saved);

        AppointmentResponse response = appointmentService.create(req);

        assertThat(response).isNotNull();
        assertThat(response.getClientName()).isEqualTo("João Silva");
        assertThat(response.getEndTime()).isEqualTo(LocalTime.of(9, 30));

        then(emailService).should().sendAppointmentConfirmation(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando houver conflito de horário")
    void create_conflictThrowsException() {
        AppointmentRequest req = buildRequest(
                LocalDate.now().plusDays(1), LocalTime.of(9, 0));

        given(serviceRepository.findByIdAndActiveTrue(1L))
                .willReturn(Optional.of(mockService));
        given(appointmentRepository.hasConflict(any(), any(), any(), any()))
                .willReturn(true);

        assertThatThrownBy(() -> appointmentService.create(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Horário indisponível");
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar agendar no domingo")
    void create_sundayThrowsException() {
        LocalDate sunday = LocalDate.now().with(java.time.DayOfWeek.SUNDAY);
        if (sunday.isBefore(LocalDate.now())) sunday = sunday.plusWeeks(1);

        AppointmentRequest req = buildRequest(sunday, LocalTime.of(9, 0));
        given(serviceRepository.findByIdAndActiveTrue(1L))
                .willReturn(Optional.of(mockService));

        assertThatThrownBy(() -> appointmentService.create(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("domingo");
    }

    @Test
    @DisplayName("Deve lançar exceção quando serviço ultrapassa horário de fechamento")
    void create_exceedsClosingHourThrowsException() {
        AppointmentRequest req = buildRequest(
                LocalDate.now().plusDays(1), LocalTime.of(17, 31));

        given(serviceRepository.findByIdAndActiveTrue(1L))
                .willReturn(Optional.of(mockService));

        assertThatThrownBy(() -> appointmentService.create(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("fechamento");
    }

    // ─── confirmByToken() ─────────────────────────────────────────────────────
    @Test
    @DisplayName("Deve confirmar agendamento via token com sucesso")
    void confirmByToken_success() {
        Appointment pending = buildSavedAppointment(
                buildRequest(LocalDate.now().plusDays(1), LocalTime.of(9, 0)));
        pending.setStatus(Appointment.AppointmentStatus.PENDING);
        pending.setConfirmationToken("valid-token");

        given(appointmentRepository.findByConfirmationToken("valid-token"))
                .willReturn(Optional.of(pending));
        given(appointmentRepository.save(any())).willReturn(pending);

        AppointmentResponse response = appointmentService.confirmByToken("valid-token");

        assertThat(response).isNotNull();
        assertThat(pending.getStatus()).isEqualTo(Appointment.AppointmentStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Deve lançar exceção para token inválido")
    void confirmByToken_invalidToken() {
        given(appointmentRepository.findByConfirmationToken("bad-token"))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.confirmByToken("bad-token"))
                .isInstanceOf(com.drystorm.api.exception.ResourceNotFoundException.class);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────
    private AppointmentRequest buildRequest(LocalDate date, LocalTime time) {
        AppointmentRequest req = new AppointmentRequest();
        req.setClientName("João Silva");
        req.setClientEmail("joao@email.com");
        req.setClientPhone("(83) 99999-9999");
        req.setClientProductNote("Camiseta P, cor azul");
        req.setServiceId(1L);
        req.setAppointmentDate(date);
        req.setAppointmentTime(time);
        return req;
    }

    private Appointment buildSavedAppointment(AppointmentRequest req) {
        return Appointment.builder()
                .id(1L)
                .clientName(req.getClientName())
                .clientEmail(req.getClientEmail())
                .clientPhone(req.getClientPhone())
                .clientProductNote(req.getClientProductNote())
                .service(mockService)
                .appointmentDate(req.getAppointmentDate())
                .appointmentTime(req.getAppointmentTime())
                .endTime(req.getAppointmentTime().plusMinutes(mockService.getDurationMinutes()))
                .status(Appointment.AppointmentStatus.PENDING)
                .confirmationToken("token-abc")
                .build();
    }
}
