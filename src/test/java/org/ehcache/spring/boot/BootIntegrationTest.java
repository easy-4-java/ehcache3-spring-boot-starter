package org.ehcache.spring.boot;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizers;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.mock.env.MockEnvironment;

class BootIntegrationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(EhCache3CacheConfiguration.class)
            .withBean(CacheProperties.class, CacheProperties::new)
            .withBean(CacheManagerCustomizers.class,
                    () -> new CacheManagerCustomizers(java.util.Collections.emptyList()));

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

    @Test void realContextActivatesAndBacksOff() {
        runner.withPropertyValues("spring.cache.type=ehcache").run(context -> {
            assertThatOneNativeAndOneSpringManager(context);
        });
        runner.withPropertyValues("spring.cache.type=simple").run(context ->
                assertFalse(context.containsBean("ehCacheCacheManager")));
        runner.withPropertyValues("spring.cache.type=ehcache")
                .withBean(org.springframework.cache.CacheManager.class,
                        org.springframework.cache.concurrent.ConcurrentMapCacheManager::new)
                .run(context -> {
                    assertEquals(1, context.getBeansOfType(org.springframework.cache.CacheManager.class).size());
                    assertFalse(context.containsBean("ehCacheCacheManager"));
                });
    }

    private static void assertThatOneNativeAndOneSpringManager(
            org.springframework.boot.test.context.assertj.AssertableApplicationContext context) {
        assertEquals(1, context.getBeansOfType(org.ehcache.CacheManager.class).size());
        assertEquals(1, context.getBeansOfType(org.springframework.cache.CacheManager.class).size());
        assertNotNull(context.getBean(org.springframework.cache.ehcache3.EhCache3CacheManager.class)
                .getCache("classpath-default"));
    }
}
