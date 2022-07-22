package com.example.mapbox.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.mapbox.Feature
import com.example.mapbox.`interface`.MostrarMarker
import com.example.mapbox.R
import com.example.mapbox.controller.Favorito
import com.example.mapbox.controller.FavoritosSqlite
import com.example.mapbox.databinding.FragmentMapsBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MapsFragment : Fragment(), OnMapClickListener, MostrarMarker {

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!
    private lateinit var mapView: MapView
    private var clicked = false
    private val rotateOpen: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.rotate_open_anim
        )
    }
    private val rotateClose: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.rotate_close_anim
        )
    }
    private val fromBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.from_bottom_anim
        )
    }
    private val toBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.to_bottom_anim
        )
    }
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var bottomSheetSave: BottomSheetDialog

    var style: String = ""
    lateinit var json: List<Feature>
    private lateinit var loading: SweetAlertDialog
    private lateinit var bd: FavoritosSqlite
    var con = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bd = FavoritosSqlite(requireContext())
        mapView = binding.mapView
        bottomSheetDialog = BottomSheetDialog(requireContext())
        click()

        val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
            //var po = Point.fromLngLat(it.longitude(),it.latitude())
            if (con == 0) {
                mapView.gestures.focalPoint = mapView.getMapboxMap().pixelForCoordinate(it)
                mapView.getMapboxMap().setCamera(
                    CameraOptions.Builder()
                        .center(it)
                        .zoom(15.0)
                        .build()
                )
                con++
            }
        }
        mapView.location.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
    }

    private fun click() {
        mapView.getMapboxMap().loadStyleUri(style) {
            val ask = SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
            ask.titleText = "¿Desea mostrar el nombre de los marcadores?"
//            ask.setCancelable(false)
            ask.confirmText = "Si"
            ask.cancelText = "no"
            ask.showCancelButton(true)
            ask.setCancelClickListener {
                it.cancel()
                mostrarSweet(false)
            }
            ask.setConfirmClickListener {
                it.cancel()
                mostrarSweet(true)
            }
            ask.show()
            mapView.getMapboxMap().addOnMapClickListener(this)
        }

        binding.sheetDialog.setOnClickListener {
            bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog)

            val streets = bottomSheetDialog.findViewById<Button>(R.id.streets)
            val outdoors = bottomSheetDialog.findViewById<Button>(R.id.outdoors)
            val satellite = bottomSheetDialog.findViewById<Button>(R.id.satellite)
            val satelliteStreets = bottomSheetDialog.findViewById<Button>(R.id.satelliteStreets)
            val light = bottomSheetDialog.findViewById<Button>(R.id.light)
            val dark = bottomSheetDialog.findViewById<Button>(R.id.dark)

            streets?.setOnClickListener { change(Style.MAPBOX_STREETS) }
            outdoors?.setOnClickListener { change(Style.OUTDOORS) }
            satellite?.setOnClickListener { change(Style.SATELLITE) }
            satelliteStreets?.setOnClickListener { change(Style.SATELLITE_STREETS) }
            light?.setOnClickListener { change(Style.LIGHT) }
            dark?.setOnClickListener { change(Style.DARK) }

            bottomSheetDialog.show()
        }

        binding.fab1.setOnClickListener { clicked() }
        binding.fab2.setOnClickListener {
            try {
                val fav = FavoritosFragment()
                fav.mostrar = this
                val fram = requireActivity().supportFragmentManager.beginTransaction()
                fram.add(R.id.container, fav)
                fram.hide(this)
                fram.addToBackStack("principal")
                fram.commit()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        binding.position.setOnClickListener { con = 0 }

    }

    private fun mostrarSweet(name: Boolean) {
        loading = SweetAlertDialog(requireContext(), SweetAlertDialog.PROGRESS_TYPE)
        loading.titleText = "Agregando marcadores"
        loading.setCancelable(false)
        loading.show()
        CoroutineScope(Dispatchers.IO).launch {
            add(name)
        }
    }

    private fun add(name: Boolean) {
        val annotationApi = mapView.annotations
        val pointAnnotationManager = annotationApi.createPointAnnotationManager()
        val icon = bitmapFromDrawableRes(requireContext(), R.drawable.red_marker)

        if (name) {
            for (i in json.indices) {
                val e = json[i]
                val pointAnnotationOptions: PointAnnotationOptions? = icon?.let {
                    PointAnnotationOptions()
                        .withPoint(
                            Point.fromLngLat(
                                e.geometry.coordinates[0],
                                e.geometry.coordinates[1]
                            )
                        )
                        .withIconImage(it)
                        .withTextField(e.properties.name)
                }
                pointAnnotationManager.create(pointAnnotationOptions!!)
                requireActivity().runOnUiThread {
                    loading.contentText = "Agregados ${i + 1} de ${json.size}"
                }
            }
        } else {
            for (i in json.indices) {
                val e = json[i]
                val pointAnnotationOptions: PointAnnotationOptions? = icon?.let {
                    PointAnnotationOptions()
                        .withPoint(
                            Point.fromLngLat(
                                e.geometry.coordinates[0],
                                e.geometry.coordinates[1]
                            )
                        )
                        .withIconImage(it)
                }
                pointAnnotationManager.create(pointAnnotationOptions!!)
                requireActivity().runOnUiThread {
                    loading.contentText = "Agregados ${i + 1} de ${json.size}"
                }
            }
        }

        loading.cancel()
        loading.dismiss()
    }

    private fun clicked() {
        setVisivility(clicked)
        setAnimation(clicked)
        clicked = !clicked
    }

    private fun change(style: String) {

        try {
            mapView.getMapboxMap().loadStyleUri(style)
        } catch (e: Exception) {
        }
    }

    private fun setVisivility(click: Boolean) {
        if (!click) {
            binding.sheetDialog.visibility = View.VISIBLE
            binding.fab2.visibility = View.VISIBLE
        } else {
            binding.sheetDialog.visibility = View.GONE
            binding.fab2.visibility = View.GONE
        }
    }

    private fun setAnimation(click: Boolean) {
        if (!click) {
            binding.fab1.startAnimation(rotateOpen)
            binding.sheetDialog.startAnimation(fromBottom)
            binding.fab2.startAnimation(fromBottom)
        } else {
            binding.fab1.startAnimation(rotateClose)
            binding.sheetDialog.startAnimation(toBottom)
            binding.fab2.startAnimation(toBottom)
        }
    }

    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
        convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))

    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) {
            return null
        }
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {
            val constantState = sourceDrawable.constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }

    override fun onMapClick(point: Point): Boolean {
        SweetAlertDialog(requireContext(), SweetAlertDialog.NORMAL_TYPE)
            .setTitleText("Atencion")
            .setContentText("¿Agregar este marcador?")
            .setCancelText("No!")
            .setConfirmText("Si!")
            .showCancelButton(true)
            .setCancelClickListener { obj: SweetAlertDialog -> obj.cancel() }
            .setConfirmClickListener {
                it.cancel()
                bottomSheetSave = BottomSheetDialog(requireContext())
                bottomSheetSave.setContentView(R.layout.bottom_sheet_fav)

                val name = bottomSheetSave.findViewById<EditText>(R.id.et)
                val lat = bottomSheetSave.findViewById<TextView>(R.id.lat)
                val lon = bottomSheetSave.findViewById<TextView>(R.id.lon)
                val save = bottomSheetSave.findViewById<Button>(R.id.save)
                lat?.text = "lat:\n " + point.latitude().toString()
                lon?.text = "lon:\n " + point.longitude().toString()

                save?.setOnClickListener {
                    val nam = name?.text.toString()
                    if (nam == "" || nam == " ") {
                        Toast.makeText(requireContext(), "Nombre vacio", Toast.LENGTH_SHORT).show()
                    } else {
                        val fav = Favorito(
                            null,
                            nam,
                            point.latitude().toString(),
                            point.longitude().toString()
                        )
                        val sav = bd.saveFav(fav)
                        if (sav) {
                            Toast.makeText(requireContext(), "Guardado", Toast.LENGTH_SHORT).show()
                            val annotationApi = mapView.annotations
                            val pointAnnotationManager =
                                annotationApi.createPointAnnotationManager()
                            val icon =
                                bitmapFromDrawableRes(requireContext(), R.drawable.red_marker)

                            val pointAnnotationOptions: PointAnnotationOptions? = icon?.let { bit ->
                                PointAnnotationOptions()
                                    .withPoint(point)
                                    .withIconImage(bit)
                                    .withTextField(nam)
                            }
                            if (pointAnnotationOptions != null) {
                                pointAnnotationManager.create(pointAnnotationOptions)
                            }
                        } else {
                            Toast.makeText(requireContext(), "Error al guardar", Toast.LENGTH_SHORT)
                                .show()
                        }

                        bottomSheetSave.dismiss()
                    }
                }
                bottomSheetSave.show()
            }
            .show()
        return true
    }

    override fun mostrarMarker(favorito: Favorito) {
        Toast.makeText(requireContext(), favorito.nombre, Toast.LENGTH_SHORT).show()
        val annotationApi = mapView.annotations
        val pointAnnotationManager =
            annotationApi.createPointAnnotationManager()
        val icon =
            bitmapFromDrawableRes(requireContext(), R.drawable.red_marker)
        val po = Point.fromLngLat(
            favorito.lon.toDouble(),
            favorito.lat.toDouble()
        )

        val pointAnnotationOptions: PointAnnotationOptions? = icon?.let { bit ->
            PointAnnotationOptions()
                .withPoint(po)
                .withIconImage(bit)
                .withTextField(favorito.nombre)
        }
        if (pointAnnotationOptions != null) {
            pointAnnotationManager.create(pointAnnotationOptions)
        }
        mapView.gestures.focalPoint = mapView.getMapboxMap().pixelForCoordinate(po)
        mapView.getMapboxMap().setCamera(
            CameraOptions.Builder()
                .center(po)
                .zoom(15.0)
                .build()
        )
    }
}