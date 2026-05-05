package com.drystorm.api.repository;

import com.drystorm.api.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Busca agendamentos de uma data específica, ordenados por horário
    List<Appointment> findByAppointmentDateOrderByAppointmentTime(LocalDate date);

    // Busca agendamentos de um cliente pelo e-mail
    List<Appointment> findByClientEmailOrderByAppointmentDateDescAppointmentTimeDesc(String email);

    // Verifica se existe conflito de horário em uma data
    // Conflito ocorre quando: novoInicio < fimExistente E novoFim > inicioExistente
    @Query("""
        SELECT COUNT(a) > 0 FROM Appointment a
        WHERE a.appointmentDate = :date
          AND a.status <> 'CANCELLED'
          AND a.appointmentTime < :endTime
          AND a.endTime > :startTime
          AND (:excludeId IS NULL OR a.id <> :excludeId)
    """)
    boolean hasConflict(
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId);

    // Busca agendamentos por período com filtro opcional de status
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
