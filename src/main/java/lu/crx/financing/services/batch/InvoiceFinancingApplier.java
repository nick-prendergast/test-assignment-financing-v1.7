package lu.crx.financing.services.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lu.crx.financing.entities.Invoice;
import lu.crx.financing.entities.FinancingAmounts;
import lu.crx.financing.entities.Purchaser;
import lu.crx.financing.entities.PurchaserFinancingResult;
import lu.crx.financing.entities.InvoiceFinancingDetails;
import lu.crx.financing.repositories.InvoiceFinancingDetailsRepository;
import lu.crx.financing.repositories.InvoiceRepository;
import lu.crx.financing.services.financing.FinancingCalculationService;
import lu.crx.financing.services.financing.PurchaserSelectionService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceFinancingApplier {

    private final PurchaserSelectionService purchaserSelectionService;
    private final FinancingCalculationService calculationService;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceFinancingDetailsRepository financingDetailsRepository;

    private static void logFinancingDetails(Invoice invoice, PurchaserFinancingResult purchaserMatch, FinancingAmounts amounts) {
        log.debug("Invoice {} financed by Purchaser {} with rate {} bps, early payment amount: {} cents",
                invoice.getId(), purchaserMatch.getPurchaser().getName(),
                purchaserMatch.getFinancingRateInBps(), amounts.getEarlyPaymentAmountInCents());
    }

    public boolean attemptToFinanceInvoice(Invoice invoice, List<Purchaser> availablePurchasers, LocalDate financingDate) {
        Optional<PurchaserFinancingResult> bestPurchaserMatch =
                purchaserSelectionService.findBestPurchaserForInvoice(invoice, availablePurchasers, financingDate);

        if (bestPurchaserMatch.isPresent()) {
            applyFinancing(invoice, bestPurchaserMatch.get());
            return true;
        } else {
            log.debug("No eligible purchaser found for invoice {}", invoice.getId());
            return false;
        }
    }

    private void applyFinancing(Invoice invoice, PurchaserFinancingResult purchaserMatch) {
        long discountAmount = calculationService.calculateDiscountAmount(
                invoice.getValueInCents(), purchaserMatch.getFinancingRateInBps());

        long earlyPaymentAmount = invoice.getValueInCents() - discountAmount;

        FinancingAmounts amounts = FinancingAmounts.builder()
                .earlyPaymentAmountInCents(earlyPaymentAmount)
                .discountAmountInCents(discountAmount)
                .build();

        updateInvoice(invoice, amounts);

        Invoice savedInvoice = invoiceRepository.save(invoice);

        saveFinancingDetails(savedInvoice, purchaserMatch, amounts);

        logFinancingDetails(invoice, purchaserMatch, amounts);
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
}