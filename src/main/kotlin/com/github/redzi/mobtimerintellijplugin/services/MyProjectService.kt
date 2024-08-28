package com.github.redzi.mobtimerintellijplugin.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.github.redzi.mobtimerintellijplugin.MyBundle
import com.github.redzi.mobtimerintellijplugin.model.MobTimerModel
import com.github.redzi.mobtimerintellijplugin.model.TimeSource

@Service(Service.Level.PROJECT)
class MyProjectService {

    val model = MobTimerModel()

    private val timeSource = TimeSource(listener = { time -> model.onTimer(time) }).start()

    fun getRandomNumber() = (1..100).random()
}
