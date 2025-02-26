package lu.crx.financing.services.financing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FinancingCalculationServiceTest {

    private FinancingCalculationService calculationService;

    @BeforeEach
    void setUp() {
        calculationService = new FinancingCalculationService();
    }

    @Test
    void shouldCalculateFinancingTermBetweenDates() {
        // Given
        LocalDate currentDate = LocalDate.of(2023, 1, 1);
        LocalDate maturityDate = LocalDate.of(2023, 1, 31);

        // When
        int result = calculationService.calculateFinancingTerm(currentDate, maturityDate);

        // Then
        assertEquals(30, result);
    }

    @Test
    void shouldCalculateFinancingRateBasedOnTermAndAnnualRate() {
        // Given
        int annualRateInBps = 50;
        int financingTermInDays = 30;

        // When
        int result = calculationService.calculateFinancingRate(annualRateInBps, financingTermInDays);

        // Then
        assertEquals(4, result);
    }

    @Test
    void shouldCalculateDiscountAmount() {
        // Given
        long invoiceValueInCents = 10_000_00L;
        int financingRateInBps = 4;

        // When
        long result = calculationService.calculateDiscountAmount(invoiceValueInCents, financingRateInBps);

        // Then
        assertEquals(4_00L, result);
    }

    @Test
    void shouldReturnZeroDiscountForZeroRate() {
        // Given
        long invoiceValueInCents = 10_000_00L;
        int financingRateInBps = 0;

        // When
        long result = calculationService.calculateDiscountAmount(invoiceValueInCents, financingRateInBps);

        // Then
        assertEquals(0L, result);
    }

    @Test
    void shouldReturnZeroRateForZeroTerm() {
        // Given
        int annualRateInBps = 50;
        int financingTermInDays = 0;

        // When
        int result = calculationService.calculateFinancingRate(annualRateInBps, financingTermInDays);

        // Then
        assertEquals(0, result);
    }

    @Test
    void shouldHandleNegativeInvoiceValue() {
        // Given
        long invoiceValueInCents = -10_000_00L;
        int financingRateInBps = 4;

        // When
        long result = calculationService.calculateDiscountAmount(invoiceValueInCents, financingRateInBps);

        // Then
        assertEquals(0L, result);
    }

    @Test
    void shouldHandleNegativeAnnualRate() {
        // Given
        int annualRateInBps = -50;
        int financingTermInDays = 30;

        // When
        int result = calculationService.calculateFinancingRate(annualRateInBps, financingTermInDays);

        // Then
        assertEquals(0, result);
    }

    @Test
    void shouldHandleNegativeFinancingTerm() {
        // Given
        int annualRateInBps = 50;
        int financingTermInDays = -30;

        // When
        int result = calculationService.calculateFinancingRate(annualRateInBps, financingTermInDays);

        // Then
        assertEquals(0, result);
    }

    @Test
    void shouldReturnZeroForNegativeDates() {
        // Given
        LocalDate currentDate = LocalDate.of(2023, 1, 31);
        LocalDate maturityDate = LocalDate.of(2023, 1, 1);

        // When
        int result = calculationService.calculateFinancingTerm(currentDate, maturityDate);

        // Then
        assertEquals(-30, result);
    }

    @Test
    void shouldCalculateExactBpsValueForDiscountAmount() {
        // Given
        long invoiceValueInCents = 1_000_000_00L; // $1,000,000.00
        int financingRateInBps = 1; // 0.01%

        // When
        long result = calculationService.calculateDiscountAmount(invoiceValueInCents, financingRateInBps);

        // Then
        assertEquals(100_00L, result); // $100.00
    }
}