package org.springframework.cache.ehcache3;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;

import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;

/**
 * Tests for {@link EhCache3CacheManager}.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 */
class EhCache3CacheManagerTest {

    private CacheManager ehCacheManager;
    private EhCache3CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        ehCacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .withCache("testCache",
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(
                                String.class, Object.class,
                                ResourcePoolsBuilder.heap(10)))
                .build(true);
        cacheManager = new EhCache3CacheManager(ehCacheManager);
    }

    @AfterEach
    void tearDown() {
        if (ehCacheManager != null) {
            ehCacheManager.close();
        }
    }

    @Test
    @DisplayName("Constructor with CacheManager sets the cache manager")
    void constructorWithCacheManager() {
        assertThat(cacheManager.getCacheManager()).isSameAs(ehCacheManager);
    }

    @Test
    @DisplayName("Default constructor creates instance with null cache manager")
    void defaultConstructor() {
        EhCache3CacheManager defaultManager = new EhCache3CacheManager();
        assertThat(defaultManager.getCacheManager()).isNull();
    }

    @Test
    @DisplayName("setCacheManager and getCacheManager work correctly")
    void setAndGetCacheManager() {
        EhCache3CacheManager manager = new EhCache3CacheManager();
        manager.setCacheManager(ehCacheManager);
        assertThat(manager.getCacheManager()).isSameAs(ehCacheManager);
    }

    @Test
    @DisplayName("afterPropertiesSet builds cache manager when null")
    void afterPropertiesSetBuildsCacheManager() {
        EhCache3CacheManager manager = new EhCache3CacheManager();
        manager.afterPropertiesSet();
        assertThat(manager.getCacheManager()).isNotNull();
        assertThat(manager.getCacheManager().getStatus()).isEqualTo(org.ehcache.Status.AVAILABLE);
    }

    @Test
    @DisplayName("loadCaches loads all caches from the cache manager")
    void loadCachesLoadsAll() {
        cacheManager.afterPropertiesSet();
        Collection<Cache> caches = cacheManager.loadCaches();
        assertThat(caches).isNotEmpty();
        assertThat(caches.stream().anyMatch(c -> c.getName().equals("testCache"))).isTrue();
    }

    @Test
    @DisplayName("getCache returns cache for existing cache name")
    void getCacheReturnsExistingCache() {
        cacheManager.afterPropertiesSet();
        Cache cache = cacheManager.getCache("testCache");
        assertThat(cache).isNotNull();
        assertThat(cache.getName()).isEqualTo("testCache");
    }

    @Test
    @DisplayName("getCache returns null for non-existent cache")
    void getCacheReturnsNullForMissing() {
        cacheManager.afterPropertiesSet();
        Cache cache = cacheManager.getCache("nonExistent");
        assertThat(cache).isNull();
    }
}
