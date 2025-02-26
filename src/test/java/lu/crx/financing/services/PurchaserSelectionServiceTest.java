package lu.crx.financing.services;

import lu.crx.financing.entities.*;
import lu.crx.financing.entities.PurchaserFinancingResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaserSelectionServiceTest {

    @Mock
    private FinancingCalculationService calculationService;

    private PurchaserSelectionService purchaserSelectionService;

    @BeforeEach
    void setUp() {
        purchaserSelectionService = new PurchaserSelectionService(calculationService);
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
        when(calculationService.calculateFinancingRate(eq(50), eq(30))).thenReturn(4);
        when(calculationService.calculateFinancingRate(eq(40), eq(30))).thenReturn(3);

        // When
        Optional<PurchaserFinancingResult> result = purchaserSelectionService.findBestPurchaser(
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
        verify(calculationService).calculateFinancingRate(eq(50), eq(30));
        verify(calculationService).calculateFinancingRate(eq(40), eq(30));
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
        when(calculationService.calculateFinancingRate(anyInt(), eq(30))).thenReturn(3);

        // When
        Optional<PurchaserFinancingResult> result = purchaserSelectionService.findBestPurchaser(
                invoice,
                Collections.singletonList(purchaser),
                LocalDate.now()
        );

        // Then
        assertFalse(result.isPresent());

        verify(calculationService).calculateFinancingTerm(eq(LocalDate.now()), eq(invoice.getMaturityDate()));
        verify(calculationService).calculateFinancingRate(anyInt(), eq(30));
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

        // When
        Optional<PurchaserFinancingResult> result = purchaserSelectionService.findBestPurchaser(
                invoice,
                Collections.singletonList(purchaser),
                LocalDate.now()
        );

        // Then
        assertFalse(result.isPresent());

        verify(calculationService).calculateFinancingTerm(eq(LocalDate.now()), eq(invoice.getMaturityDate()));
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

        // When
        Optional<PurchaserFinancingResult> result = purchaserSelectionService.findBestPurchaser(
                invoice,
                Collections.singletonList(purchaser),
                LocalDate.now()
        );

        // Then
        assertFalse(result.isPresent());

        verify(calculationService).calculateFinancingTerm(eq(LocalDate.now()), eq(invoice.getMaturityDate()));
    }
}