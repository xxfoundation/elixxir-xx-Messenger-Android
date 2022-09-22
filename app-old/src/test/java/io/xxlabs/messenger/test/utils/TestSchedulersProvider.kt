package io.xxlabs.messenger.test.utils

import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import io.xxlabs.messenger.application.SchedulerProvider

class TestSchedulersProvider : SchedulerProvider {
    override val single: Scheduler
        get() = Schedulers.trampoline()
    override val io: Scheduler
        get() = Schedulers.trampoline()
    override val main: Scheduler
        get() = Schedulers.trampoline()
    override val computation: Scheduler
        get() = Schedulers.trampoline()
}