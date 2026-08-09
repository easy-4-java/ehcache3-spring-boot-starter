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

import org.ehcache.CacheManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link EhCache3ManagerUtils}.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
@DisplayName("EhCache3ManagerUtils Tests")
class CachingSetupTest {

    @Test
    @DisplayName("buildCacheManager creates a CacheManager")
    void testBuildCacheManager() {
        CacheManager cacheManager = EhCache3ManagerUtils.buildCacheManager();
        assertThat(cacheManager).isNotNull();
        assertThat(cacheManager.getStatus()).isEqualTo(org.ehcache.Status.AVAILABLE);
        cacheManager.close();
    }

    @Test
    @DisplayName("buildCacheManager with name creates a CacheManager")
    void testBuildCacheManagerWithName() {
        CacheManager cacheManager = EhCache3ManagerUtils.buildCacheManager("testManager");
        assertThat(cacheManager).isNotNull();
        cacheManager.close();
    }
}
