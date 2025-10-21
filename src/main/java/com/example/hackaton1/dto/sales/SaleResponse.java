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
public class SaleResponse {
    private String id;
    private String sku;
    private Integer units;
    private Double price;
    private String branch;
    private Instant soldAt;
    private String createdBy;
}