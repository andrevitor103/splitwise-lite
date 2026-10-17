package com.example.splitwise.security

import com.example.splitwise.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
	private val userRepository: UserRepository,
) : UserDetailsService {

	override fun loadUserByUsername(username: String): UserDetails {
		val user = userRepository.findByEmail(username)
			?: throw UsernameNotFoundException("User not found: $username")
		return UserPrincipal.from(user)
	}
}
