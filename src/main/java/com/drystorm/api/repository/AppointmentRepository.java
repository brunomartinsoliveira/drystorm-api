package com.drystorm.api.repository;

import com.drystorm.api.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // ─── Busca por data ──────────────────────────────────────────────────────
    List<Appointment> findByAppointmentDateOrderByAppointmentTime(LocalDate date);

    List<Appointment> findByAppointmentDateBetweenOrderByAppointmentDateAscAppointmentTimeAsc(
            LocalDate start, LocalDate end);

    // ─── Busca por status ────────────────────────────────────────────────────
    List<Appointment> findByStatusOrderByAppointmentDateAscAppointmentTimeAsc(
            Appointment.AppointmentStatus status);

    // ─── Detecção de conflito de horário ─────────────────────────────────────
    /**
     * Verifica se existe algum agendamento que conflite com o novo slot.
     * Um conflito ocorre quando o novo agendamento se sobrepõe a um existente.
     * Condição: novoInicio < fimExistente AND novoFim > inicioExistente
     */
    @Query("""
        SELECT COUNT(a) > 0 FROM Appointment a
        WHERE a.appointmentDate = :date
          AND a.status NOT IN ('CANCELLED', 'NO_SHOW')
          AND a.appointmentTime < :endTime
          AND a.endTime > :startTime
          AND (:excludeId IS NULL OR a.id <> :excludeId)
    """)
    boolean hasConflict(
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId);

    // ─── Horários ocupados em um dia ─────────────────────────────────────────
    @Query("""
        SELECT a FROM Appointment a
        WHERE a.appointmentDate = :date
          AND a.status NOT IN ('CANCELLED', 'NO_SHOW')
        ORDER BY a.appointmentTime
    """)
    List<Appointment> findActiveByDate(@Param("date") LocalDate date);

    // ─── Token de confirmação ────────────────────────────────────────────────
    Optional<Appointment> findByConfirmationToken(String token);

    // ─── Busca por cliente ───────────────────────────────────────────────────
    List<Appointment> findByClientEmailOrderByAppointmentDateDescAppointmentTimeDesc(String email);

    // ─── Relatório ───────────────────────────────────────────────────────────
    @Query("""
        SELECT COUNT(a) FROM Appointment a
        WHERE a.appointmentDate = :date
          AND a.status NOT IN ('CANCELLED', 'NO_SHOW')
    """)
    long countActiveByDate(@Param("date") LocalDate date);

    @Query("""
        SELECT a FROM Appointment a
        WHERE a.appointmentDate >= :startDate
          AND a.appointmentDate <= :endDate
          AND (:status IS NULL OR a.status = :status)
        ORDER BY a.appointmentDate, a.appointmentTime
    """)
    List<Appointment> findByPeriodAndStatus(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") Appointment.AppointmentStatus status);
}
