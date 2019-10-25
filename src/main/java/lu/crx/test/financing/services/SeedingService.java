package lu.crx.test.financing.services;

import java.time.LocalDate;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import lu.crx.test.financing.entities.Creditor;
import lu.crx.test.financing.entities.Debtor;
import lu.crx.test.financing.entities.Invoice;
import lu.crx.test.financing.entities.Purchaser;
import lu.crx.test.financing.entities.PurchaserFinancingSettings;
import org.springframework.stereotype.Service;

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
        // creditors
        creditor1 = new Creditor();
        creditor1.setName("Coffee Beans LLC");
        entityManager.persist(creditor1);

        creditor2 = new Creditor();
        creditor2.setName("Home Brew");
        entityManager.persist(creditor2);

        creditor3 = new Creditor();
        creditor3.setName("Beanstalk");
        entityManager.persist(creditor3);

        creditor4 = new Creditor();
        creditor4.setName("MyBeans");
        entityManager.persist(creditor4);

        creditor5 = new Creditor();
        creditor5.setName("Happy Bean");
        entityManager.persist(creditor5);

        // debtors
        debtor1 = new Debtor();
        debtor1.setName("Chocolate Factory");
        entityManager.persist(debtor1);

        debtor2 = new Debtor();
        debtor2.setName("Sweets Inc");
        entityManager.persist(debtor2);

        debtor3 = new Debtor();
        debtor3.setName("ChocoLoco");
        entityManager.persist(debtor3);

        // purchasers
        purchaser1 = new Purchaser();
        purchaser1.setName("FatRichBank");
        purchaser1.setMinimumFinancingTermInDays(10);
        purchaser1.getPurchaserFinancingSettings().add(PurchaserFinancingSettings.builder()
                .creditor(creditor1)
                .creditLineLimitInCents(10_000_000_00)
                .rateInBps(50)
                .build());
    }

    @Transactional
    public void seedFirstBatch() {
        entityManager.persist(Invoice.builder()
                .creditor(creditor1)
                .debtor(debtor1)
                .valueInCents(1_000_00)
                .maturityDate(LocalDate.now().plusDays(50))
                .build());
    }

    @Transactional
    public void seedSecondBatch() {
        entityManager.persist(Invoice.builder()
                .creditor(creditor1)
                .debtor(debtor1)
                .valueInCents(100)
                .maturityDate(LocalDate.now().plusDays(52))
                .build());
    }

}
