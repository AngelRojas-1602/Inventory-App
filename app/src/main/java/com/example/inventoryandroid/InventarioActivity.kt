package com.example.inventoryandroid

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class InventarioActivity : AppCompatActivity() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var listaProductos: MutableList<Producto>
    private lateinit var spinnerTienda: Spinner
    private lateinit var btnFiltrar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventario)

        firestore = FirebaseFirestore.getInstance()
        listaProductos = mutableListOf()
        spinnerTienda = findViewById(R.id.spinner_filter_inventory)
        btnFiltrar = findViewById(R.id.btn_filtro)

        val tiendas = arrayOf("Todas", "Santa Rosa", "Brenamiel", "Bodega") // Reemplaza con tus opciones reales
        val recyclerView = findViewById<RecyclerView>(R.id.rv_product_list_inventory)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tiendas)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTienda.adapter = adapter

        recyclerView.layoutManager = LinearLayoutManager(this)

        // Cargar productos inicialmente (sin filtro)
        cargarProductos()

        // Configurar botón para aplicar filtro
        btnFiltrar.setOnClickListener {
            val tiendaSeleccionada = spinnerTienda.selectedItem.toString()
            if (tiendaSeleccionada == "Todas") {
                cargarProductos() // Sin filtro, carga todos los productos
            } else {
                cargarProductosPorTienda(tiendaSeleccionada)
            }
        }
    }

    private fun cargarProductos() {
        firestore.collection("productos")
            .get()
            .addOnSuccessListener { result ->
                listaProductos.clear()
                for (document in result) {
                    val nombre = document.getString("nombre") ?: ""
                    val codigo = document.getString("codigo") ?: ""
                    val cantidad = document.getString("cantidad") ?: ""
                    val stock = document.getLong("stock")?.toInt() ?: 0
                    val categoria = document.getString("categoria") ?: ""
                    val store = document.getString("tienda") ?: ""

                    listaProductos.add(Producto(
                        nombre,
                        codigo,
                        cantidad,
                        stock,
                        categoria,
                        store,
                        false
                    ))
                }
                actualizarRecyclerView(listaProductos)
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Error al cargar productos: ", exception)
            }
    }

    private fun cargarProductosPorTienda(tienda: String) {
        firestore.collection("productos")
            .whereEqualTo("tienda", tienda) // Filtrar por tienda
            .get()
            .addOnSuccessListener { result ->
                listaProductos.clear()
                for (document in result) {
                    val nombre = document.getString("nombre") ?: ""
                    val codigo = document.getString("codigo") ?: ""
                    val cantidad = document.getString("cantidad") ?: ""
                    val stock = document.getLong("stock")?.toInt() ?: 0
                    val categoria = document.getString("categoria") ?: ""
                    val store = document.getString("tienda") ?: ""

                    listaProductos.add(Producto(
                        nombre,
                        codigo,
                        cantidad,
                        stock,
                        categoria,
                        store,
                        false
                    ))
                }
                actualizarRecyclerView(listaProductos)
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Error al cargar productos filtrados: ", exception)
            }
    }

    private fun actualizarRecyclerView(lista: List<Producto>) {
        val recyclerView = findViewById<RecyclerView>(R.id.rv_product_list_inventory)
        recyclerView.adapter = ProductoAdapter(lista)
    }
}
