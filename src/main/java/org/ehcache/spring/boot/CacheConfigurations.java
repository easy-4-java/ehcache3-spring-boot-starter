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

import org.springframework.boot.autoconfigure.cache.CacheType;

/** The one configuration class supplied by this starter. */
final class CacheConfigurations {
    private CacheConfigurations() { }
    static CacheType getType(String configurationClassName) {
        if (EhCache3CacheConfiguration.class.getName().equals(configurationClassName)) {
            return CacheType.EHCACHE;
        }
        throw new IllegalStateException("Unknown configuration class " + configurationClassName);
    }
    static String getConfigurationClass(CacheType type) {
        if (type == CacheType.EHCACHE) return EhCache3CacheConfiguration.class.getName();
        throw new IllegalStateException("Unknown cache type " + type);
    }
}
