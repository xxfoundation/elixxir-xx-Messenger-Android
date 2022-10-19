package io.xxlabs.messenger.ui.intro.registration

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.xxlabs.messenger.backup.ui.list.RestoreListFragment
import io.xxlabs.messenger.backup.ui.restore.RestoreDetailFragment
import io.xxlabs.messenger.ui.intro.registration.email.EmailRegistration
import io.xxlabs.messenger.ui.intro.registration.email.EmailRegistrationController
import io.xxlabs.messenger.ui.intro.registration.email.RegistrationEmailFragment
import io.xxlabs.messenger.ui.intro.registration.phone.PhoneRegistration
import io.xxlabs.messenger.ui.intro.registration.phone.PhoneRegistrationController
import io.xxlabs.messenger.ui.intro.registration.phone.RegistrationPhoneFragment
import io.xxlabs.messenger.ui.intro.registration.success.CompletedRegistrationStep
import io.xxlabs.messenger.ui.intro.registration.success.CompletedRegistrationStepController
import io.xxlabs.messenger.ui.intro.registration.success.RegistrationCompletedStepFragment
import io.xxlabs.messenger.ui.intro.registration.tfa.RegistrationTfaFragment
import io.xxlabs.messenger.ui.intro.registration.tfa.TfaRegistration
import io.xxlabs.messenger.ui.intro.registration.tfa.TfaRegistrationController
import io.xxlabs.messenger.ui.intro.registration.username.RegistrationUsernameFragment
import io.xxlabs.messenger.ui.intro.registration.username.UsernameRegistration
import io.xxlabs.messenger.ui.intro.registration.username.UsernameRegistrationController
import io.xxlabs.messenger.ui.intro.registration.welcome.RegistrationWelcomeFragment
import io.xxlabs.messenger.ui.intro.registration.welcome.WelcomeRegistration
import io.xxlabs.messenger.ui.intro.registration.welcome.WelcomeRegistrationController

@Module
interface RegistrationModule {

    /* Fragments */

    @ContributesAndroidInjector
    abstract fun contributeRegistrationFlowFragment(): RegistrationFlowFragment

    @ContributesAndroidInjector
    abstract fun contributeRegistrationUsernameFragment(): RegistrationUsernameFragment

    @ContributesAndroidInjector
    abstract fun contributeRegistrationWelcomeFragment(): RegistrationWelcomeFragment

    @ContributesAndroidInjector
    abstract fun contributeRegistrationEmailFragment(): RegistrationEmailFragment

    @ContributesAndroidInjector
    abstract fun contributeRegistrationPhoneFragment(): RegistrationPhoneFragment

    @ContributesAndroidInjector
    abstract fun contributeRegistrationAddedFragment(): RegistrationCompletedStepFragment

    @ContributesAndroidInjector
    abstract fun contributeRegistrationTfaFragment(): RegistrationTfaFragment

    @ContributesAndroidInjector
    abstract fun contributeRestoreAccountFragment(): RestoreListFragment

    @ContributesAndroidInjector
    abstract fun contributeRestoreDetailFragment(): RestoreDetailFragment

    /* Logic */

    @Binds
    fun usernameStep(registration: UsernameRegistration): UsernameRegistrationController

    @Binds
    fun welcomeStep(registration: WelcomeRegistration): WelcomeRegistrationController

    @Binds
    fun emailStep(registration: EmailRegistration): EmailRegistrationController

    @Binds
    fun phoneStep(registration: PhoneRegistration): PhoneRegistrationController

    @Binds
    fun tfaStep(registration: TfaRegistration): TfaRegistrationController

    @Binds
    fun completedRegistrationStep(
        registration: CompletedRegistrationStep
    ): CompletedRegistrationStepController
}