package com.example.splitwise.service

import com.example.splitwise.repository.ExpenseRepository
import com.example.splitwise.web.dto.BalanceResponse
import com.example.splitwise.web.dto.MemberBalanceResponse
import com.example.splitwise.web.dto.SettlementResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class BalanceService(
	private val expenseRepository: ExpenseRepository,
	private val groupService: GroupService,
) {

	@Transactional(readOnly = true)
	fun getBalance(currentUserId: Long, groupId: Long): BalanceResponse {
		groupService.assertMembership(groupId, currentUserId)

		val members = groupService.membersOf(groupId)
		val paid = mutableMapOf<Long, BigDecimal>()
		val owed = mutableMapOf<Long, BigDecimal>()

		members.forEach { member ->
			val id = requireNotNull(member.id)
			paid[id] = BigDecimal.ZERO.setScale(2)
			owed[id] = BigDecimal.ZERO.setScale(2)
		}

		expenseRepository.findByGroupIdWithShares(groupId).forEach { expense ->
			val payerId = requireNotNull(expense.paidBy.id)
			paid[payerId] = (paid[payerId] ?: BigDecimal.ZERO) + expense.amount
			expense.shares.forEach { share ->
				val userId = requireNotNull(share.user.id)
				owed[userId] = (owed[userId] ?: BigDecimal.ZERO) + share.shareAmount
			}
		}

		val nets = members.map { member ->
			val id = requireNotNull(member.id)
			val net = ((paid[id] ?: BigDecimal.ZERO) - (owed[id] ?: BigDecimal.ZERO))
				.setScale(2, RoundingMode.HALF_UP)
			NetBalance(userId = id, name = member.name, net = net)
		}

		val settlements = BalanceCalculator.simplify(nets)

		return BalanceResponse(
			balances = nets.map {
				MemberBalanceResponse(userId = it.userId, name = it.name, net = it.net)
			},
			settlements = settlements.map {
				SettlementResponse(
					fromUserId = it.fromUserId,
					fromName = it.fromName,
					toUserId = it.toUserId,
					toName = it.toName,
					amount = it.amount,
				)
			},
		)
	}
}
