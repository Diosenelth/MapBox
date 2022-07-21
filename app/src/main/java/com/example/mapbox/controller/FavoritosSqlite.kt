package com.example.mapbox.controller

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase

class FavoritosSqlite(context: Context) {
    private val db: SQLiteDatabase = SqliteHelper(context).writableDatabase

    fun saveFav(fav: Favorito): Boolean {
        val contentValues = ContentValues()
        contentValues.put("nombre", fav.nombre)
        contentValues.put("lat", fav.lat)
        contentValues.put("lon", fav.lon)
        val result = db.insert("favoritos", null, contentValues)
        return result.toInt() != -1
    }

    fun getList(): ArrayList<Favorito> {
        val cursor = db.rawQuery("Select * from favoritos", null)
        val items = ArrayList<Favorito>()
        with(cursor) {
            while (this.moveToNext()) {
                items.add(
                    Favorito(
                        getString(getColumnIndexOrThrow("id")),
                        getString(getColumnIndexOrThrow("nombre")),
                        getString(getColumnIndexOrThrow("lat")),
                        getString(getColumnIndexOrThrow("lon"))
                    )
                )
            }
        }
        return items
    }

    fun delFav(id: String): Boolean {
        val resultado = db.delete("favoritos", "id= ?", arrayOf(id))
        return resultado != -1
    }
}