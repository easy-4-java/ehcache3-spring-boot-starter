package org.springframework.cache.ehcache3;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.Configuration;
import org.ehcache.xml.XmlConfiguration;
import org.springframework.core.io.Resource;

/**
 * Convenient builder methods for EhCache 3.x {@link CacheManager} setup,
 * providing easy programmatic bootstrapping from a Spring-provided resource.
 * This is primarily intended for use within {@code @Bean} methods in a
 * Spring configuration class.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public abstract class EhCache3ManagerUtils {

	/**
	 * Build an EhCache {@link CacheManager} from the default configuration.
	 * @return the new EhCache CacheManager
	 */
	public static CacheManager buildCacheManager() {
		return CacheManagerBuilder.newCacheManagerBuilder().build(true);
	}

	/**
	 * Build an EhCache {@link CacheManager} with the given name.
	 * @param name the desired name of the cache manager
	 * @return the new EhCache CacheManager
	 */
	public static CacheManager buildCacheManager(String name) {
		return CacheManagerBuilder.newCacheManagerBuilder().build(true);
	}

	/**
	 * Build an EhCache {@link CacheManager} from the given configuration resource.
	 * @param configLocation the location of the configuration file (as a Spring resource)
	 * @return the new EhCache CacheManager
	 */
	public static CacheManager buildCacheManager(Resource configLocation) {
		Configuration configuration = parseConfiguration(configLocation);
		return CacheManagerBuilder.newCacheManager(configuration);
	}

	/**
	 * Build an EhCache {@link CacheManager} from the given configuration resource.
	 * @param name the desired name of the cache manager
	 * @param configLocation the location of the configuration file (as a Spring resource)
	 * @return the new EhCache CacheManager
	 */
	public static CacheManager buildCacheManager(String name, Resource configLocation) {
		Configuration configuration = parseConfiguration(configLocation);
		return CacheManagerBuilder.newCacheManager(configuration);
	}

	/**
	 * Parse EhCache configuration from the given resource.
	 * @param configLocation the location of the configuration file (as a Spring resource)
	 * @return the EhCache Configuration handle
	 */
	public static Configuration parseConfiguration(Resource configLocation) {
		try {
			URL url = configLocation.getURL();
			return new XmlConfiguration(url);
		}
		catch (Exception ex) {
			throw new RuntimeException("Failed to parse EhCache configuration resource", ex);
		}
	}

}
