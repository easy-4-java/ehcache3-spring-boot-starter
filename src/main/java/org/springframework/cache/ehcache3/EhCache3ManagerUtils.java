package org.springframework.cache.ehcache3;

import java.io.IOException;
import java.io.InputStream;
import javax.cache.CacheException;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.ehcache.CacheManager;
import org.ehcache.config.Configuration;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.xml.XmlConfiguration;
import org.springframework.core.io.Resource;
import org.xml.sax.InputSource;

/** Builders for Ehcache 3 managers and XML configuration. */
public abstract class EhCache3ManagerUtils {
    public static CacheManager buildCacheManager() throws CacheException {
        return CacheManagerBuilder.newCacheManagerBuilder().build(true);
    }
    public static CacheManager buildCacheManager(String name) throws CacheException {
        // Ehcache 3 has no manager name; the name is used by this library's factory registry.
        return buildCacheManager();
    }
    public static CacheManager buildCacheManager(Resource configLocation) throws CacheException {
        return build(parseConfiguration(configLocation));
    }
    public static CacheManager buildCacheManager(String name, Resource configLocation) throws CacheException {
        return buildCacheManager(configLocation);
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
