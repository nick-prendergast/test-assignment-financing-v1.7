package lu.crx.financing.repositories;

import lu.crx.financing.entities.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    @Query("SELECT i FROM Invoice i WHERE i.earlyPaymentAmountInCents IS NULL")
    List<Invoice> findNonFinancedInvoices();
}