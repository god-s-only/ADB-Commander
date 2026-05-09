package com.adbcommand.app.domain.usecase.inspector

import com.adbcommand.app.domain.models.AppInspection
import com.adbcommand.app.domain.repository.AppInspectorRepository
import javax.inject.Inject

class InspectAppUseCase @Inject constructor(
    private val repository: AppInspectorRepository
) {
    suspend operator fun invoke(packageName: String): Result<AppInspection> =
        repository.inspectApp(packageName)
}
