package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.dao.RecurrenceDao
import com.example.data.local.dao.ReminderDao
import com.example.data.local.dao.ReminderDeliveryDao
import com.example.data.local.dao.TaskDao
import com.example.data.local.db.YawmiDatabase
import com.example.data.local.entity.ReminderOwnerType
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.TaskRecurrenceEntity
import com.example.data.local.entity.TaskStatus
import com.example.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow

class RoomTaskRepository(
  private val database: YawmiDatabase,
  private val taskDao: TaskDao,
  private val recurrenceDao: RecurrenceDao,
  private val reminderDao: ReminderDao,
  private val deliveryDao: ReminderDeliveryDao,
  private val outboxCoordinator: ReminderOutboxCoordinator,
) : TaskRepository {
  override fun observeAllTasks(): Flow<List<TaskEntity>> = taskDao.observeAllTasks()

  override fun observeTasksForDate(date: String): Flow<List<TaskEntity>> =
    taskDao.observeTasksForDate(date)

  override fun observeUnscheduledTasks(): Flow<List<TaskEntity>> =
    taskDao.observeUnscheduledTasks()

  override fun observeTaskById(id: String): Flow<TaskEntity?> = taskDao.observeTaskById(id)

  override suspend fun getById(id: String): TaskEntity? = taskDao.getById(id)

  override suspend fun insert(task: TaskEntity) = taskDao.insert(task)

  override suspend fun update(task: TaskEntity) = taskDao.update(task)

  override suspend fun updateStatus(
    id: String,
    status: TaskStatus,
    updatedAtEpochMs: Long,
    completedAtEpochMs: Long?,
  ): Int = database.withTransaction {
    val updated = taskDao.updateStatus(id, status, updatedAtEpochMs, completedAtEpochMs)
    if (updated > 0 && (status == TaskStatus.COMPLETED || status == TaskStatus.CANCELLED)) {
      val reminders = reminderDao.getTaskRemindersByTaskId(id)
      for (reminder in reminders) {
        deliveryDao.markPendingTaskDeliveriesStale(reminder.id, updatedAtEpochMs)
        if (reminder.isEnabled) {
          val nextRev = reminder.scheduleRevision + 1L
          reminderDao.updateTaskReminder(
            reminder.copy(
              isEnabled = false,
              scheduleRevision = nextRev,
              updatedAtEpochMs = updatedAtEpochMs,
            )
          )
          outboxCoordinator.stageCancelAlarmAndNotificationLocked(
            ownerType = ReminderOwnerType.TASK,
            reminderId = reminder.id,
            requestCode = reminder.requestCode,
            targetScheduleRevision = nextRev,
            nowEpochMs = updatedAtEpochMs,
          )
        }
      }
    }
    updated
  }

  override suspend fun deleteById(id: String): Int = database.withTransaction {
    val now = System.currentTimeMillis()
    val reminders = reminderDao.getTaskRemindersByTaskId(id)
    for (reminder in reminders) {
      deliveryDao.markPendingTaskDeliveriesStale(reminder.id, now)
      outboxCoordinator.stageCancelAlarmAndNotificationLocked(
        ownerType = ReminderOwnerType.TASK,
        reminderId = reminder.id,
        requestCode = reminder.requestCode,
        targetScheduleRevision = reminder.scheduleRevision + 1L,
        nowEpochMs = now,
      )
    }
    taskDao.deleteById(id)
  }

  override fun observeTaskRecurrence(taskId: String): Flow<TaskRecurrenceEntity?> =
    recurrenceDao.observeTaskRecurrence(taskId)

  override suspend fun getTaskRecurrence(taskId: String): TaskRecurrenceEntity? =
    recurrenceDao.getTaskRecurrenceByTaskId(taskId)

  override suspend fun upsertTaskRecurrence(recurrence: TaskRecurrenceEntity) =
    database.withTransaction {
      val existing = recurrenceDao.getTaskRecurrenceByTaskId(recurrence.taskId)
      if (existing == null) {
        recurrenceDao.insertTaskRecurrence(recurrence)
      } else {
        recurrenceDao.updateTaskRecurrence(recurrence)
      }
    }

  override suspend fun deleteTaskRecurrence(taskId: String): Int =
    recurrenceDao.deleteTaskRecurrenceByTaskId(taskId)
}
