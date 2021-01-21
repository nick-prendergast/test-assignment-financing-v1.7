package lu.crx.test.financing.services;

import java.util.Collections;
import java.util.List;
import javax.transaction.Transactional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ReportingService {

    @Transactional
    public void printFinancingReport() {
        log.info("Latest financing results:");

        generateFinancingReport().stream()
                .map(FinancingReportDto::toString)
                .forEach(log::info);
    }

    private List<FinancingReportDto> generateFinancingReport() {

        // TODO
        // This is the reporting part that needs to be implemented. This method should return a list of DTO records.
        // The list should contain a record for each unique Purchaser/Creditor pair that participated in the latest
        // financing: if the Purchaser financed any invoices of this Creditor in this financing round, there should
        // be one record for this Purchaser/Creditor. If the Purchaser didn't finance any invoices of this Creditor,
        // there should be no record for this Purchaser/Creditor.

        return Collections.emptyList();
    }

    @Data
    public static class FinancingReportDto {

        private final String purchaserName;
        private final String creditorName;
        // the total amount that the Purchaser will pay to the Creditor for this financing
        private final long totalCreditorPaymentInCents;
        // the total amount of Purchaser interest for the latest financing of this Creditor
        private final long totalPurchaserInterestInCents;
    }

}
