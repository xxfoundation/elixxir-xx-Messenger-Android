package io.xxlabs.messenger.di.modules

import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.xxlabs.messenger.backup.ui.backup.BackupDetailFragment
import io.xxlabs.messenger.backup.ui.backup.BackupSettingsFragment
import io.xxlabs.messenger.backup.ui.list.BackupListFragment
import io.xxlabs.messenger.media.FullScreenImageFragment
import io.xxlabs.messenger.requests.deprecated.RequestGenericFragment
import io.xxlabs.messenger.requests.ui.RequestsFragment
import io.xxlabs.messenger.requests.ui.accepted.contact.RequestAcceptedDialog
import io.xxlabs.messenger.requests.ui.accepted.group.InvitationAcceptedDialog
import io.xxlabs.messenger.requests.ui.details.contact.RequestDetailsDialog
import io.xxlabs.messenger.requests.ui.details.group.InvitationDetailsDialog
import io.xxlabs.messenger.requests.ui.list.FailedRequestsFragment
import io.xxlabs.messenger.requests.ui.list.ReceivedRequestsFragment
import io.xxlabs.messenger.requests.ui.list.SentRequestsFragment
import io.xxlabs.messenger.requests.ui.nickname.SaveNicknameDialog
import io.xxlabs.messenger.requests.ui.send.SendRequestDialog
import io.xxlabs.messenger.search.UserSearchFragment
import io.xxlabs.messenger.ui.main.chat.PrivateMessagesFragment
import io.xxlabs.messenger.ui.main.chats.ChatsFragment
import io.xxlabs.messenger.ui.main.contacts.list.ContactListFragment
import io.xxlabs.messenger.ui.main.contacts.PhotoSelectorFragment
import io.xxlabs.messenger.ui.main.contacts.invitation.ContactInvitation
import io.xxlabs.messenger.ui.main.contacts.profile.ContactProfileFragment
import io.xxlabs.messenger.ui.main.contacts.select.ContactSelectionFragment
import io.xxlabs.messenger.ui.main.contacts.success.ContactSuccessFragment
import io.xxlabs.messenger.ui.main.groups.GroupMessagesFragment
import io.xxlabs.messenger.ui.main.qrcode.QrCodeFragment
import io.xxlabs.messenger.ui.main.qrcode.scan.QrCodeScanFragment
import io.xxlabs.messenger.ui.main.qrcode.show.QrCodeShowFragment
import io.xxlabs.messenger.ui.main.settings.DeleteAccountFragment
import io.xxlabs.messenger.ui.main.settings.SettingsAdvancedFragment
import io.xxlabs.messenger.ui.main.settings.SettingsFragment
import io.xxlabs.messenger.ui.main.ud.profile.UdProfileFragment
import io.xxlabs.messenger.ui.main.ud.search.UdSearchFragment

@Suppress("unused")
@Module
abstract class FragmentMainBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeUdSearchFragment(): UdSearchFragment

    @ContributesAndroidInjector
    abstract fun contributeRequestsGenericFragment(): RequestGenericFragment

    @ContributesAndroidInjector
    abstract fun contributeRequestsFragment(): RequestsFragment

    @ContributesAndroidInjector
    abstract fun contributeUdProfileFragment(): UdProfileFragment

    @ContributesAndroidInjector
    abstract fun contributeContactsFragment(): ContactListFragment

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
    abstract fun contributeDeleteAccountFragment(): DeleteAccountFragment

    @ContributesAndroidInjector
    abstract fun contributeFullScreenImageFragment(): FullScreenImageFragment

    @ContributesAndroidInjector
    abstract fun contributeBackupSetupFragment(): BackupListFragment

    @ContributesAndroidInjector
    abstract fun contributeBackupDetailFragment(): BackupDetailFragment

    @ContributesAndroidInjector
    abstract fun contributeBackupSettingsFragment(): BackupSettingsFragment

    @ContributesAndroidInjector
    abstract fun contributeSentRequestsFragment(): SentRequestsFragment

    @ContributesAndroidInjector
    abstract fun contributeReceivedRequestsFragment(): ReceivedRequestsFragment

    @ContributesAndroidInjector
    abstract fun contributeFailedRequestsFragment(): FailedRequestsFragment

    @ContributesAndroidInjector
    abstract fun contributeRequestDetailsDialog(): RequestDetailsDialog

    @ContributesAndroidInjector
    abstract fun contributeRequestAcceptedDialog(): RequestAcceptedDialog

    @ContributesAndroidInjector
    abstract fun contributeInvitationDetailsDialog(): InvitationDetailsDialog

    @ContributesAndroidInjector
    abstract fun contributeSendRequestDialog(): SendRequestDialog

    @ContributesAndroidInjector
    abstract fun contributeSaveNicknameDialog(): SaveNicknameDialog

    @ContributesAndroidInjector
    abstract fun contributeInvitationAcceptedDialog(): InvitationAcceptedDialog

    @ContributesAndroidInjector
    abstract fun contributeUserSearchFragment(): UserSearchFragment
}