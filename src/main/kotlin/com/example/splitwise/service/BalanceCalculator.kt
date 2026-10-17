package com.example.splitwise.service

import java.math.BigDecimal
import java.math.RoundingMode

data class NetBalance(
	val userId: Long,
	val name: String,
	val net: BigDecimal,
)

data class Settlement(
	val fromUserId: Long,
	val fromName: String,
	val toUserId: Long,
	val toName: String,
	val amount: BigDecimal,
)

object BalanceCalculator {

	fun simplify(nets: List<NetBalance>): List<Settlement> {
		val debtors = nets
			.filter { it.net < BigDecimal.ZERO }
			.map { it.copy(net = it.net.abs().setScale(2, RoundingMode.HALF_UP)) }
			.sortedByDescending { it.net }
			.toMutableList()

		val creditors = nets
			.filter { it.net > BigDecimal.ZERO }
			.map { it.copy(net = it.net.setScale(2, RoundingMode.HALF_UP)) }
			.sortedByDescending { it.net }
			.toMutableList()

		val settlements = mutableListOf<Settlement>()
		var i = 0
		var j = 0

		while (i < debtors.size && j < creditors.size) {
			val debtor = debtors[i]
			val creditor = creditors[j]
			val amount = debtor.net.min(creditor.net).setScale(2, RoundingMode.HALF_UP)

			if (amount > BigDecimal.ZERO) {
				settlements += Settlement(
					fromUserId = debtor.userId,
					fromName = debtor.name,
					toUserId = creditor.userId,
					toName = creditor.name,
					amount = amount,
				)
			}

			debtors[i] = debtor.copy(net = debtor.net - amount)
			creditors[j] = creditor.copy(net = creditor.net - amount)

			if (debtors[i].net.compareTo(BigDecimal.ZERO) == 0) i++
			if (creditors[j].net.compareTo(BigDecimal.ZERO) == 0) j++
		}

		return settlements
	}
}
