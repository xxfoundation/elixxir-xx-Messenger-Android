package io.xxlabs.messenger.application

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class AppRxSchedulers : SchedulerProvider {
    override val single: Scheduler
        get() = Schedulers.single()
    override val io: Scheduler
        get() = Schedulers.io()
    override val computation: Scheduler
        get() = Schedulers.computation()
    override val main: Scheduler
        get() = AndroidSchedulers.mainThread()
}
