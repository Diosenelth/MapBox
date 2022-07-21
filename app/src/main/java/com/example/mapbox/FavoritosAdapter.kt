package com.example.mapbox

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.mapbox.controller.Favorito

class FavoritosAdapter(
    private val list: ArrayList<Favorito>,
    private val inter: updateView
) : RecyclerView.Adapter<FavoritosAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
        val delete: Button = view.findViewById(R.id.del)
        val see: Button = view.findViewById(R.id.see)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.favorito, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.name.text = item.nombre
        holder.delete.setOnClickListener { view ->
            SweetAlertDialog(view.context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Desea eliminar ${item.nombre} de favoritos")
                .setConfirmText("Si")
                .setCancelText("No")
                .setConfirmClickListener {
                    inter.actualizarVista(item.id!!)
                }
                .setCancelClickListener {
                    it.cancel()
                }
                .show()
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }


}