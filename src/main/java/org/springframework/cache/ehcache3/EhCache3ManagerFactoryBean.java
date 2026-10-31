package org.springframework.cache.ehcache3;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.cache.CacheException;
import org.ehcache.CacheManager;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;

/**
 * Creates or borrows an Ehcache 3 manager within this library's ClassLoader.
 * Ehcache 3 itself provides no named/global manager lookup.
 */
public class EhCache3ManagerFactoryBean
        implements FactoryBean<CacheManager>, InitializingBean, DisposableBean {
    private static final String DEFAULT_NAME = "default";
    private static final String SHARED_NAME = "\u0000shared";
    private static final Map<String, CacheManager> REGISTRY = new HashMap<>();

    @Nullable private Resource configLocation;
    @Nullable private String cacheManagerName;
    private boolean acceptExisting;
    private boolean shared;
    @Nullable private CacheManager cacheManager;
    private boolean locallyManaged;

    public void setConfigLocation(Resource configLocation) { this.configLocation = configLocation; }
    public void setCacheManagerName(String cacheManagerName) { this.cacheManagerName = cacheManagerName; }
    public void setAcceptExisting(boolean acceptExisting) { this.acceptExisting = acceptExisting; }
    public void setShared(boolean shared) { this.shared = shared; }

    @Override public void afterPropertiesSet() throws CacheException {
        synchronized (REGISTRY) {
            String key = shared ? SHARED_NAME : cacheManagerName == null ? DEFAULT_NAME : cacheManagerName;
            CacheManager existing = REGISTRY.get(key);
            if (existing != null) {
                if (!shared && !acceptExisting) {
                    throw new CacheException("Ehcache manager name already registered: " + key);
                }
                cacheManager = existing;
                locallyManaged = false;
                return;
            }
            CacheManager created = configLocation == null ? EhCache3ManagerUtils.buildCacheManager()
                    : EhCache3ManagerUtils.buildCacheManager(configLocation);
            REGISTRY.put(key, created);
            cacheManager = created;
            locallyManaged = true;
        }
    }

    @Override @Nullable public CacheManager getObject() { return cacheManager; }
    @Override public Class<? extends CacheManager> getObjectType() { return CacheManager.class; }
    @Override public boolean isSingleton() { return true; }

    @Override public void destroy() {
        synchronized (REGISTRY) {
            if (cacheManager != null && locallyManaged) {
                try { cacheManager.close(); }
                finally {
                    // Remove by identity even if setters changed after initialization.
                    Iterator<Map.Entry<String, CacheManager>> entries = REGISTRY.entrySet().iterator();
                    while (entries.hasNext()) {
                        if (entries.next().getValue() == cacheManager) entries.remove();
                    }
                    cacheManager = null;
                    locallyManaged = false;
                }
            }
        }
    }
}
