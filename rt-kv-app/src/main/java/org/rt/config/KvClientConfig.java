package org.rt.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Registers client-side configuration properties.
 */
@Configuration
@EnableConfigurationProperties(KvClientProperties.class)
public class KvClientConfig {
}
