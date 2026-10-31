package org.springframework.cache.ehcache3;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;
import org.ehcache.CacheManager;
import org.ehcache.Status;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.builders.UserManagedCacheBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.cache.Cache;
import org.springframework.core.io.ByteArrayResource;

class EhCache3IntegrationTest {
    private static final String XML = "<config xmlns='http://www.ehcache.org/v3'>"
            + "<cache alias='numbers'><key-type>java.lang.Integer</key-type>"
            + "<value-type>java.lang.String</value-type><heap unit='entries'>10</heap></cache></config>";

    @Test void cacheOperationsAndTypedManagerAliases() {
        CacheManager nativeManager = CacheManagerBuilder.newCacheManagerBuilder()
                .withCache("numbers", CacheConfigurationBuilder.newCacheConfigurationBuilder(
                        Integer.class, String.class, ResourcePoolsBuilder.heap(10)))
                .withCache("objects", CacheConfigurationBuilder.newCacheConfigurationBuilder(
                        Object.class, Object.class, ResourcePoolsBuilder.heap(10))).build(true);
        try {
            EhCache3CacheManager manager = new EhCache3CacheManager(nativeManager);
            manager.afterPropertiesSet();
            Cache numbers = manager.getCache("numbers");
            assertNotNull(numbers);
            assertEquals("numbers", numbers.getName());
            numbers.put(42, "answer");
            assertEquals("answer", numbers.get(42, String.class));
            assertNotNull(numbers.putIfAbsent(42, "other"));
            assertEquals("answer", numbers.get(42, String.class));
            numbers.evict(42);
            assertNull(numbers.get(42));
            numbers.put(43, "other");
            numbers.clear();
            assertNull(numbers.get(43));
            assertEquals("objects", manager.getCache("objects").getName());
            Cache objects = manager.getCache("objects");
            objects.put("null", null);
            assertNotNull(objects.get("null"));
            assertNull(objects.get("null").get());
            assertNotNull(objects.putIfAbsent("null", "replacement"));
            assertNull(objects.get("null").get());
            assertThrows(IllegalArgumentException.class, () -> numbers.put(99, null));
            nativeManager.createCache("dynamic", CacheConfigurationBuilder.newCacheConfigurationBuilder(
                    Long.class, String.class, ResourcePoolsBuilder.heap(10)));
            assertEquals("dynamic", manager.getCache("dynamic").getName());
            assertNotNull(((EhCache3Cache) numbers).getNativeCache());
            assertEquals(Status.AVAILABLE, nativeManager.getStatus());
        } finally { nativeManager.close(); }
    }

    @Test void loaderIsOnceAcrossAdaptersAndRetriesAfterFailure() throws Exception {
        org.ehcache.UserManagedCache<String, Object> nativeCache =
                UserManagedCacheBuilder.newUserManagedCacheBuilder(String.class, Object.class)
                        .withResourcePools(ResourcePoolsBuilder.heap(10)).build(true);
        try {
            EhCache3Cache first = new EhCache3Cache(nativeCache);
            EhCache3Cache second = new EhCache3Cache("alias", nativeCache);
            AtomicInteger loads = new AtomicInteger();
            CountDownLatch start = new CountDownLatch(1);
            AtomicReference<Throwable> failure = new AtomicReference<>();
            Thread[] threads = IntStream.range(0, 8).mapToObj(i -> new Thread(() -> {
                try {
                    start.await();
                    assertEquals("loaded", (i % 2 == 0 ? first : second).get("key", () -> {
                        loads.incrementAndGet();
                        Thread.sleep(10);
                        return "loaded";
                    }));
                } catch (Throwable ex) { failure.compareAndSet(null, ex); }
            })).toArray(Thread[]::new);
            for (Thread thread : threads) thread.start();
            start.countDown();
            for (Thread thread : threads) thread.join();
            assertNull(failure.get());
            assertEquals(1, loads.get());
            assertThrows(Cache.ValueRetrievalException.class,
                    () -> first.get("failed", () -> { throw new IllegalStateException("retry"); }));
            assertEquals("ok", second.get("failed", () -> "ok"));
            assertEquals("default", first.getName());
        } finally { nativeCache.close(); }
    }

    @Test void factoryRegistryAndLifecycle() {
        EhCache3ManagerFactoryBean owner = new EhCache3ManagerFactoryBean();
        owner.setCacheManagerName("Case");
        EhCache3ManagerFactoryBean borrower = new EhCache3ManagerFactoryBean();
        borrower.setCacheManagerName("Case");
        borrower.setAcceptExisting(true);
        EhCache3ManagerFactoryBean otherCase = new EhCache3ManagerFactoryBean();
        otherCase.setCacheManagerName("case");
        owner.afterPropertiesSet();
        borrower.afterPropertiesSet();
        otherCase.afterPropertiesSet();
        try {
            assertSame(owner.getObject(), borrower.getObject());
            assertNotSame(owner.getObject(), otherCase.getObject());
            EhCache3ManagerFactoryBean duplicate = new EhCache3ManagerFactoryBean();
            duplicate.setCacheManagerName("Case");
            assertThrows(javax.cache.CacheException.class, duplicate::afterPropertiesSet);
            borrower.destroy();
            assertEquals(Status.AVAILABLE, owner.getObject().getStatus());
        } finally {
            owner.destroy();
            otherCase.destroy();
        }
        assertEquals(Status.UNINITIALIZED, borrower.getObject().getStatus());
        EhCache3ManagerFactoryBean replacement = new EhCache3ManagerFactoryBean();
        replacement.setCacheManagerName("Case");
        replacement.afterPropertiesSet();
        replacement.destroy();
    }

