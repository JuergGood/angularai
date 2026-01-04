package ch.goodone.angularai.android.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import ch.goodone.angularai.android.data.local.entity.TaskEntity

@Database(entities = [TaskEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract val taskDao: TaskDao
}
