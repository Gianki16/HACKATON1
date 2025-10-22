package com.example.hackaton1.controller;

import com.example.hackaton1.dto.sales.*;
import com.example.hackaton1.event.ReportRequestedEvent;
import com.example.hackaton1.security.UserPrincipal;
import com.example.hackaton1.service.SalesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/sales")
@RequiredArgsConstructor
public class SalesController {

    private final SalesService salesService;
    private final ApplicationEventPublisher eventPublisher;

    @PostMapping
    @PreAuthorize("hasAnyRole('CENTRAL', 'BRANCH')")
    public ResponseEntity<SaleResponse> createSale(@Valid @RequestBody SaleRequest request) {
        SaleResponse response = salesService.createSale(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CENTRAL', 'BRANCH')")
    public ResponseEntity<SaleResponse> getSale(@PathVariable String id) {
        SaleResponse response = salesService.getSale(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CENTRAL', 'BRANCH')")
    public ResponseEntity<Page<SaleResponse>> listSales(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String branch,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<SaleResponse> response = salesService.listSales(from, to, branch, page, size);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CENTRAL', 'BRANCH')")
    public ResponseEntity<SaleResponse> updateSale(@PathVariable String id, @Valid @RequestBody SaleRequest request) {
        SaleResponse response = salesService.updateSale(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CENTRAL')")
    public ResponseEntity<Void> deleteSale(@PathVariable String id) {
        salesService.deleteSale(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/summary/weekly")
    @PreAuthorize("hasAnyRole('CENTRAL', 'BRANCH')")
    public ResponseEntity<SummaryResponse> requestWeeklySummary(
            @Valid @RequestBody SummaryRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        // Validar que BRANCH solo pida su propia sucursal
        if ("BRANCH".equals(principal.getRole())) {
            if (request.getBranch() != null && !request.getBranch().equals(principal.getBranch())) {
                throw new AccessDeniedException("You can only request reports for your own branch");
            }
            request.setBranch(principal.getBranch());
        }

        // Calcular fechas si no se proporcionan
        LocalDate to = request.getTo() != null ? request.getTo() : LocalDate.now();
        LocalDate from = request.getFrom() != null ? request.getFrom() : to.minusDays(7);

        String requestId = "req_" + UUID.randomUUID().toString().substring(0, 8);

        // Publicar evento para procesamiento asíncrono
        ReportRequestedEvent event = ReportRequestedEvent.builder()
                .requestId(requestId)
                .from(from)
                .to(to)
                .branch(request.getBranch())
                .emailTo(request.getEmailTo())
                .requestedBy(principal.getUsername())
                .isPremium(false)
                .build();

        eventPublisher.publishEvent(event);

        SummaryResponse response = SummaryResponse.builder()
                .requestId(requestId)
                .status("PROCESSING")
                .message(String.format("Su solicitud de reporte está siendo procesada. Recibirá el resumen en %s en unos momentos.", request.getEmailTo()))
                .estimatedTime("30-60 segundos")
                .requestedAt(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @PostMapping("/summary/weekly/premium")
    @PreAuthorize("hasAnyRole('CENTRAL', 'BRANCH')")
    public ResponseEntity<SummaryResponse> requestPremiumSummary(
            @Valid @RequestBody PremiumSummaryRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        // Validar que BRANCH solo pida su propia sucursal
        if ("BRANCH".equals(principal.getRole())) {
            if (request.getBranch() != null && !request.getBranch().equals(principal.getBranch())) {
                throw new AccessDeniedException("You can only request reports for your own branch");
            }
            request.setBranch(principal.getBranch());
        }

        // Calcular fechas si no se proporcionan
        LocalDate to = request.getTo() != null ? request.getTo() : LocalDate.now();
        LocalDate from = request.getFrom() != null ? request.getFrom() : to.minusDays(7);

        String requestId = "req_premium_" + UUID.randomUUID().toString().substring(0, 8);

        // Publicar evento para procesamiento asíncrono
        ReportRequestedEvent event = ReportRequestedEvent.builder()
                .requestId(requestId)
                .from(from)
                .to(to)
                .branch(request.getBranch())
                .emailTo(request.getEmailTo())
                .requestedBy(principal.getUsername())
                .isPremium(true)
                .includeCharts(request.getIncludeCharts())
                .attachPdf(request.getAttachPdf())
                .build();

        eventPublisher.publishEvent(event);

        SummaryResponse response = SummaryResponse.builder()
                .requestId(requestId)
                .status("PROCESSING")
                .message("Su reporte premium está siendo generado. Incluirá gráficos y PDF adjunto.")
                .estimatedTime("60-90 segundos")
                .requestedAt(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}