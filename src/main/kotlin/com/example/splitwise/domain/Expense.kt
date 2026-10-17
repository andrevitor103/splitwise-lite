package com.example.splitwise.domain

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "expenses")
class Expense(
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	var id: Long? = null,

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "group_id", nullable = false)
	var group: ExpenseGroup,

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "paid_by_id", nullable = false)
	var paidBy: User,

	@Column(nullable = false)
	var description: String,

	@Column(nullable = false, precision = 19, scale = 2)
	var amount: BigDecimal,

	@Column(nullable = false)
	var createdAt: Instant = Instant.now(),

	@OneToMany(mappedBy = "expense", cascade = [CascadeType.ALL], orphanRemoval = true)
	var shares: MutableList<ExpenseShare> = mutableListOf(),
)
