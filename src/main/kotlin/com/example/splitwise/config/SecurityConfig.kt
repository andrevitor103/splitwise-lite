package com.example.splitwise.config

import com.example.splitwise.security.JwtAuthenticationFilter
import com.example.splitwise.security.JwtProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(JwtProperties::class)
class SecurityConfig(
	private val jwtAuthenticationFilter: JwtAuthenticationFilter,
	private val userDetailsService: UserDetailsService,
) {

	@Bean
	fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

	@Bean
	fun authenticationManager(passwordEncoder: PasswordEncoder): AuthenticationManager {
		val provider = DaoAuthenticationProvider()
		provider.setUserDetailsService(userDetailsService)
		provider.setPasswordEncoder(passwordEncoder)
		return ProviderManager(provider)
	}

	@Bean
	fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
		http
			.csrf { it.disable() }
			.cors { }
			.sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
			.authorizeHttpRequests {
				it.requestMatchers("/api/auth/**").permitAll()
				it.requestMatchers("/h2-console/**").permitAll()
				it.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				it.anyRequest().authenticated()
			}
			.headers { headers -> headers.frameOptions { it.sameOrigin() } }
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

		return http.build()
	}

	@Bean
	fun corsConfigurationSource(): CorsConfigurationSource {
		val config = CorsConfiguration().apply {
			allowedOriginPatterns = listOf("*")
			allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
			allowedHeaders = listOf("*")
			allowCredentials = true
		}
		return UrlBasedCorsConfigurationSource().also {
			it.registerCorsConfiguration("/**", config)
		}
	}
}
