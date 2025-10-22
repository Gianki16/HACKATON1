package com.example.hackaton1.service;

import com.example.hackaton1.entity.Sale;
import com.example.hackaton1.model.SalesAggregates;
import com.example.hackaton1.repository.SalesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalesAggregationService {

    private final SalesRepository salesRepository;

    public SalesAggregates calculateAggregates(LocalDate from, LocalDate to, String branch) {
        Instant fromInstant = from.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant toInstant = to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        List<Sale> sales;
        if (branch != null && !branch.isBlank()) {
            sales = salesRepository.findByDateRangeAndBranch(fromInstant, toInstant, branch);
        } else {
            sales = salesRepository.findByDateRange(fromInstant, toInstant);
        }

        if (sales.isEmpty()) {
            return SalesAggregates.builder()
                    .totalUnits(0)
                    .totalRevenue(0.0)
                    .topSku("N/A")
                    .topBranch("N/A")
                    .uniqueSkus(0)
                    .totalSales(0)
                    .build();
        }

        int totalUnits = sales.stream().mapToInt(Sale::getUnits).sum();
        double totalRevenue = sales.stream().mapToDouble(s -> s.getUnits() * s.getPrice()).sum();

        // Top SKU por unidades vendidas
        Map<String, Integer> skuUnits = sales.stream()
                .collect(Collectors.groupingBy(Sale::getSku, Collectors.summingInt(Sale::getUnits)));
        String topSku = skuUnits.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

        // Top Branch por ventas totales
        Map<String, Integer> branchSales = sales.stream()
                .collect(Collectors.groupingBy(Sale::getBranch, Collectors.summingInt(Sale::getUnits)));
        String topBranch = branchSales.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

        return SalesAggregates.builder()
                .totalUnits(totalUnits)
                .totalRevenue(Math.round(totalRevenue * 100.0) / 100.0)
                .topSku(topSku)
                .topBranch(topBranch)
                .uniqueSkus(skuUnits.size())
                .totalSales(sales.size())
                .build();
    }
}