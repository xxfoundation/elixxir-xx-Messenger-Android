package io.xxlabs.messenger.di.components

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import io.xxlabs.messenger.application.XxMessengerApplication
import io.xxlabs.messenger.di.modules.ActivityBuildersModule
import io.xxlabs.messenger.di.modules.AppModule
import io.xxlabs.messenger.di.modules.ListenersBuildersModule
import io.xxlabs.messenger.di.modules.ServiceBuilderModule
import io.xxlabs.messenger.xxclient.MessengerModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidInjectionModule::class,
        AppModule::class,
        ActivityBuildersModule::class,
        ListenersBuildersModule::class,
        ServiceBuilderModule::class,
        MessengerModule::class
    ]
)
interface AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }

    fun inject(messengerApplication: XxMessengerApplication)
}