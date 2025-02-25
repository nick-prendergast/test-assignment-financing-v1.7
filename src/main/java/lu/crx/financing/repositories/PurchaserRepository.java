package lu.crx.financing.repositories;

import lu.crx.financing.entities.Purchaser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaserRepository extends JpaRepository<Purchaser, Long> {

    @Query("SELECT DISTINCT p FROM Purchaser p LEFT JOIN FETCH p.purchaserFinancingSettings")
    List<Purchaser> findAllWithFinancingSettings();
}