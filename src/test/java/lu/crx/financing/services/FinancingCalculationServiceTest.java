package lu.crx.financing.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        BigDecimal result = calculationService.calculateFinancingRate(annualRateInBps, financingTermInDays);

        // Then
        // 50 * 30 / 360 = 4.1666...
        assertEquals(0, new BigDecimal("4.1666666667").compareTo(result));
    }

    @Test
    void shouldCalculateDiscountAmount() {
        // Given
        long invoiceValueInCents = 10_000_00L;
        BigDecimal financingRateInBps = new BigDecimal("4.1666666667");

        // When
        BigDecimal result = calculationService.calculateDiscountAmount(invoiceValueInCents, financingRateInBps);

        // Then
        // 10_000_00 * 4.1666666667 / 10000 = 416.6666...
        assertTrue(result.compareTo(new BigDecimal("416.666667")) == 0);
    }

    @Test
    void shouldCalculateEarlyPaymentAmount() {
        // Given
        long invoiceValueInCents = 10_000_00L;
        BigDecimal financingRateInBps = new BigDecimal("4.1666666667");

        // When
        BigDecimal result = calculationService.calculateEarlyPaymentAmount(invoiceValueInCents, financingRateInBps);

        // Then
        // 10_000_00 - 416.6666... = 999583.3333...
        BigDecimal expected = new BigDecimal(10_000_00).subtract(new BigDecimal("416.666667"));
        assertTrue(result.compareTo(expected) == 0);
    }

    @Test
    void shouldReturnZeroDiscountForZeroRate() {
        // Given
        long invoiceValueInCents = 10_000_00L;
        BigDecimal financingRateInBps = BigDecimal.ZERO;

        // When
        BigDecimal result = calculationService.calculateDiscountAmount(invoiceValueInCents, financingRateInBps);

        // Then
        assertTrue(result.compareTo(BigDecimal.ZERO) == 0);
    }

    @Test
    void shouldReturnZeroRateForZeroTerm() {
        // Given
        int annualRateInBps = 50;
        int financingTermInDays = 0;

        // When
        BigDecimal result = calculationService.calculateFinancingRate(annualRateInBps, financingTermInDays);

        // Then
        assertTrue(result.compareTo(BigDecimal.ZERO) == 0);
    }

    @Test
    void shouldHandleNegativeInvoiceValue() {
        // Given
        long invoiceValueInCents = -10_000_00L;
        BigDecimal financingRateInBps = new BigDecimal("4.1666666667");

        // When
        BigDecimal result = calculationService.calculateDiscountAmount(invoiceValueInCents, financingRateInBps);

        // Then
        assertTrue(result.compareTo(BigDecimal.ZERO) == 0);
    }

    @Test
    void shouldHandleNegativeAnnualRate() {
        // Given
        int annualRateInBps = -50;
        int financingTermInDays = 30;

        // When
        BigDecimal result = calculationService.calculateFinancingRate(annualRateInBps, financingTermInDays);

        // Then
        assertTrue(result.compareTo(BigDecimal.ZERO) == 0);
    }

    @Test
    void shouldHandleNegativeFinancingTerm() {
        // Given
        int annualRateInBps = 50;
        int financingTermInDays = -30;

        // When
        BigDecimal result = calculationService.calculateFinancingRate(annualRateInBps, financingTermInDays);

        // Then
        assertTrue(result.compareTo(BigDecimal.ZERO) == 0);
    }

    @Test
    void shouldReturnNegativeForNegativeDates() {
        // Given
        LocalDate currentDate = LocalDate.of(2023, 1, 31);
        LocalDate maturityDate = LocalDate.of(2023, 1, 1);

        // When
        int result = calculationService.calculateFinancingTerm(currentDate, maturityDate);

        // Then
        assertEquals(-30, result);
    }

    @Test
    void shouldCalculatePreciselyWithoutRounding() {
        // Given - values that would produce exactly the same result with rounding
        int annualRateInBps1 = 43;
        int annualRateInBps2 = 44;
        int financingTermInDays = 17;

        // When
        BigDecimal result1 = calculationService.calculateFinancingRate(annualRateInBps1, financingTermInDays);
        BigDecimal result2 = calculationService.calculateFinancingRate(annualRateInBps2, financingTermInDays);

        // Then - these values should be different
        assertTrue(result1.compareTo(result2) < 0);
    }
}