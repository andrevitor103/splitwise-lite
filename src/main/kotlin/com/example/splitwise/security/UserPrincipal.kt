package com.example.splitwise.security

import com.example.splitwise.domain.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class UserPrincipal(
	val id: Long,
	val email: String,
	private val passwordHash: String,
	val displayName: String,
) : UserDetails {

	override fun getAuthorities(): Collection<GrantedAuthority> =
		listOf(SimpleGrantedAuthority("ROLE_USER"))

	override fun getPassword(): String = passwordHash
	override fun getUsername(): String = email
	override fun isAccountNonExpired(): Boolean = true
	override fun isAccountNonLocked(): Boolean = true
	override fun isCredentialsNonExpired(): Boolean = true
	override fun isEnabled(): Boolean = true

	companion object {
		fun from(user: User): UserPrincipal =
			UserPrincipal(
				id = requireNotNull(user.id),
				email = user.email,
				passwordHash = user.passwordHash,
				displayName = user.name,
			)
	}
}
