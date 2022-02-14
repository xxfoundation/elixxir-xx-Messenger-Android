package io.xxlabs.messenger.di.modules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import io.xxlabs.messenger.di.utils.DaggerViewModelFactory
import io.xxlabs.messenger.di.utils.ViewModelKey
import io.xxlabs.messenger.ui.base.ContactDetailsViewModel
import io.xxlabs.messenger.ui.global.ContactsViewModel
import io.xxlabs.messenger.ui.global.NetworkViewModel
import io.xxlabs.messenger.ui.intro.registration.RegistrationViewModel
import io.xxlabs.messenger.ui.intro.splash.SplashScreenViewModel
import io.xxlabs.messenger.ui.main.MainViewModel
import io.xxlabs.messenger.ui.main.chats.ChatsViewModel
import io.xxlabs.messenger.ui.main.contacts.invitation.ContactInvitationViewModel
import io.xxlabs.messenger.ui.main.qrcode.QrCodeViewModel
import io.xxlabs.messenger.ui.main.settings.SettingsViewModel
import io.xxlabs.messenger.ui.main.ud.profile.UdProfileViewModel
import io.xxlabs.messenger.ui.main.ud.registration.UdRegistrationViewModel
import io.xxlabs.messenger.ui.main.ud.search.UdSearchViewModel

@Suppress("unused")
@Module
abstract class ViewModelModule {
    //Globals
    @Binds
    @IntoMap
    @ViewModelKey(NetworkViewModel::class)
    abstract fun bindNetworkViewModel(networkViewModel: NetworkViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ContactsViewModel::class)
    abstract fun bindContactViewModel(contactsViewModel: ContactsViewModel): ViewModel

    //Local
    @Binds
    @IntoMap
    @ViewModelKey(SplashScreenViewModel::class)
    abstract fun bindSplashScreenViewModel(splashScreenViewModel: SplashScreenViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun bindMainViewModel(mainViewModel: MainViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UdSearchViewModel::class)
    abstract fun bindSearchViewModel(searchViewModel: UdSearchViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UdProfileViewModel::class)
    abstract fun bindUdProfileViewModel(udProfileViewModel: UdProfileViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ContactDetailsViewModel::class)
    abstract fun bindChatProfileViewModel(contactDetailsViewModel: ContactDetailsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ContactInvitationViewModel::class)
    abstract fun bindContactInvitationViewModel(contactInvitationViewModel: ContactInvitationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChatsViewModel::class)
    abstract fun bindConversationsViewModel(chatsViewModel: ChatsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RegistrationViewModel::class)
    abstract fun bindRegistrationViewModel(registrationViewModel: RegistrationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UdRegistrationViewModel::class)
    abstract fun bindRegistrationUdbViewModel(udRegistrationViewModel: UdRegistrationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    abstract fun bindSettingsViewModel(settingsViewModel: SettingsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(QrCodeViewModel::class)
    abstract fun bindQrCodeViewModel(qrCodeViewModel: QrCodeViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: DaggerViewModelFactory): ViewModelProvider.Factory
}