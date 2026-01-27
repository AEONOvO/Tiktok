package com.example.tiktok.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [CommentEntity::class], // 登记表：告诉数据库有哪些表
    version = 1,                       // 版本号：数据库结构变更时要升级这个数字
    exportSchema = false
)
@TypeConverters(UserBeanConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun commentDao(): CommentDao

    companion object {
        // 保证多线程可见性
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tiktok_database"
                )
                    .fallbackToDestructiveMigration()  // 开发阶段，版本升级时清空数据
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}