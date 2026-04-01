package com.adbcommand.app.di

import android.content.Context
import com.adbcommand.app.data.local.AdbKeyStoreManager
import com.adbcommand.app.core.ShellCommandsExecution
import com.adbcommand.app.data.repository.PairingRepositoryImpl
import com.adbcommand.app.domain.repository.PairingRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideShellCommandsExecution(): ShellCommandsExecution {
        return ShellCommandsExecution()
    }

    @Provides
    @Singleton
    fun provideAdbKeyStoreManager(
        @ApplicationContext context: Context
    ): AdbKeyStoreManager = AdbKeyStoreManager(context)


    @Provides
    @Singleton
    fun providePairingRepository(
        keyStoreManager: AdbKeyStoreManager,
        shellExecutor: ShellCommandsExecution
    ): PairingRepository = PairingRepositoryImpl(keyStoreManager, shellExecutor)
}