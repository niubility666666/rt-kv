package org.rt.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties(KvClientProperties.class)
@Configuration
public class KvClientConfig {
}

