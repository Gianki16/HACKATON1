package com.example.hackaton1.service;

import com.example.hackaton1.entity.Sale;
import com.example.hackaton1.model.SalesAggregates;
import com.example.hackaton1.repository.SalesRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
class SalesAggregationServiceTest {

    @Mock
    private SalesRepository salesRepository;

    @InjectMocks
    private SalesAggregationService salesAggregationService;

    private Sale createSale(String sku, int units, double price, String branch) {
        Sale sale = new Sale();
        sale.setId("test_" + System.nanoTime());
        sale.setSku(sku);
        sale.setUnits(units);
        sale.setPrice(price);
        sale.setBranch(branch);
        sale.setSoldAt(Instant.now());
        sale.setCreatedBy("test");
        return sale;
    }

    @Test
    void shouldCalculateCorrectAggregatesWithValidData() {
        // Given
        List<Sale> mockSales = List.of(
                createSale("OREO_CLASSIC", 10, 1.99, "Miraflores"),
                createSale("OREO_DOUBLE", 5, 2.49, "San Isidro"),
                createSale("OREO_CLASSIC", 15, 1.99, "Miraflores")
        );
        when(salesRepository.findByDateRange(any(), any())).thenReturn(mockSales);

        // When
        SalesAggregates result = salesAggregationService.calculateAggregates(
                LocalDate.now().minusDays(7), LocalDate.now(), null
        );

        // Then
        assertThat(result.getTotalUnits()).isEqualTo(30);
        assertThat(result.getTotalRevenue()).isEqualTo(42.43);
        assertThat(result.getTopSku()).isEqualTo("OREO_CLASSIC");
        assertThat(result.getTopBranch()).isEqualTo("Miraflores");
        assertThat(result.getUniqueSkus()).isEqualTo(2);
        assertThat(result.getTotalSales()).isEqualTo(3);
    }

    @Test
    void shouldHandleEmptyList() {
        // Given
        when(salesRepository.findByDateRange(any(), any())).thenReturn(new ArrayList<>());

        // When
        SalesAggregates result = salesAggregationService.calculateAggregates(
                LocalDate.now().minusDays(7), LocalDate.now(), null
        );

        // Then
        assertThat(result.getTotalUnits()).isEqualTo(0);
        assertThat(result.getTotalRevenue()).isEqualTo(0.0);
        assertThat(result.getTopSku()).isEqualTo("N/A");
        assertThat(result.getTopBranch()).isEqualTo("N/A");
    }

    @Test
    void shouldFilterByBranch() {
        // Given
        List<Sale> mockSales = List.of(
                createSale("OREO_CLASSIC", 10, 1.99, "Miraflores"),
                createSale("OREO_DOUBLE", 15, 2.49, "Miraflores")
        );
        when(salesRepository.findByDateRangeAndBranch(any(), any(), any())).thenReturn(mockSales);

        // When
        SalesAggregates result = salesAggregationService.calculateAggregates(
                LocalDate.now().minusDays(7), LocalDate.now(), "Miraflores"
        );

        // Then
        assertThat(result.getTotalUnits()).isEqualTo(25);
        assertThat(result.getTopBranch()).isEqualTo("Miraflores");
    }

    @Test
    void shouldCalculateCorrectDateRange() {
        // Given
        LocalDate from = LocalDate.of(2025, 9, 1);
        LocalDate to = LocalDate.of(2025, 9, 7);

        List<Sale> mockSales = List.of(
                createSale("OREO_CLASSIC", 20, 1.99, "Miraflores")
        );
        when(salesRepository.findByDateRange(any(), any())).thenReturn(mockSales);

        // When
        SalesAggregates result = salesAggregationService.calculateAggregates(from, to, null);

        // Then
        assertThat(result.getTotalUnits()).isEqualTo(20);
        assertThat(result.getTotalRevenue()).isEqualTo(39.8);
    }

    @Test
    void shouldIdentifyTopSkuWithTies() {
        // Given - Ambos SKUs tienen 10 unidades, pero OREO_DOUBLE aparece primero
        List<Sale> mockSales = List.of(
                createSale("OREO_DOUBLE", 10, 2.49, "Miraflores"),
                createSale("OREO_CLASSIC", 10, 1.99, "San Isidro")
        );
        when(salesRepository.findByDateRange(any(), any())).thenReturn(mockSales);

        // When
        SalesAggregates result = salesAggregationService.calculateAggregates(
                LocalDate.now().minusDays(7), LocalDate.now(), null
        );

        // Then - Cualquiera de los dos es válido en caso de empate
        assertThat(result.getTopSku()).isIn("OREO_DOUBLE", "OREO_CLASSIC");
        assertThat(result.getTotalUnits()).isEqualTo(20);
    }
}
