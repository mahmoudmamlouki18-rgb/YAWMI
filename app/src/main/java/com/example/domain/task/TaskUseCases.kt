package com.example.domain.task

import com.example.core.time.LocalClock
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.TaskStatus
import com.example.domain.repository.CategoryRepository
import com.example.domain.repository.TaskRepository
import java.time.LocalDate
import java.util.UUID

class TaskUseCases(
  private val taskRepository: TaskRepository,
  private val categoryRepository: CategoryRepository,
  private val localClock: LocalClock,
  private val idGenerator: () -> String = { UUID.randomUUID().toString() },
) {
  suspend fun createTask(input: TaskInput): TaskDomainResult<TaskEntity> {
    val now = localClock.currentEpochMs()
    val built = TaskRules.buildNewTaskEntity(input = input, nowEpochMs = now, idGenerator = idGenerator)
    val entity =
      when (built) {
        is TaskDomainResult.Failure -> return built
        is TaskDomainResult.Success -> built.value
      }

    val categoryId = entity.categoryId
    if (categoryId != null && categoryRepository.getById(categoryId) == null) {
      return TaskDomainResult.Failure(listOf(TaskValidationError.CategoryNotFound(categoryId)))
    }

    taskRepository.insert(entity)
    return TaskDomainResult.Success(entity)
  }

  suspend fun updateTask(taskId: String, input: TaskInput): TaskDomainResult<TaskEntity> {
    val existing =
      taskRepository.getById(taskId)
        ?: return TaskDomainResult.Failure(listOf(TaskValidationError.TaskNotFound(taskId)))

    val now = localClock.currentEpochMs()
    val updatedResult = TaskRules.applyEdit(existing = existing, input = input, nowEpochMs = now)
    val updated =
      when (updatedResult) {
        is TaskDomainResult.Failure -> return updatedResult
        is TaskDomainResult.Success -> updatedResult.value
      }

    val categoryId = updated.categoryId
    if (categoryId != null && categoryRepository.getById(categoryId) == null) {
      return TaskDomainResult.Failure(listOf(TaskValidationError.CategoryNotFound(categoryId)))
    }

    taskRepository.update(updated)
    return TaskDomainResult.Success(updated)
  }

  suspend fun transitionStatus(
    taskId: String,
    targetStatus: TaskStatus,
  ): TaskDomainResult<TaskEntity> {
    val existing =
      taskRepository.getById(taskId)
        ?: return TaskDomainResult.Failure(listOf(TaskValidationError.TaskNotFound(taskId)))

    val now = localClock.currentEpochMs()
    val transitionedResult =
      TaskRules.applyStatusTransition(
        existing = existing,
        targetStatus = targetStatus,
        nowEpochMs = now,
      )
    val updated =
      when (transitionedResult) {
        is TaskDomainResult.Failure -> return transitionedResult
        is TaskDomainResult.Success -> transitionedResult.value
      }

    taskRepository.updateStatus(
      id = updated.id,
      status = updated.status,
      updatedAtEpochMs = updated.updatedAtEpochMs,
      completedAtEpochMs = updated.completedAtEpochMs,
    )
    return TaskDomainResult.Success(updated)
  }

  suspend fun completeTask(taskId: String): TaskDomainResult<TaskEntity> =
    transitionStatus(taskId = taskId, targetStatus = TaskStatus.COMPLETED)

  suspend fun startTask(taskId: String): TaskDomainResult<TaskEntity> =
    transitionStatus(taskId = taskId, targetStatus = TaskStatus.IN_PROGRESS)

  suspend fun cancelTask(taskId: String): TaskDomainResult<TaskEntity> =
    transitionStatus(taskId = taskId, targetStatus = TaskStatus.CANCELLED)

  suspend fun reopenTask(taskId: String): TaskDomainResult<TaskEntity> {
    val existing =
      taskRepository.getById(taskId)
        ?: return TaskDomainResult.Failure(listOf(TaskValidationError.TaskNotFound(taskId)))

    val now = localClock.currentEpochMs()
    val reopenedResult = TaskRules.reopenTask(existing = existing, nowEpochMs = now)
    val updated =
      when (reopenedResult) {
        is TaskDomainResult.Failure -> return reopenedResult
        is TaskDomainResult.Success -> reopenedResult.value
      }

    taskRepository.updateStatus(
      id = updated.id,
      status = updated.status,
      updatedAtEpochMs = updated.updatedAtEpochMs,
      completedAtEpochMs = null,
    )
    return TaskDomainResult.Success(updated)
  }

  suspend fun postponeTask(
    taskId: String,
    targetDate: LocalDate,
    clearTimeWindow: Boolean = false,
  ): TaskDomainResult<TaskEntity> {
    val existing =
      taskRepository.getById(taskId)
        ?: return TaskDomainResult.Failure(listOf(TaskValidationError.TaskNotFound(taskId)))

    val now = localClock.currentEpochMs()
    val postponedResult =
      TaskRules.applyPostpone(
        existing = existing,
        targetDate = targetDate,
        clearTimeWindow = clearTimeWindow,
        nowEpochMs = now,
      )
    val updated =
      when (postponedResult) {
        is TaskDomainResult.Failure -> return postponedResult
        is TaskDomainResult.Success -> postponedResult.value
      }

    taskRepository.update(updated)
    return TaskDomainResult.Success(updated)
  }

  suspend fun deleteTask(taskId: String): Boolean = taskRepository.deleteById(taskId) > 0
}
