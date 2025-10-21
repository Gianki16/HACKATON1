package com.example.hackaton1.dto.sales;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummaryResponse {
    private String requestId;
    private String status;
    private String message;
    private String estimatedTime;
    private Instant requestedAt;
}