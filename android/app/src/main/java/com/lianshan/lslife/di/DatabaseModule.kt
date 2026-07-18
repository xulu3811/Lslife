package com.lianshan.lslife.di

import android.content.Context
import androidx.room.Room
import com.lianshan.lslife.core.database.AppDatabase
import com.lianshan.lslife.core.database.MerchantDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "lslife.db").build()

    @Provides
    fun provideMerchantDao(db: AppDatabase): MerchantDao = db.merchantDao()
}
