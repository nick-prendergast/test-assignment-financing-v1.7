package lu.crx.financing.services;

import lu.crx.financing.entities.Creditor;
import lu.crx.financing.entities.Debtor;
import lu.crx.financing.entities.Purchaser;
import lu.crx.financing.entities.PurchaserFinancingSettings;
import lu.crx.financing.entities.Invoice;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestEntityManager
@TestPropertySource(locations = "classpath:application-test.properties")
class FinancingServiceIT {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TransactionTemplate template;

    @Autowired
    private InvoiceFinancingService financingService;

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
                .minimumFinancingTermInDays(20)
                .purchaserFinancingSetting(PurchaserFinancingSettings.builder()
                        .annualRateInBps(50)
                        .creditor(creditor)
                        .build())
                .build();

        Purchaser purchaser2 = Purchaser.builder()
                .name("Purchaser1")
                .minimumFinancingTermInDays(20)
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

        template.executeWithoutResult(transactionStatus -> {
            entityManager.persist(creditor);
            entityManager.persist(debtor);
            entityManager.persist(invoice);
            entityManager.persist(purchaser1);
            entityManager.persist(purchaser2);
        });

        financingService.processInvoiceFinancing();

        template.executeWithoutResult(transactionStatus -> {
            Invoice updatedInvoice = entityManager.find(Invoice.class, invoice.getId());

            assertEquals(9_997_00, updatedInvoice.getEarlyPaymentAmountInCents());
            assertEquals(300, updatedInvoice.getDiscountedAmountInCents());
            assertEquals(10_000_00, updatedInvoice.getEarlyPaymentAmountInCents() + updatedInvoice.getDiscountedAmountInCents());
        });
    }
}
