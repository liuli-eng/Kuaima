package com.kuaima.app.config;

import java.net.URI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

/** 开发环境数据库直连，避免 IDE 的 JVM SOCKS 设置接管 JDBC TCP 连接。 */
public class DevDirectDatabaseEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (environment.matchesProfiles("dev")) {
            String jdbcUrl = environment.getProperty("spring.datasource.url");
            String databaseHost = databaseHost(jdbcUrl);
            if (databaseHost != null) {
                String current = System.getProperty("socksNonProxyHosts", "localhost|127.*|[::1]");
                if (!current.contains(databaseHost)) {
                    System.setProperty("socksNonProxyHosts", current + "|" + databaseHost);
                }
            }
        }
    }

    private String databaseHost(String jdbcUrl) {
        if (jdbcUrl == null || !jdbcUrl.startsWith("jdbc:mysql://")) return null;
        try {
            return URI.create(jdbcUrl.substring("jdbc:".length())).getHost();
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
