package com.example.splitwise.repository

import com.example.splitwise.domain.Expense
import com.example.splitwise.domain.ExpenseGroup
import com.example.splitwise.domain.GroupMember
import com.example.splitwise.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserRepository : JpaRepository<User, Long> {
	fun findByEmail(email: String): User?
	fun existsByEmail(email: String): Boolean
}

interface ExpenseGroupRepository : JpaRepository<ExpenseGroup, Long>

interface GroupMemberRepository : JpaRepository<GroupMember, Long> {
	fun findByGroupId(groupId: Long): List<GroupMember>
	fun existsByGroupIdAndUserId(groupId: Long, userId: Long): Boolean

	@Query(
		"""
		select gm.group from GroupMember gm
		where gm.user.id = :userId
		order by gm.group.createdAt desc
		""",
	)
	fun findGroupsByUserId(userId: Long): List<ExpenseGroup>
}

interface ExpenseRepository : JpaRepository<Expense, Long> {
	@Query(
		"""
		select distinct e from Expense e
		left join fetch e.shares
		left join fetch e.paidBy
		where e.group.id = :groupId
		order by e.createdAt desc
		""",
	)
	fun findByGroupIdWithShares(groupId: Long): List<Expense>
}
