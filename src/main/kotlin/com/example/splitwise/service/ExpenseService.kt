package com.example.splitwise.service

import com.example.splitwise.domain.Expense
import com.example.splitwise.domain.ExpenseShare
import com.example.splitwise.repository.ExpenseRepository
import com.example.splitwise.web.BadRequestException
import com.example.splitwise.web.dto.CreateExpenseRequest
import com.example.splitwise.web.dto.ExpenseResponse
import com.example.splitwise.web.dto.ExpenseShareResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class ExpenseService(
	private val expenseRepository: ExpenseRepository,
	private val groupService: GroupService,
) {

	@Transactional
	fun createExpense(currentUserId: Long, groupId: Long, request: CreateExpenseRequest): ExpenseResponse {
		groupService.assertMembership(groupId, currentUserId)

		val group = groupService.findGroup(groupId)
		val members = groupService.membersOf(groupId)
		val memberIds = members.map { requireNotNull(it.id) }.toSet()

		val paidById = request.paidByUserId ?: currentUserId
		if (paidById !in memberIds) {
			throw BadRequestException("Payer must be a member of the group")
		}

		val participantIds = (request.participantIds ?: memberIds.toList()).distinct()
		if (participantIds.isEmpty()) {
			throw BadRequestException("At least one participant is required")
		}
		if (participantIds.any { it !in memberIds }) {
			throw BadRequestException("All participants must be members of the group")
		}

		val amount = request.amount.setScale(2, RoundingMode.HALF_UP)
		if (amount <= BigDecimal.ZERO) {
			throw BadRequestException("Amount must be positive")
		}

		val shareAmount = amount.divide(BigDecimal(participantIds.size), 2, RoundingMode.HALF_UP)
		var allocated = BigDecimal.ZERO.setScale(2)

		val paidBy = members.first { it.id == paidById }
		val expense = Expense(
			group = group,
			paidBy = paidBy,
			description = request.description.trim(),
			amount = amount,
		)

		participantIds.forEachIndexed { index, userId ->
			val user = members.first { it.id == userId }
			val value = if (index == participantIds.lastIndex) {
				amount - allocated
			} else {
				shareAmount.also { allocated += it }
			}
			expense.shares += ExpenseShare(expense = expense, user = user, shareAmount = value)
		}

		return toResponse(expenseRepository.save(expense))
	}

	@Transactional(readOnly = true)
	fun listExpenses(currentUserId: Long, groupId: Long): List<ExpenseResponse> {
		groupService.assertMembership(groupId, currentUserId)
		return expenseRepository.findByGroupIdWithShares(groupId).map(::toResponse)
	}

	private fun toResponse(expense: Expense): ExpenseResponse =
		ExpenseResponse(
			id = requireNotNull(expense.id),
			description = expense.description,
			amount = expense.amount,
			paidBy = groupService.toUserSummary(expense.paidBy),
			shares = expense.shares.map {
				ExpenseShareResponse(
					userId = requireNotNull(it.user.id),
					name = it.user.name,
					shareAmount = it.shareAmount,
				)
			},
			createdAt = expense.createdAt,
		)
}
