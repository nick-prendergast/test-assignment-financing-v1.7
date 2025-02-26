package lu.crx.financing.utils;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EntityManagerUtil {

    private final EntityManager entityManager;

    public void flushAndClearEntityManager(int processedCount) {
        entityManager.flush();
        entityManager.clear();
        log.info("Processed {} invoices so far", processedCount);
    }
}