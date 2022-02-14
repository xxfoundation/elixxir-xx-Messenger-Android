package io.xxlabs.messenger.data.room.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object MigrationsFactory {
    /**
     * Create an array of [Migration]s to update earlier versions
     * of the app database.
     */
    fun create(): Array<Migration> {
        val migrations = mutableListOf<Migration>()
        return migrations.toTypedArray()
    }
}

