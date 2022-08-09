package io.xxlabs.messenger.data.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.xxlabs.messenger.data.room.model.ContactData
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactsDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertContact(contact: ContactData): Single<Long>

    @Update
    fun updateContact(contact: ContactData): Single<Int>

    @Delete
    fun deleteContact(contact: ContactData): Single<Int>

    @Query("DELETE FROM Contacts WHERE userId = :userId")
    fun deleteContact(userId: ByteArray): Single<Int>

    @Query("SELECT * FROM Contacts")
    fun queryAllContactsList(): List<ContactData>

    @Query("SELECT * FROM Contacts")
    fun queryAllContacts(): Single<List<ContactData>>

    @Query("SELECT * FROM Contacts WHERE username IN (:usernameList)")
    fun queryAllContacts(usernameList: List<String>): Single<List<ContactData>>

    @Query("SELECT * FROM Contacts")
    fun queryAllContactsFlowable(): Flowable<List<ContactData>>

    @Query("SELECT * FROM Contacts")
    fun getAllContactsLive(): LiveData<List<ContactData>>

    @Query("SELECT * FROM Contacts WHERE status = :status")
    fun getAllContactsWithStatus(status: Int): Flowable<List<ContactData>>

    @Query("SELECT * FROM Contacts WHERE status = :status")
    fun getAllContactsWithStatusLive(status: Int): LiveData<List<ContactData>>

    @Query("SELECT * FROM Contacts WHERE username = :username LIMIT 1")
    fun queryContactByUsername(username: String): Maybe<ContactData>

    @Query("SELECT * FROM Contacts WHERE username = :username LIMIT 1")
    fun queryContactByUsernameForce(username: String): Single<ContactData>

    @Query("UPDATE Contacts SET status = :status WHERE id = :contactId")
    fun updateContactState(contactId: Long, status: Int): Single<Int>

    @Query("UPDATE Contacts SET status = :status WHERE userId = :userId")
    fun updateContactState(userId: ByteArray, status: Int): Single<Int>

    @Query("SELECT * FROM Contacts WHERE userId = :userId LIMIT 1")
    fun queryContactByUserId(userId: ByteArray): Maybe<ContactData>

    @Query("SELECT * FROM Contacts WHERE userId = :userId LIMIT 1")
    fun queryContactByUserIdForce(userId: ByteArray): Single<ContactData>

    @Query("SELECT * FROM Contacts WHERE userId = :userId LIMIT 1")
    fun getContactFlow(userId: ByteArray): Flow<ContactData>

    @Query("SELECT * FROM Contacts WHERE id = :id LIMIT 1")
    fun queryContactById(id: Long): Maybe<ContactData>

    @Query("SELECT * FROM Contacts WHERE id = :id LIMIT 1")
    fun queryContactByIdForce(id: Long): Single<ContactData>

    @Query("UPDATE Contacts SET name = :name WHERE id = :id")
    fun updateContactName(id: Long, name: String): Single<Int>

    @Query("UPDATE Contacts SET name = :nickname WHERE userId = :userId")
    suspend fun updateContactNickname(userId: ByteArray, nickname: String): Int

    @Query("UPDATE Contacts SET username = :username WHERE id = :id")
    fun updateContactUsername(id: Long, username: String): Single<Int>

    @Query("UPDATE Contacts SET photo = :photo WHERE userId = :userId")
    fun updateContactPhoto(userId: ByteArray, photo: ByteArray): Single<Int>

    @Query("UPDATE Contacts SET email = :email WHERE id = :id")
    fun updateContactEmail(id: Long, email: String): Single<Int>

    @Query("UPDATE Contacts SET phone = :phone WHERE id = :id")
    fun updateContactPhone(id: Long, phone: String): Single<Int>

    @Query("UPDATE Contacts SET marshaled = :marshaledContact WHERE id = :id")
    fun updateContact(id: Long, marshaledContact: ByteArray): Single<Int>
}