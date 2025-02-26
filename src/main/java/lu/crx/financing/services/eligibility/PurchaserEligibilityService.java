package lu.crx.financing.services.eligibility;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lu.crx.financing.entities.Creditor;
import lu.crx.financing.entities.Purchaser;
import lu.crx.financing.entities.PurchaserFinancingResult;
import lu.crx.financing.entities.PurchaserFinancingSettings;
import lu.crx.financing.services.financing.FinancingCalculationService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Predicate;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaserEligibilityService {

    private final FinancingCalculationService calculationService;

    public boolean isPurchaserEligible(
            Purchaser purchaser,
            Creditor creditor,
            int financingTermInDays,
            int maxFinancingRateInBps) {

        return findSettingsForCreditor(purchaser, creditor)
                .filter(settings -> meetsMinimumFinancingTerm(purchaser, financingTermInDays))
                .map(settings -> calculationService.calculateFinancingRate(
                        settings.getAnnualRateInBps(), financingTermInDays))
                .filter(rate -> !exceedsMaxRate(rate, maxFinancingRateInBps))
                .isPresent();
    }

    public PurchaserFinancingResult createFinancingResult(
            Purchaser purchaser,
            Creditor creditor,
            int financingTermInDays,
            LocalDate financingDate) {

        return findSettingsForCreditor(purchaser, creditor)
                .map(settings -> {
                    int rateInBps = calculationService.calculateFinancingRate(
                            settings.getAnnualRateInBps(), financingTermInDays);

                    double exactRate = calculationService.calculateExactFinancingRate(
                            settings.getAnnualRateInBps(), financingTermInDays);

                    return new PurchaserFinancingResult(
                            purchaser, rateInBps, financingTermInDays, financingDate, exactRate);
                })
                .orElseThrow(() -> new IllegalArgumentException(
                        "No settings found for purchaser " + purchaser.getName()));
    }


    public Optional<PurchaserFinancingSettings> findSettingsForCreditor(Purchaser purchaser, Creditor creditor) {
        Predicate<PurchaserFinancingSettings> matchesCreditor =
                settings -> settings.getCreditor().getId() == creditor.getId();

        Optional<PurchaserFinancingSettings> settings = purchaser.getPurchaserFinancingSettings().stream()
                .filter(matchesCreditor)
                .findFirst();

        if (settings.isEmpty()) {
            log.debug("Purchaser {} rejected: no settings for creditor {}",
                    purchaser.getName(), creditor.getId());
        }

        return settings;
    }

    public boolean meetsMinimumFinancingTerm(Purchaser purchaser, int financingTermInDays) {
        boolean isValid = financingTermInDays >= purchaser.getMinimumFinancingTermInDays();

        if (!isValid) {
            log.debug("Purchaser {} rejected: term {} days < minimum {} days",
                    purchaser.getName(), financingTermInDays, purchaser.getMinimumFinancingTermInDays());
        }

        return isValid;
    }

    public boolean exceedsMaxRate(int rateInBps, int maxRateInBps) {
        boolean exceeds = rateInBps > maxRateInBps;

        if (exceeds) {
            log.debug("Purchaser rejected: rate {} bps > maximum {} bps", rateInBps, maxRateInBps);
        }

        return exceeds;
    }
}