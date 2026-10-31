package org.springframework.cache.ehcache3;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.concurrent.Callable;
import org.ehcache.Status;
import org.ehcache.UserManagedCache;
import org.ehcache.core.Ehcache;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/** Spring Cache adapter for Ehcache 3. */
public class EhCache3Cache implements Cache {
    private static final String DEFAULT_NAME = "default";
    private final org.ehcache.Cache<Object, Object> cache;
    private final String name;

    /** Retained for callers supplying a user-managed cache. */
    public EhCache3Cache(UserManagedCache<String, Object> ehcache) {
        this(DEFAULT_NAME, ehcache);
    }

    /** Supply the alias explicitly: Ehcache 3 caches do not expose their manager alias. */
    public EhCache3Cache(String name, UserManagedCache<?, ?> ehcache) {
        Assert.notNull(ehcache, "Ehcache must not be null");
        Assert.isTrue(Status.AVAILABLE.equals(ehcache.getStatus()), "An available Ehcache is required");
        Assert.hasText(name, "Cache alias must not be empty");
        this.name = name;
        this.cache = cast(ehcache);
    }

    /** Adapt a cache owned by an Ehcache 3 CacheManager. */
    public EhCache3Cache(String name, org.ehcache.Cache<?, ?> ehcache) {
        Assert.hasText(name, "Cache alias must not be empty");
        Assert.notNull(ehcache, "Ehcache must not be null");
        this.name = name;
        this.cache = cast(ehcache);
    }

    @SuppressWarnings("unchecked")
    private static org.ehcache.Cache<Object, Object> cast(org.ehcache.Cache<?, ?> cache) {
        return (org.ehcache.Cache<Object, Object>) cache;
    }

    @Override public final String getName() { return name; }

    /** The historic concrete return type cannot represent every Ehcache 3 implementation. */
    @Override public final Ehcache getNativeCache() {
        if (!(cache instanceof Ehcache)) {
            throw new IllegalStateException("Native cache is not an Ehcache: " + cache.getClass().getName());
        }
        return (Ehcache) cache;
    }

    @Override @Nullable public ValueWrapper get(Object key) {
        Object value = cache.get(key);
        return value == null ? null : new SimpleValueWrapper(unmask(value));
    }

    @Override @SuppressWarnings("unchecked") @Nullable
    public <T> T get(Object key, @Nullable Class<T> type) {
        Object value = unmask(cache.get(key));
        if (value != null && type != null && !type.isInstance(value)) {
            throw new IllegalStateException("Cached value is not of required type [" + type.getName() + "]: " + value);
        }
        return (T) value;
    }

    @Override @SuppressWarnings("unchecked") @Nullable
    public <T> T get(Object key, Callable<T> valueLoader) {
        Object value = cache.get(key);
        if (value != null) return (T) unmask(value);
        // Separate adapters for one native cache must share this lock.
        synchronized (cache) {
            value = cache.get(key);
            if (value != null) return (T) unmask(value);
            T loaded;
            try { loaded = valueLoader.call(); }
            catch (Throwable ex) { throw new ValueRetrievalException(key, valueLoader, ex); }
            cache.put(key, mask(loaded));
            return loaded;
        }
    }

    @Override public void put(Object key, @Nullable Object value) { cache.put(key, mask(value)); }
    @Override @Nullable public ValueWrapper putIfAbsent(Object key, @Nullable Object value) {
        Object previous = cache.putIfAbsent(key, mask(value));
        return previous == null ? null : new SimpleValueWrapper(unmask(previous));
    }
    @Override public void evict(Object key) { cache.remove(key); }
    @Override public void clear() { cache.clear(); }

    private Object mask(@Nullable Object value) {
        if (value != null) return value;
        Class<?> valueType = cache.getRuntimeConfiguration().getValueType();
        if (!valueType.isAssignableFrom(NullValue.class)) {
            throw new IllegalArgumentException("Cache '" + name + "' value type " + valueType.getName()
                    + " cannot store the Spring cached-null marker");
        }
        return NullValue.INSTANCE;
    }
    @Nullable private static Object unmask(@Nullable Object value) {
        return value instanceof NullValue ? null : value;
    }
    private static final class NullValue implements Serializable {
        private static final long serialVersionUID = 1L;
        private static final NullValue INSTANCE = new NullValue();
        private Object readResolve() throws ObjectStreamException { return INSTANCE; }
    }
}
