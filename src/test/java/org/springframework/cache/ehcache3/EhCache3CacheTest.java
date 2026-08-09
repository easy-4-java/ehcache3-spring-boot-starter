package org.springframework.cache.ehcache3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.ehcache.CacheManager;
import org.ehcache.UserManagedCache;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.UserManagedCacheBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;

/**
 * Tests for {@link EhCache3Cache}.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 */
class EhCache3CacheTest {

    private UserManagedCache<String, Object> userManagedCache;
    private EhCache3Cache cache;

    @BeforeEach
    void setUp() {
        userManagedCache = UserManagedCacheBuilder.newUserManagedCacheBuilder(String.class, Object.class)
                .build(true);
        cache = new EhCache3Cache(userManagedCache, "testCache");
    }

    @AfterEach
    void tearDown() {
        if (userManagedCache != null) {
            userManagedCache.close();
        }
    }

    @Test
    @DisplayName("getName returns the cache name")
    void getName() {
        assertThat(cache.getName()).isEqualTo("testCache");
    }

    @Test
    @DisplayName("getNativeCache returns the underlying cache")
    void getNativeCache() {
        assertThat(cache.getNativeCache()).isSameAs(userManagedCache);
    }

    @Test
    @DisplayName("put and get work correctly")
    void putAndGet() {
        cache.put("key1", "value1");
        Cache.ValueWrapper wrapper = cache.get("key1");
        assertThat(wrapper).isNotNull();
        assertThat(wrapper.get()).isEqualTo("value1");
    }

    @Test
    @DisplayName("get returns null for non-existent key")
    void getReturnsNullForMissingKey() {
        Cache.ValueWrapper wrapper = cache.get("nonExistent");
        assertThat(wrapper).isNull();
    }

    @Test
    @DisplayName("get with type returns the value")
    void getWithType() {
        cache.put("key1", "value1");
        String value = cache.get("key1", String.class);
        assertThat(value).isEqualTo("value1");
    }

    @Test
    @DisplayName("get with type returns null for missing key")
    void getWithTypeReturnsNullForMissing() {
        String value = cache.get("nonExistent", String.class);
        assertThat(value).isNull();
    }

    @Test
    @DisplayName("get with type throws for wrong type")
    void getWithTypeThrowsForWrongType() {
        cache.put("key1", "value1");
        assertThatThrownBy(() -> cache.get("key1", Integer.class))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cached value is not of required type");
    }

    @Test
    @DisplayName("get with callable loads value when missing")
    void getWithCallable() {
        String value = cache.get("key1", () -> "loadedValue");
        assertThat(value).isEqualTo("loadedValue");
    }

    @Test
    @DisplayName("get with callable returns existing value")
    void getWithCallableReturnsExisting() {
        cache.put("key1", "existingValue");
        String value = cache.get("key1", () -> "newValue");
        assertThat(value).isEqualTo("existingValue");
    }

    @Test
    @DisplayName("get with callable throws on loader exception")
    void getWithCallableThrowsOnLoaderException() {
        assertThatThrownBy(() -> cache.get("key1", () -> {
            throw new RuntimeException("load failed");
        }))
                .isInstanceOf(Cache.ValueRetrievalException.class);
    }

    @Test
    @DisplayName("putIfAbsent returns null when key is absent")
    void putIfAbsentReturnsNullWhenAbsent() {
        Cache.ValueWrapper wrapper = cache.putIfAbsent("key1", "value1");
        assertThat(wrapper).isNull();
        assertThat(cache.get("key1").get()).isEqualTo("value1");
    }

    @Test
    @DisplayName("putIfAbsent returns existing value when key exists")
    void putIfAbsentReturnsExistingWhenPresent() {
        cache.put("key1", "existingValue");
        Cache.ValueWrapper wrapper = cache.putIfAbsent("key1", "newValue");
        assertThat(wrapper).isNotNull();
        assertThat(wrapper.get()).isEqualTo("existingValue");
    }

    @Test
    @DisplayName("evict removes the key")
    void evict() {
        cache.put("key1", "value1");
        cache.evict("key1");
        assertThat(cache.get("key1")).isNull();
    }

    @Test
    @DisplayName("clear removes all entries")
    void clear() {
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.clear();
        assertThat(cache.get("key1")).isNull();
        assertThat(cache.get("key2")).isNull();
    }

    @Test
    @DisplayName("Constructor throws for null cache")
    void constructorThrowsForNull() {
        assertThatThrownBy(() -> new EhCache3Cache(null, "test"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
