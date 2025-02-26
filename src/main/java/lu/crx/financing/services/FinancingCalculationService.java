package lu.crx.financing.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinancingCalculationService {

    private static final int DAYS_IN_FINANCIAL_YEAR = 360;
    private static final int BPS_DIVISOR = 10000;
    private static final int CALCULATION_SCALE = 10;
    private static final int RESULT_SCALE = 6;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private static final BigDecimal DAYS_IN_YEAR = new BigDecimal(DAYS_IN_FINANCIAL_YEAR);
    private static final BigDecimal DIVISOR = new BigDecimal(BPS_DIVISOR);

    public int calculateFinancingTerm(LocalDate currentDate, LocalDate maturityDate) {
        return (int) ChronoUnit.DAYS.between(currentDate, maturityDate);
    }

    public BigDecimal calculateFinancingRate(int annualRateInBps, int financingTermInDays) {
        if (annualRateInBps <= 0 || financingTermInDays <= 0) {
            log.warn("Invalid input for financing rate calculation: rate={}, term={}",
                    annualRateInBps, financingTermInDays);
            return BigDecimal.ZERO;
        }

        return new BigDecimal(annualRateInBps)
                .multiply(new BigDecimal(financingTermInDays))
                .divide(DAYS_IN_YEAR, CALCULATION_SCALE, ROUNDING_MODE);
    }

    public BigDecimal calculateDiscountAmount(long invoiceValueInCents, BigDecimal financingRateInBps) {
        if (invoiceValueInCents <= 0 || financingRateInBps.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Invalid input for discount calculation: value={}, rate={}",
                    invoiceValueInCents, financingRateInBps);
            return BigDecimal.ZERO;
        }

        return new BigDecimal(invoiceValueInCents)
                .multiply(financingRateInBps)
                .divide(DIVISOR, CALCULATION_SCALE, ROUNDING_MODE)
                .setScale(RESULT_SCALE, ROUNDING_MODE);
    }

    public BigDecimal calculateEarlyPaymentAmount(long invoiceValueInCents, BigDecimal financingRateInBps) {
        BigDecimal discount = calculateDiscountAmount(invoiceValueInCents, financingRateInBps);
        return new BigDecimal(invoiceValueInCents).subtract(discount)
                .setScale(RESULT_SCALE, ROUNDING_MODE);
    }
}