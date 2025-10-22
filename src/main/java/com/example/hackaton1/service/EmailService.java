package com.example.hackaton1.service;

import com.example.hackaton1.model.SalesAggregates;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendWeeklySummary(String to, String summary, SalesAggregates aggregates, LocalDate from, LocalDate toDate, String branch) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(String.format("🍪 Reporte Semanal Oreo - %s a %s", from, toDate));

            String branchText = (branch != null && !branch.isBlank()) ? " - " + branch : "";

            String body = String.format(
                    "REPORTE SEMANAL OREO%s\n" +
                            "Período: %s a %s\n\n" +
                            "📊 RESUMEN EJECUTIVO\n" +
                            "%s\n\n" +
                            "📈 MÉTRICAS CLAVE\n" +
                            "━━━━━━━━━━━━━━━━━━━━━━\n" +
                            "• Total Unidades: %,d\n" +
                            "• Revenue Total: $%,.2f\n" +
                            "• SKU Top: %s\n" +
                            "• Sucursal Top: %s\n" +
                            "• SKUs Únicos: %d\n" +
                            "• Total Ventas: %d\n\n" +
                            "━━━━━━━━━━━━━━━━━━━━━━\n" +
                            "Oreo Insight Factory\n" +
                            "Generado automáticamente con IA",
                    branchText, from, toDate,
                    summary,
                    aggregates.getTotalUnits(),
                    aggregates.getTotalRevenue(),
                    aggregates.getTopSku(),
                    aggregates.getTopBranch(),
                    aggregates.getUniqueSkus(),
                    aggregates.getTotalSales()
            );

            message.setText(body);

            mailSender.send(message);
            log.info("Weekly summary sent to: {}", to);

        } catch (Exception e) {
            log.error("Error sending email to: {}", to, e);
            throw new RuntimeException("Email service unavailable", e);
        }
    }
}