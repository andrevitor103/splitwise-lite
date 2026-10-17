package com.example.splitwise.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class BalanceCalculatorTest {

	@Test
	fun `simplifies two people debt`() {
		val nets = listOf(
			NetBalance(1, "André", BigDecimal("-42.50")),
			NetBalance(2, "Maria", BigDecimal("42.50")),
		)

		val settlements = BalanceCalculator.simplify(nets)

		assertEquals(1, settlements.size)
		assertEquals(1L, settlements[0].fromUserId)
		assertEquals(2L, settlements[0].toUserId)
		assertEquals(BigDecimal("42.50"), settlements[0].amount)
	}

	@Test
	fun `simplifies three people with multiple creditors`() {
		// André paid 600 hotel / 3 = net +400
		// Maria paid 150 / 3 = net -50? Let's use explicit nets:
		// André +250, Maria -100, João -150
		val nets = listOf(
			NetBalance(1, "André", BigDecimal("250.00")),
			NetBalance(2, "Maria", BigDecimal("-100.00")),
			NetBalance(3, "João", BigDecimal("-150.00")),
		)

		val settlements = BalanceCalculator.simplify(nets)

		assertEquals(2, settlements.size)
		val total = settlements.fold(BigDecimal.ZERO) { acc, s -> acc + s.amount }
		assertEquals(BigDecimal("250.00"), total)
		assertTrue(settlements.all { it.toUserId == 1L })
	}

	@Test
	fun `returns empty when everyone is settled`() {
		val nets = listOf(
			NetBalance(1, "André", BigDecimal.ZERO),
			NetBalance(2, "Maria", BigDecimal.ZERO),
		)
		assertTrue(BalanceCalculator.simplify(nets).isEmpty())
	}
}
