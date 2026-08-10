package org.springframework.cache.ehcache3;

import static org.assertj.core.api.Assertions.assertThat;

import org.ehcache.CacheManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link EhCache3ManagerFactoryBean}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 */
class EhCache3ManagerFactoryBeanTest {

    @Test
    @DisplayName("FactoryBean creates CacheManager after properties set")
    void afterPropertiesSetCreatesCacheManager() throws Exception {
        EhCache3ManagerFactoryBean factoryBean = new EhCache3ManagerFactoryBean();
        factoryBean.afterPropertiesSet();

        CacheManager cacheManager = factoryBean.getObject();
        assertThat(cacheManager).isNotNull();
        assertThat(cacheManager.getStatus()).isEqualTo(org.ehcache.Status.AVAILABLE);

        factoryBean.destroy();
    }

    @Test
    @DisplayName("getObjectType returns CacheManager class")
    void getObjectType() {
        EhCache3ManagerFactoryBean factoryBean = new EhCache3ManagerFactoryBean();
        assertThat(factoryBean.getObjectType()).isEqualTo(CacheManager.class);
    }

    @Test
    @DisplayName("isSingleton returns true")
    void isSingleton() {
        EhCache3ManagerFactoryBean factoryBean = new EhCache3ManagerFactoryBean();
        assertThat(factoryBean.isSingleton()).isTrue();
    }

    @Test
    @DisplayName("getObject returns null before afterPropertiesSet")
    void getObjectReturnsNullBeforeInit() {
        EhCache3ManagerFactoryBean factoryBean = new EhCache3ManagerFactoryBean();
        assertThat(factoryBean.getObject()).isNull();
    }

    @Test
    @DisplayName("destroy closes the cache manager")
    void destroyClosesCacheManager() throws Exception {
        EhCache3ManagerFactoryBean factoryBean = new EhCache3ManagerFactoryBean();
        factoryBean.afterPropertiesSet();

        CacheManager cacheManager = factoryBean.getObject();
        assertThat(cacheManager).isNotNull();

        factoryBean.destroy();
        assertThat(cacheManager.getStatus()).isEqualTo(org.ehcache.Status.UNINITIALIZED);
    }

    @Test
    @DisplayName("setCacheManagerName works")
    void setCacheManagerName() throws Exception {
        EhCache3ManagerFactoryBean factoryBean = new EhCache3ManagerFactoryBean();
        factoryBean.setCacheManagerName("testManager");
        factoryBean.afterPropertiesSet();

        CacheManager cacheManager = factoryBean.getObject();
        assertThat(cacheManager).isNotNull();

        factoryBean.destroy();
    }
}
