package com.example.hackaton1.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
public class ReportRequestedEvent {
    private String requestId;
    private LocalDate from;
    private LocalDate to;
    private String branch;
    private String emailTo;
    private String requestedBy;
    private boolean isPremium;
    private boolean includeCharts;
    private boolean attachPdf;
}
