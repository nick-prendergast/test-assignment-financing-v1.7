package lu.crx.test.financing;

import lu.crx.test.financing.services.FinancingService;
import lu.crx.test.financing.services.ReportingService;
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
    public CommandLineRunner run(
            SeedingService seedingService,
            FinancingService financingService,
            ReportingService reportingService) {
        return args -> {
            // seeding master data - creditors, debtors and purchasers
            seedingService.seedMasterData();

            // seeding the first batch of invoices, running the financing, printing out the results
            seedingService.seedFirstBatch();
            financingService.finance();
            reportingService.printFinancingReport();

            // seeding the second batch of invoices, running the financing, printing out the results
            seedingService.seedSecondBatch();
            financingService.finance();
            reportingService.printFinancingReport();

        };
    }

}
