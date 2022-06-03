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
     * Add NewConnections table.
     */
    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            val createNewConnectionsQuery =
                "CREATE TABLE IF NOT EXISTS `NewConnections` " +
                        "(`userId` TEXT NOT NULL," +
                        " PRIMARY KEY(`userId`))"
            database.execSQL(createNewConnectionsQuery)
        }
    }

    /**
     * Create an array of [Migration]s to update earlier versions
     * of the app database.
     */
    fun create(): Array<Migration> {
        val migrations = mutableListOf(
            MIGRATION_1_2,
            MIGRATION_2_3
        )
        return migrations.toTypedArray()
    }
}

