package lu.crx.financing.repositories;

import lu.crx.financing.entities.InvoiceFinancingDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceFinancingDetailsRepository extends JpaRepository<InvoiceFinancingDetails, Long> {
}