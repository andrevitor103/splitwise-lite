package com.example.splitwise.config

import com.example.splitwise.domain.Expense
import com.example.splitwise.domain.ExpenseGroup
import com.example.splitwise.domain.ExpenseShare
import com.example.splitwise.domain.GroupMember
import com.example.splitwise.domain.User
import com.example.splitwise.repository.ExpenseGroupRepository
import com.example.splitwise.repository.ExpenseRepository
import com.example.splitwise.repository.GroupMemberRepository
import com.example.splitwise.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode

@Component
class DataSeeder(
	private val userRepository: UserRepository,
	private val expenseGroupRepository: ExpenseGroupRepository,
	private val groupMemberRepository: GroupMemberRepository,
	private val expenseRepository: ExpenseRepository,
	private val passwordEncoder: PasswordEncoder,
) : ApplicationRunner {

	private val log = LoggerFactory.getLogger(javaClass)

	@Transactional
	override fun run(args: ApplicationArguments) {
		if (userRepository.count() > 0) {
			return
		}

		val andre = userRepository.save(
			User(name = "André", email = "andre@email.com", passwordHash = passwordEncoder.encode("senha123")),
		)
		val maria = userRepository.save(
			User(name = "Maria", email = "maria@email.com", passwordHash = passwordEncoder.encode("senha123")),
		)
		val joao = userRepository.save(
			User(name = "João", email = "joao@email.com", passwordHash = passwordEncoder.encode("senha123")),
		)

		val group = expenseGroupRepository.save(
			ExpenseGroup(name = "Viagem Florianópolis", createdBy = andre),
		)

		listOf(andre, maria, joao).forEach { user ->
			groupMemberRepository.save(GroupMember(group = group, user = user))
		}

		createEqualExpense(group, andre, "Hotel 2 noites", BigDecimal("600.00"), listOf(andre, maria, joao))
		createEqualExpense(group, maria, "Jantar", BigDecimal("150.00"), listOf(andre, maria, joao))
		createEqualExpense(group, joao, "Uber aeroporto", BigDecimal("90.00"), listOf(andre, maria, joao))

		log.info("Seed OK — login: andre@email.com / senha123 (também maria@ e joao@)")
	}

	private fun createEqualExpense(
		group: ExpenseGroup,
		paidBy: User,
		description: String,
		amount: BigDecimal,
		participants: List<User>,
	) {
		val expense = Expense(
			group = group,
			paidBy = paidBy,
			description = description,
			amount = amount.setScale(2, RoundingMode.HALF_UP),
		)
		val share = amount.divide(BigDecimal(participants.size), 2, RoundingMode.HALF_UP)
		var allocated = BigDecimal.ZERO.setScale(2)

		participants.forEachIndexed { index, user ->
			val value = if (index == participants.lastIndex) {
				amount - allocated
			} else {
				share.also { allocated += it }
			}
			expense.shares += ExpenseShare(expense = expense, user = user, shareAmount = value)
		}

		expenseRepository.save(expense)
	}
}
