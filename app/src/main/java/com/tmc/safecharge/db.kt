package com.tmc.safecharge

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns



object ConnectedDevices {
    // Table contents are grouped together in an anonymous object.
    object ConnectedDeviceEntry : BaseColumns {
        const val TABLE_NAME = "safecharge_devices"
        const val COLUMN_NAME_BT_MAC = "bt_mac"

        data class Entry(val bt_mac: String) {
            companion object {
                fun fromCursor(c: Cursor): Entry {
                    return Entry(c.getString(c.getColumnIndex(COLUMN_NAME_BT_MAC)))
                }
            }
        }
    }

}

class ConnectedDevicesDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }
    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    fun getEntries(): ArrayList<ConnectedDevices.ConnectedDeviceEntry.Entry> {
        return this.readableDatabase.rawQuery(SQL_GET_ENTRIES,null).map {
            ConnectedDevices.ConnectedDeviceEntry.Entry.fromCursor(it)
        }
    }

    fun addEntry(e: ConnectedDevices.ConnectedDeviceEntry.Entry) {
        val values = ContentValues().apply {
            put(ConnectedDevices.ConnectedDeviceEntry.COLUMN_NAME_BT_MAC, e.bt_mac)
        }
        this.writableDatabase.insert(ConnectedDevices.ConnectedDeviceEntry.TABLE_NAME, null, values)

    }

    companion object {
        // If you change the database schema, you must increment the database version.
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "ConnectedDevices.db"

        const val SQL_CREATE_ENTRIES =
                "CREATE TABLE ${ConnectedDevices.ConnectedDeviceEntry.TABLE_NAME} (" +
                        "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                        "${ConnectedDevices.ConnectedDeviceEntry.COLUMN_NAME_BT_MAC} TEXT)"

        const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${ConnectedDevices.ConnectedDeviceEntry.TABLE_NAME}"
        const val SQL_GET_ENTRIES = "SELECT * FROM ${ConnectedDevices.ConnectedDeviceEntry.TABLE_NAME}"
    }


}

inline fun Cursor.forEach(crossinline e: (Cursor) -> Unit) {
    if (this.moveToFirst()) {
        while (!this.isAfterLast) {
            e(this)
            this.moveToNext()
        }
    }
}

inline fun <T> Cursor.map(crossinline e: (Cursor) -> T): ArrayList<T> {
    val ret = ArrayList<T>()
    if (this.moveToFirst()) {
        while (!this.isAfterLast) {
            ret.add(e(this))
            this.moveToNext()
        }
    }
    return ret
}