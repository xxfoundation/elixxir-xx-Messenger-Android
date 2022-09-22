package io.xxlabs.messenger.application

import io.reactivex.Scheduler

interface SchedulerProvider {
    val single: Scheduler
    val io: Scheduler
    val computation: Scheduler
    val main: Scheduler
}
