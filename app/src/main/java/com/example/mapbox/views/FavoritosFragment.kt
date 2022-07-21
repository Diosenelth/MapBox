package com.example.mapbox.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.mapbox.FavoritosAdapter
import com.example.mapbox.controller.FavoritosSqlite
import com.example.mapbox.databinding.FragmentFavoritosBinding
import com.example.mapbox.updateView
import com.mapbox.maps.MapView
import com.mapbox.maps.Style




class FavoritosFragment : Fragment(), updateView {
    private var _binding: FragmentFavoritosBinding? = null
    private val binding get() = _binding!!
    val bd = FavoritosSqlite(requireContext())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentFavoritosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mostrar()
    }

    private fun mostrar(){
        binding.rv.setHasFixedSize(true)
        val list = bd.getList()
        val adapter = FavoritosAdapter(list, this)
        binding.rv.layoutManager= androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        val dividerItemDecoration = androidx.recyclerview.widget.DividerItemDecoration(
            context, androidx.recyclerview.widget.LinearLayoutManager(this.context).orientation
        )
        binding.rv.addItemDecoration(dividerItemDecoration)
        binding.rv.adapter=adapter
    }

    override fun actualizarVista(string: String) {
        val res = bd.delFav(string)
        if (res){
            Toast.makeText(requireContext(), "Contacto eliminado", Toast.LENGTH_SHORT).show()
            mostrar()
        }else{
            Toast.makeText(requireContext(), "Error al eliminar", Toast.LENGTH_SHORT).show()
        }
    }

}