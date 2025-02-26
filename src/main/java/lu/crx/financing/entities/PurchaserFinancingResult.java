package lu.crx.financing.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class PurchaserFinancingResult {
    private final Purchaser purchaser;
    private final int financingRateInBps;
    private final int financingTermInDays;
    private final LocalDate financingDate;
}