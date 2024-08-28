package com.github.redzi.mobtimerintellijplugin.model

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.util.concurrency.AppExecutorUtil
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.time.Instant
import java.time.Duration as JavaDuration

class TimeSource(private val listener: (Time) -> Unit) {
    private var future: ScheduledFuture<*>? = null

    fun start(): TimeSource {
        val runnable = {
            ApplicationManager.getApplication().invokeLater(
                { listener(Time.now()) },
                ModalityState.any() // Use "any" so that timer is updated even while modal dialog like IDE Settings is open.
            )
        }
        future = AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay(runnable, 0, 1000L, MILLISECONDS)
        return this
    }

    fun stop() {
        future?.cancel(true)
    }
}

data class Time(internal val instant: Instant = Instant.EPOCH) : Comparable<Time> {
    val epochMilli: Long = instant.toEpochMilli()

    constructor(epochMilli: Long) : this(Instant.ofEpochMilli(epochMilli))

    override fun compareTo(other: Time) = instant.compareTo(other.instant)

    operator fun plus(duration: Duration) = Time(instant + duration.delegate)

    companion object {
        val zero = Time(Instant.EPOCH)

        fun now() = Time(Instant.now())
    }
}

/**
 * The main reason to wrap java Duration is that it doesn't have default constructor
 * and IJ persistence needs it when creating transient fields.
 * Using wrapper class might be useful to see exactly which part of Duration API is used.
 */
data class Duration(internal val delegate: JavaDuration = JavaDuration.ZERO) : Comparable<Duration> {
    val millis: Long = delegate.toMillis()
    val seconds: Long = delegate.seconds
    val minutes: Long = delegate.toMinutes()

    constructor(minutes: Int) : this(minutes.toLong())
    constructor(minutes: Long) : this(JavaDuration.ofMinutes(minutes))

    override fun compareTo(other: Duration) = delegate.compareTo(other.delegate)

    operator fun minus(that: Duration): Duration = Duration(delegate - that.delegate)

    fun capAt(max: Duration) = if (this > max) max else this

    companion object {
        val zero = Duration(0)

        fun between(start: Time, end: Time) = Duration(JavaDuration.between(start.instant, end.instant))
    }
}

val Number.minutes: Duration
    get() = Duration(minutes = toInt())