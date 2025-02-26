package lu.crx.financing.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class PurchaserFinancingResult {
    private final Purchaser purchaser;
    private final BigDecimal financingRateInBps;
    private final int financingTermInDays;
    private final LocalDate financingDate;
}