    @Test void sharedOverridesAcceptExistingAndClosesOnlyOwner() {
        EhCache3ManagerFactoryBean owner = new EhCache3ManagerFactoryBean();
        owner.setCacheManagerName("shared-name");
        owner.setShared(true);
        EhCache3ManagerFactoryBean borrower = new EhCache3ManagerFactoryBean();
        borrower.setCacheManagerName("shared-name");
        borrower.setAcceptExisting(true);
        owner.afterPropertiesSet();
        borrower.afterPropertiesSet();
        CacheManager manager = owner.getObject();
        assertSame(manager, borrower.getObject());
        borrower.destroy();
        assertEquals(Status.AVAILABLE, manager.getStatus());
        owner.destroy();
        assertEquals(Status.UNINITIALIZED, manager.getStatus());
    }

    @Test void defaultBuilderReadsClasspathXml() {
        CacheManager manager = EhCache3ManagerUtils.buildCacheManager();
        try {
            assertNotNull(manager.getCache("classpath-default", String.class, String.class));
        } finally { manager.close(); }
    }

    @Test void namedBuilderAndFactoryShareRegistry() {
        CacheManager manager = EhCache3ManagerUtils.buildCacheManager("named-utils");
        EhCache3ManagerFactoryBean borrower = new EhCache3ManagerFactoryBean();
        borrower.setCacheManagerName("named-utils");
        borrower.setAcceptExisting(true);
        borrower.afterPropertiesSet();
        try {
            assertSame(manager, borrower.getObject());
            assertThrows(javax.cache.CacheException.class,
                    () -> EhCache3ManagerUtils.buildCacheManager("named-utils"));
        } finally {
            borrower.destroy();
            manager.close();
        }
    }

