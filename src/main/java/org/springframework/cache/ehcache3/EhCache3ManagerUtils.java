package org.springframework.cache.ehcache3;


import java.io.IOException;
import java.io.InputStream;

import jakarta.cache.CacheException;

import org.ehcache.CacheManager;
import org.ehcache.config.Configuration;
import org.springframework.core.io.Resource;

/**
 * Convenient builder methods for EhCache 2.5+ {@link CacheManager} setup,
 * providing easy programmatic bootstrapping from a Spring-provided resource.
 * This is primarily intended for use within {@code @Bean} methods in a
 * Spring configuration class.
 *
 * <p>These methods are a simple alternative to custom {@link CacheManager} setup
 * code. For any advanced purposes, consider using {@link #parseConfiguration},
 * customizing the configuration object, and then calling the
 * {@link CacheManager#CacheManager(Configuration)} constructor.
 *
 * @author Juergen Hoeller
 * @since 4.1
 */
public abstract class EhCache3ManagerUtils {

	/**
	 * Build an EhCache {@link CacheManager} from the default configuration.
	 * <p>The CacheManager will be configured from "ehcache.xml" in the root of the class path
	 * (that is, default EhCache initialization - as defined in the EhCache docs - will apply).
	 * If no configuration file can be found, a fail-safe fallback configuration will be used.
	 * @return the new EhCache CacheManager
	 * @throws CacheException in case of configuration parsing failure
	 */
	public static CacheManager buildCacheManager() throws CacheException {
		return new CacheManager(ConfigurationFactory.parseConfiguration());
	}

	/**
	 * Build an EhCache {@link CacheManager} from the default configuration.
	 * <p>The CacheManager will be configured from "ehcache.xml" in the root of the class path
	 * (that is, default EhCache initialization - as defined in the EhCache docs - will apply).
	 * If no configuration file can be found, a fail-safe fallback configuration will be used.
	 * @param name the desired name of the cache manager
	 * @return the new EhCache CacheManager
	 * @throws CacheException in case of configuration parsing failure
	 */
	public static CacheManager buildCacheManager(String name) throws CacheException {
		Configuration configuration = ConfigurationFactory.parseConfiguration();
		configuration.setName(name);
		return new CacheManager(configuration);
	}

	/**
	 * Build an EhCache {@link CacheManager} from the given configuration resource.
	 * @param configLocation the location of the configuration file (as a Spring resource)
	 * @return the new EhCache CacheManager
	 * @throws CacheException in case of configuration parsing failure
	 */
	public static CacheManager buildCacheManager(Resource configLocation) throws CacheException {
		return new CacheManager(parseConfiguration(configLocation));
	}

	/**
	 * Build an EhCache {@link CacheManager} from the given configuration resource.
	 * @param name the desired name of the cache manager
	 * @param configLocation the location of the configuration file (as a Spring resource)
	 * @return the new EhCache CacheManager
	 * @throws CacheException in case of configuration parsing failure
	 */
	public static CacheManager buildCacheManager(String name, Resource configLocation) throws CacheException {
		Configuration configuration = parseConfiguration(configLocation);
		configuration.setName(name);
		return new CacheManager(configuration);
	}

	/**
	 * Parse EhCache configuration from the given resource, for further use with
	 * custom {@link CacheManager} creation.
	 * @param configLocation the location of the configuration file (as a Spring resource)
	 * @return the EhCache Configuration handle
	 * @throws CacheException in case of configuration parsing failure
	 * @see CacheManager#CacheManager(Configuration)
	 * @see CacheManager#create(Configuration)
	 */
	public static Configuration parseConfiguration(Resource configLocation) throws CacheException {
		InputStream is = null;
		try {
			is = configLocation.getInputStream();
			return ConfigurationFactory.parseConfiguration(is);
		}
		catch (IOException ex) {
			throw new CacheException("Failed to parse EhCache configuration resource", ex);
		}
		finally {
			if (is != null) {
				try {
					is.close();
				}
				catch (IOException ex) {
					// ignore
				}
			}
		}
	}

}
