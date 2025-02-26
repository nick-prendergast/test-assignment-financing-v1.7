package lu.crx.financing.services.batch;

import lombok.RequiredArgsConstructor;
import lu.crx.financing.entities.Invoice;
import lu.crx.financing.entities.Purchaser;
import lu.crx.financing.utils.EntityManagerUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceBatchProcessor {

    private static final int MEMORY_FLUSH_BATCH_SIZE = 1000;
    private final InvoiceFinancingApplier invoiceFinancingApplier;
    private final EntityManagerUtil entityManagerUtil;

    public int processInvoiceBatch(List<Invoice> pendingInvoices, List<Purchaser> availablePurchasers, LocalDate financingDate) {
        int financedInvoiceCount = 0;

        for (Invoice invoice : pendingInvoices) {
            boolean financingApplied = invoiceFinancingApplier.attemptToFinanceInvoice(invoice, availablePurchasers, financingDate);
            if (financingApplied) {
                financedInvoiceCount++;
            }

            if (shouldFlushMemory(financedInvoiceCount)) {
                entityManagerUtil.flushAndClearEntityManager(financedInvoiceCount);
            }
        }

        return financedInvoiceCount;
    }

    private boolean shouldFlushMemory(int processedCount) {
        return processedCount % MEMORY_FLUSH_BATCH_SIZE == 0 && processedCount > 0;
    }
}