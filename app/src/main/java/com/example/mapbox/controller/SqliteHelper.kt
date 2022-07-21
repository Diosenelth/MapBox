package com.example.mapbox.controller

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SqliteHelper(contex: Context?): SQLiteOpenHelper(contex, "Mapbox.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("Create table favoritos(id INTEGER PRIMARY KEY AUTOINCREMENT, nombre text, lat text, lon text)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }
}