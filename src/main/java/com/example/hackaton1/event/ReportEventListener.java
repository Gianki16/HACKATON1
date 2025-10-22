package com.example.hackaton1.event;

import com.example.hackaton1.model.SalesAggregates;
import com.example.hackaton1.service.EmailService;
import com.example.hackaton1.service.LLMService;
import com.example.hackaton1.service.PremiumEmailService;
import com.example.hackaton1.service.SalesAggregationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReportEventListener {

    private final SalesAggregationService aggregationService;
    private final LLMService llmService;
    private final EmailService emailService;
    private final PremiumEmailService premiumEmailService;

    @Async
    @EventListener
    public void handleReportRequest(ReportRequestedEvent event) {
        log.info("Processing report request: {}", event.getRequestId());

        try {
            // 1. Calcular agregados
            LocalDate from = event.getFrom();
            LocalDate to = event.getTo();

            SalesAggregates aggregates = aggregationService.calculateAggregates(from, to, event.getBranch());

            // 2. Generar resumen con LLM
            String summary = llmService.generateSummary(aggregates, from, to, event.getBranch());

            // 3. Enviar email
            if (event.isPremium()) {
                premiumEmailService.sendPremiumReport(
                        event.getEmailTo(),
                        summary,
                        aggregates,
                        from,
                        to,
                        event.getBranch(),
                        event.isIncludeCharts(),
                        event.isAttachPdf()
                );
            } else {
                emailService.sendWeeklySummary(
                        event.getEmailTo(),
                        summary,
                        aggregates,
                        from,
                        to,
                        event.getBranch()
                );
            }

            log.info("Report request completed successfully: {}", event.getRequestId());

        } catch (Exception e) {
            log.error("Error processing report request: {}", event.getRequestId(), e);
        }
    }
}
