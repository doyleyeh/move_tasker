package com.example.movetasker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.movetasker.data.dao.MoveLogDao
import com.example.movetasker.data.dao.RuleDao
import com.example.movetasker.data.entity.MoveLogEntity
import com.example.movetasker.data.entity.RuleEntity

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
        private const val DB_NAME = "movetasker-db"
        private const val LEGACY_DB_NAME = "autosorter-db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            migrateLegacyDatabaseIfNeeded(context)
            return Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
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

        private fun migrateLegacyDatabaseIfNeeded(context: Context) {
            val legacyDb = context.getDatabasePath(LEGACY_DB_NAME)
            val newDb = context.getDatabasePath(DB_NAME)
            if (legacyDb.exists() && !newDb.exists()) {
                legacyDb.parentFile?.mkdirs()
                legacyDb.renameTo(newDb)
                migrateSidecarFile(context, LEGACY_DB_NAME, DB_NAME, "-wal")
                migrateSidecarFile(context, LEGACY_DB_NAME, DB_NAME, "-shm")
            }
        }

        private fun migrateSidecarFile(
            context: Context,
            legacyName: String,
            newName: String,
            suffix: String
        ) {
            val oldFile = context.getDatabasePath(legacyName + suffix)
            if (!oldFile.exists()) return
            val newFile = context.getDatabasePath(newName + suffix)
            if (newFile.exists()) newFile.delete()
            oldFile.renameTo(newFile)
        }
    }
}
