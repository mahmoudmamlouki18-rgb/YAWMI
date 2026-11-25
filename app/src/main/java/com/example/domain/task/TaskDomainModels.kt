package com.example.domain.task

import com.example.data.local.entity.TaskPriority
import com.example.data.local.entity.TaskStatus

data class TaskInput(
  val title: String,
  val description: String? = null,
  val scheduledDate: String? = null,
  val startTime: String? = null,
  val endTime: String? = null,
  val priority: TaskPriority = TaskPriority.MEDIUM,
  val categoryId: String? = null,
  val notes: String? = null,
)

sealed interface TaskValidationError {
  data object EmptyTitle : TaskValidationError

  data class InvalidScheduledDate(val raw: String) : TaskValidationError

  data class InvalidStartTime(val raw: String) : TaskValidationError

  data class InvalidEndTime(val raw: String) : TaskValidationError

  data object EndTimeWithoutStartTime : TaskValidationError

  data object EndTimeBeforeStartTime : TaskValidationError

  data class CategoryNotFound(val categoryId: String) : TaskValidationError

  data class TaskNotFound(val taskId: String) : TaskValidationError

  data class InvalidStatusTransition(
    val from: TaskStatus,
    val to: TaskStatus,
  ) : TaskValidationError
}

sealed interface TaskDomainResult<out T> {
  data class Success<T>(val value: T) : TaskDomainResult<T>

  data class Failure(val errors: List<TaskValidationError>) : TaskDomainResult<Nothing> {
    init {
      require(errors.isNotEmpty()) { "Failure must contain at least one TaskValidationError" }
    }
  }
}
