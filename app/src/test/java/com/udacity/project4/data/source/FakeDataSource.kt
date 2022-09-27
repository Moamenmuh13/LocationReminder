package com.udacity.project4.data.source

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result


//Create fakeDataSource as double to realtime database
class FakeDataSource(private val reminders: MutableList<ReminderDTO> = mutableListOf()) :
    ReminderDataSource {

    private var returnError: Boolean = false

    fun setReturnError(hasError: Boolean) {
        this.returnError = hasError
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders.add(reminder)
    }
    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return if (returnError) {
            Result.Error("Error", 404)
        } else
            Result.Success(ArrayList(reminders))
    }
    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        return when {
            returnError -> {
                Result.Error("You got error")
            }
            else -> {
                val reminder = reminders.find { it.id == id }
                if (reminder != null) {
                    Result.Success(reminder)
                } else {
                    Result.Error("You gout Error")
                }
            }
        }
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }
}