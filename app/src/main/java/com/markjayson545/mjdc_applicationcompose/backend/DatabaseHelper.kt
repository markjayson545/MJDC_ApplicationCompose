package com.markjayson545.mjdc_applicationcompose.backend

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "mjdcDatabase.db"
        private const val DATABASE_VERSION = 1

        // Entity
        const val TABLE_PRODUCTS = "products"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_DESCRIPTION = "description"
    }

    private val createProductTable = """
        CREATE TABLE IF NOT EXISTS $TABLE_PRODUCTS (
            $COLUMN_ID STRING PRIMARY KEY,
            $COLUMN_NAME TEXT NOT NULL,
            $COLUMN_DESCRIPTION TEXT NOT NULL
            )
    """.trimIndent()

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(createProductTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }

}