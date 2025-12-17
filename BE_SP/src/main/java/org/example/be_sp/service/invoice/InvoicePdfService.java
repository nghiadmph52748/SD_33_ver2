package org.example.be_sp.service.invoice;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.be_sp.model.response.SanPhamDaBanResponse;
import org.example.be_sp.model.response.ThongTinDonHangResponse;
import org.example.be_sp.model.response.invoice.InvoiceResponse;
import org.example.be_sp.service.ThongTinHoaDonService;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoicePdfService {

    private final InvoiceService invoiceService;
    private final ThongTinHoaDonService thongTinHoaDonService;
    private final TemplateEngine templateEngine;

    public byte[] generateInvoicePdf(Integer invoiceId) {
        InvoiceResponse invoice = invoiceService.getById(invoiceId);
        ThongTinDonHangResponse orderInfo = thongTinHoaDonService.getLatestByHoaDonId(invoiceId);
        List<SanPhamDaBanResponse> soldProducts = thongTinHoaDonService.getSanPhamDaBanByHoaDonId(invoiceId);

        Context context = new Context();
        context.setVariable("invoice", invoice);
        context.setVariable("orderInfo", orderInfo);
        context.setVariable("products", soldProducts);

        String html = templateEngine.process("invoice/invoice-pdf", context);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            // Register Inter fonts from classpath resources
            builder.useFont(() -> InvoicePdfService.class.getResourceAsStream("/fonts/Inter/Inter-VariableFont_opsz,wght.ttf"),
                    "Inter");
            builder.useFont(() -> InvoicePdfService.class.getResourceAsStream("/fonts/Inter/Inter-Italic-VariableFont_opsz,wght.ttf"),
                    "Inter");
            // Base URL so relative resources (logo-datn.png) resolve correctly
            String baseUrl = InvoicePdfService.class.getResource("/templates/invoice/").toExternalForm();
            builder.withHtmlContent(html, baseUrl);
            builder.toStream(baos);
            builder.run();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate invoice PDF for id {}", invoiceId, e);
            throw new RuntimeException("Cannot generate invoice PDF", e);
        }
    }
}


