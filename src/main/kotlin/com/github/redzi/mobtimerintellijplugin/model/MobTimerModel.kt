package com.github.redzi.mobtimerintellijplugin.model

class MobTimerModel {

    private var countdown: Int = 0
    var breakCountdown: Int = 0
    var breakTime: Int = 0
    var sessionBreakTime: Int = 0
    var currentTurn: Int = 1
    var numberOfTurns: Int = 1
    private var timeInput: Int = 0

    private var paused: Boolean = true
    private var popupPaused: Boolean = false
    var popup: Boolean = false

    fun onTimer(time: Time) {
        if (!popupPaused) {
            if (popup) {
                --breakCountdown
                if (breakCountdown < 0) {
                    // turn break
                    start()
                    breakCountdown = breakTime
                }
            }
        }

        if (!paused) {
            --countdown
            if (countdown < 0) {
                nextTurn()
                countdown = timeInput
            }
        }

        listeners.values.forEach { it.onStateChange(this) }
    }

    fun nextTurn() {
        currentTurn++
        listeners.values.forEach {
            it.onStateChange(this)
            it.onTurnEnded(this)
        }
        if (currentTurn > numberOfTurns) {
            // session break
            currentTurn = 1
            breakCountdown = sessionBreakTime
        }
    }

    fun skipTurn() {
        currentTurn++
        listeners.values.forEach {
            it.onStateChange(this)
        }
        if (currentTurn > numberOfTurns) {
            // session break
            currentTurn = 1
            breakCountdown = sessionBreakTime
        }
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
    }

    fun getDisplayTime(): String {
        return String.format("%02d:%02d", countdown / 60, countdown % 60)
    }

    fun start() {
        paused = false
        breakCountdown = this.breakTime
        popup = false
    }

    fun pause() {
        paused = true
    }

    fun popupPause() {
        this.popupPaused = true
    }

    fun resume() {
        this.popupPaused = false
    }
}