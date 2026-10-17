package com.example.splitwise.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
	private val jwtService: JwtService,
	private val userDetailsService: CustomUserDetailsService,
) : OncePerRequestFilter() {

	override fun doFilterInternal(
		request: HttpServletRequest,
		response: HttpServletResponse,
		filterChain: FilterChain,
	) {
		val header = request.getHeader("Authorization")
		if (header.isNullOrBlank() || !header.startsWith("Bearer ")) {
			filterChain.doFilter(request, response)
			return
		}

		val token = header.removePrefix("Bearer ").trim()
		runCatching {
			val email = jwtService.extractEmail(token)
			if (SecurityContextHolder.getContext().authentication == null && jwtService.isValid(token, email)) {
				val userDetails = userDetailsService.loadUserByUsername(email)
				val auth = UsernamePasswordAuthenticationToken(
					userDetails,
					null,
					userDetails.authorities,
				)
				auth.details = WebAuthenticationDetailsSource().buildDetails(request)
				SecurityContextHolder.getContext().authentication = auth
			}
		}

		filterChain.doFilter(request, response)
	}
}
