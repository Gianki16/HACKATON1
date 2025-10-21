package com.example.hackaton1.dto.sales;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SummaryRequest {
    private LocalDate from;
    private LocalDate to;
    private String branch;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String emailTo;
}