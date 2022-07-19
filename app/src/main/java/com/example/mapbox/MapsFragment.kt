package com.example.mapbox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import com.example.mapbox.databinding.FragmentMapsBinding
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location

class MapsFragment : Fragment() {

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!
    private lateinit var mapView: MapView
    private var clicked = false
    private val rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_open_anim) }
    private val rotateClose: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_close_anim) }
    private val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.from_bottom_anim) }
    private val toBottom: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.to_bottom_anim) }


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
        mapView.getMapboxMap().loadStyleUri(
            Style.MAPBOX_STREETS
        ) {
            mapView.location.updateSettings {
                enabled = true
                pulsingEnabled = true
            }
        }
        val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
            if (con == 0) {
                mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(it).build())
                mapView.gestures.focalPoint = mapView.getMapboxMap().pixelForCoordinate(it)
                con++
            }
        }
        mapView.location.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)

        binding.fab1.setOnClickListener {
            clicked()
        }
    }

    private fun clicked() {
        setVisivility(clicked)
        setAnimation(clicked)
        clicked = !clicked
    }

    private fun setVisivility(click: Boolean) {
        if (!click){
            binding.fab2.visibility = View.VISIBLE
            binding.fab3.visibility = View.VISIBLE
        }else{
            binding.fab2.visibility = View.INVISIBLE
            binding.fab3.visibility = View.INVISIBLE
        }
    }

    private fun setAnimation(click: Boolean) {
        if (!click){
            binding.fab1.startAnimation(rotateOpen)
            binding.fab2.startAnimation(fromBottom)
            binding.fab3.startAnimation(fromBottom)
        }else{
            binding.fab1.startAnimation(rotateClose)
            binding.fab2.startAnimation(toBottom)
            binding.fab3.startAnimation(toBottom)
        }
    }


}