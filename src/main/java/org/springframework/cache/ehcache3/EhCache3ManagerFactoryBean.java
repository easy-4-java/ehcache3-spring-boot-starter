/*
 * Copyright (c) 2018, hiwepy (https://github.com/hiwepy).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.cache.ehcache3;

import javax.cache.CacheException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
    protected final Log logger = LogFactory.getLog(getClass());
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
        synchronized (EhCache3ManagerUtils.registryLock()) {
            String key = cacheManagerName == null ? EhCache3ManagerUtils.DEFAULT_NAME : cacheManagerName;
            CacheManager existing = EhCache3ManagerUtils.lookup(key);
            if (existing != null) {
                if (!shared && !acceptExisting) {
                    throw new CacheException("Ehcache manager name already registered: " + key);
                }
                cacheManager = existing;
                // Ehcache 2 shared factories retained shutdown responsibility even on reuse.
                locallyManaged = shared;
                return;
            }
            CacheManager created = configLocation == null ? EhCache3ManagerUtils.buildCacheManager(key)
                    : EhCache3ManagerUtils.buildCacheManager(key, configLocation);
            cacheManager = created;
            locallyManaged = true;
        }
    }

    @Override @Nullable public CacheManager getObject() { return cacheManager; }
    @Override public Class<? extends CacheManager> getObjectType() {
        return cacheManager != null ? cacheManager.getClass() : CacheManager.class;
    }
    @Override public boolean isSingleton() { return true; }

    @Override public void destroy() {
        synchronized (EhCache3ManagerUtils.registryLock()) {
            if (cacheManager != null && locallyManaged) {
                try { cacheManager.close(); }
                finally {
                    EhCache3ManagerUtils.release(cacheManager);
                    cacheManager = null;
                    locallyManaged = false;
                }
            }
        }
    }
}
