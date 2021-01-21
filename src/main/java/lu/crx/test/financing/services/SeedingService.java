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
                .name("RichBank")
                .minimumFinancingTermInDays(10)
                .purchaserFinancingSetting(PurchaserFinancingSettings.builder()
                        .creditor(creditor1)
                        .rateInBps(50)
                        .build())
                .purchaserFinancingSetting(PurchaserFinancingSettings.builder()
                        .creditor(creditor2)
                        .rateInBps(60)
                        .build())
                .purchaserFinancingSetting(PurchaserFinancingSettings.builder()
                        .creditor(creditor3)
                        .rateInBps(30)
                        .build())
                .build();
        entityManager.persist(purchaser1);

        purchaser2 = Purchaser.builder()
                .name("FatBank")
                .minimumFinancingTermInDays(12)
                .purchaserFinancingSetting(PurchaserFinancingSettings.builder()
                        .creditor(creditor1)
                        .rateInBps(40)
                        .build())
                .purchaserFinancingSetting(PurchaserFinancingSettings.builder()
                        .creditor(creditor2)
                        .rateInBps(80)
                        .build())
                .purchaserFinancingSetting(PurchaserFinancingSettings.builder()
                        .creditor(creditor3)
                        .rateInBps(25)
                        .build())
                .build();
        entityManager.persist(purchaser2);

        purchaser3 = Purchaser.builder()
                .name("MegaBank")
                .minimumFinancingTermInDays(8)
                .purchaserFinancingSetting(PurchaserFinancingSettings.builder()
                        .creditor(creditor1)
                        .rateInBps(50)
                        .build())
                .purchaserFinancingSetting(PurchaserFinancingSettings.builder()
                        .creditor(creditor2)
                        .rateInBps(60)
                        .build())
                .purchaserFinancingSetting(PurchaserFinancingSettings.builder()
                        .creditor(creditor3)
                        .rateInBps(45)
                        .build())
                .build();
        entityManager.persist(purchaser3);
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
        // TODO add more invoices
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
        // TODO add more invoices
    }

}
