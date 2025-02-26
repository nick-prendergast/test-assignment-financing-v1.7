package lu.crx.financing.services.financing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lu.crx.financing.entities.Invoice;
import lu.crx.financing.entities.Purchaser;
import lu.crx.financing.entities.Creditor;
import lu.crx.financing.entities.PurchaserFinancingResult;
import lu.crx.financing.services.eligibility.PurchaserEligibilityService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaserSelectionService {

    private final FinancingCalculationService calculationService;
    private final PurchaserEligibilityService eligibilityService;

    public Optional<PurchaserFinancingResult> findBestPurchaserForInvoice(
            Invoice invoice, List<Purchaser> purchasers, LocalDate financingDate) {

        Creditor creditor = invoice.getCreditor();
        int financingTermInDays = calculationService.calculateFinancingTerm(financingDate, invoice.getMaturityDate());

        return purchasers.stream()
                .filter(purchaser -> eligibilityService.isPurchaserEligible(
                        purchaser, creditor, financingTermInDays, creditor.getMaxFinancingRateInBps()))
                .map(purchaser -> eligibilityService.createFinancingResult(
                        purchaser, creditor, financingTermInDays, financingDate))
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        this::selectPurchaserWithLowestRate
                ));
    }

    private Optional<PurchaserFinancingResult> selectPurchaserWithLowestRate(List<PurchaserFinancingResult> eligiblePurchasers) {
        return eligiblePurchasers.stream()
                .min(Comparator.comparing(PurchaserFinancingResult::getFinancingRateInBps)
                        .thenComparing(PurchaserFinancingResult::getExactFinancingRate));
    }
}