package com.example.splitwise.web

import com.example.splitwise.service.BalanceService
import com.example.splitwise.service.ExpenseService
import com.example.splitwise.service.GroupService
import com.example.splitwise.web.dto.AddMemberRequest
import com.example.splitwise.web.dto.BalanceResponse
import com.example.splitwise.web.dto.CreateExpenseRequest
import com.example.splitwise.web.dto.CreateGroupRequest
import com.example.splitwise.web.dto.ExpenseResponse
import com.example.splitwise.web.dto.GroupResponse
import com.example.splitwise.web.dto.GroupSummaryResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/groups")
class GroupController(
	private val groupService: GroupService,
	private val expenseService: ExpenseService,
	private val balanceService: BalanceService,
) {

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	fun create(@Valid @RequestBody request: CreateGroupRequest): GroupResponse =
		groupService.createGroup(SecurityUtils.currentUser().id, request)

	@GetMapping
	fun listMine(): List<GroupSummaryResponse> =
		groupService.listMyGroups(SecurityUtils.currentUser().id)

	@GetMapping("/{id}")
	fun get(@PathVariable id: Long): GroupResponse =
		groupService.getGroup(SecurityUtils.currentUser().id, id)

	@PostMapping("/{id}/members")
	fun addMember(
		@PathVariable id: Long,
		@Valid @RequestBody request: AddMemberRequest,
	): GroupResponse =
		groupService.addMember(SecurityUtils.currentUser().id, id, request)

	@PostMapping("/{id}/expenses")
	@ResponseStatus(HttpStatus.CREATED)
	fun createExpense(
		@PathVariable id: Long,
		@Valid @RequestBody request: CreateExpenseRequest,
	): ExpenseResponse =
		expenseService.createExpense(SecurityUtils.currentUser().id, id, request)

	@GetMapping("/{id}/expenses")
	fun listExpenses(@PathVariable id: Long): List<ExpenseResponse> =
		expenseService.listExpenses(SecurityUtils.currentUser().id, id)

	@GetMapping("/{id}/balance")
	fun balance(@PathVariable id: Long): BalanceResponse =
		balanceService.getBalance(SecurityUtils.currentUser().id, id)
}
