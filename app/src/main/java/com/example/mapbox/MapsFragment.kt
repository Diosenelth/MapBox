package com.example.mapbox

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
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import com.example.mapbox.databinding.FragmentMapsBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MapsFragment : Fragment() {

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
    var style: String = ""
    lateinit var json: GeoJSON
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

        mapView = binding.mapView
        bottomSheetDialog = BottomSheetDialog(requireContext())
        mapView.getMapboxMap().loadStyleUri(style) {
            CoroutineScope(Dispatchers.IO).launch {
                add()
            }
        }

        mapView.setOnClickListener {
            Toast.makeText(requireContext(), "Clicked " + it.pivotX, Toast.LENGTH_SHORT).show()
        }

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

        binding.fab1.setOnClickListener {
            clicked()
        }

        binding.position.setOnClickListener {
            con = 0
        }

        binding.sheetDialog.setOnClickListener {
            bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog)

            val streets = bottomSheetDialog.findViewById<Button>(R.id.streets)
            val outdoors = bottomSheetDialog.findViewById<Button>(R.id.outdoors)
            val satellite = bottomSheetDialog.findViewById<Button>(R.id.satellite)
            val satelliteStreets = bottomSheetDialog.findViewById<Button>(R.id.satelliteStreets)
            val light = bottomSheetDialog.findViewById<Button>(R.id.light)
            val dark = bottomSheetDialog.findViewById<Button>(R.id.dark)
            streets?.setOnClickListener {
                change(Style.MAPBOX_STREETS)
            }
            outdoors?.setOnClickListener {
                change(Style.OUTDOORS)
            }
            satellite?.setOnClickListener {
                change(Style.SATELLITE)
            }
            satelliteStreets?.setOnClickListener {
                change(Style.SATELLITE_STREETS)
            }
            light?.setOnClickListener {
                change(Style.LIGHT)
            }
            dark?.setOnClickListener {
                change(Style.DARK)
            }

            bottomSheetDialog.show()
        }
    }

    private fun add() {
        val annotationApi = mapView.annotations
        val pointAnnotationManager = annotationApi.createPointAnnotationManager()
        val icon = bitmapFromDrawableRes(requireContext(), R.drawable.red_marker)

        try {
            val list = json.features
            for (i in list) {
                val pointAnnotationOptions: PointAnnotationOptions? = icon?.let {
                    PointAnnotationOptions()
                        .withPoint(
                            Point.fromLngLat(
                                i.geometry.coordinates[0],
                                i.geometry.coordinates[1]
                            )
                        )
                        .withIconImage(it)
                    //.withTextField(i.properties.name)
                }
                if (pointAnnotationOptions != null) {
                    pointAnnotationManager.create(pointAnnotationOptions)
                }
            }

        } catch (e: Exception) {
        }
    }

    private fun clicked() {
        setVisivility(clicked)
        setAnimation(clicked)
        clicked = !clicked
    }

    private fun change(style: String) {
        bottomSheetDialog.dismiss()
        val fragment = MapsFragment()
        fragment.style = style
        fragment.json = json
        val fram = parentFragmentManager.beginTransaction()
        fram.replace(R.id.container, fragment)
        fram.commit()
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
}