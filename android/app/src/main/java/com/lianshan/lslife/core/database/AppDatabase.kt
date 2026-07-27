package com.lianshan.lslife.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [MerchantEntity::class, ChatSessionEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun merchantDao(): MerchantDao
    abstract fun chatSessionDao(): ChatSessionDao
}