    @Test void namedResourceBuilderAndFactoryShareRegistry() {
        ByteArrayResource xml = new ByteArrayResource(XML.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        CacheManager manager = EhCache3ManagerUtils.buildCacheManager("named-resource", xml);
        EhCache3ManagerFactoryBean borrower = new EhCache3ManagerFactoryBean();
        borrower.setCacheManagerName("named-resource");
        borrower.setAcceptExisting(true);
        borrower.afterPropertiesSet();
        try {
            assertSame(manager, borrower.getObject());
            assertNotNull(manager.getCache("numbers", Integer.class, String.class));
        } finally {
            borrower.destroy();
            manager.close();
        }
    }

    @Test void closedManagerIsNotBorrowedAgain() {
        CacheManager closed = EhCache3ManagerUtils.buildCacheManager("closed-name");
        closed.close();
        EhCache3ManagerFactoryBean replacement = new EhCache3ManagerFactoryBean();
        replacement.setCacheManagerName("closed-name");
        replacement.setAcceptExisting(true);
        replacement.afterPropertiesSet();
        try {
            assertNotSame(closed, replacement.getObject());
            assertEquals(Status.AVAILABLE, replacement.getObject().getStatus());
            assertEquals(replacement.getObject().getClass(), replacement.getObjectType());
        } finally { replacement.destroy(); }
    }

    @Test void persistentUserManagedCacheHasExplicitNativeTypeBoundary(@TempDir java.nio.file.Path directory)
            throws org.ehcache.CachePersistenceException {
        org.ehcache.impl.persistence.DefaultLocalPersistenceService service =
                new org.ehcache.impl.persistence.DefaultLocalPersistenceService(
                        new org.ehcache.impl.config.persistence.DefaultPersistenceConfiguration(directory.toFile()));
        org.ehcache.PersistentUserManagedCache<String, String> persistent =
                UserManagedCacheBuilder.newUserManagedCacheBuilder(String.class, String.class)
                        .with(new org.ehcache.impl.config.persistence.UserManagedPersistenceContext<>(
                                "persistent", service))
                        .withResourcePools(ResourcePoolsBuilder.newResourcePoolsBuilder()
                                .heap(10, org.ehcache.config.units.EntryUnit.ENTRIES)
                                .disk(10, org.ehcache.config.units.MemoryUnit.MB, true))
                        .build(true);
        try {
            EhCache3Cache adapter = new EhCache3Cache("persistent", persistent);
            adapter.put("key", "value");
            assertEquals("value", adapter.get("key", String.class));
            assertThrows(IllegalStateException.class, adapter::getNativeCache);
        } finally {
            persistent.close();
            persistent.destroy();
            service.stop();
        }
    }

    @Test void sharedFactoryReusesNamedBuilderAndStillShutsItDown() {
        CacheManager manager = EhCache3ManagerUtils.buildCacheManager("shared-utils");
        EhCache3ManagerFactoryBean shared = new EhCache3ManagerFactoryBean();
        shared.setCacheManagerName("shared-utils");
        shared.setShared(true);
        shared.afterPropertiesSet();
        assertSame(manager, shared.getObject());
        shared.destroy();
        assertEquals(Status.UNINITIALIZED, manager.getStatus());
    }

    @Test void sharedFactoriesWithDifferentNamesUseOneSingleton() {
        EhCache3ManagerFactoryBean first = new EhCache3ManagerFactoryBean();
        first.setCacheManagerName("shared-first");
        first.setShared(true);
        EhCache3ManagerFactoryBean second = new EhCache3ManagerFactoryBean();
        second.setCacheManagerName("shared-second");
        second.setShared(true);
        first.afterPropertiesSet();
        second.afterPropertiesSet();
        CacheManager manager = first.getObject();
        EhCache3ManagerFactoryBean borrower = new EhCache3ManagerFactoryBean();
        borrower.setCacheManagerName("shared-second");
        borrower.setAcceptExisting(true);
        borrower.afterPropertiesSet();
        try {
            assertSame(manager, second.getObject());
            assertNotSame(manager, borrower.getObject());
            assertEquals(Status.AVAILABLE, borrower.getObject().getStatus());
        } finally {
            borrower.destroy();
            first.destroy();
            if (second.getObject() != manager) second.destroy();
        }
        assertEquals(Status.UNINITIALIZED, manager.getStatus());
    }

    @Test void externallyClosedSharedSingletonIsReplaced() {
        EhCache3ManagerFactoryBean first = new EhCache3ManagerFactoryBean();
        first.setCacheManagerName("shared-closed-first");
        first.setShared(true);
        first.afterPropertiesSet();
        CacheManager closed = first.getObject();
        closed.close();
        EhCache3ManagerFactoryBean second = new EhCache3ManagerFactoryBean();
        second.setCacheManagerName("shared-closed-second");
        second.setShared(true);
        second.afterPropertiesSet();
        try {
            assertNotSame(closed, second.getObject());
            assertEquals(Status.AVAILABLE, second.getObject().getStatus());
        } finally { second.destroy(); }
    }

    @Test void xmlSupportsStreamsAndRejectsBadInput() {
        ByteArrayResource xml = new ByteArrayResource(XML.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        CacheManager manager = EhCache3ManagerUtils.buildCacheManager(xml);
        try {
            assertNotNull(manager.getCache("numbers", Integer.class, String.class));
        } finally { manager.close(); }
        javax.cache.CacheException doctype = assertThrows(javax.cache.CacheException.class,
                () -> EhCache3ManagerUtils.parseConfiguration(new ByteArrayResource(
                        ("<!DOCTYPE config [<!ENTITY key 'java.lang.String'>]>"
                        + "<config xmlns='http://www.ehcache.org/v3'><cache alias='x'>"
                        + "<key-type>&key;</key-type><value-type>java.lang.String</value-type>"
                        + "<heap unit='entries'>10</heap></cache></config>")
                        .getBytes(java.nio.charset.StandardCharsets.UTF_8))));
        Throwable cause = doctype;
        while (cause.getCause() != null) cause = cause.getCause();
        assertTrue(cause.getMessage().contains("DOCTYPE"));
        assertThrows(javax.cache.CacheException.class, () -> EhCache3ManagerUtils.parseConfiguration(
                new ByteArrayResource("<config".getBytes(java.nio.charset.StandardCharsets.UTF_8))));
    }

    @Test void invalidFactoryConfigurationDoesNotReserveName() {
        EhCache3ManagerFactoryBean broken = new EhCache3ManagerFactoryBean();
        broken.setCacheManagerName("failed-setup");
        broken.setConfigLocation(new ByteArrayResource("<config".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        assertThrows(javax.cache.CacheException.class, broken::afterPropertiesSet);
        EhCache3ManagerFactoryBean working = new EhCache3ManagerFactoryBean();
        working.setCacheManagerName("failed-setup");
        working.afterPropertiesSet();
        working.destroy();
    }

    @Test void jsr107CustomizerSetsTouchedExpiry() {
        javax.cache.CacheManager manager = javax.cache.Caching.getCachingProvider().getCacheManager();
        try {
            new CachingSetup().customize(manager);
            assertThrows(javax.cache.CacheException.class, () -> new CachingSetup().customize(manager));
            javax.cache.Cache<Object, Object> cache = manager.getCache("people");
            assertNotNull(cache);
            javax.cache.configuration.CompleteConfiguration<?, ?> configuration =
                    cache.getConfiguration(javax.cache.configuration.CompleteConfiguration.class);
            javax.cache.expiry.Duration duration =
                    configuration.getExpiryPolicyFactory().create().getExpiryForAccess();
            assertEquals(10, duration.getDurationAmount());
            assertEquals(java.util.concurrent.TimeUnit.SECONDS, duration.getTimeUnit());
        } finally { manager.close(); }
    }
}
