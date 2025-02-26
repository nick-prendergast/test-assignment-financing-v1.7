package lu.crx.financing.services.eligibility;


import lu.crx.financing.entities.Creditor;
import lu.crx.financing.entities.Purchaser;
import lu.crx.financing.entities.PurchaserFinancingResult;
import lu.crx.financing.entities.PurchaserFinancingSettings;
import lu.crx.financing.services.financing.FinancingCalculationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaserEligibilityServiceTest {

    @Mock
    private FinancingCalculationService calculationService;

    private PurchaserEligibilityService purchaserEligibilityService;

    @BeforeEach
    void setUp() {
        purchaserEligibilityService = new PurchaserEligibilityService(calculationService);
    }

    @Test
    void shouldReturnTrueWhenPurchaserIsEligible() {
        // Given
        Creditor creditor = Creditor.builder()
                .id(1L)
                .name("Creditor1")
                .maxFinancingRateInBps(50)
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

        when(calculationService.calculateFinancingRate(eq(50), eq(30))).thenReturn(3);

        // When
        boolean isEligible = purchaserEligibilityService.isPurchaserEligible(
                purchaser, creditor, 30, 50);

        // Then
        assertTrue(isEligible);

        verify(calculationService).calculateFinancingRate(eq(50), eq(30));
    }

    @Test
    void shouldReturnFalseWhenFinancingTermIsTooShort() {
        // Given
        Creditor creditor = Creditor.builder()
                .id(1L)
                .name("Creditor1")
                .maxFinancingRateInBps(50)
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

        // When
        boolean isEligible = purchaserEligibilityService.isPurchaserEligible(
                purchaser, creditor, 30, 50);

        // Then
        assertFalse(isEligible);
    }

    @Test
    void shouldReturnFalseWhenRateExceedsMaxRate() {
        // Given
        Creditor creditor = Creditor.builder()
                .id(1L)
                .name("Creditor1")
                .maxFinancingRateInBps(2)
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

        when(calculationService.calculateFinancingRate(eq(50), eq(30))).thenReturn(3);

        // When
        boolean isEligible = purchaserEligibilityService.isPurchaserEligible(
                purchaser, creditor, 30, 2);

        // Then
        assertFalse(isEligible);

        verify(calculationService).calculateFinancingRate(eq(50), eq(30));
    }

    @Test
    void shouldReturnFalseWhenPurchaserHasNoSettings() {
        // Given
        Creditor creditor = Creditor.builder()
                .id(1L)
                .name("Creditor1")
                .maxFinancingRateInBps(50)
                .build();

        Purchaser purchaser = Purchaser.builder()
                .id(1L)
                .name("Purchaser1")
                .minimumFinancingTermInDays(20)
                .purchaserFinancingSettings(new HashSet<>())
                .build();

        // When
        boolean isEligible = purchaserEligibilityService.isPurchaserEligible(
                purchaser, creditor, 30, 50);

        // Then
        assertFalse(isEligible);
    }

    @Test
    void shouldCreateFinancingResult() {
        // Given
        Creditor creditor = Creditor.builder()
                .id(1L)
                .name("Creditor1")
                .maxFinancingRateInBps(50)
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

        when(calculationService.calculateFinancingRate(eq(50), eq(30))).thenReturn(3);
        when(calculationService.calculateExactFinancingRate(eq(50), eq(30))).thenReturn(3.0);

        // When
        PurchaserFinancingResult result = purchaserEligibilityService.createFinancingResult(
                purchaser, creditor, 30, LocalDate.now());

        // Then
        assertNotNull(result);
        assertEquals(purchaser.getId(), result.getPurchaser().getId());
        assertEquals(3, result.getFinancingRateInBps());
        assertEquals(30, result.getFinancingTermInDays());
        assertEquals(LocalDate.now(), result.getFinancingDate());
        assertEquals(3.0, result.getExactFinancingRate());

        verify(calculationService).calculateFinancingRate(eq(50), eq(30));
        verify(calculationService).calculateExactFinancingRate(eq(50), eq(30));
    }
}