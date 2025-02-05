package com.example.inventoryandroid

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class ReporteAdapter(
    val reportes: MutableList<Reporte>, // Hacer la lista mutable para que podamos eliminar elementos
    val onReporteSelected: (Reporte) -> Unit // Callback para manejar la selección de un reporte
) : RecyclerView.Adapter<ReporteAdapter.ReporteViewHolder>() {

    private var reporteSeleccionadoIndex: Int = -1

    val reporteSeleccionado: Reporte?
        get() = if (reporteSeleccionadoIndex != -1) reportes[reporteSeleccionadoIndex] else null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReporteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reporte, parent, false)
        return ReporteViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReporteViewHolder, position: Int) {
        val reporte = reportes[position]
        holder.bind(reporte)

        if (position == reporteSeleccionadoIndex) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.Letras))
            holder.tvCodigo.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.white))
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.background_color))
            holder.tvCodigo.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.Botones))
        }

        holder.itemView.setOnClickListener {
            val prevSeleccionado = reporteSeleccionadoIndex
            reporteSeleccionadoIndex = position

            notifyItemChanged(prevSeleccionado)
            notifyItemChanged(reporteSeleccionadoIndex)

            onReporteSelected(reporte)
        }

    }

    override fun getItemCount(): Int = reportes.size

    // Método para eliminar un reporte de la lista local
    fun eliminarReporte(reporte: Reporte) {
        reportes.remove(reporte)
        notifyDataSetChanged()
    }

    class ReporteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCodigo: TextView = itemView.findViewById(R.id.tv_codigo_producto)
        val tvUsuario: TextView = itemView.findViewById(R.id.tv_usuario)
        val tvFecha: TextView = itemView.findViewById(R.id.tv_fecha)
        val tvHora: TextView = itemView.findViewById(R.id.tv_hora)
        val tvStock: TextView = itemView.findViewById(R.id.tv_stock)

        fun bind(reporte: Reporte) {
            tvCodigo.text = reporte.ProductoCodigo
            tvUsuario.text = reporte.idUsuario
            tvFecha.text = reporte.fecha
            tvHora.text = reporte.hora
            tvStock.text = reporte.stock.toString()
        }
    }
}