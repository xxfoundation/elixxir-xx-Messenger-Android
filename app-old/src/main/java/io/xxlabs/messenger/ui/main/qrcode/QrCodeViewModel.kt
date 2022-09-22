package io.xxlabs.messenger.ui.main.qrcode

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.annotation.StringRes
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.xxlabs.messenger.R
import io.xxlabs.messenger.application.SchedulerProvider
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBase
import io.xxlabs.messenger.data.data.Country
import io.xxlabs.messenger.data.data.DataRequestState
import io.xxlabs.messenger.data.datatype.RequestStatus
import io.xxlabs.messenger.data.datatype.RequestStatus.*
import io.xxlabs.messenger.data.room.model.ContactData
import io.xxlabs.messenger.media.ImageSaver
import io.xxlabs.messenger.repository.DaoRepository
import io.xxlabs.messenger.repository.PreferencesRepository
import io.xxlabs.messenger.repository.base.BaseRepository
import io.xxlabs.messenger.requests.ui.send.RequestSender
import io.xxlabs.messenger.support.appContext
import io.xxlabs.messenger.support.extensions.toBase64String
import io.xxlabs.messenger.support.misc.DebugLogger.Companion.isExternalStorageWritable
import io.xxlabs.messenger.support.util.Utils
import io.xxlabs.messenger.ui.main.qrcode.scan.ScanQrCode
import io.xxlabs.messenger.ui.main.qrcode.scan.ScanQrCodeUI
import io.xxlabs.messenger.ui.main.qrcode.scan.ScanState
import io.xxlabs.messenger.ui.main.qrcode.show.ShareContactInfo
import io.xxlabs.messenger.ui.main.qrcode.show.ShareContactInfoListener
import io.xxlabs.messenger.ui.main.qrcode.show.ShareContactInfoUI
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import javax.inject.Inject
import com.google.zxing.Result as QrScanResult

