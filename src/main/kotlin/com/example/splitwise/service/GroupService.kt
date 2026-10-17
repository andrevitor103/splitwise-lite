package com.example.splitwise.service

import com.example.splitwise.domain.ExpenseGroup
import com.example.splitwise.domain.GroupMember
import com.example.splitwise.domain.User
import com.example.splitwise.repository.ExpenseGroupRepository
import com.example.splitwise.repository.GroupMemberRepository
import com.example.splitwise.repository.UserRepository
import com.example.splitwise.web.ConflictException
import com.example.splitwise.web.ForbiddenException
import com.example.splitwise.web.NotFoundException
import com.example.splitwise.web.dto.AddMemberRequest
import com.example.splitwise.web.dto.CreateGroupRequest
import com.example.splitwise.web.dto.GroupResponse
import com.example.splitwise.web.dto.GroupSummaryResponse
import com.example.splitwise.web.dto.UserSummaryResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GroupService(
	private val expenseGroupRepository: ExpenseGroupRepository,
	private val groupMemberRepository: GroupMemberRepository,
	private val userRepository: UserRepository,
) {

	@Transactional
	fun createGroup(currentUserId: Long, request: CreateGroupRequest): GroupResponse {
		val creator = findUser(currentUserId)
		val group = expenseGroupRepository.save(
			ExpenseGroup(name = request.name.trim(), createdBy = creator),
		)
		groupMemberRepository.save(GroupMember(group = group, user = creator))
		return toGroupResponse(group)
	}

	@Transactional(readOnly = true)
	fun listMyGroups(currentUserId: Long): List<GroupSummaryResponse> =
		groupMemberRepository.findGroupsByUserId(currentUserId).map { group ->
			GroupSummaryResponse(
				id = requireNotNull(group.id),
				name = group.name,
				createdAt = group.createdAt,
				memberCount = groupMemberRepository.findByGroupId(requireNotNull(group.id)).size,
			)
		}

	@Transactional(readOnly = true)
	fun getGroup(currentUserId: Long, groupId: Long): GroupResponse {
		assertMembership(groupId, currentUserId)
		return toGroupResponse(findGroup(groupId))
	}

	@Transactional
	fun addMember(currentUserId: Long, groupId: Long, request: AddMemberRequest): GroupResponse {
		assertMembership(groupId, currentUserId)
		val group = findGroup(groupId)
		val user = userRepository.findByEmail(request.email.lowercase().trim())
			?: throw NotFoundException("User with email ${request.email} not found")

		if (groupMemberRepository.existsByGroupIdAndUserId(groupId, requireNotNull(user.id))) {
			throw ConflictException("User is already a member of this group")
		}

		groupMemberRepository.save(GroupMember(group = group, user = user))
		return toGroupResponse(group)
	}

	fun assertMembership(groupId: Long, userId: Long) {
		if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
			throw ForbiddenException("You are not a member of this group")
		}
	}

	fun findGroup(groupId: Long): ExpenseGroup =
		expenseGroupRepository.findById(groupId)
			.orElseThrow { NotFoundException("Group $groupId not found") }

	fun findUser(userId: Long): User =
		userRepository.findById(userId)
			.orElseThrow { NotFoundException("User $userId not found") }

	fun membersOf(groupId: Long): List<User> =
		groupMemberRepository.findByGroupId(groupId).map { it.user }

	private fun toGroupResponse(group: ExpenseGroup): GroupResponse {
		val groupId = requireNotNull(group.id)
		val members = groupMemberRepository.findByGroupId(groupId).map { toUserSummary(it.user) }
		return GroupResponse(
			id = groupId,
			name = group.name,
			createdBy = toUserSummary(group.createdBy),
			members = members,
			createdAt = group.createdAt,
		)
	}

	fun toUserSummary(user: User): UserSummaryResponse =
		UserSummaryResponse(
			id = requireNotNull(user.id),
			name = user.name,
			email = user.email,
		)
}
