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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;

/**
 * {@link FactoryBean} that exposes an EhCache 3 {@link CacheManager}
 * instance, configured from a specified config location.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @see #setConfigLocation
 */
public class EhCache3ManagerFactoryBean implements FactoryBean<CacheManager>, InitializingBean, DisposableBean {

	protected final Log logger = LogFactory.getLog(getClass());

	@Nullable
	private Resource configLocation;

	@Nullable
	private String cacheManagerName;

	private boolean shared = false;

	@Nullable
	private CacheManager cacheManager;

	private boolean locallyManaged = true;


	/**
	 * Set the location of the EhCache config file.
	 * @param configLocation the config resource
	 */
	public void setConfigLocation(Resource configLocation) {
		this.configLocation = configLocation;
	}

	/**
	 * Set the name of the EhCache CacheManager.
	 * @param cacheManagerName the cache manager name
	 */
	public void setCacheManagerName(String cacheManagerName) {
		this.cacheManagerName = cacheManagerName;
	}

	/**
	 * Set whether the EhCache CacheManager should be shared.
	 * @param shared true to share
	 */
	public void setShared(boolean shared) {
		this.shared = shared;
	}


	@Override
	/**
	 * <p>After properties set.</p>
	 */
	public void afterPropertiesSet() {
		if (logger.isInfoEnabled()) {
			logger.info("Initializing EhCache CacheManager" +
					(this.cacheManagerName != null ? " '" + this.cacheManagerName + "'" : ""));
		}

		CacheManagerBuilder<CacheManager> builder = CacheManagerBuilder.newCacheManagerBuilder();
		if (this.configLocation != null) {
			this.cacheManager = EhCache3ManagerUtils.buildCacheManager(this.configLocation);
		}
		else {
			this.cacheManager = EhCache3ManagerUtils.buildCacheManager();
		}
	}


	@Override
	@Nullable
	/** @return return the object. */
	public CacheManager getObject() {
		return this.cacheManager;
	}

	@Override
	/** @return return the object type. */
	public Class<? extends CacheManager> getObjectType() {
		return (this.cacheManager != null ? this.cacheManager.getClass() : CacheManager.class);
	}

	@Override
	/** @return return whether singleton is enabled. */
	public boolean isSingleton() {
		return true;
	}

	@Override
	/**
	 * <p>Destroy.</p>
	 */
	public void destroy() {
		if (this.cacheManager != null && this.locallyManaged) {
			if (logger.isInfoEnabled()) {
				logger.info("Shutting down EhCache CacheManager" +
						(this.cacheManagerName != null ? " '" + this.cacheManagerName + "'" : ""));
			}
			this.cacheManager.close();
		}
	}

}
