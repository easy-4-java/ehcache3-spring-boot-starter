package org.springframework.cache.ehcache3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.ehcache.CacheManager;
import org.ehcache.config.Configuration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

/**
 * Tests for {@link EhCache3ManagerUtils}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 */
class EhCache3ManagerUtilsTest {

    @Test
    @DisplayName("buildCacheManager creates a CacheManager")
    void buildCacheManager() {
        CacheManager cacheManager = EhCache3ManagerUtils.buildCacheManager();
        assertThat(cacheManager).isNotNull();
        assertThat(cacheManager.getStatus()).isEqualTo(org.ehcache.Status.AVAILABLE);
        cacheManager.close();
    }

    @Test
    @DisplayName("buildCacheManager with name creates a CacheManager")
    void buildCacheManagerWithName() {
        CacheManager cacheManager = EhCache3ManagerUtils.buildCacheManager("testManager");
        assertThat(cacheManager).isNotNull();
        assertThat(cacheManager.getStatus()).isEqualTo(org.ehcache.Status.AVAILABLE);
        cacheManager.close();
    }

    @Test
    @DisplayName("parseConfiguration throws for invalid resource")
    void parseConfigurationThrowsForInvalid() throws Exception {
        Resource resource = mock(Resource.class);
        when(resource.getURL()).thenThrow(new IOException("Cannot read resource"));

        assertThatThrownBy(() -> EhCache3ManagerUtils.parseConfiguration(resource))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to parse EhCache configuration resource");
    }

    @Test
    @DisplayName("buildCacheManager with resource and name throws for invalid resource")
    void buildCacheManagerWithResourceAndNameThrowsForInvalid() throws Exception {
        Resource resource = mock(Resource.class);
        when(resource.getURL()).thenThrow(new IOException("Cannot read resource"));

        assertThatThrownBy(() -> EhCache3ManagerUtils.buildCacheManager("test", resource))
                .isInstanceOf(RuntimeException.class);
    }
}
