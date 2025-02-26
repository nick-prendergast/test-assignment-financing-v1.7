package lu.crx.financing.services.financing;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lu.crx.financing.entities.Invoice;
import lu.crx.financing.entities.Purchaser;
import lu.crx.financing.repositories.InvoiceRepository;
import lu.crx.financing.repositories.PurchaserRepository;
import lu.crx.financing.services.batch.InvoiceBatchProcessor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceFinancingService {

    private final InvoiceRepository invoiceRepository;
    private final PurchaserRepository purchaserRepository;
    private final InvoiceBatchProcessor invoiceBatchProcessor;

    @Transactional
    public void processInvoiceFinancing() {
        log.info("Financing started");

        List<Invoice> pendingInvoices = fetchPendingInvoices();
        List<Purchaser> availablePurchasers = fetchAvailablePurchasersWithSettings();
        LocalDate financingDate = LocalDate.now();

        int financedInvoiceCount = invoiceBatchProcessor.processInvoiceBatch(pendingInvoices, availablePurchasers, financingDate);

        log.info("Financing completed. Financed {} invoices out of {}",
                financedInvoiceCount, pendingInvoices.size());
    }

    private List<Invoice> fetchPendingInvoices() {
        List<Invoice> invoices = invoiceRepository.findNonFinancedInvoices();
        log.info("Found {} non-financed invoices", invoices.size());
        return invoices;
    }

    private List<Purchaser> fetchAvailablePurchasersWithSettings() {
        List<Purchaser> purchasers = purchaserRepository.findAllWithFinancingSettings();
        log.info("Found {} purchasers", purchasers.size());
        return purchasers;
    }
}