package lu.crx.financing.services;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lu.crx.financing.entities.FinancingAmounts;
import lu.crx.financing.entities.Invoice;
import lu.crx.financing.entities.InvoiceFinancingDetails;
import lu.crx.financing.entities.Purchaser;
import lu.crx.financing.entities.PurchaserFinancingResult;
import lu.crx.financing.repositories.InvoiceFinancingDetailsRepository;
import lu.crx.financing.repositories.InvoiceRepository;
import lu.crx.financing.repositories.PurchaserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceFinancingService {

    private final InvoiceRepository invoiceRepository;
    private final PurchaserRepository purchaserRepository;
    private final InvoiceFinancingDetailsRepository financingDetailsRepository;
    private final EntityManager entityManager;
    private final PurchaserSelectionService purchaserSelectionService;
    private final FinancingCalculationService calculationService;

    private static final int MEMORY_FLUSH_BATCH_SIZE = 1000;

    @Transactional
    public void processInvoiceFinancing() {
        log.info("Financing started");

        List<Invoice> pendingInvoices = fetchPendingInvoices();
        List<Purchaser> availablePurchasers = fetchAvailablePurchasersWithSettings();
        LocalDate financingDate = LocalDate.now();

        int financedInvoiceCount = processInvoiceBatch(pendingInvoices, availablePurchasers, financingDate);

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

    private int processInvoiceBatch(List<Invoice> pendingInvoices, List<Purchaser> availablePurchasers, LocalDate financingDate) {
        int financedInvoiceCount = 0;

        for (Invoice invoice : pendingInvoices) {
            boolean financingApplied = attemptToFinanceInvoice(invoice, availablePurchasers, financingDate);
            if (financingApplied) {
                financedInvoiceCount++;
            }

            if (shouldFlushMemory(financedInvoiceCount)) {
                flushAndClearEntityManager(financedInvoiceCount);
            }
        }

        return financedInvoiceCount;
    }

    private boolean shouldFlushMemory(int processedCount) {
        return processedCount % MEMORY_FLUSH_BATCH_SIZE == 0 && processedCount > 0;
    }

    private boolean attemptToFinanceInvoice(Invoice invoice, List<Purchaser> availablePurchasers, LocalDate financingDate) {
        Optional<PurchaserFinancingResult> bestPurchaserMatch =
                purchaserSelectionService.findBestPurchaser(invoice, availablePurchasers, financingDate);

        if (bestPurchaserMatch.isPresent()) {
            applyFinancing(invoice, bestPurchaserMatch.get());
            return true;
        } else {
            log.debug("No eligible purchaser found for invoice {}", invoice.getId());
            return false;
        }
    }

    private void applyFinancing(Invoice invoice, PurchaserFinancingResult purchaserMatch) {
        FinancingAmounts amounts = calculateFinancingAmounts(invoice, purchaserMatch);

        updateInvoice(invoice, amounts);
        Invoice savedInvoice = invoiceRepository.save(invoice);

        saveFinancingDetails(savedInvoice, purchaserMatch, amounts);

        logFinancingDetails(invoice, purchaserMatch, amounts.getEarlyPaymentAmountInCents());
    }

    private FinancingAmounts calculateFinancingAmounts(Invoice invoice, PurchaserFinancingResult purchaserMatch) {
        long discountAmount = calculationService.calculateDiscountAmount(
                invoice.getValueInCents(), purchaserMatch.getFinancingRateInBps());
        long earlyPaymentAmount = invoice.getValueInCents() - discountAmount;

        return FinancingAmounts.builder()
                .earlyPaymentAmountInCents(earlyPaymentAmount)
                .discountAmountInCents(discountAmount)
                .build();
    }

    private void updateInvoice(Invoice invoice, FinancingAmounts amounts) {
        invoice.setEarlyPaymentAmountInCents(amounts.getEarlyPaymentAmountInCents());
        invoice.setDiscountedAmountInCents(amounts.getDiscountAmountInCents());
    }

    private void saveFinancingDetails(Invoice invoice, PurchaserFinancingResult purchaserMatch, FinancingAmounts amounts) {
        InvoiceFinancingDetails financingRecord = InvoiceFinancingDetails.builder()
                .invoice(invoice)
                .purchaser(purchaserMatch.getPurchaser())
                .financingDate(purchaserMatch.getFinancingDate())
                .financingTermInDays(purchaserMatch.getFinancingTermInDays())
                .financingRateInBps(purchaserMatch.getFinancingRateInBps())
                .earlyPaymentAmountInCents(amounts.getEarlyPaymentAmountInCents())
                .discountedAmountInCents(amounts.getDiscountAmountInCents())
                .build();

        financingDetailsRepository.save(financingRecord);
    }

    private void logFinancingDetails(Invoice invoice, PurchaserFinancingResult purchaserMatch, long earlyPaymentAmount) {
        log.debug("Invoice {} financed by Purchaser {} with rate {} bps, early payment amount: {} cents",
                invoice.getId(), purchaserMatch.getPurchaser().getName(),
                purchaserMatch.getFinancingRateInBps(), earlyPaymentAmount);
    }

    private void flushAndClearEntityManager(int processedCount) {
        entityManager.flush();
        entityManager.clear();
        log.info("Processed {} invoices so far", processedCount);
    }

}