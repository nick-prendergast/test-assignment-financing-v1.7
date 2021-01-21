package lu.crx.test.financing.services;

import java.time.LocalDate;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import lu.crx.test.financing.entities.Creditor;
import lu.crx.test.financing.entities.Debtor;
import lu.crx.test.financing.entities.Invoice;
import lu.crx.test.financing.entities.Purchaser;
import lu.crx.test.financing.entities.PurchaserFinancingSettings;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SeedingService {

    private EntityManager entityManager;

    private Creditor creditor1;
    private Creditor creditor2;
    private Creditor creditor3;
    private Creditor creditor4;
    private Creditor creditor5;

    private Debtor debtor1;
    private Debtor debtor2;
    private Debtor debtor3;

    private Purchaser purchaser1;
    private Purchaser purchaser2;
    private Purchaser purchaser3;

    public SeedingService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public void seedMasterData() {
        log.info("Seeding master data");

        // creditors
        creditor1 = Creditor.builder()
                .name("Coffee Beans LLC")
                .build();
        entityManager.persist(creditor1);

        creditor2 = Creditor.builder()
                .name("Home Brew")
                .build();
        entityManager.persist(creditor2);

        creditor3 = Creditor.builder()
                .name("Beanstalk")
                .build();
        entityManager.persist(creditor3);

        creditor4 = Creditor.builder()
                .name("MyBeans")
                .build();
        entityManager.persist(creditor4);

        creditor5 = Creditor.builder()
                .name("Happy Bean")
                .build();
        entityManager.persist(creditor5);

        // debtors
        debtor1 = Debtor.builder()
                .name("Chocolate Factory")
                .build();
        entityManager.persist(debtor1);

        debtor2 = Debtor.builder()
                .name("Sweets Inc")
                .build();
        entityManager.persist(debtor2);

        debtor3 = Debtor.builder()
                .name("ChocoLoco")
                .build();
        entityManager.persist(debtor3);

        // purchasers
        purchaser1 = Purchaser.builder()
                .name("FatRichBank")
                .minimumFinancingTermInDays(10)
                .purchaserFinancingSetting(PurchaserFinancingSettings.builder()
                        .creditor(creditor1)
                        .creditLineLimitInCents(10_000_000_00)
                        .rateInBps(50)
                        .build())
                .build();
        entityManager.persist(purchaser1);
    }

    @Transactional
    public void seedFirstBatch() {
        log.info("Seeding the first batch of invoices");

        entityManager.persist(Invoice.builder()
                .creditor(creditor1)
                .debtor(debtor1)
                .valueInCents(1_000_00)
                .maturityDate(LocalDate.now().plusDays(50))
                .build());
    }

    @Transactional
    public void seedSecondBatch() {
        log.info("Seeding the second batch of invoices");

        entityManager.persist(Invoice.builder()
                .creditor(creditor1)
                .debtor(debtor1)
                .valueInCents(100)
                .maturityDate(LocalDate.now().plusDays(52))
                .build());
    }

}
