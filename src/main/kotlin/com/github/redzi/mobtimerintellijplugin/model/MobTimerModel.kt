package com.github.redzi.mobtimerintellijplugin.model

class MobTimerModel() {
    fun onTimer(time: Time) {
        listeners.values.forEach { it.onStateChange() }
    }

    private val listeners = HashMap<Any, Listener>()

    interface Listener {
        fun onStateChange()
    }

    fun addListener(key: Any, listener: Listener) {
        listeners[key] = listener
    }
}