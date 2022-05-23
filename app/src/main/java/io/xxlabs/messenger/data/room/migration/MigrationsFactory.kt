package io.xxlabs.messenger.data.room.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object MigrationsFactory {

    /**
     * Add Requests table.
     */
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            val createRequestsTableQuery =
                "CREATE TABLE IF NOT EXISTS `Requests` " +
                        "(`requestId` BLOB NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL, " +
                        "`unread` INTEGER NOT NULL," +
                        " PRIMARY KEY(`requestId`))"
            database.execSQL(createRequestsTableQuery)
        }
    }

    /**
     * Create an array of [Migration]s to update earlier versions
     * of the app database.
     */
    fun create(): Array<Migration> {
        val migrations = mutableListOf<Migration>(MIGRATION_1_2)
        return migrations.toTypedArray()
    }
}

