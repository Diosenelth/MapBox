package com.example.mapbox.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.mapbox.adapter.FavoritosAdapter
import com.example.mapbox.`interface`.MostrarMarker
import com.example.mapbox.`interface`.RegresarFragment
import com.example.mapbox.controller.FavoritosSqlite
import com.example.mapbox.databinding.FragmentFavoritosBinding
import com.example.mapbox.`interface`.UpdateView


class FavoritosFragment : Fragment(), UpdateView, RegresarFragment {
    private var _binding: FragmentFavoritosBinding? = null
    private val binding get() = _binding!!
    private lateinit var bd : FavoritosSqlite
    lateinit var mostrar: MostrarMarker

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bd = FavoritosSqlite(context!!)
        mostrar()
    }

    private fun mostrar(){
        binding.rv.setHasFixedSize(true)
        val list = bd.getList()
        if (list.size== 0) {
            Toast.makeText(context, "No hay favoritos", Toast.LENGTH_SHORT).show()
        }else {
            val adapter = FavoritosAdapter(list, this, mostrar, this)
            binding.rv.layoutManager =
                androidx.recyclerview.widget.LinearLayoutManager(requireContext())
            val dividerItemDecoration = androidx.recyclerview.widget.DividerItemDecoration(
                context, androidx.recyclerview.widget.LinearLayoutManager(this.context).orientation
            )
            binding.rv.addItemDecoration(dividerItemDecoration)
            binding.rv.adapter = adapter
        }
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

    override fun regresar() {
        parentFragmentManager.popBackStack()
    }

}