package org.springframework.cache.ehcache3;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.cache.CacheException;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.ehcache.CacheManager;
import org.ehcache.config.Configuration;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.xml.XmlConfiguration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;
import org.xml.sax.InputSource;

/** Builders for Ehcache 3 managers and XML configuration. */
public abstract class EhCache3ManagerUtils {
    static final String DEFAULT_NAME = "default";
    private static final Map<String, CacheManager> REGISTRY = new HashMap<>();
    private static CacheManager sharedSingleton;

    public static CacheManager buildCacheManager() throws CacheException {
        return buildCacheManager(DEFAULT_NAME);
    }
    public static CacheManager buildCacheManager(String name) throws CacheException {
        return create(name, null);
    }
    public static CacheManager buildCacheManager(Resource configLocation) throws CacheException {
        return create(DEFAULT_NAME, configLocation);
    }
    public static CacheManager buildCacheManager(String name, Resource configLocation) throws CacheException {
        return create(name, configLocation);
    }

    static Object registryLock() { return REGISTRY; }
    static CacheManager lookup(String name) {
        CacheManager manager = REGISTRY.get(name);
        if (manager != null && manager.getStatus() != org.ehcache.Status.AVAILABLE) {
            release(manager);
            return null;
        }
        return manager;
    }
    static CacheManager lookupShared() {
        if (sharedSingleton != null && sharedSingleton.getStatus() != org.ehcache.Status.AVAILABLE) {
            release(sharedSingleton);
        }
        return sharedSingleton;
    }
    static void registerShared(CacheManager manager) {
        sharedSingleton = manager;
    }
    static void release(CacheManager manager) {
        if (sharedSingleton == manager) sharedSingleton = null;
        Iterator<Map.Entry<String, CacheManager>> entries = REGISTRY.entrySet().iterator();
        while (entries.hasNext()) {
            if (entries.next().getValue() == manager) entries.remove();
        }
    }
    private static CacheManager create(String name, Resource location) {
        if (name == null || name.isEmpty()) throw new CacheException("Cache manager name must not be empty");
        synchronized (REGISTRY) {
            if (lookup(name) != null) throw new CacheException("Ehcache manager name already registered: " + name);
            Resource resource = location;
            if (resource == null) {
                ClassPathResource defaultXml = new ClassPathResource("ehcache.xml");
                if (defaultXml.exists()) resource = defaultXml;
            }
            CacheManager manager = resource == null ? CacheManagerBuilder.newCacheManagerBuilder().build(true)
                    : build(parseConfiguration(resource));
            REGISTRY.put(name, manager);
            return manager;
        }
    }
    static CacheManager build(Configuration configuration) {
        CacheManager manager = CacheManagerBuilder.newCacheManager(configuration);
        manager.init();
        return manager;
    }
    public static Configuration parseConfiguration(Resource configLocation) throws CacheException {
        try (InputStream stream = configLocation.getInputStream()) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            return new XmlConfiguration(factory.newDocumentBuilder().parse(new InputSource(stream)));
        }
        catch (Exception ex) {
            throw new CacheException("Failed to parse Ehcache configuration resource: " + configLocation, ex);
        }
    }
}
