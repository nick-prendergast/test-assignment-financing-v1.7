package lu.crx.financing.services;

import jakarta.persistence.EntityManager;
import lu.crx.financing.entities.Creditor;
import lu.crx.financing.entities.Debtor;
import lu.crx.financing.entities.Purchaser;
import lu.crx.financing.entities.PurchaserFinancingSettings;
import lu.crx.financing.entities.Invoice;
import lu.crx.financing.testconfig.TestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ContextConfiguration(classes = TestConfiguration.class)
class FinancingServiceIT {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private FinancingService financingService;

    /**
     * The test case based on example from README.md
     */
    @Test
    void testFinancing() {
        Creditor creditor = Creditor.builder()
                .maxFinancingRateInBps(3)
                .name("Creditor1")
                .build();

        Debtor debtor = Debtor.builder()
                .name("Debtor1")
                .build();

        Purchaser purchaser1 = Purchaser.builder()
                .name("Purchaser1")
                .minimumFinancingTermInDays(90)
                .purchaserFinancingSetting(PurchaserFinancingSettings.builder()
                        .annualRateInBps(50)
                        .creditor(creditor)
                        .build())
                .build();

        Purchaser purchaser2 = Purchaser.builder()
                .name("Purchaser1")
                .minimumFinancingTermInDays(90)
                .purchaserFinancingSetting(PurchaserFinancingSettings.builder()
                        .annualRateInBps(40)
                        .creditor(creditor)
                        .build())
                .build();

        Invoice invoice = Invoice.builder()
                .creditor(creditor)
                .debtor(debtor)
                .valueInCents(10_000_00L)
                .maturityDate(LocalDate.now().plusDays(30))
                .build();

        entityManager.persist(creditor);
        entityManager.persist(debtor);
        entityManager.persist(invoice);
        entityManager.persist(purchaser1);
        entityManager.persist(purchaser2);

        financingService.finance();

        assertEquals(9_997_00, invoice.getEarlyPaymentAmountInCents());
        assertEquals(300, invoice.getDiscountedAmountInCents());
        assertEquals(10_000_00, invoice.getEarlyPaymentAmountInCents() + invoice.getDiscountedAmountInCents());
    }
}
