package lu.crx.financing.utils;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class EntityManagerUtilTest {

    @Mock
    private EntityManager entityManager;

    private EntityManagerUtil entityManagerUtil;

    @BeforeEach
    void setUp() {
        entityManagerUtil = new EntityManagerUtil(entityManager);
    }

    @Test
    void shouldFlushAndClearEntityManager() {
        // Given
        int processedCount = 100;

        // When
        entityManagerUtil.flushAndClearEntityManager(processedCount);

        // Then
        verify(entityManager).flush();
        verify(entityManager).clear();
        verifyNoMoreInteractions(entityManager);
    }

    @Test
    void shouldHandleZeroProcessedCount() {
        // Given
        int processedCount = 0;

        // When
        entityManagerUtil.flushAndClearEntityManager(processedCount);

        // Then
        verify(entityManager).flush();
        verify(entityManager).clear();
        verifyNoMoreInteractions(entityManager);
    }
}