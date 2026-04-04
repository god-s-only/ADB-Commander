package com.adbcommand.app.di

import com.adbcommand.app.core.ShellCommandsExecution
import com.adbcommand.app.data.repository.CommandsRepositoryImpl
import com.adbcommand.app.data.repository.HomeRepositoryImpl
import com.adbcommand.app.domain.repository.CommandsRepository
import com.adbcommand.app.domain.repository.HomeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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

}