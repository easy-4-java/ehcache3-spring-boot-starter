package org.springframework.cache.ehcache3;

import java.util.concurrent.TimeUnit;

import javax.cache.CacheManager;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.TouchedExpiryPolicy;

import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.stereotype.Component;

@Component
public class CachingSetup implements JCacheManagerCustomizer {

	@Override
	public void customize(CacheManager cacheManager) {
		if (cacheManager.getCache("people") != null) return;
		cacheManager.createCache("people",
				new MutableConfiguration<>()
						.setExpiryPolicyFactory(TouchedExpiryPolicy.factoryOf(new javax.cache.expiry.Duration(TimeUnit.SECONDS, 10)))
						.setStoreByValue(false).setStatisticsEnabled(true));
	}

}
