package com.example.hackaton1.service;

import com.example.hackaton1.model.SalesAggregates;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;


import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LLMService {

    @Value("${github.models.url}")
    private String githubModelsUrl;

    @Value("${github.models.token}")
    private String githubToken;

    @Value("${github.models.model}")
    private String modelId;

    private final WebClient.Builder webClientBuilder;

    public String generateSummary(SalesAggregates aggregates, LocalDate from, LocalDate to, String branch) {
        try {
            String branchText = (branch != null && !branch.isBlank()) ? " en la sucursal " + branch : " a nivel general";

            String userPrompt = String.format(
                    "Con estos datos de ventas%s entre %s y %s: " +
                            "totalUnits=%d, totalRevenue=%.2f, topSku=%s, topBranch=%s, uniqueSkus=%d, totalSales=%d. " +
                            "Genera un resumen ejecutivo en español de máximo 120 palabras para enviar por email, " +
                            "destacando los puntos clave y tendencias.",
                    branchText, from, to,
                    aggregates.getTotalUnits(),
                    aggregates.getTotalRevenue(),
                    aggregates.getTopSku(),
                    aggregates.getTopBranch(),
                    aggregates.getUniqueSkus(),
                    aggregates.getTotalSales()
            );

            Map<String, Object> requestBody = Map.of(
                    "model", modelId,
                    "messages", List.of(
                            Map.of("role", "system", "content", "Eres un analista de ventas que escribe resúmenes breves, claros y profesionales para emails corporativos."),
                            Map.of("role", "user", "content", userPrompt)
                    ),
                    "max_tokens", 250,
                    "temperature", 0.7
            );

            WebClient webClient = webClientBuilder
                    .baseUrl(githubModelsUrl)
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + githubToken)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();

            Map<String, Object> response = webClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }

            throw new RuntimeException("Invalid response from LLM service");

        } catch (Exception e) {
            log.error("Error calling GitHub Models API", e);
            throw new RuntimeException("LLM service unavailable", e);
        }
    }
}