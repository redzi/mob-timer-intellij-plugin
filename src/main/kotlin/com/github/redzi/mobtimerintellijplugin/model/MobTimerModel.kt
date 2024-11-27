package com.github.redzi.mobtimerintellijplugin.model

class MobTimerModel {

    private var countdown: Int = 0
    var breakCountdown: Int = 0
    var breakTime: Int = 0
    var sessionBreakTime: Int = 0
    var currentTurn: Int = 1
    private var numberOfTurns: Int = 0
    private var timeInput: Int = 0

    private var paused: Boolean = true
    var popup: Boolean = false

    fun onTimer(time: Time) {
        if (popup) {
            --breakCountdown
            if (breakCountdown < 0) {
                // turn break
                start()
                breakCountdown = breakTime
            }
        }
        if (!paused) {
            --countdown
            if (countdown < 0) {
                currentTurn++
                listeners.values.forEach { it.onTurnEnded(this) }
                if (currentTurn > numberOfTurns) {
                    // session break
                    currentTurn = 1
                    breakCountdown = sessionBreakTime
                }
                countdown = timeInput
            }
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

    fun setTimeInput(timeInput: String, breakInput: String, sessionBreakInput: String) {
        this.timeInput = toSeconds(timeInput)
        this.countdown = toSeconds(timeInput)
        this.breakTime = toSeconds(breakInput)
        this.sessionBreakTime = toSeconds(sessionBreakInput)
        this.breakCountdown = this.breakTime
    }

    fun setNumberOfTurns(turns: Int) {
        numberOfTurns = turns
    }

    fun getDisplayTime(): String {
        return String.format("%02d:%02d", countdown / 60, countdown % 60)
    }

    fun start() {
        paused = false
        popup = false
    }

    fun pause() {
        paused = true
    }
}