package com.example.mapbox.views

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.mapbox.model.GeoJSON
import com.example.mapbox.R
import com.example.mapbox.databinding.ActivityMainBinding
import com.google.gson.Gson
import com.mapbox.maps.Style
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val gson = Gson()
    private val url =
        "https://d2ad6b4ur7yvpq.cloudfront.net/naturalearth-3.3.0/ne_50m_populated_places_simple.geojson"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        try {
            checkPermission()
        }catch (e: Exception){
            Toast.makeText(this, "Error ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }



    private fun obtenerJson() {
        CoroutineScope(Dispatchers.IO).launch {
            val queue = Volley.newRequestQueue(baseContext)
            val jsonObjectRequest = StringRequest(Request.Method.GET, url,
                { res ->
                    val json = JSONObject(res.toString())
                    val geo: GeoJSON = gson.fromJson(json.toString(), GeoJSON::class.java)
                    runOnUiThread {
                        cargarMapa(geo)
                    }
                },
                {
                    Toast.makeText(baseContext, "Error al obtener respuesta, verifique conexion a internet", Toast.LENGTH_LONG)
                        .show()
                    finish()
                })
            queue.add(jsonObjectRequest)
        }
    }


    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                this,android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ), 1
            )
        } else {
            obtenerJson()
        }
    }

    private fun cargarMapa(geo: GeoJSON) {
        try {
            binding.progressBar.visibility = View.GONE
            val fragment = MapsFragment()
            fragment.style = Style.MAPBOX_STREETS
            fragment.json = geo.features
            val fram = supportFragmentManager.beginTransaction()
            fram.replace(R.id.container, fragment)
            fram.commit()
        } catch (e: Exception) {
            Toast.makeText(this, "Error ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) !=
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Permisos denegados", Toast.LENGTH_LONG).show()
            val i = Intent()
            i.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            i.addCategory(Intent.CATEGORY_DEFAULT)
            i.data = Uri.parse("package:$packageName")
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            startActivity(i)
            finish()
        } else {
            obtenerJson()
        }
    }
}