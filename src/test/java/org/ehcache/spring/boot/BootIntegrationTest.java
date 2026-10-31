package org.ehcache.spring.boot;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.mock.env.MockEnvironment;

class BootIntegrationTest {
    @Test void cacheTypeConditionUsesBoot2Binder() {
        ConditionContext context = mock(ConditionContext.class);
        MockEnvironment environment = new MockEnvironment();
        when(context.getEnvironment()).thenReturn(environment);
        AnnotationMetadata metadata = AnnotationMetadata.introspect(EhCache3CacheConfiguration.class);
        CacheCondition condition = new CacheCondition();
        assertTrue(condition.getMatchOutcome(context, metadata).isMatch());
        environment.setProperty("spring.cache.type", "ehcache");
        assertTrue(condition.getMatchOutcome(context, metadata).isMatch());
        environment.setProperty("spring.cache.type", "simple");
        assertFalse(condition.getMatchOutcome(context, metadata).isMatch());
    }
}
