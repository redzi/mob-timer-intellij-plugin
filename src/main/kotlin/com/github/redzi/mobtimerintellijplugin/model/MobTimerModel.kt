package com.github.redzi.mobtimerintellijplugin.model

class MobTimerModel {

    private var countdown: Int = 0
    var currentTurn: Int = 1
    private var numberOfTurns: Int = 0
    private var timeInput: Int = 0

    private var paused: Boolean = true

    fun onTimer(time: Time) {
        if (paused) {
            return
        }

        --countdown
        if (countdown < 0) {
            currentTurn++
            listeners.values.forEach { it.onTurnEnded(this) }
            if (currentTurn > numberOfTurns) {
                currentTurn = 1
                paused = true
            }
            countdown = timeInput
        }

        listeners.values.forEach { it.onStateChange(this) }
    }

    private val listeners = HashMap<Any, Listener>()

    interface Listener {
        fun onStateChange(mobTimerModel: MobTimerModel)
        fun onTurnEnded(mobTimerModel: MobTimerModel)
    }

    fun addListener(key: Any, listener: Listener) {
        listeners[key] = listener
    }

    private fun toSeconds(input: String): Int {
        val s = input.split(":")
        return s[0].toInt() * 60 + s[1].toInt()
    }

    fun setTimeInput(timeInput: String) {
        this.timeInput = toSeconds(timeInput)
        this.countdown = toSeconds(timeInput)
    }

    fun setNumberOfTurns(turns: Int) {
        numberOfTurns = turns
    }

    fun getDisplayTime(): String {
        return String.format("%02d:%02d", countdown / 60, countdown % 60)
    }

    fun start() {
        paused = false
    }

    fun pause() {
        paused = true
    }
}