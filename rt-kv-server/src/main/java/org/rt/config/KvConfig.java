package org.rt.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Registers server-side configuration properties.
 */
@Configuration
@EnableConfigurationProperties(KvNodeProperties.class)
public class KvConfig {
}
