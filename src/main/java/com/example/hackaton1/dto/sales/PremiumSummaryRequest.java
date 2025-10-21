package com.example.hackaton1.dto.sales;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PremiumSummaryRequest {
    private LocalDate from;
    private LocalDate to;
    private String branch;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String emailTo;

    private String format = "PREMIUM";
    private Boolean includeCharts = true;
    private Boolean attachPdf = true;
}