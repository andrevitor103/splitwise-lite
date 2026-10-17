package com.example.splitwise.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService(
	private val properties: JwtProperties,
) {
	private val key: SecretKey by lazy {
		Keys.hmacShaKeyFor(properties.secret.toByteArray(StandardCharsets.UTF_8))
	}

	fun generateToken(userId: Long, email: String): String {
		val now = Date()
		val expiry = Date(now.time + properties.expirationMs)
		return Jwts.builder()
			.subject(email)
			.claim("userId", userId)
			.issuedAt(now)
			.expiration(expiry)
			.signWith(key)
			.compact()
	}

	fun extractEmail(token: String): String = parseClaims(token).subject

	fun isValid(token: String, email: String): Boolean =
		runCatching {
			val claims = parseClaims(token)
			claims.subject == email && claims.expiration.after(Date())
		}.getOrDefault(false)

	private fun parseClaims(token: String): Claims =
		Jwts.parser()
			.verifyWith(key)
			.build()
			.parseSignedClaims(token)
			.payload
}
