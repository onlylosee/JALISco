package com.team.jalisco.domain.model

enum class CustomDrawerState {
    Opened,
    Closed
}

fun CustomDrawerState.isOpened(): Boolean{
    return this.name == "Opened"
}
fun CustomDrawerState.opposite(): CustomDrawerState{
    return if (this == CustomDrawerState.Opened) CustomDrawerState.Closed
    else CustomDrawerState.Opened
}