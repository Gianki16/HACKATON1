package com.example.hackaton1.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesAggregates {
    private Integer totalUnits;
    private Double totalRevenue;
    private String topSku;
    private String topBranch;
    private Integer uniqueSkus;
    private Integer totalSales;
}