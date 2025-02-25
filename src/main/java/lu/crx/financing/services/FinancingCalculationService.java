package lu.crx.financing.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinancingCalculationService {

    private static final int DAYS_IN_FINANCIAL_YEAR = 360;
    private static final int BPS_DIVISOR = 10000;

    public int calculateFinancingTerm(LocalDate currentDate, LocalDate maturityDate) {
        return (int) ChronoUnit.DAYS.between(currentDate, maturityDate);
    }

    public int calculateFinancingRate(int annualRateInBps, int financingTermInDays) {
        if (annualRateInBps <= 0 || financingTermInDays <= 0) {
            log.warn("Invalid input for financing rate calculation: rate={}, term={}",
                    annualRateInBps, financingTermInDays);
            return 0;
        }

        return Math.round((float) annualRateInBps * financingTermInDays / DAYS_IN_FINANCIAL_YEAR);
    }

    public long calculateDiscountAmount(long invoiceValueInCents, int financingRateInBps) {
        if (invoiceValueInCents <= 0 || financingRateInBps <= 0) {
            log.warn("Invalid input for discount calculation: value={}, rate={}",
                    invoiceValueInCents, financingRateInBps);
            return 0;
        }

        return invoiceValueInCents * financingRateInBps / BPS_DIVISOR;
    }
}