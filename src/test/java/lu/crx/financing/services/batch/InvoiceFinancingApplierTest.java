package lu.crx.financing.services.batch;

import lu.crx.financing.entities.Invoice;
import lu.crx.financing.entities.Purchaser;
import lu.crx.financing.entities.PurchaserFinancingResult;
import lu.crx.financing.entities.InvoiceFinancingDetails;
import lu.crx.financing.repositories.InvoiceFinancingDetailsRepository;
import lu.crx.financing.repositories.InvoiceRepository;
import lu.crx.financing.services.financing.FinancingCalculationService;
import lu.crx.financing.services.financing.PurchaserSelectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class InvoiceFinancingApplierTest {

    @Mock
    private PurchaserSelectionService purchaserSelectionService;

    @Mock
    private FinancingCalculationService calculationService;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceFinancingDetailsRepository financingDetailsRepository;


    private InvoiceFinancingApplier invoiceFinancingApplier;

    @BeforeEach
    void setUp() {
        invoiceFinancingApplier = new InvoiceFinancingApplier(
                purchaserSelectionService,
                calculationService,
                invoiceRepository,
                financingDetailsRepository
        );
    }

    @Test
    void shouldAttemptToFinanceInvoiceAndApplyFinancingWhenPurchaserFound() {
        // Given
        Invoice invoice = mock(Invoice.class);
        when(invoice.getId()).thenReturn(1L);
        when(invoice.getValueInCents()).thenReturn(10_000_00L);

        Purchaser purchaser = Purchaser.builder()
                .id(1L)
                .name("Purchaser1")
                .build();

        PurchaserFinancingResult purchaserMatch = new PurchaserFinancingResult(
                purchaser,
                3,
                30,
                LocalDate.now(),
                3
        );

        when(purchaserSelectionService.findBestPurchaserForInvoice(any(Invoice.class), anyList(), any(LocalDate.class)))
                .thenReturn(Optional.of(purchaserMatch));
        when(calculationService.calculateDiscountAmount(anyLong(), anyInt())).thenReturn(3_00L);
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);

        // When
        boolean result = invoiceFinancingApplier.attemptToFinanceInvoice(invoice, Collections.emptyList(), LocalDate.now());

        // Then
        assertTrue(result);

        verify(invoice).setEarlyPaymentAmountInCents(9_997_00L); // 10_000_00 - 3_00
        verify(invoice).setDiscountedAmountInCents(3_00L);
        verify(invoiceRepository).save(invoice);
        verify(financingDetailsRepository).save(any(InvoiceFinancingDetails.class));
    }

    @Test
    void shouldAttemptToFinanceInvoiceAndReturnFalseWhenNoPurchaserFound() {
        // Given
        Invoice invoice = Invoice.builder().id(1L).build();

        when(purchaserSelectionService.findBestPurchaserForInvoice(any(Invoice.class), anyList(), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        // When
        boolean result = invoiceFinancingApplier.attemptToFinanceInvoice(invoice, Collections.emptyList(), LocalDate.now());

        // Then
        assertFalse(result);

        verifyNoInteractions(calculationService, invoiceRepository, financingDetailsRepository);
    }

}