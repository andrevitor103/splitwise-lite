package com.example.splitwise.service

import com.example.splitwise.domain.User
import com.example.splitwise.repository.UserRepository
import com.example.splitwise.security.JwtService
import com.example.splitwise.security.UserPrincipal
import com.example.splitwise.web.ConflictException
import com.example.splitwise.web.dto.AuthResponse
import com.example.splitwise.web.dto.LoginRequest
import com.example.splitwise.web.dto.RegisterRequest
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
	private val userRepository: UserRepository,
	private val passwordEncoder: PasswordEncoder,
	private val jwtService: JwtService,
	private val authenticationManager: AuthenticationManager,
) {

	@Transactional
	fun register(request: RegisterRequest): AuthResponse {
		if (userRepository.existsByEmail(request.email.lowercase())) {
			throw ConflictException("Email already registered")
		}

		val user = userRepository.save(
			User(
				name = request.name.trim(),
				email = request.email.lowercase().trim(),
				passwordHash = passwordEncoder.encode(request.password),
			),
		)

		return toAuthResponse(user)
	}

	fun login(request: LoginRequest): AuthResponse {
		val authentication = authenticationManager.authenticate(
			UsernamePasswordAuthenticationToken(request.email.lowercase().trim(), request.password),
		)
		val principal = authentication.principal as UserPrincipal
		val user = userRepository.findByEmail(principal.email)
			?: throw ConflictException("User not found after authentication")
		return toAuthResponse(user)
	}

	private fun toAuthResponse(user: User): AuthResponse {
		val id = requireNotNull(user.id)
		return AuthResponse(
			token = jwtService.generateToken(id, user.email),
			userId = id,
			name = user.name,
			email = user.email,
		)
	}
}
