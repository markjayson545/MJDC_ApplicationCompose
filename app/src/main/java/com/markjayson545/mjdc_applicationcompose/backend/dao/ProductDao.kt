package com.markjayson545.mjdc_applicationcompose.backend.dao

import android.content.ContentValues
import android.database.Cursor
import com.markjayson545.mjdc_applicationcompose.backend.DatabaseHelper
import com.markjayson545.mjdc_applicationcompose.backend.DatabaseHelper.Companion.COLUMN_DESCRIPTION
import com.markjayson545.mjdc_applicationcompose.backend.DatabaseHelper.Companion.COLUMN_ID
import com.markjayson545.mjdc_applicationcompose.backend.DatabaseHelper.Companion.COLUMN_NAME
import com.markjayson545.mjdc_applicationcompose.backend.DatabaseHelper.Companion.TABLE_PRODUCTS
import com.markjayson545.mjdc_applicationcompose.backend.model.Product

class ProductDao(private val databaseHelper: DatabaseHelper) {

    fun insertProduct(name: String, description: String): Long {
        val nextId = getProductCount()
        val maskId = "SN-$nextId"
        val values = ContentValues().apply {
            put(COLUMN_ID, maskId)
            put(COLUMN_NAME, name)
            put(COLUMN_DESCRIPTION, description)
        }
        return databaseHelper.writableDatabase.insert(TABLE_PRODUCTS, null, values)
    }

    fun getAllProducts(): List<Product> {
        val dataList = mutableListOf<Product>()
        val cursor: Cursor = databaseHelper.readableDatabase.query(
            TABLE_PRODUCTS, null, null, null, null, null, null
        )
        with(cursor) {
            while (moveToNext()) {
                val id = getString(getColumnIndexOrThrow(COLUMN_ID))
                val name = getString(getColumnIndexOrThrow(COLUMN_NAME))
                val description = getString(getColumnIndexOrThrow(COLUMN_DESCRIPTION))

                dataList.add(Product(id, name, description))
            }
        }
        cursor.close()
        return dataList
    }

    fun getProductCount(): Long {
        val cursor: Cursor = databaseHelper.readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_PRODUCTS", null
        )
        var count: Long = 0
        if (cursor.moveToFirst()) {
            count = cursor.getLong(0)
        }
        cursor.close()
        return count
    }

    fun updateProduct(id: String, name: String, description: String): Int {
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_DESCRIPTION, description)
        }
        val selection = "$COLUMN_ID = ?"
        val selectionArgs = arrayOf(id)
        return databaseHelper.writableDatabase.update(
            TABLE_PRODUCTS,
            values,
            selection,
            selectionArgs
        )
    }

    fun deleteProduct(id: String): Int {
        val selection = "$COLUMN_ID = ?"
        val selectionArgs = arrayOf(id.toString())
        return databaseHelper.writableDatabase.delete(TABLE_PRODUCTS, selection, selectionArgs)
    }
}