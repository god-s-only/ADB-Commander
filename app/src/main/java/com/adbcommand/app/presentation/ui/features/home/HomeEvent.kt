package com.adbcommand.app.presentation.ui.features.home

sealed class HomeEvent {
    object LoadInfo: HomeEvent()
    object GenerateCode: HomeEvent()
    object TestConnection: HomeEvent()
    object DismissStatus: HomeEvent()
    object RequestShizukuPermission: HomeEvent()
}