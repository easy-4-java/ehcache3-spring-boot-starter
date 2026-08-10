package org.ehcache.spring.boot.event;

import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**\n * Auto-configuration for EhcacheEventLogger.\n *\n * @author <a href="https://github.com/loong10k">Loong Wan</a>\n * @since 1.0.0\n */
public class EhcacheEventLogger implements CacheEventListener<Object, Object> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EhcacheEventLogger.class);

	@Override
	public void onEvent(CacheEvent<?, ?> event) {
		LOGGER.info("Event: " + event.getType() + " Key: " + event.getKey() + " old value: " + event.getOldValue()
				+ " new value: " + event.getNewValue());
	}
	
}
