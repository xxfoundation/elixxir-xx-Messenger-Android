package io.xxlabs.messenger.di.modules

import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.xxlabs.messenger.ui.intro.splash.SplashScreenLoadingActivity
import io.xxlabs.messenger.ui.intro.splash.SplashScreenPlaceholderActivity
import io.xxlabs.messenger.ui.main.MainActivity

@Suppress("unused")
@Module
abstract class ActivityBuildersModule {
    @ContributesAndroidInjector(modules = [FragmentMainBuildersModule::class, FragmentSharedBuildersModule::class])
    abstract fun contributeMainActivity(): MainActivity

    @ContributesAndroidInjector
    abstract fun contributeSplashScreenPlaceholder(): SplashScreenPlaceholderActivity

    @ContributesAndroidInjector
    abstract fun contributeSplashScreenLoading(): SplashScreenLoadingActivity
}