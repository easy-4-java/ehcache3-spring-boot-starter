package org.ehcache.spring.boot;

import org.springframework.boot.autoconfigure.cache.CacheType;

/** The one configuration class supplied by this starter. */
final class CacheConfigurations {
    private CacheConfigurations() { }
    static CacheType getType(String configurationClassName) {
        if (EhCache3CacheConfiguration.class.getName().equals(configurationClassName)) {
            return CacheType.EHCACHE;
        }
        throw new IllegalStateException("Unknown configuration class " + configurationClassName);
    }
    static String getConfigurationClass(CacheType type) {
        if (type == CacheType.EHCACHE) return EhCache3CacheConfiguration.class.getName();
        throw new IllegalStateException("Unknown cache type " + type);
    }
}
