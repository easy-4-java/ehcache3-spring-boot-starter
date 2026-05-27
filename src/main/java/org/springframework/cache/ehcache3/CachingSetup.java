package org.springframework.cache.ehcache3;

import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import jakarta.cache.CacheManager;
import jakarta.cache.configuration.MutableConfiguration;
import jakarta.cache.expiry.TouchedExpiryPolicy;

import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.stereotype.Component;

@Component
public class CachingSetup implements JCacheManagerCustomizer {
	
	@Override
	public void customize(CacheManager cacheManager) {
		cacheManager.createCache("people",
				new MutableConfiguration<>()
						.setExpiryPolicyFactory(TouchedExpiryPolicy.factoryOf(new Duration(Duration.SECONDS, 10)))
						.setStoreByValue(false).setStatisticsEnabled(true));
	}

}
