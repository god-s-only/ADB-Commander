package com.adbcommand.app.di

import android.content.Context
import com.adbcommand.app.core.ShellCommandsExecution
import com.adbcommand.app.data.remote.ShizukuManager
import com.adbcommand.app.data.repository.AppManagerRepositoryImpl
import com.adbcommand.app.data.repository.CommandsRepositoryImpl
import com.adbcommand.app.data.repository.DeviceInfoRepositoryImpl
import com.adbcommand.app.data.repository.HomeRepositoryImpl
import com.adbcommand.app.data.repository.ShizukuAppManagerRepository
import com.adbcommand.app.domain.repository.AppManagerRepository
import com.adbcommand.app.domain.repository.CommandsRepository
import com.adbcommand.app.domain.repository.DeviceInfoRepository
import com.adbcommand.app.domain.repository.HomeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideHomeRepository(shizuku: ShizukuManager, @ApplicationContext context: Context): HomeRepository{
        return HomeRepositoryImpl(context, shizuku)
    }

    @Provides
    @Singleton
    fun provideCommandsRepository(): CommandsRepository{
        return CommandsRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideDeviceInfoRepository(shellExecutor: ShellCommandsExecution, @ApplicationContext context: Context): DeviceInfoRepository {
        return DeviceInfoRepositoryImpl(shellExecutor, context)
    }

    @Provides
    @Singleton
    fun provideAppManagerRepository(shizuku: ShizukuManager, @ApplicationContext context: Context): AppManagerRepository {
        return ShizukuAppManagerRepository(context, shizuku)
    }
}