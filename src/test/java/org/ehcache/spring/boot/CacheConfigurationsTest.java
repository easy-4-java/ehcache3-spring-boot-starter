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
package org.ehcache.spring.boot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link CacheConfigurations}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@DisplayName("CacheConfigurations Tests")
class CacheConfigurationsTest {

    @Test
    @DisplayName("getType returns ehcache3 for EhCache3CacheConfiguration")
    void getTypeReturnsEhcache3() {
        String type = CacheConfigurations.getType(EhCache3CacheConfiguration.class.getName());
        assertThat(type).isEqualTo("ehcache3");
    }

    @Test
    @DisplayName("getConfigurationClass returns EhCache3CacheConfiguration for ehcache3")
    void getConfigurationClassReturnsEhCache3() {
        String configClass = CacheConfigurations.getConfigurationClass("ehcache3");
        assertThat(configClass).isEqualTo(EhCache3CacheConfiguration.class.getName());
    }

    @Test
    @DisplayName("getType throws for unknown configuration class")
    void getTypeThrowsForUnknown() {
        assertThatThrownBy(() -> CacheConfigurations.getType("com.unknown.Class"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unknown configuration class");
    }

    @Test
    @DisplayName("getConfigurationClass throws for unknown cache type")
    void getConfigurationClassThrowsForUnknown() {
        assertThatThrownBy(() -> CacheConfigurations.getConfigurationClass("unknown"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unknown cache type");
    }
}
