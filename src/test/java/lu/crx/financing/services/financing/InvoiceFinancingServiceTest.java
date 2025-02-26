package lu.crx.financing.services.financing;

import lu.crx.financing.entities.Invoice;
import lu.crx.financing.entities.Purchaser;

import lu.crx.financing.repositories.InvoiceRepository;
import lu.crx.financing.repositories.PurchaserRepository;
import lu.crx.financing.services.batch.InvoiceBatchProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InvoiceFinancingServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private PurchaserRepository purchaserRepository;

    @Mock
    private InvoiceBatchProcessor invoiceBatchProcessor;

    private InvoiceFinancingService invoiceFinancingService;

    @BeforeEach
    void setUp() {
        invoiceFinancingService = new InvoiceFinancingService(
                invoiceRepository,
                purchaserRepository,
                invoiceBatchProcessor
        );
    }

    @Test
    void shouldProcessInvoiceFinancing() {
        // Given
        List<Invoice> pendingInvoices = Collections.singletonList(Invoice.builder().build());
        List<Purchaser> availablePurchasers = Collections.singletonList(Purchaser.builder().build());
        LocalDate financingDate = LocalDate.now();

        when(invoiceRepository.findNonFinancedInvoices()).thenReturn(pendingInvoices);
        when(purchaserRepository.findAllWithFinancingSettings()).thenReturn(availablePurchasers);
        when(invoiceBatchProcessor.processInvoiceBatch(pendingInvoices, availablePurchasers, financingDate))
                .thenReturn(1);

        // When
        invoiceFinancingService.processInvoiceFinancing();

        // Then
        verify(invoiceRepository).findNonFinancedInvoices();
        verify(purchaserRepository).findAllWithFinancingSettings();
        verify(invoiceBatchProcessor).processInvoiceBatch(pendingInvoices, availablePurchasers, financingDate);
    }

}