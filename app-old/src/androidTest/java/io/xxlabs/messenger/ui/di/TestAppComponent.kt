package io.xxlabs.messenger.ui.di

import dagger.Component
import dagger.android.AndroidInjectionModule
import io.xxlabs.messenger.di.components.AppComponent
import io.xxlabs.messenger.di.modules.ActivityBuildersModule
import io.xxlabs.messenger.di.modules.AppModule
import io.xxlabs.messenger.di.modules.ListenersBuildersModule
import io.xxlabs.messenger.di.modules.ServiceBuilderModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidInjectionModule::class,
        AppModule::class,
        ActivityBuildersModule::class,
        ListenersBuildersModule::class,
        ServiceBuilderModule::class
    ]
)
interface TestAppComponent