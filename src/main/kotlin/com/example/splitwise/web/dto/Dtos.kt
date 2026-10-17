package com.example.splitwise.web.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.Instant

data class RegisterRequest(
	@field:NotBlank @field:Size(min = 2, max = 100)
	val name: String,

	@field:NotBlank @field:Email
	val email: String,

	@field:NotBlank @field:Size(min = 6, max = 100)
	val password: String,
)

data class LoginRequest(
	@field:NotBlank @field:Email
	val email: String,

	@field:NotBlank
	val password: String,
)

data class AuthResponse(
	val token: String,
	val userId: Long,
	val name: String,
	val email: String,
)

data class CreateGroupRequest(
	@field:NotBlank @field:Size(min = 2, max = 120)
	val name: String,
)

data class AddMemberRequest(
	@field:NotBlank @field:Email
	val email: String,
)

data class CreateExpenseRequest(
	@field:NotBlank @field:Size(min = 2, max = 200)
	val description: String,

	@field:Positive
	val amount: BigDecimal,

	val paidByUserId: Long? = null,

	val participantIds: List<Long>? = null,
)

data class UserSummaryResponse(
	val id: Long,
	val name: String,
	val email: String,
)

data class GroupResponse(
	val id: Long,
	val name: String,
	val createdBy: UserSummaryResponse,
	val members: List<UserSummaryResponse>,
	val createdAt: Instant,
)

data class GroupSummaryResponse(
	val id: Long,
	val name: String,
	val createdAt: Instant,
	val memberCount: Int,
)

data class ExpenseShareResponse(
	val userId: Long,
	val name: String,
	val shareAmount: BigDecimal,
)

data class ExpenseResponse(
	val id: Long,
	val description: String,
	val amount: BigDecimal,
	val paidBy: UserSummaryResponse,
	val shares: List<ExpenseShareResponse>,
	val createdAt: Instant,
)

data class MemberBalanceResponse(
	val userId: Long,
	val name: String,
	val net: BigDecimal,
)

data class SettlementResponse(
	val fromUserId: Long,
	val fromName: String,
	val toUserId: Long,
	val toName: String,
	val amount: BigDecimal,
)

data class BalanceResponse(
	val balances: List<MemberBalanceResponse>,
	val settlements: List<SettlementResponse>,
)
