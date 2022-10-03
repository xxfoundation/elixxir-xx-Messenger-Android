package io.xxlabs.messenger.ui.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.xxlabs.messenger.application.SchedulerProvider
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBase
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBindings
import io.xxlabs.messenger.data.data.DataRequestState
import io.xxlabs.messenger.data.room.model.ContactData
import io.xxlabs.messenger.repository.DaoRepository
import io.xxlabs.messenger.repository.base.BaseRepository
import io.xxlabs.messenger.support.extensions.toBase64String
import timber.log.Timber
import javax.inject.Inject

class ContactDetailsViewModel @Inject constructor(
    val repo: BaseRepository,
    val daoRepo: DaoRepository,
    val schedulers: SchedulerProvider
) : ViewModel() {
    val contactData = MutableLiveData<ContactData?>()
    val deletedChat = MutableLiveData<DataRequestState<Boolean>>()
    val deletedContact = MutableLiveData<DataRequestState<Boolean>>()
    var searchState = MutableLiveData<DataRequestState<ContactData>>()

    private var subscriptions = CompositeDisposable()
    var currContact: ContactData? = null

    fun getContactInfo(contactId: ByteArray) {
        Timber.v("Searching contact with contactId: $contactId || ${contactId.toBase64String()}")
        subscriptions.add(daoRepo.getContactByUserId(contactId)
            .subscribeOn(schedulers.io)
            .observeOn(schedulers.main)
            .subscribeBy(
                onError = { err ->
                    Timber.e(err)
                },
                onSuccess = { contact ->
                    currContact = contact
                    contactData.postValue(contact)
                },
                onComplete = {
                    Timber.v("Contact not found")
                    contactData.postValue(null)
                }
            ))
    }

    fun updateContactName(temporaryContact: ContactData) {
        subscriptions.add(daoRepo.updateContactName(temporaryContact)
            .subscribeOn(schedulers.io)
            .observeOn(schedulers.main)
            .subscribeBy(
                onError = { err ->
                    Timber.e(err)
                },
                onSuccess = {
                    currContact = temporaryContact
                    contactData.postValue(temporaryContact)
                }
            ))
    }

    private fun updateContactPhoto(temporaryContact: ContactData, photo: ByteArray) {
        subscriptions.add(daoRepo.changeContactPhoto(temporaryContact.userId, photo)
            .subscribeOn(schedulers.io)
            .observeOn(schedulers.main)
            .subscribeBy(
                onError = { err ->
                    Timber.e(err)
                },
                onSuccess = {
                    currContact = temporaryContact
                    contactData.postValue(temporaryContact)
                }
            ))
    }


    fun updateContact(temporaryContact: ContactData) {
        subscriptions.add(daoRepo.updateContact(temporaryContact)
            .subscribeOn(schedulers.io)
            .observeOn(schedulers.main)
            .subscribeBy(
                onError = { err ->
                    Timber.e(err)
                },
                onSuccess = { updatedContact ->
                    currContact = temporaryContact
                    contactData.postValue(temporaryContact)
                }
            ))
    }

    fun deleteContact(currContact: ContactData) {
        deletedContact.postValue(DataRequestState.Start())
        subscriptions.add(repo.deleteContact(currContact.marshaled!!)
            .flatMap { daoRepo.deleteAllMessages(currContact.userId) }
            .flatMap { daoRepo.deleteContact(currContact) }
            .subscribeOn(schedulers.io)
            .observeOn(schedulers.main)
            .subscribeBy(
                onError = { deletedContact.postValue(DataRequestState.Error(it)) },
                onSuccess = { deletedContact.postValue(DataRequestState.Success(true)) }
            ))
    }

    fun deleteContactChat(currContact: ContactData) {
        deletedChat.postValue(DataRequestState.Start())
        subscriptions.add(daoRepo.deleteAllMessages(currContact.userId)
            .subscribeOn(schedulers.io)
            .observeOn(schedulers.main)
            .subscribeBy(
                onError = { err ->
                    Timber.e(err)
                    deletedChat.postValue(DataRequestState.Error(err))
                },
                onSuccess = {
                    deletedChat.postValue(DataRequestState.Success(true))
                }
            ))
    }

    fun doesUserExist(id: ByteArray): ContactData {
        return daoRepo.getContactByUserId(id)
            .subscribeOn(schedulers.io)
            .blockingGet(ContactData(id = -1L))
    }

    fun searchForContact(
        dbContact: ContactData,
        marshalledContact: ByteArray
    ) {
        val bindingsContact = repo.unmarshallContact(marshalledContact)
        dbContact.userId = bindingsContact!!.getId()
        dbContact.marshaled = marshalledContact

        searchState.postValue(DataRequestState.Start())
        subscriptions.add(
            daoRepo.getContactByUserId(dbContact.userId)
                .subscribeOn(schedulers.io)
                .observeOn(schedulers.io)
                .subscribeBy(
                    onError = { err ->
                        Timber.e("Could not search for contact: ${err.localizedMessage}")
                        searchState.postValue(DataRequestState.Error(err))
                    },
                    onSuccess = { contact ->
                        Timber.e("Contact is already added ${contact.userId.toBase64String()}")
                        searchState.postValue(DataRequestState.Error(Exception("Contact is already added")))
                    },
                    onComplete = {
                        Timber.v("No contact with id ${dbContact.userId.toBase64String()} was found. Adding new contact...")
                        searchForNewContact(dbContact, marshalledContact)
                    })
        )
    }

    private fun searchForNewContact(
        contact: ContactData,
        marshalledContact: ByteArray
    ) {
        subscriptions.add(
            daoRepo.addNewContact(contact)
                .subscribeOn(schedulers.io)
                .observeOn(schedulers.main)
                .subscribeBy(
                    onError = { err ->
                        Timber.e("Couldn't save contact: ${err.localizedMessage}")
                        searchState.postValue(DataRequestState.Error(err))
                    },
                    onSuccess = { id ->
                        contact.id = id
                        Timber.v("Successfully requested authenticated channel")
                        searchState.postValue(DataRequestState.Success(contact))
                    })
        )
    }

    fun setCurrentPhoto(bitmapArray: ByteArray) {
        val currContactInstance = currContact
        if (currContactInstance != null) {
            currContactInstance.photo = bitmapArray
            updateContactPhoto(currContactInstance, bitmapArray)
        }
    }

    fun updateName(name: String) {
        val currentContactObjInstance = currContact
        if (currentContactObjInstance != null) {
            currentContactObjInstance.nickname = name
            updateContactName(currentContactObjInstance)
        }
    }

    fun isContactNameValid(text: String): Boolean {
        return text.trim().length > 3
    }

    fun generateContact(rawData: ByteArray): ContactWrapperBase? {
        return repo.unmarshallContact(rawData)
    }

    fun getNickname(): String {
        TODO()
//        return ContactWrapperBindings(contact).getNameFact()
    }

    override fun onCleared() {
        subscriptions.dispose()
        super.onCleared()
    }
}