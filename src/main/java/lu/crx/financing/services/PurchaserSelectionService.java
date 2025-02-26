package lu.crx.financing.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lu.crx.financing.entities.Creditor;
import lu.crx.financing.entities.Invoice;
import lu.crx.financing.entities.Purchaser;
import lu.crx.financing.entities.PurchaserFinancingResult;
import lu.crx.financing.entities.PurchaserFinancingSettings;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaserSelectionService {

    private final FinancingCalculationService calculationService;

    public Optional<PurchaserFinancingResult> findBestPurchaser(Invoice invoice, List<Purchaser> purchasers, LocalDate financingDate) {
        Creditor creditor = invoice.getCreditor();
        int financingTermInDays = calculationService.calculateFinancingTerm(financingDate, invoice.getMaturityDate());
        BigDecimal maxFinancingRateInBps = creditor.getMaxFinancingRateInBps();

        List<PurchaserFinancingResult> eligiblePurchasers = new ArrayList<>();

        for (Purchaser purchaser : purchasers) {
            Optional<PurchaserFinancingSettings> settingsOpt = findSettingsForCreditor(purchaser, creditor);
            if (settingsOpt.isEmpty()) {
                continue;
            }

            if (!meetsMinimumFinancingTerm(purchaser, financingTermInDays)) {
                continue;
            }

            BigDecimal financingRateInBps = calculationService.calculateFinancingRate(
                    settingsOpt.get().getAnnualRateInBps(), financingTermInDays);

            if (exceedsMaxFinancingRate(financingRateInBps, maxFinancingRateInBps)) {
                continue;
            }

            eligiblePurchasers.add(new PurchaserFinancingResult(
                    purchaser, financingRateInBps, financingTermInDays, financingDate));
        }

        return findPurchaserWithLowestRate(eligiblePurchasers);
    }

    private Optional<PurchaserFinancingSettings> findSettingsForCreditor(Purchaser purchaser, Creditor creditor) {
        return purchaser.getPurchaserFinancingSettings().stream()
                .filter(settings -> settings.getCreditor().getId() == creditor.getId())
                .findFirst();
    }

    private boolean meetsMinimumFinancingTerm(Purchaser purchaser, int financingTermInDays) {
        boolean isValid = financingTermInDays >= purchaser.getMinimumFinancingTermInDays();
        if (!isValid) {
            log.debug("Purchaser {} rejected: financing term {} days is less than minimum {} days",
                    purchaser.getName(), financingTermInDays, purchaser.getMinimumFinancingTermInDays());
        }
        return isValid;
    }

    private boolean exceedsMaxFinancingRate(BigDecimal financingRateInBps, BigDecimal maxFinancingRateInBps) {
        boolean exceeds = financingRateInBps.compareTo(maxFinancingRateInBps) > 0;
        if (exceeds) {
            log.debug("Purchaser rejected: financing rate {} bps exceeds maximum {} bps",
                    financingRateInBps, maxFinancingRateInBps);
        }
        return exceeds;
    }

    private Optional<PurchaserFinancingResult> findPurchaserWithLowestRate(List<PurchaserFinancingResult> eligiblePurchasers) {
        return eligiblePurchasers.stream()
                .min(Comparator.comparing(PurchaserFinancingResult::getFinancingRateInBps));
    }
}