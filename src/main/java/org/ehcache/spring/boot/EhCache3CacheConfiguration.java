package org.ehcache.spring.boot;


import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ResourceCondition;
import org.springframework.boot.cache.autoconfigure.CacheManagerCustomizers;
import org.springframework.boot.cache.autoconfigure.CacheProperties;
import org.springframework.cache.ehcache3.EhCache3CacheManager;
import org.springframework.cache.ehcache3.EhCache3ManagerUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;


/**
 * EhCache cache configuration. Only kick in if a configuration file location is set or if
 * a default configuration file exists.
 *
 * @author Eddu Melendez
 * @author Stephane Nicoll
 * @since 1.3.0
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 */
@Configuration
@ConditionalOnClass({ Cache.class, EhCache3CacheManager.class })
@ConditionalOnMissingBean(org.springframework.cache.CacheManager.class)
@Conditional({ CacheCondition.class,
	EhCache3CacheConfiguration.ConfigAvailableCondition.class })
public class EhCache3CacheConfiguration {

	private final CacheProperties cacheProperties;

	private final CacheManagerCustomizers customizers;

	EhCache3CacheConfiguration(CacheProperties cacheProperties,
			CacheManagerCustomizers customizers) {
		this.cacheProperties = cacheProperties;
		this.customizers = customizers;
	}

	@Bean
	public EhCache3CacheManager cacheManager(CacheManager ehCacheCacheManager) {
		return this.customizers.customize(new EhCache3CacheManager(ehCacheCacheManager));
	}

	@Bean
	@ConditionalOnMissingBean
	public CacheManager ehCacheCacheManager() {
		Resource location = this.cacheProperties.resolveConfigLocation(null);
		if (location != null) {
			return EhCache3ManagerUtils.buildCacheManager(location);
		}
		return EhCache3ManagerUtils.buildCacheManager();
	}

	/**
	 * Determine if the EhCache configuration is available. This either kick in if a
	 * default configuration has been found or if property referring to the file to use
	 * has been set.
	 */
	static class ConfigAvailableCondition extends ResourceCondition {

		ConfigAvailableCondition() {
			super("EhCache", "spring.cache.ehcache", "config", "classpath:/ehcache.xml");
		}

	}

}
