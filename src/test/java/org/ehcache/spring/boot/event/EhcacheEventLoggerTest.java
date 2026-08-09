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
package org.ehcache.spring.boot.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.ehcache.event.CacheEvent;
import org.ehcache.event.EventType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link EhcacheEventLogger}.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
@DisplayName("EhcacheEventLogger Tests")
class EhcacheEventLoggerTest {

    @Test
    @DisplayName("Instance can be created via constructor")
    void testInstantiation() {
        EhcacheEventLogger instance = new EhcacheEventLogger();
        assertThat(instance).isNotNull();
    }

    @Test
    @DisplayName("onEvent logs the event")
    void onEvent() {
        EhcacheEventLogger logger = new EhcacheEventLogger();

        CacheEvent<String, String> event = mock(CacheEvent.class);
        when(event.getType()).thenReturn(EventType.CREATED);
        when(event.getKey()).thenReturn("key1");
        when(event.getOldValue()).thenReturn(null);
        when(event.getNewValue()).thenReturn("value1");

        // Should not throw
        logger.onEvent(event);
    }

    @Test
    @DisplayName("onEvent handles UPDATED event")
    void onEventUpdated() {
        EhcacheEventLogger logger = new EhcacheEventLogger();

        CacheEvent<String, String> event = mock(CacheEvent.class);
        when(event.getType()).thenReturn(EventType.UPDATED);
        when(event.getKey()).thenReturn("key1");
        when(event.getOldValue()).thenReturn("oldValue");
        when(event.getNewValue()).thenReturn("newValue");

        logger.onEvent(event);
    }

    @Test
    @DisplayName("onEvent handles REMOVED event")
    void onEventRemoved() {
        EhcacheEventLogger logger = new EhcacheEventLogger();

        CacheEvent<String, String> event = mock(CacheEvent.class);
        when(event.getType()).thenReturn(EventType.REMOVED);
        when(event.getKey()).thenReturn("key1");
        when(event.getOldValue()).thenReturn("value1");
        when(event.getNewValue()).thenReturn(null);

        logger.onEvent(event);
    }

    @Test
    @DisplayName("onEvent handles EVICTED event")
    void onEventEvicted() {
        EhcacheEventLogger logger = new EhcacheEventLogger();

        CacheEvent<String, String> event = mock(CacheEvent.class);
        when(event.getType()).thenReturn(EventType.EVICTED);
        when(event.getKey()).thenReturn("key1");
        when(event.getOldValue()).thenReturn("value1");
        when(event.getNewValue()).thenReturn(null);

        logger.onEvent(event);
    }

    @Test
    @DisplayName("onEvent handles EXPIRED event")
    void onEventExpired() {
        EhcacheEventLogger logger = new EhcacheEventLogger();

        CacheEvent<String, String> event = mock(CacheEvent.class);
        when(event.getType()).thenReturn(EventType.EXPIRED);
        when(event.getKey()).thenReturn("key1");
        when(event.getOldValue()).thenReturn("value1");
        when(event.getNewValue()).thenReturn(null);

        logger.onEvent(event);
    }
}
