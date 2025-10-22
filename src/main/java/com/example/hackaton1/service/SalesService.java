package com.example.hackaton1.service;

import com.example.hackaton1.dto.sales.SaleRequest;
import com.example.hackaton1.dto.sales.SaleResponse;
import com.example.hackaton1.entity.Sale;
import com.example.hackaton1.repository.SalesRepository;
import com.example.hackaton1.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class SalesService {

    private final SalesRepository salesRepository;

    @Transactional
    public SaleResponse createSale(SaleRequest request) {
        UserPrincipal principal = getCurrentUser();

        // Validar permiso de sucursal para BRANCH
        if ("BRANCH".equals(principal.getRole()) && !request.getBranch().equals(principal.getBranch())) {
            throw new AccessDeniedException("You can only create sales for your own branch");
        }

        Sale sale = new Sale();
        sale.setSku(request.getSku());
        sale.setUnits(request.getUnits());
        sale.setPrice(request.getPrice());
        sale.setBranch(request.getBranch());
        sale.setSoldAt(request.getSoldAt());
        sale.setCreatedBy(principal.getUsername());

        sale = salesRepository.save(sale);

        return mapToResponse(sale);
    }

    public SaleResponse getSale(String id) {
        Sale sale = salesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sale not found"));

        UserPrincipal principal = getCurrentUser();

        // Validar permiso de sucursal para BRANCH
        if ("BRANCH".equals(principal.getRole()) && !sale.getBranch().equals(principal.getBranch())) {
            throw new AccessDeniedException("You can only view sales from your own branch");
        }

        return mapToResponse(sale);
    }

    public Page<SaleResponse> listSales(LocalDate from, LocalDate to, String branch, int page, int size) {
        UserPrincipal principal = getCurrentUser();

        // Para BRANCH, forzar su propia sucursal
        if ("BRANCH".equals(principal.getRole())) {
            branch = principal.getBranch();
        }

        Instant fromInstant = from != null ? from.atStartOfDay().toInstant(ZoneOffset.UTC) : Instant.MIN;
        Instant toInstant = to != null ? to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC) : Instant.MAX;

        Pageable pageable = PageRequest.of(page, size, Sort.by("soldAt").descending());

        return salesRepository.findWithFilters(fromInstant, toInstant, branch, pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public SaleResponse updateSale(String id, SaleRequest request) {
        Sale sale = salesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sale not found"));

        UserPrincipal principal = getCurrentUser();

        // Validar permiso de sucursal para BRANCH
        if ("BRANCH".equals(principal.getRole()) && !sale.getBranch().equals(principal.getBranch())) {
            throw new AccessDeniedException("You can only update sales from your own branch");
        }

        sale.setSku(request.getSku());
        sale.setUnits(request.getUnits());
        sale.setPrice(request.getPrice());
        sale.setBranch(request.getBranch());
        sale.setSoldAt(request.getSoldAt());

        sale = salesRepository.save(sale);

        return mapToResponse(sale);
    }

    @Transactional
    public void deleteSale(String id) {
        UserPrincipal principal = getCurrentUser();

        // Solo CENTRAL puede eliminar
        if (!"CENTRAL".equals(principal.getRole())) {
            throw new AccessDeniedException("Only CENTRAL users can delete sales");
        }

        if (!salesRepository.existsById(id)) {
            throw new IllegalArgumentException("Sale not found");
        }

        salesRepository.deleteById(id);
    }

    private SaleResponse mapToResponse(Sale sale) {
        return SaleResponse.builder()
                .id(sale.getId())
                .sku(sale.getSku())
                .units(sale.getUnits())
                .price(sale.getPrice())
                .branch(sale.getBranch())
                .soldAt(sale.getSoldAt())
                .createdBy(sale.getCreatedBy())
                .build();
    }

    private UserPrincipal getCurrentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}