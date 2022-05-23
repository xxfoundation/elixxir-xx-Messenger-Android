package io.xxlabs.messenger.ui.main.qrcode

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.xxlabs.messenger.application.SchedulerProvider
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBase
import io.xxlabs.messenger.data.data.Country
import io.xxlabs.messenger.data.data.DataRequestState
import io.xxlabs.messenger.data.room.model.ContactData
import io.xxlabs.messenger.repository.DaoRepository
import io.xxlabs.messenger.repository.PreferencesRepository
import io.xxlabs.messenger.repository.base.BaseRepository
import io.xxlabs.messenger.support.extensions.toBase64String
import io.xxlabs.messenger.support.util.Utils
import timber.log.Timber
import java.io.InvalidObjectException
import javax.inject.Inject

class QrCodeViewModel @Inject constructor(
    val repo: BaseRepository,
    val daoRepo: DaoRepository,
    val preferences: PreferencesRepository,
    private val schedulers: SchedulerProvider
) : ViewModel() {
    private val subscriptions = CompositeDisposable()
    val background = MutableLiveData<Int?>()
    val scanNavigation = MutableLiveData<DataRequestState<Bundle>>()
    val qrResult = MutableLiveData<DataRequestState<Pair<String, ContactData?>>>()
    var userQrCode = MutableLiveData<DataRequestState<Bitmap?>>()
    var disableScanner = false

    fun doesUserExist(id: ByteArray): ContactData? {
        return daoRepo.getContactByUserId(id)
                .subscribeOn(schedulers.io)
                .blockingGet(ContactData(id = -1L))
    }

    fun setWindowBackgroundColor(newColor: Int?) {
        background.postValue(newColor)
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
                            userQrCode.value = (DataRequestState.Success(bmp))
                        }.subscribe()
        )
    }

    fun generateContact(rawData: ByteArray): ContactWrapperBase? {
        return repo.unmarshallContact(rawData)
    }

    fun navigateSuccess(bundle: Bundle? = null) {
        if (bundle == null) {
            scanNavigation.postValue(DataRequestState.Error(Exception("Null")))
        } else {
            scanNavigation.postValue(DataRequestState.Success(bundle))
        }
    }

    fun disableScan(): Boolean {
        return disableScanner
    }

    override fun onCleared() {
        super.onCleared()
        subscriptions.clear()
    }

    fun getUserEmail(): CharSequence {
        return repo.getStoredEmail()
    }

    fun getUserPhone(): String? {
        return Country.toFormattedNumber(repo.getStoredPhone(), false)
    }

    fun getUsername(): CharSequence {
        return repo.getStoredUsername()
    }

    fun shouldShareQrCodeEmail(): Boolean {
        return preferences.shareEmailWhenRequesting
    }

    fun shouldShareQrCodePhone(): Boolean {
        return preferences.sharePhoneWhenRequesting
    }

    fun setShareQrCodeEmail(checked: Boolean) {
        preferences.shareEmailWhenRequesting = checked
    }

    fun setShareQrCodePhone(checked: Boolean) {
        preferences.sharePhoneWhenRequesting = checked
    }

    fun handleAnalysis(
        result: Result
    ) {
        try {
            qrResult.postValue(DataRequestState.Start())
            val rawUser = result.text.toByteArray(Charsets.ISO_8859_1)
            Timber.v("User contact validation: $rawUser")
            val contactCode = generateContact(rawUser)
            if (contactCode != null) {
                Timber.v("QrCode userId: ${contactCode.getId().toBase64String()}")
                Timber.v("QrCode userFacts: ${contactCode.getStringifiedFacts()}")

                if (contactCode.getId().contentEquals(preferences.getUserId())) {
                    throw InvalidObjectException("You cannot add yourself.")
                }

                val dbContact = doesUserExist(contactCode.getId())

                if (dbContact?.id != -1L) {
                    qrResult.postValue(DataRequestState.Success(Pair(result.text, dbContact)))
                } else {
                    qrResult.postValue(DataRequestState.Success(Pair(result.text, null)))
                }
            } else {
                qrResult.postValue(DataRequestState.Error(Exception()))
            }
        } catch (err: Exception) {
            Timber.e(err.localizedMessage)
            qrResult.postValue(DataRequestState.Error(err))
        }
    }
}