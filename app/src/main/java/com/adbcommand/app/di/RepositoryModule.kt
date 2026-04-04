package com.adbcommand.app.di

import android.content.Context
import com.adbcommand.app.core.ShellCommandsExecution
import com.adbcommand.app.data.repository.CommandsRepositoryImpl
import com.adbcommand.app.data.repository.DeviceInfoRepositoryImpl
import com.adbcommand.app.data.repository.HomeRepositoryImpl
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
    fun provideHomeRepository(shellExecutor: ShellCommandsExecution): HomeRepository{
        return HomeRepositoryImpl(shellExecutor)
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
}