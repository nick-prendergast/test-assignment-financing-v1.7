package lu.crx.financing.services;

import jakarta.persistence.EntityManager;
import lu.crx.financing.entities.*;
import lu.crx.financing.entities.PurchaserFinancingResult;
import lu.crx.financing.repositories.InvoiceFinancingDetailsRepository;
import lu.crx.financing.repositories.InvoiceRepository;
import lu.crx.financing.repositories.PurchaserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceFinancingServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private PurchaserRepository purchaserRepository;

    @Mock
    private InvoiceFinancingDetailsRepository financingDetailsRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private PurchaserSelectionService purchaserSelectionService;

    @Mock
    private FinancingCalculationService calculationService;

    private InvoiceFinancingService invoiceFinancingService;

    @BeforeEach
    void setUp() {
        invoiceFinancingService = new InvoiceFinancingService(
                invoiceRepository,
                purchaserRepository,
                financingDetailsRepository,
                entityManager,
                purchaserSelectionService,
                calculationService
        );
    }

    @Test
    void shouldProcessInvoiceFinancingWithEligiblePurchaser() {
        // Given
        Creditor creditor = Creditor.builder()
                .id(1L)
                .name("Creditor1")
                .maxFinancingRateInBps(3)
                .build();

        Debtor debtor = Debtor.builder()
                .id(1L)
                .name("Debtor1")
                .build();

        Invoice invoice = Invoice.builder()
                .id(1L)
                .creditor(creditor)
                .debtor(debtor)
                .valueInCents(10_000_00L)
                .maturityDate(LocalDate.now().plusDays(30))
                .build();

        Purchaser purchaser1 = Purchaser.builder()
                .id(1L)
                .name("Purchaser1")
                .minimumFinancingTermInDays(20)
                .purchaserFinancingSettings(new HashSet<>(Collections.singletonList(
                        PurchaserFinancingSettings.builder()
                                .id(1L)
                                .creditor(creditor)
                                .annualRateInBps(50)
                                .build()
                )))
                .build();

        Purchaser purchaser2 = Purchaser.builder()
                .id(2L)
                .name("Purchaser2")
                .minimumFinancingTermInDays(20)
                .purchaserFinancingSettings(new HashSet<>(Collections.singletonList(
                        PurchaserFinancingSettings.builder()
                                .id(2L)
                                .creditor(creditor)
                                .annualRateInBps(40)
                                .build()
                )))
                .build();

        PurchaserFinancingResult financingResult = new PurchaserFinancingResult(
                purchaser2,
                3,
                30,
                LocalDate.now()
        );

        when(invoiceRepository.findNonFinancedInvoices()).thenReturn(Collections.singletonList(invoice));
        when(purchaserRepository.findAllWithFinancingSettings()).thenReturn(Arrays.asList(purchaser1, purchaser2));
        when(purchaserSelectionService.findBestPurchaser(eq(invoice), anyList(), any(LocalDate.class)))
                .thenReturn(Optional.of(financingResult));
        when(calculationService.calculateDiscountAmount(eq(10_000_00L), eq(3))).thenReturn(3_00L);
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);

        // When
        invoiceFinancingService.processInvoiceFinancing();

        // Then
        verify(invoiceRepository).findNonFinancedInvoices();
        verify(purchaserRepository).findAllWithFinancingSettings();
        verify(purchaserSelectionService).findBestPurchaser(eq(invoice), anyList(), any(LocalDate.class));
        verify(calculationService).calculateDiscountAmount(eq(10_000_00L), eq(3));

        verify(invoiceRepository).save(any(Invoice.class));

        verify(financingDetailsRepository).save(any());
    }

    @Test
    void shouldNotProcessInvoiceFinancingWhenNoEligiblePurchaser() {
        // Given
        Invoice invoice = Invoice.builder().build();

        when(invoiceRepository.findNonFinancedInvoices()).thenReturn(Collections.singletonList(invoice));
        when(purchaserRepository.findAllWithFinancingSettings()).thenReturn(Collections.emptyList());
        when(purchaserSelectionService.findBestPurchaser(eq(invoice), anyList(), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        // When
        invoiceFinancingService.processInvoiceFinancing();

        // Then
        verify(invoiceRepository).findNonFinancedInvoices();
        verify(purchaserRepository).findAllWithFinancingSettings();
        verify(purchaserSelectionService).findBestPurchaser(eq(invoice), anyList(), any(LocalDate.class));

        verify(invoiceRepository, never()).save(any());
        verify(financingDetailsRepository, never()).save(any());
        verify(calculationService, never()).calculateDiscountAmount(anyLong(), anyInt());
    }

    @Test
    void shouldFlushAndClearEntityManagerForLargeBatches() {
        // Given
        List<Invoice> invoices = new ArrayList<>();
        for (int i = 0; i < 2000; i++) {
            invoices.add(Invoice.builder().build());
        }

        PurchaserFinancingResult financingResult = new PurchaserFinancingResult(
                Purchaser.builder().build(),
                3,
                30,
                LocalDate.now()
        );

        when(invoiceRepository.findNonFinancedInvoices()).thenReturn(invoices);
        when(purchaserRepository.findAllWithFinancingSettings()).thenReturn(Collections.emptyList());
        when(purchaserSelectionService.findBestPurchaser(any(Invoice.class), anyList(), any(LocalDate.class)))
                .thenReturn(Optional.of(financingResult));
        when(calculationService.calculateDiscountAmount(anyLong(), anyInt())).thenReturn(3_00L);

        // When
        invoiceFinancingService.processInvoiceFinancing();

        // Then
        verify(entityManager, atLeastOnce()).flush();
        verify(entityManager, atLeastOnce()).clear();
    }
}