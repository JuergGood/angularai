package ch.goodone.angularai.android.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val description: String,
    val dueDate: String?,
    val priority: String
)
