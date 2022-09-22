package io.xxlabs.messenger.backup.ui.list

import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.xxlabs.messenger.R
import io.xxlabs.messenger.backup.cloud.CloudAuthentication
import io.xxlabs.messenger.backup.data.backup.BackupManager
import io.xxlabs.messenger.support.appContext

class BackupListViewModel @AssistedInject constructor(
    backupManager: BackupManager,
    @Assisted cloudAuthSource: CloudAuthentication
) : BackupLocationsViewModel(backupManager, cloudAuthSource) {

    override val backupLocationsTitle: Spanned = getSpannableTitle()
    override val backupLocationsDescription: Spanned = getSpannableDescription()

    private fun getSpannableTitle(): Spanned {
        val highlight = appContext().getColor(R.color.brand_default)
        val title = appContext().getString(R.string.backup_setup_title)
        val highlightedText = appContext().getString(R.string.backup_setup_title_span_text)
        val startIndex = title.indexOf(highlightedText, ignoreCase = true)

        return SpannableString(title).apply {
            setSpan(
                ForegroundColorSpan(highlight),
                startIndex,
                startIndex + highlightedText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    private fun getSpannableDescription(): Spanned {
        val description = appContext().getString(R.string.backup_setup_description)
        return SpannableString(description)
    }

    companion object {
        fun provideFactory(
            assistedFactory: BackupListViewModelFactory,
            cloudAuthSource: CloudAuthentication
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(cloudAuthSource) as T
            }
        }
    }
}

@AssistedFactory
interface BackupListViewModelFactory {
    fun create(cloudAuthSource: CloudAuthentication): BackupListViewModel
}