package com.drystorm.api.service;

import com.drystorm.api.entity.Appointment;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("pt", "BR"));
    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm");

    // ─── E-mail de confirmação para o cliente ─────────────────────────────────
    @Async
    public void sendAppointmentConfirmation(Appointment appointment) {
        try {
            Context ctx = buildContext(appointment);
            ctx.setVariable("confirmUrl",
                    frontendUrl + "/confirmar/" + appointment.getConfirmationToken());

            String html = templateEngine.process("email/appointment-confirmation", ctx);
            send(appointment.getClientEmail(),
                    "✅ Agendamento Recebido - DryStorm", html);

            log.info("E-mail de confirmação enviado para: {}", appointment.getClientEmail());
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail de confirmação para {}: {}",
                    appointment.getClientEmail(), e.getMessage());
        }
    }

    // ─── E-mail de lembrete (D-1) ─────────────────────────────────────────────
    @Async
    public void sendAppointmentReminder(Appointment appointment) {
        try {
            Context ctx = buildContext(appointment);
            String html = templateEngine.process("email/appointment-reminder", ctx);
            send(appointment.getClientEmail(),
                    "⏰ Lembrete do seu Agendamento - DryStorm", html);

            log.info("E-mail de lembrete enviado para: {}", appointment.getClientEmail());
        } catch (Exception e) {
            log.error("Falha ao enviar lembrete para {}: {}",
                    appointment.getClientEmail(), e.getMessage());
        }
    }

    // ─── E-mail de cancelamento ───────────────────────────────────────────────
    @Async
    public void sendAppointmentCancellation(Appointment appointment) {
        try {
            Context ctx = buildContext(appointment);
            String html = templateEngine.process("email/appointment-cancellation", ctx);
            send(appointment.getClientEmail(),
                    "❌ Agendamento Cancelado - DryStorm", html);

            log.info("E-mail de cancelamento enviado para: {}", appointment.getClientEmail());
        } catch (Exception e) {
            log.error("Falha ao enviar cancelamento para {}: {}",
                    appointment.getClientEmail(), e.getMessage());
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────
    private Context buildContext(Appointment appointment) {
        Context ctx = new Context(new Locale("pt", "BR"));
        ctx.setVariable("clientName", appointment.getClientName());
        ctx.setVariable("serviceName", appointment.getService().getName());
        ctx.setVariable("servicePrice", appointment.getService().getPrice());
        ctx.setVariable("productNote", appointment.getClientProductNote());
        ctx.setVariable("date", appointment.getAppointmentDate().format(DATE_FMT));
        ctx.setVariable("time", appointment.getAppointmentTime().format(TIME_FMT));
        ctx.setVariable("endTime", appointment.getEndTime().format(TIME_FMT));
        ctx.setVariable("appointmentId", appointment.getId());
        ctx.setVariable("frontendUrl", frontendUrl);
        return ctx;
    }

    private void send(String to, String subject, String html) throws MessagingException, UnsupportedEncodingException {
        MimeMessage msg = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
        helper.setFrom(from, fromName);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);
        mailSender.send(msg);
    }
}
