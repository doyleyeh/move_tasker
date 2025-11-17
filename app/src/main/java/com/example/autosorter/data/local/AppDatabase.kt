package com.example.autosorter.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.autosorter.data.dao.MoveLogDao
import com.example.autosorter.data.dao.RuleDao
import com.example.autosorter.data.entity.MoveLogEntity
import com.example.autosorter.data.entity.RuleEntity

@Database(
    entities = [RuleEntity::class, MoveLogEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ruleDao(): RuleDao
    abstract fun moveLogDao(): MoveLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, "autosorter-db")
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        db.execSQL(
                            "INSERT INTO rules (name, sourcePath, destinationPath, fileTypeFilter, extensionsFilter, enabled) " +
                                "VALUES ('Camera Photos Rule', '/storage/emulated/0/DCIM/Camera', '/storage/emulated/0/MyAutoMoved/Camera', 'IMAGE', 'jpg,jpeg,png,heic', 1)"
                        )
                    }
                })
                .build()
        }
    }
}
