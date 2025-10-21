package com.example.hackaton1.dto.sales;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.Instant;

@Data
public class SaleRequest {

    @NotBlank(message = "SKU is required")
    private String sku;

    @NotNull(message = "Units is required")
    @Min(value = 1, message = "Units must be at least 1")
    private Integer units;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private Double price;

    @NotBlank(message = "Branch is required")
    private String branch;

    @NotNull(message = "SoldAt is required")
    private Instant soldAt;
}