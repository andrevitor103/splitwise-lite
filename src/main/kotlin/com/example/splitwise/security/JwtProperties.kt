package com.example.splitwise.security

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.jwt")
data class JwtProperties(
	val secret: String,
	val expirationMs: Long,
)
