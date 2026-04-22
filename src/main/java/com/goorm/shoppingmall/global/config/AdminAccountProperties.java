package com.goorm.shoppingmall.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.admin")
public record AdminAccountProperties(
        String email,
        String password,
        String name
) {
}
