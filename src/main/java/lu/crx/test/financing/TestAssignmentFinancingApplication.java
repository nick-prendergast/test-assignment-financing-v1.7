package lu.crx.test.financing;

import lu.crx.test.financing.services.FinancingService;
import lu.crx.test.financing.services.SeedingService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TestAssignmentFinancingApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestAssignmentFinancingApplication.class, args);
    }

    @Bean
    public CommandLineRunner run(SeedingService seedingService, FinancingService financingService) {
        return args -> {
            // seeding master data - creditors, debtors and purchasers
            seedingService.seedMasterData();

            // seeding first batch of invoices and running the financing
            seedingService.seedFirstBatch();
            financingService.finance();

            // seeding second batch of invoices and running the financing
            seedingService.seedSecondBatch();
            financingService.finance();
        };
    }

}
