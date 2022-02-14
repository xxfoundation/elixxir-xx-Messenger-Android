package io.xxlabs.messenger.di.modules

import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.xxlabs.messenger.biometrics.BiometricContainerProvider
import io.xxlabs.messenger.ui.base.BaseFragment

@Suppress("unused")
@Module
abstract class FragmentSharedBuildersModule {
    @ContributesAndroidInjector
    abstract fun contributeBiometricContainerFragment(): BiometricContainerProvider

    @ContributesAndroidInjector
    abstract fun contributeBaseFragment(): BaseFragment
}