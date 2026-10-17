package com.example.splitwise.web

import com.example.splitwise.security.UserPrincipal
import org.springframework.security.core.context.SecurityContextHolder

object SecurityUtils {
	fun currentUser(): UserPrincipal {
		val auth = SecurityContextHolder.getContext().authentication
			?: throw ForbiddenException("Unauthenticated")
		return auth.principal as? UserPrincipal
			?: throw ForbiddenException("Unauthenticated")
	}
}
