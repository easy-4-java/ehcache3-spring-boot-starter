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

import java.util.Collections;
import java.util.concurrent.Callable;

import org.ehcache.Status;
import org.ehcache.UserManagedCache;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueRetrievalException;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Spring {@link Cache} adapter implementation backed by an EhCache 3
 * {@link UserManagedCache}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 */
public class EhCache3Cache implements Cache {

	private final UserManagedCache<String, Object> cache;
	private final String name;


	/**
	 * Create an {@link EhCache3Cache} instance.
	 * @param ehcache backing Ehcache instance
	 * @param name the cache name
	 */
	public EhCache3Cache(UserManagedCache<String, Object> ehcache, String name) {
		Assert.notNull(ehcache, "Ehcache must not be null");
		Status status = ehcache.getStatus();
		Assert.isTrue(Status.AVAILABLE.equals(status),
				"An 'alive' Ehcache is required - current cache is " + status.toString());
		this.cache = ehcache;
		this.name = name;
	}


	@Override
	public final String getName() {
		return this.name;
	}

	@Override
	public final UserManagedCache<String, Object> getNativeCache() {
		return this.cache;
	}

	@Override
	@Nullable
	public ValueWrapper get(Object key) {
		Object value = this.cache.get(key.toString());
		return toValueWrapper(value);
	}

	@SuppressWarnings("unchecked")
	@Override
	@Nullable
	public <T> T get(Object key, Callable<T> valueLoader) {
		Object value = this.cache.get(key.toString());
		if (value != null) {
			return (T) value;
		}
		else {
			return loadValue(key, valueLoader);
		}
	}

	private <T> T loadValue(Object key, Callable<T> valueLoader) {
		T value;
		try {
			value = valueLoader.call();
		}
		catch (Throwable ex) {
			throw new ValueRetrievalException(key, valueLoader, ex);
		}
		put(key, value);
		return value;
	}

	@Override
	@SuppressWarnings("unchecked")
	@Nullable
	public <T> T get(Object key, @Nullable Class<T> type) {
		Object value = this.cache.get(key.toString());
		if (value != null && type != null && !type.isInstance(value)) {
			throw new IllegalStateException("Cached value is not of required type [" + type.getName() + "]: " + value);
		}
		return (T) value;
	}

	@Override
	public void put(Object key, @Nullable Object value) {
		this.cache.put(key.toString(), value);
	}

	@Override
	@Nullable
	public ValueWrapper putIfAbsent(Object key, @Nullable Object value) {
		Object existing = this.cache.putIfAbsent(key.toString(), value);
		return toValueWrapper(existing);
	}

	@Override
	public void evict(Object key) {
		this.cache.remove(key.toString());
	}

	@Override
	public void clear() {
		this.cache.clear();
	}


	@Nullable
	private ValueWrapper toValueWrapper(@Nullable Object value) {
		return (value != null ? new SimpleValueWrapper(value) : null);
	}

}
