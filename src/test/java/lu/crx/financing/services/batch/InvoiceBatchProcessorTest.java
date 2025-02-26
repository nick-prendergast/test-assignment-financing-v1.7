package lu.crx.financing.services.batch;

import lu.crx.financing.entities.Invoice;
import lu.crx.financing.entities.Purchaser;
import lu.crx.financing.utils.EntityManagerUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class InvoiceBatchProcessorTest {

    @Mock
    private InvoiceFinancingApplier invoiceFinancingApplier;

    @Mock
    private EntityManagerUtil entityManagerUtil;

    private InvoiceBatchProcessor invoiceBatchProcessor;

    @BeforeEach
    void setUp() {
        invoiceBatchProcessor = new InvoiceBatchProcessor(invoiceFinancingApplier, entityManagerUtil);
    }

    @Test
    void shouldProcessInvoiceBatchAndApplyFinancing() {
        // Given
        Invoice invoice1 = Invoice.builder().id(1L).build();
        Invoice invoice2 = Invoice.builder().id(2L).build();
        List<Invoice> pendingInvoices = Arrays.asList(invoice1, invoice2);
        List<Purchaser> availablePurchasers = Collections.singletonList(Purchaser.builder().build());
        LocalDate financingDate = LocalDate.now();

        when(invoiceFinancingApplier.attemptToFinanceInvoice(any(Invoice.class), anyList(), any(LocalDate.class)))
                .thenReturn(true);

        // When
        int financedInvoiceCount = invoiceBatchProcessor.processInvoiceBatch(pendingInvoices, availablePurchasers, financingDate);

        // Then
        verify(invoiceFinancingApplier, times(2)).attemptToFinanceInvoice(any(Invoice.class), anyList(), any(LocalDate.class));
        verify(entityManagerUtil, never()).flushAndClearEntityManager(anyInt()); // No flush for small batch
        assert financedInvoiceCount == 2;
    }

    @Test
    void shouldNotApplyFinancingWhenNoEligiblePurchaser() {
        // Given
        Invoice invoice1 = Invoice.builder().id(1L).build();
        List<Invoice> pendingInvoices = Collections.singletonList(invoice1);
        List<Purchaser> availablePurchasers = Collections.singletonList(Purchaser.builder().build());
        LocalDate financingDate = LocalDate.now();

        when(invoiceFinancingApplier.attemptToFinanceInvoice(any(Invoice.class), anyList(), any(LocalDate.class)))
                .thenReturn(false);

        // When
        int financedInvoiceCount = invoiceBatchProcessor.processInvoiceBatch(pendingInvoices, availablePurchasers, financingDate);

        // Then
        verify(invoiceFinancingApplier).attemptToFinanceInvoice(any(Invoice.class), anyList(), any(LocalDate.class));
        verify(entityManagerUtil, never()).flushAndClearEntityManager(anyInt()); // No flush for small batch
        assert financedInvoiceCount == 0;
    }


    @Test
    void shouldHandleEmptyInvoiceBatch() {
        // Given
        List<Invoice> pendingInvoices = Collections.emptyList();
        List<Purchaser> availablePurchasers = Collections.singletonList(Purchaser.builder().build());
        LocalDate financingDate = LocalDate.now();

        // When
        int financedInvoiceCount = invoiceBatchProcessor.processInvoiceBatch(pendingInvoices, availablePurchasers, financingDate);

        // Then
        verify(invoiceFinancingApplier, never()).attemptToFinanceInvoice(any(Invoice.class), anyList(), any(LocalDate.class));
        verify(entityManagerUtil, never()).flushAndClearEntityManager(anyInt());
        assert financedInvoiceCount == 0;
    }

    @Test
    void shouldFlushAndClearEntityManagerForLargeBatches() {
        // Given
        List<Invoice> invoices = new ArrayList<>();
        for (int i = 0; i < 2000; i++) {
            invoices.add(Invoice.builder().build());
        }

        List<Purchaser> availablePurchasers = Collections.singletonList(Purchaser.builder().build());
        LocalDate financingDate = LocalDate.now();

        when(invoiceFinancingApplier.attemptToFinanceInvoice(any(Invoice.class), anyList(), any(LocalDate.class)))
                .thenReturn(true);

        // When
        int financedInvoiceCount = invoiceBatchProcessor.processInvoiceBatch(invoices, availablePurchasers, financingDate);

        // Then
        verify(entityManagerUtil, times(2)).flushAndClearEntityManager(anyInt());
        assert financedInvoiceCount == 2000;
    }
}