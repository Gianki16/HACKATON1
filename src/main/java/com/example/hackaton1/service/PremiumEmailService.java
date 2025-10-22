package com.example.hackaton1.service;

import com.example.hackaton1.model.SalesAggregates;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.element.Paragraph;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.itextpdf.layout.Document;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class PremiumEmailService {

    private final JavaMailSender mailSender;

    public void sendPremiumReport(String to, String summary, SalesAggregates aggregates,
                                  LocalDate from, LocalDate toDate, String branch,
                                  boolean includeCharts, boolean attachPdf) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(String.format("🍪 Reporte Premium Oreo - %s a %s", from, to));

            String htmlBody = buildHtmlEmail(summary, aggregates, from, toDate, branch, includeCharts);
            helper.setText(htmlBody, true);

            if (attachPdf) {
                byte[] pdfBytes = generatePdfReport(summary, aggregates, from, toDate, branch);
                helper.addAttachment("reporte_oreo.pdf", new ByteArrayResource(pdfBytes));
            }

            mailSender.send(message);
            log.info("Premium report sent to: {}", to);

        } catch (Exception e) {
            log.error("Error sending premium email to: {}", to, e);
            throw new RuntimeException("Premium email service unavailable", e);
        }
    }

    private String buildHtmlEmail(String summary, SalesAggregates aggregates, LocalDate from, LocalDate to, String branch, boolean includeCharts) {
        String branchText = (branch != null && !branch.isBlank()) ? " - " + branch : "";

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html><head>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; background-color: #f5f5f5; margin: 0; padding: 20px; }");
        html.append(".header { background: linear-gradient(135deg, #6B46C1 0%, #805AD5 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }");
        html.append(".content { background: white; padding: 30px; border-radius: 0 0 10px 10px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }");
        html.append(".metric { display: inline-block; margin: 15px; padding: 20px; background: linear-gradient(135deg, #f0f0f0 0%, #e8e8e8 100%); border-radius: 10px; text-align: center; min-width: 150px; }");
        html.append(".metric h3 { margin: 0; color: #6B46C1; font-size: 14px; }");
        html.append(".metric p { margin: 10px 0 0 0; font-size: 24px; font-weight: bold; color: #2D3748; }");
        html.append(".summary { background: #FFF5E1; padding: 20px; border-left: 4px solid #6B46C1; margin: 20px 0; border-radius: 5px; }");
        html.append(".footer { text-align: center; margin-top: 30px; color: #718096; font-size: 12px; }");
        html.append("</style>");
        html.append("</head><body>");

        html.append("<div class='header'>");
        html.append("<h1>🍪 Reporte Premium Oreo</h1>");
        html.append("<p>").append(from).append(" - ").append(to).append(branchText).append("</p>");
        html.append("</div>");

        html.append("<div class='content'>");

        html.append("<div class='summary'>");
        html.append("<h2>📊 Resumen Ejecutivo</h2>");
        html.append("<p>").append(summary).append("</p>");
        html.append("</div>");

        html.append("<h2 style='color: #6B46C1; margin-top: 30px;'>📈 Métricas Clave</h2>");
        html.append("<div style='text-align: center;'>");

        html.append("<div class='metric'>");
        html.append("<h3>Total Unidades</h3>");
        html.append("<p>").append(String.format("%,d", aggregates.getTotalUnits())).append("</p>");
        html.append("</div>");

        html.append("<div class='metric'>");
        html.append("<h3>Revenue Total</h3>");
        html.append("<p>$").append(String.format("%,.2f", aggregates.getTotalRevenue())).append("</p>");
        html.append("</div>");

        html.append("<div class='metric'>");
        html.append("<h3>SKU Top</h3>");
        html.append("<p style='font-size: 16px;'>").append(aggregates.getTopSku()).append("</p>");
        html.append("</div>");

        html.append("<div class='metric'>");
        html.append("<h3>Sucursal Top</h3>");
        html.append("<p style='font-size: 16px;'>").append(aggregates.getTopBranch()).append("</p>");
        html.append("</div>");

        html.append("</div>");

        if (includeCharts) {
            // Gráfico de barras usando QuickChart
            String chartUrl = String.format(
                    "https://quickchart.io/chart?c={type:'bar',data:{labels:['Unidades','Revenue','SKUs','Ventas'],datasets:[{label:'Métricas',data:[%d,%.0f,%d,%d],backgroundColor:['rgba(107,70,193,0.8)','rgba(128,90,213,0.8)','rgba(159,122,234,0.8)','rgba(180,155,245,0.8)']}]},options:{scales:{y:{beginAtZero:true}}}}",
                    aggregates.getTotalUnits(),
                    aggregates.getTotalRevenue(),
                    aggregates.getUniqueSkus(),
                    aggregates.getTotalSales()
            );

            html.append("<h2 style='color: #6B46C1; margin-top: 30px;'>📊 Visualización</h2>");
            html.append("<div style='text-align: center;'>");
            html.append("<img src='").append(chartUrl).append("' alt='Chart' style='max-width: 100%; height: auto; border-radius: 10px;'/>");
            html.append("</div>");
        }

        html.append("</div>");

        html.append("<div class='footer'>");
        html.append("<p>Oreo Insight Factory - Generado automáticamente con IA</p>");
        html.append("</div>");

        html.append("</body></html>");

        return html.toString();
    }

    private byte[] generatePdfReport(String summary, SalesAggregates aggregates, LocalDate from, LocalDate to, String branch) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            String branchText = (branch != null && !branch.isBlank()) ? " - " + branch : "";

            document.add(new Paragraph("REPORTE OREO INSIGHT FACTORY")
                    .setFontSize(20).setBold());
            document.add(new Paragraph(String.format("Período: %s a %s%s", from, to, branchText))
                    .setFontSize(12));
            document.add(new Paragraph("\n"));

            document.add(new Paragraph("RESUMEN EJECUTIVO").setFontSize(16).setBold());
            document.add(new Paragraph(summary).setFontSize(11));
            document.add(new Paragraph("\n"));

            document.add(new Paragraph("MÉTRICAS CLAVE").setFontSize(16).setBold());
            document.add(new Paragraph(String.format("Total Unidades: %,d", aggregates.getTotalUnits())));
            document.add(new Paragraph(String.format("Revenue Total: $%,.2f", aggregates.getTotalRevenue())));
            document.add(new Paragraph(String.format("SKU Top: %s", aggregates.getTopSku())));
            document.add(new Paragraph(String.format("Sucursal Top: %s", aggregates.getTopBranch())));
            document.add(new Paragraph(String.format("SKUs Únicos: %d", aggregates.getUniqueSkus())));
            document.add(new Paragraph(String.format("Total Ventas: %d", aggregates.getTotalSales())));

            document.close();

            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating PDF", e);
            throw new RuntimeException("PDF generation failed", e);
        }
    }
}
