package lu.crx.financing.services.financing;

import lu.crx.financing.entities.Invoice;
import lu.crx.financing.entities.Purchaser;
import lu.crx.financing.entities.PurchaserFinancingResult;
import lu.crx.financing.entities.PurchaserFinancingSettings;
import lu.crx.financing.entities.Creditor;
import lu.crx.financing.entities.Debtor;
import lu.crx.financing.services.eligibility.PurchaserEligibilityService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaserSelectionServiceTest {

    @Mock
    private FinancingCalculationService calculationService;

    @Mock
    private PurchaserEligibilityService eligibilityService;

    private PurchaserSelectionService purchaserSelectionService;

    @BeforeEach
    void setUp() {
        purchaserSelectionService = new PurchaserSelectionService(calculationService, eligibilityService);
    }

    @Test
    void shouldFindBestPurchaserWithLowestFinancingRate() {
        // Given
        Creditor creditor = Creditor.builder()
                .id(1L)
                .name("Creditor1")
                .maxFinancingRateInBps(3)
                .build();

        Debtor debtor = Debtor.builder()
                .id(1L)
                .name("Debtor1")
                .build();

        Invoice invoice = Invoice.builder()
                .id(1L)
                .creditor(creditor)
                .debtor(debtor)
                .valueInCents(10_000_00L)
                .maturityDate(LocalDate.now().plusDays(30))
                .build();

        Purchaser purchaser1 = Purchaser.builder()
                .id(1L)
                .name("Purchaser1")
                .minimumFinancingTermInDays(20)
                .purchaserFinancingSettings(new HashSet<>(Collections.singletonList(
                        PurchaserFinancingSettings.builder()
                                .id(1L)
                                .creditor(creditor)
                                .annualRateInBps(50)
                                .build()
                )))
                .build();

        Purchaser purchaser2 = Purchaser.builder()
                .id(2L)
                .name("Purchaser2")
                .minimumFinancingTermInDays(20)
                .purchaserFinancingSettings(new HashSet<>(Collections.singletonList(
                        PurchaserFinancingSettings.builder()
                                .id(2L)
                                .creditor(creditor)
                                .annualRateInBps(40)
                                .build()
                )))
                .build();

        when(calculationService.calculateFinancingTerm(eq(LocalDate.now()), eq(invoice.getMaturityDate())))
                .thenReturn(30);
        when(eligibilityService.isPurchaserEligible(eq(purchaser1), eq(creditor), eq(30), eq(3)))
                .thenReturn(true);
        when(eligibilityService.isPurchaserEligible(eq(purchaser2), eq(creditor), eq(30), eq(3)))
                .thenReturn(true);
        when(eligibilityService.createFinancingResult(eq(purchaser1), eq(creditor), eq(30), eq(LocalDate.now())))
                .thenReturn(new PurchaserFinancingResult(purchaser1, 4, 30, LocalDate.now(), 4.0));
        when(eligibilityService.createFinancingResult(eq(purchaser2), eq(creditor), eq(30), eq(LocalDate.now())))
                .thenReturn(new PurchaserFinancingResult(purchaser2, 3, 30, LocalDate.now(), 3.0));

        // When
        Optional<PurchaserFinancingResult> result = purchaserSelectionService.findBestPurchaserForInvoice(
                invoice,
                Arrays.asList(purchaser1, purchaser2),
                LocalDate.now()
        );

        // Then
        assertTrue(result.isPresent());
        PurchaserFinancingResult financingResult = result.get();
        assertEquals(purchaser2.getId(), financingResult.getPurchaser().getId());
        assertEquals(3, financingResult.getFinancingRateInBps());
        assertEquals(30, financingResult.getFinancingTermInDays());
        assertEquals(LocalDate.now(), financingResult.getFinancingDate());

        verify(calculationService).calculateFinancingTerm(eq(LocalDate.now()), eq(invoice.getMaturityDate()));
        verify(eligibilityService).isPurchaserEligible(eq(purchaser1), eq(creditor), eq(30), eq(3));
        verify(eligibilityService).isPurchaserEligible(eq(purchaser2), eq(creditor), eq(30), eq(3));
        verify(eligibilityService).createFinancingResult(eq(purchaser1), eq(creditor), eq(30), eq(LocalDate.now()));
        verify(eligibilityService).createFinancingResult(eq(purchaser2), eq(creditor), eq(30), eq(LocalDate.now()));
    }

    @Test
    void shouldReturnEmptyOptionalWhenCreditorMaxRateIsTooLow() {
        // Given
        Creditor creditor = Creditor.builder()
                .id(1L)
                .name("Creditor1")
                .maxFinancingRateInBps(2)
                .build();

        Invoice invoice = Invoice.builder()
                .id(1L)
                .creditor(creditor)
                .build();

        Purchaser purchaser = Purchaser.builder()
                .id(1L)
                .name("Purchaser1")
                .minimumFinancingTermInDays(20)
                .purchaserFinancingSettings(new HashSet<>(Collections.singletonList(
                        PurchaserFinancingSettings.builder()
                                .id(1L)
                                .creditor(creditor)
                                .annualRateInBps(50)
                                .build()
                )))
                .build();

        when(calculationService.calculateFinancingTerm(eq(LocalDate.now()), eq(invoice.getMaturityDate())))
                .thenReturn(30);
        when(eligibilityService.isPurchaserEligible(eq(purchaser), eq(creditor), eq(30), eq(2)))
                .thenReturn(false);

        // When
        Optional<PurchaserFinancingResult> result = purchaserSelectionService.findBestPurchaserForInvoice(
                invoice,
                Collections.singletonList(purchaser),
                LocalDate.now()
        );

        // Then
        assertFalse(result.isPresent());

        verify(calculationService).calculateFinancingTerm(eq(LocalDate.now()), eq(invoice.getMaturityDate()));
        verify(eligibilityService).isPurchaserEligible(eq(purchaser), eq(creditor), eq(30), eq(2));
    }

    @Test
    void shouldReturnEmptyOptionalWhenFinancingTermIsTooShort() {
        // Given
        Creditor creditor = Creditor.builder()
                .id(1L)
                .name("Creditor1")
                .maxFinancingRateInBps(50)
                .build();

        Invoice invoice = Invoice.builder()
                .id(1L)
                .creditor(creditor)
                .build();

        Purchaser purchaser = Purchaser.builder()
                .id(1L)
                .name("Purchaser1")
                .minimumFinancingTermInDays(40)
                .purchaserFinancingSettings(new HashSet<>(Collections.singletonList(
                        PurchaserFinancingSettings.builder()
                                .id(1L)
                                .creditor(creditor)
                                .annualRateInBps(50)
                                .build()
                )))
                .build();

        when(calculationService.calculateFinancingTerm(eq(LocalDate.now()), eq(invoice.getMaturityDate())))
                .thenReturn(30);
        when(eligibilityService.isPurchaserEligible(eq(purchaser), eq(creditor), eq(30), eq(50)))
                .thenReturn(false);

        // When
        Optional<PurchaserFinancingResult> result = purchaserSelectionService.findBestPurchaserForInvoice(
                invoice,
                Collections.singletonList(purchaser),
                LocalDate.now()
        );

        // Then
        assertFalse(result.isPresent());

        verify(calculationService).calculateFinancingTerm(eq(LocalDate.now()), eq(invoice.getMaturityDate()));
        verify(eligibilityService).isPurchaserEligible(eq(purchaser), eq(creditor), eq(30), eq(50));
    }

    @Test
    void shouldReturnEmptyOptionalWhenPurchaserHasNoSettings() {
        // Given
        Creditor creditor = Creditor.builder()
                .id(1L)
                .name("Creditor1")
                .maxFinancingRateInBps(50)
                .build();

        Invoice invoice = Invoice.builder()
                .id(1L)
                .creditor(creditor)
                .build();

        Purchaser purchaser = Purchaser.builder()
                .id(1L)
                .name("Purchaser1")
                .minimumFinancingTermInDays(20)
                .purchaserFinancingSettings(new HashSet<>())
                .build();

        when(calculationService.calculateFinancingTerm(eq(LocalDate.now()), eq(invoice.getMaturityDate())))
                .thenReturn(30);
        when(eligibilityService.isPurchaserEligible(eq(purchaser), eq(creditor), eq(30), eq(50)))
                .thenReturn(false);

        // When
        Optional<PurchaserFinancingResult> result = purchaserSelectionService.findBestPurchaserForInvoice(
                invoice,
                Collections.singletonList(purchaser),
                LocalDate.now()
        );

        // Then
        assertFalse(result.isPresent());

        verify(calculationService).calculateFinancingTerm(eq(LocalDate.now()), eq(invoice.getMaturityDate()));
        verify(eligibilityService).isPurchaserEligible(eq(purchaser), eq(creditor), eq(30), eq(50));
    }
}