class QrCodeViewModel @Inject constructor(
    val repo: BaseRepository,
    val daoRepo: DaoRepository,
    val preferences: PreferencesRepository,
    private val schedulers: SchedulerProvider
) : ViewModel(), ShareContactInfoListener {

    private val clipCacheDir: File =
        File(appContext().cacheDir, "xx messenger")

    val validUserData: LiveData<String?> by ::_validUserData
    private val _validUserData = MutableLiveData<String?>(null)

    val contactInfo: LiveData<ShareContactInfoUI> by ::_contactInfo
    private val _contactInfo = MutableLiveData(shareContactInfo())

    val showContactInfoOptions: LiveData<Boolean> by ::_showContactInfoOptions
    private val _showContactInfoOptions = MutableLiveData(false)

    val navigateToAddEmail: LiveData<Boolean> by ::_navigateToAddEmail
    private val _navigateToAddEmail = MutableLiveData(false)

    val navigateToAddPhone: LiveData<Boolean> by ::_navigateToAddPhone
    private val _navigateToAddPhone = MutableLiveData(false)

    val onQrCopied: LiveData<Boolean> by ::_onQrCopied
    private val _onQrCopied = MutableLiveData(false)

    override val expandVisible: LiveData<Boolean> by ::_expandVisible
    private val _expandVisible = MutableLiveData(true)

    val scanQrCodeState: LiveData<ScanQrCodeUI> by ::_scanQrCodeState
    private val _scanQrCodeState = MutableLiveData<ScanQrCodeUI>(readyState)

    val startScanner: LiveData<Boolean> by ::_startScanner
    private val _startScanner = MutableLiveData(false)

    val navigateToChat: LiveData<ContactData?> by ::_navigateToChat
    private val _navigateToChat = MutableLiveData<ContactData?>(null)

    val navigateToRequests: LiveData<Boolean> by ::_navigateToRequests
    private val _navigateToRequests = MutableLiveData(false)

    val launchSettings: LiveData<Boolean> by ::_launchSettings
    private val _launchSettings = MutableLiveData(false)

    private var cachedQrCode: Bitmap? = null
    private var clippedQrCode: Bitmap? = null
    private var cachedUri: Uri? = null

    private val subscriptions = CompositeDisposable()
    var userQrCode = MutableLiveData<DataRequestState<Bitmap?>>()

    private var hasCameraPermission = false

    private val readyState get() = ScanQrCode(
        hasCameraPermission,
        ScanState.Ready(),
        getString(R.string.scan_qr_ready_description).toSpannable(),
        null,
        _onPermissionWarningClicked = ::onPermissionWarningClicked
    )

    private val scanningState get() = ScanQrCode(
        hasCameraPermission,
        ScanState.Scanning(),
        getString(R.string.scan_qr_ready_description).toSpannable(),
        null,
        _onPermissionWarningClicked = ::onPermissionWarningClicked
    )

    private val successfulState get() = ScanQrCode(
        hasCameraPermission,
        ScanState.Successful(),
        getString(R.string.scan_qr_ready_description).toSpannable(),
        null,
        _onPermissionWarningClicked = ::onPermissionWarningClicked
    )

    private val errorState get() = ScanQrCode(
        hasCameraPermission,
        ScanState.Error(),
        getString(R.string.scan_qr_ready_description).toSpannable(),
        null,
        _onPermissionWarningClicked = ::onPermissionWarningClicked
    )

    private val ContactData.isMe : Boolean
        get() = userId.contentEquals(preferences.getUserId())

    private var cachedUser: ContactData? = null

    private val ContactData.isAlreadyKnown: Boolean
        get() {
            cachedUser = doesUserExist(userId)
            return cachedUser?.id?.let {
                it > 0L
            } ?: false
        }

    private fun doesUserExist(id: ByteArray): ContactData? {
        return daoRepo.getContactByUserId(id)
            .subscribeOn(schedulers.io)
            .blockingGet(ContactData(id = -1L))
    }

    private fun getString(@StringRes stringRes: Int): String =
        appContext().getString(stringRes)

    private fun String.toSpannable() = SpannableString(this)

    fun onCameraPermissionResult(granted: Boolean) {
        hasCameraPermission = granted
        _startScanner.value = granted
        _scanQrCodeState.value = readyState
    }

    private fun onPermissionWarningClicked() {
        _launchSettings.value = true
    }

    fun onLaunchSettingsHandled() {
        _launchSettings.value = false
    }

    fun onScannerStarted() {
        _startScanner.value = false
    }


    fun generateUserQrCode() {
        userQrCode.postValue(DataRequestState.Start())
        try {
            val marshalledUser = repo.getMashalledUser()
            generateQrCode(marshalledUser)
        } catch (err: Exception) {
            Timber.e("Error generating QR Code: ${err.localizedMessage}")
            userQrCode.postValue(DataRequestState.Error(err))
        }
    }

    private fun generateQrCode(qrByteArray: ByteArray) {
        subscriptions.add(
                Single.create<BitMatrix> {
                    val qrSize = 512
                    val writer = QRCodeWriter()
                    val bitMatrix = writer.encode(
                            String(qrByteArray, Charsets.ISO_8859_1),
                            BarcodeFormat.QR_CODE,
                            Utils.dpToPx(qrSize),
                            Utils.dpToPx(qrSize)
                    )
                    it.onSuccess(bitMatrix)
                }.subscribeOn(schedulers.single)
                        .observeOn(schedulers.computation)
                        .flatMap { bitMatrix ->
                            val width = bitMatrix.width
                            val height = bitMatrix.height
                            val bmp: Bitmap =
                                    Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                            for (x in 0 until width) {
                                for (y in 0 until height) {
                                    bmp.setPixel(
                                            x,
                                            y,
                                            if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                                    )
                                }
                            }
                            Single.just(bmp)
                        }.observeOn(schedulers.main).doOnError {
                            userQrCode.value = (DataRequestState.Error(it))
                        }.doOnSuccess { bmp ->
                            cachedQrCode = bmp
                            userQrCode.value = (DataRequestState.Success(bmp))
                        }.subscribe()
        )
    }

    private fun generateUser(rawData: ByteArray): ContactWrapperBase? =
        repo.unmarshallContact(rawData)

    private fun getUserEmail(): String? = repo.getStoredEmail().ifBlank { null }

    private fun getUserPhone(): String? =
        Country.toFormattedNumber(repo.getStoredPhone(), false)

    fun getUsername(): CharSequence = repo.getStoredUsername()

    fun parseData(result: QrScanResult) {
        if (_scanQrCodeState.value != readyState) return
        _scanQrCodeState.postValue(scanningState)
        try {
            val rawUser = result.text.toByteArray(Charsets.ISO_8859_1)
            Timber.v("User contact validation: $rawUser")
            val userBindings = generateUser(rawUser)
            if (userBindings != null) {
                Timber.v("QrCode userId: ${userBindings.getId().toBase64String()}")
                Timber.v("QrCode userFacts: ${userBindings.getStringifiedFacts()}")
                handleUser(ContactData.from(userBindings), result.text)
            } else {
                noUserFoundError()
            }
        } catch (err: Exception) {
            Timber.e(err.localizedMessage)
            scanFailedError()
        }
    }

    private fun handleUser(user: ContactData, resultData: String) {
        when {
            user.isMe -> scannedOwnCodeError()
            user.isAlreadyKnown -> handleKnownUser(cachedUser, resultData)
            else -> onScanSuccessful(resultData)
        }
    }

    private fun handleKnownUser(contact: ContactData?, resultData: String) {
        if (contact == null) {
            onScanSuccessful(resultData)
            return
        }

        when (RequestStatus.from(contact.status)) {
            ACCEPTED-> alreadyConnectionError(contact)
            SENT, RESENT, SEND_FAIL -> alreadyRequestedError()
            else -> onScanSuccessful(resultData)
        }
    }

    private fun error(
        @StringRes message: Int,
        @StringRes ctaText: Int? = null,
        onCtaClicked: ()-> Unit = {}
    ) {
        val state = errorState.copy(
            description = getString(message).toSpannable(),
            callToActionText = ctaText?.let { getString(it) },
            _onCtaClicked = onCtaClicked
        )
        _scanQrCodeState.postValue(state)
        readyAfterDelay(3000)
    }

    private fun error(
        message: Spannable,
        @StringRes ctaText: Int? = null,
        onCtaClicked: ()-> Unit = {}
    ) {
        val state = errorState.copy(
            description = message,
            callToActionText = ctaText?.let { getString(it) },
            _onCtaClicked = onCtaClicked
        )
        _scanQrCodeState.postValue(state)
        readyAfterDelay(3000)
    }

    private fun readyAfterDelay(ms: Long = 2000L) {
        viewModelScope.launch {
            delay(ms)
            _scanQrCodeState.value = readyState
            _startScanner.value = true
        }
    }

    private fun scannedOwnCodeError() {
        error(R.string.scan_qr_self_scan_error)
    }

    private fun alreadyConnectionError(contact: ContactData) {
        error(
            contact.alreadyConnectionError,
            R.string.scan_qr_already_connection_error_cta,
            ::onAlreadyConnectionCtaClicked
        )
    }

    private val ContactData.alreadyConnectionError: Spannable
        get() {
            val highlight = appContext().getColor(R.color.brand_default)
            val errorMessage = appContext().getString(
                R.string.scan_qr_already_connection_error,
                displayName
            )
            val highlightStart = errorMessage.indexOf(displayName, ignoreCase = true)
            val highlightEnd = highlightStart + displayName.length
            return SpannableString(errorMessage).apply {
                setSpan(
                    ForegroundColorSpan(highlight),
                    highlightStart,
                    highlightEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

    private fun onAlreadyConnectionCtaClicked() {
        cachedUser?.let { _navigateToChat.value = it }
    }

    fun onNavigateToChatHandled() {
        _navigateToChat.value = null
    }

    private fun alreadyRequestedError() {
        error(
            R.string.scan_qr_already_connection_error,
            R.string.scan_qr_already_requested_error_cta,
            ::onAlreadyRequestedCtaClicked
        )
    }

    private fun onAlreadyRequestedCtaClicked() {
        _navigateToRequests.value = true
    }

    fun onNavigateToRequestsHandled() {
        _navigateToRequests.value = false
    }

    private fun onScanSuccessful(resultData: String) {
        _scanQrCodeState.postValue(successfulState)
        navigateAfterDelay(resultData)
    }

    private fun navigateAfterDelay(resultData: String, ms: Long = 2000) {
        viewModelScope.launch {
            delay(ms)
            _validUserData.value = resultData
        }
    }

    fun onUserDataHandled() {
        _validUserData.value = null
    }

    private fun noUserFoundError() {
        scanFailedError()
    }

    private fun scanFailedError() {
        error(R.string.scan_qr_generic_error)
    }

    private fun shareContactInfo(): ShareContactInfoUI =
        ShareContactInfo(getContactInfo(), this)

    private fun getContactInfo(): RequestSender {
        return object : RequestSender {
            override val email: String? = getUserEmail()
            override val phone: String? = getUserPhone()
        }
    }

    override fun onExpandClicked() {
        _showContactInfoOptions.value = true
        _expandVisible.value = false
    }

    override fun onCollapseClicked() {
        _showContactInfoOptions.value = false
        _expandVisible.value = true
    }

    override fun onCopyClicked() {
        if (_onQrCopied.value == false) {
            cachedQrCode?.let { qrCode ->
                cachedUri?.let { clipUri ->
                    tryCachedClipData(qrCode, clipUri)
                } ?: createClipData(qrCode)
            }
        }
    }

    private fun tryCachedClipData(qrCode: Bitmap, clipUri: Uri) {
        if (clippedQrCode === qrCode) {
            saveToClipboard(clipUri)
            saveToGallery(qrCode)
        } else createClipData(qrCode)
    }

    private fun createClipData(qrCode: Bitmap) {
        val file = cacheImage(qrCode)
        saveToClipboard(getUri(file))
        saveToGallery(qrCode)
    }

    private fun cacheImage(bitmap: Bitmap): File? {
        clippedQrCode = bitmap
        if (isExternalStorageWritable) {
            val imageFile = File(clipCacheDir, "clip_${System.currentTimeMillis()}")
            return try {
                val outputStream: OutputStream = FileOutputStream(imageFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.close()
                imageFile
            } catch (e: Exception) {
                null
            }
        }
        return null
    }

    private fun getUri(file: File?): Uri {
        return file?.let {
            FileProvider.getUriForFile(
                appContext(),
                "io.xxlabs.messenger",
                it
            )
        } ?: Uri.EMPTY
    }

    private fun saveToClipboard(uri: Uri) {
        cachedUri = uri
        val clipboard: ClipboardManager? =
            appContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clipData = ClipData.newUri(appContext().contentResolver, "qrcode", uri)
        clipboard?.setPrimaryClip(clipData)
        _onQrCopied.value = true
    }

    private fun saveToGallery(bitmap: Bitmap) {
        ImageSaver.saveImage(bitmap)
    }

    fun onQrCopiedHandled() {
        _onQrCopied.value = false
    }

    override fun onEmailToggled(enabled: Boolean) {
        setShareQrCodeEmail(enabled)
        generateUserQrCode()
    }

    private fun setShareQrCodeEmail(checked: Boolean) {
        preferences.shareEmailWhenRequesting = checked
    }

    override fun onPhoneToggled(enabled: Boolean) {
        setShareQrCodePhone(enabled)
        generateUserQrCode()
    }

    private fun setShareQrCodePhone(checked: Boolean) {
        preferences.sharePhoneWhenRequesting = checked
    }

    override fun onAddEmailClicked() {
        _navigateToAddEmail.value = true
    }

    fun onNavigateToAddEmailHandled() {
        _navigateToAddEmail.value = false
    }

    override fun onAddPhoneClicked() {
        _navigateToAddPhone.value = true
    }

    fun onNavigateToAddPhoneHandled() {
        _navigateToAddPhone.value = false
    }

    fun onContactInfoChanged() {
        generateUserQrCode()
        _contactInfo.value = shareContactInfo()
    }

    override fun onCleared() {
        super.onCleared()
        subscriptions.clear()
    }
}