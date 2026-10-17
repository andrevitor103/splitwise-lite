package com.example.splitwise.web

import com.example.splitwise.service.AuthService
import com.example.splitwise.web.dto.AuthResponse
import com.example.splitwise.web.dto.LoginRequest
import com.example.splitwise.web.dto.RegisterRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
	private val authService: AuthService,
) {

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	fun register(@Valid @RequestBody request: RegisterRequest): AuthResponse =
		authService.register(request)

	@PostMapping("/login")
	fun login(@Valid @RequestBody request: LoginRequest): AuthResponse =
		authService.login(request)
}
