/*
 * Copyright 2012-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ehcache.spring.boot;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.autoconfigure.cache.CacheType;
import org.springframework.util.Assert;

/**
 * Mappings between {@link CacheType} and {@code @Configuration}.
 *
 * @author Phillip Webb
 * @author Eddu Melendez
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 */
final class CacheConfigurations {

	private static final Map<String, Class<?>> MAPPINGS;

	static {
		Map<String, Class<?>> mappings = new HashMap<String, Class<?>>();
		mappings.put("ehcache3", EhCache3CacheConfiguration.class);
		MAPPINGS = Collections.unmodifiableMap(mappings);
	}

	private CacheConfigurations() {
	}

	/** @return return the configuration class. */
	public static String getConfigurationClass(String cacheType) {
		Class<?> configurationClass = MAPPINGS.get(cacheType);
		Assert.state(configurationClass != null, "Unknown cache type " + cacheType);
		return configurationClass.getName();
	}

	/** @return return the type. */
	public static String getType(String configurationClassName) {
		for (Map.Entry<String, Class<?>> entry : MAPPINGS.entrySet()) {
			if (entry.getValue().getName().equals(configurationClassName)) {
				return entry.getKey();
			}
		}
		throw new IllegalStateException(
				"Unknown configuration class " + configurationClassName);
	}

}
