package com.example.inventoryandroid

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProductoAdapter(private val listaProductos: List<Producto>) :
    RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

    // Lista para mantener el estado de selección de cada producto (true para seleccionado)
    private val productosSeleccionados = mutableListOf<Producto>()

    // Lista para mantener el estado visual de cada ítem (true para seleccionado)
    private val seleccionados = mutableMapOf<Int, Boolean>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = listaProductos[position]

        // Verificamos si el producto está seleccionado y cambiamos el fondo del item
        val isSelected = seleccionados[position] ?: false
        holder.bind(producto, isSelected)

        // Agregar acción al hacer clic en el item
        holder.itemView.setOnClickListener {
            // Cambiar el estado de selección del producto
            if (seleccionados[position] == true) {
                seleccionados[position] = false
                productosSeleccionados.remove(producto)
            } else {
                seleccionados[position] = true
                productosSeleccionados.add(producto)
            }

            // Notificar al adaptador que se actualizó el estado de la selección
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int {
        return listaProductos.size
    }

    fun obtenerProductosSeleccionados(): List<Producto> {
        return productosSeleccionados
    }

    inner class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre: TextView = itemView.findViewById(R.id.tv_producto_nombre)
        private val tvCodigo: TextView = itemView.findViewById(R.id.tv_producto_codigo)
        private val tvCantidad: TextView = itemView.findViewById(R.id.tv_producto_cantidad)
        private val tvStock: TextView = itemView.findViewById(R.id.tv_producto_stock)

        fun bind(producto: Producto, isSelected: Boolean) {
            tvNombre.text = producto.nombre
            tvCodigo.text = producto.codigo
            tvCantidad.text = producto.cantidad
            tvStock.text = producto.stock.toString()

            // Cambiar el fondo del item si está seleccionado
            if (isSelected) {
                //itemView.setBackgroundColor(Color.rgb(199, 91, 122)) // Color de fondo para items seleccionados
                // Cambiar color del texto para el nombre
                itemView.setBackgroundResource(R.drawable.border_selected)
                tvNombre.setTextColor(Color.rgb(255, 255, 255)) // Blanco

                // Cambiar color del texto para los demás
                tvCodigo.setTextColor(Color.rgb(255, 255, 255)) // Blanco
                tvCantidad.setTextColor(Color.rgb(255, 255, 255)) // Blanco
                tvStock.setTextColor(Color.rgb(255, 255, 255)) // Blanco

            } else {
                itemView.setBackgroundResource(R.drawable.border)
                //itemView.setBackgroundColor(Color.rgb(244, 217, 208)) // Fondo predeterminado (blanco)
                tvNombre.setTextColor(Color.rgb(146, 26, 64)) // Texto negro por defecto
                tvCodigo.setTextColor(Color.rgb(199, 91, 122)) // Blanco
                tvCantidad.setTextColor(Color.rgb(146, 26, 64)) // Blanco
                tvStock.setTextColor(Color.rgb(199, 91, 122)) // Blanco

            }

        }
    }
}