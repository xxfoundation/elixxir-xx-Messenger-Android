package io.xxlabs.messenger.di.modules

import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.xxlabs.messenger.media.FullScreenImageFragment
import io.xxlabs.messenger.ui.intro.registration.RegistrationFragment
import io.xxlabs.messenger.ui.intro.registration.added.RegistrationAddedFragment
import io.xxlabs.messenger.ui.intro.registration.email.RegistrationEmailFragment
import io.xxlabs.messenger.ui.intro.registration.phone.RegistrationPhoneFragment
import io.xxlabs.messenger.ui.intro.registration.tfa.RegistrationTfaFragment
import io.xxlabs.messenger.ui.intro.registration.username.RegistrationUsernameFragment
import io.xxlabs.messenger.ui.intro.registration.welcome.RegistrationWelcomeFragment
import io.xxlabs.messenger.ui.main.TransitionMainScreen
import io.xxlabs.messenger.ui.main.chat.PrivateMessagesFragment
import io.xxlabs.messenger.ui.main.chats.ChatsFragment
import io.xxlabs.messenger.ui.main.contacts.ContactsFragment
import io.xxlabs.messenger.ui.main.contacts.PhotoSelectorFragment
import io.xxlabs.messenger.ui.main.contacts.invitation.ContactInvitation
import io.xxlabs.messenger.ui.main.contacts.profile.ContactProfileFragment
import io.xxlabs.messenger.ui.main.contacts.select.ContactSelectionFragment
import io.xxlabs.messenger.ui.main.contacts.success.ContactSuccessFragment
import io.xxlabs.messenger.ui.main.groups.GroupMessagesFragment
import io.xxlabs.messenger.ui.main.qrcode.QrCodeFragment
import io.xxlabs.messenger.ui.main.qrcode.QrCodeScanFragment
import io.xxlabs.messenger.ui.main.qrcode.QrCodeShowFragment
import io.xxlabs.messenger.ui.main.requests.RequestGenericFragment
import io.xxlabs.messenger.ui.main.requests.RequestsFragment
import io.xxlabs.messenger.ui.main.settings.DeleteAccountFragment
import io.xxlabs.messenger.ui.main.settings.SettingsFragment
import io.xxlabs.messenger.ui.main.settings.SettingsAdvancedFragment
import io.xxlabs.messenger.ui.main.ud.profile.UdProfileFragment
import io.xxlabs.messenger.ui.main.ud.search.UdSearchFragment

@Suppress("unused")
@Module
abstract class FragmentMainBuildersModule {
    @ContributesAndroidInjector
    abstract fun contributeTransitionScreen(): TransitionMainScreen

    @ContributesAndroidInjector
    abstract fun contributeRegistrationFragment(): RegistrationFragment

    @ContributesAndroidInjector
    abstract fun contributeUdSearchFragment(): UdSearchFragment

    @ContributesAndroidInjector
    abstract fun contributeRequestsGenericFragment(): RequestGenericFragment

    @ContributesAndroidInjector
    abstract fun contributeRequestsFragment(): RequestsFragment

    @ContributesAndroidInjector
    abstract fun contributeUdProfileFragment(): UdProfileFragment

    @ContributesAndroidInjector
    abstract fun contributeContactsFragment(): ContactsFragment

    @ContributesAndroidInjector
    abstract fun contributeContactInvitation(): ContactInvitation

    @ContributesAndroidInjector
    abstract fun contributeQrCodeSuccessFragment(): ContactSuccessFragment

    @ContributesAndroidInjector
    abstract fun contributeContactsProfileFragment(): ContactProfileFragment

    @ContributesAndroidInjector
    abstract fun contributeContactsSelectionFragment(): ContactSelectionFragment

    @ContributesAndroidInjector
    abstract fun contributePhotoSelectorFragment(): PhotoSelectorFragment

    @ContributesAndroidInjector
    abstract fun contributeQrCodeFragment(): QrCodeFragment

    @ContributesAndroidInjector
    abstract fun contributeQrCodeScanFragment(): QrCodeScanFragment

    @ContributesAndroidInjector
    abstract fun contributeQrCodeShowFragment(): QrCodeShowFragment

    @ContributesAndroidInjector
    abstract fun contributeConversationListFragment(): ChatsFragment

    @ContributesAndroidInjector
    abstract fun contributeChatFragment(): PrivateMessagesFragment

    @ContributesAndroidInjector
    abstract fun contributeGroupChatFragment(): GroupMessagesFragment

    @ContributesAndroidInjector
    abstract fun contributeSettingsFragment(): SettingsFragment

    @ContributesAndroidInjector
    abstract fun contributeSettingsAdvancedFragment(): SettingsAdvancedFragment

    @ContributesAndroidInjector
    abstract fun contributeRegistrationUsernameFragment(): RegistrationUsernameFragment

    @ContributesAndroidInjector
    abstract fun contributeRegistrationWelcomeFragment(): RegistrationWelcomeFragment

    @ContributesAndroidInjector
    abstract fun contributeRegistrationEmailFragment(): RegistrationEmailFragment

    @ContributesAndroidInjector
    abstract fun contributeRegistrationPhoneFragment(): RegistrationPhoneFragment

    @ContributesAndroidInjector
    abstract fun contributeRegistrationAddedFragment(): RegistrationAddedFragment

    @ContributesAndroidInjector
    abstract fun contributeRegistrationTfaFragment(): RegistrationTfaFragment

    @ContributesAndroidInjector
    abstract fun contributeDeleteAccountFragment(): DeleteAccountFragment

    @ContributesAndroidInjector
    abstract fun contributeFullScreenImageFragment(): FullScreenImageFragment
}