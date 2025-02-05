package com.example.inventoryandroid

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.SearchView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore

class Act_BD_Activity : AppCompatActivity() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var listaProductos: MutableList<Producto>
    private lateinit var btnDelete: Button
    private lateinit var btnUpdate: Button
    private lateinit var btnModify: Button
    private lateinit var btnActualizarEstado: Button
    private lateinit var adapter: ProductoAdapter
    private lateinit var spinnerCategoria: Spinner
    private lateinit var btnPlus: FloatingActionButton
    private lateinit var btnMinus: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_act_bd)

        firestore = FirebaseFirestore.getInstance()

        val searchView = findViewById<SearchView>(R.id.search_view_db)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    filtrarProductos(newText)
                }
                return true
            }
        })
        // Cargar las opciones del spinner desde el archivo strings.xml
        spinnerCategoria = findViewById<Spinner>(R.id.spinner_filter)

        // Configuración dinámica del Spinner de categorías desde Firestore
        cargarCategoriasEnSpinner()

        val spinnerOptions = resources.getStringArray(R.array.spinner_options)
        val adapter1 = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerOptions)
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategoria.adapter = adapter1

        btnDelete = findViewById(R.id.btn_delete)
        btnUpdate = findViewById(R.id.btn_update)
        btnModify = findViewById(R.id.btn_modificar)
        btnActualizarEstado = findViewById(R.id.btn_cambiar_estado)
        btnPlus = findViewById(R.id.floatingActionButton)
        btnMinus = findViewById(R.id.floatingActionButton2)

        val etStockUpdate = findViewById<EditText>(R.id.et_stock_quantity)

        // Configurar RecyclerView y Adapter
        val recyclerView = findViewById<RecyclerView>(R.id.rv_product_list)
        listaProductos = mutableListOf()
        adapter = ProductoAdapter(listaProductos)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnDelete.setOnClickListener {
            val productosSeleccionados = adapter.obtenerProductosSeleccionados()
            if (productosSeleccionados.isNotEmpty()) {
                productosSeleccionados.forEach { producto ->
                    eliminarProducto(producto)
                    listaProductos.remove(producto)
                }
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "Productos eliminados correctamente", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Por favor, selecciona un producto para eliminar", Toast.LENGTH_SHORT).show()
            }
        }

        btnPlus.setOnClickListener {
            val productosSeleccionados = adapter.obtenerProductosSeleccionados()
            if (productosSeleccionados.isNotEmpty()) {
                productosSeleccionados.forEach { producto ->
                    val nuevoStock = producto.stock + 1 // Incrementar el stock en 1
                    actualizarStock(producto, nuevoStock)
                }
                //Toast.makeText(this, "Stock incrementado correctamente", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Selecciona un producto para incrementar su stock", Toast.LENGTH_SHORT).show()
            }
        }

        btnMinus.setOnClickListener {
            val productosSeleccionados = adapter.obtenerProductosSeleccionados()
            if (productosSeleccionados.isNotEmpty()) {
                productosSeleccionados.forEach { producto ->
                    if (producto.stock > 0) { // Evitar valores negativos en el stock
                        val nuevoStock = producto.stock - 1 // Decrementar el stock en 1
                        actualizarStock(producto, nuevoStock)
                    } else {
                        Toast.makeText(this, "El stock no puede ser negativo", Toast.LENGTH_SHORT).show()
                    }
                }
                //Toast.makeText(this, "Stock decrementado correctamente", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Selecciona un producto para decrementar su stock", Toast.LENGTH_SHORT).show()
            }
        }


        btnUpdate.setOnClickListener {
            val productosSeleccionados = adapter.obtenerProductosSeleccionados()
            val nuevoStock = etStockUpdate.text.toString().toIntOrNull()

            if (productosSeleccionados.isNotEmpty() && nuevoStock != null) {
                productosSeleccionados.forEach { producto ->
                    actualizarStock(producto, nuevoStock)
                }
                Toast.makeText(this, "Stock actualizado correctamente", Toast.LENGTH_SHORT).show()
                etStockUpdate.text.clear()
            } else {
                Toast.makeText(this, "Selecciona un producto y asegura ingresar un stock válido", Toast.LENGTH_SHORT).show()
            }
        }


        btnModify.setOnClickListener {
            val productosSeleccionados = adapter.obtenerProductosSeleccionados()
            if (productosSeleccionados.size == 1) {
                val producto = productosSeleccionados[0] // Solo se permite un producto a modificar
                val intent = Intent(this, ModificarActivity::class.java).apply {
                    putExtra("codigo", producto.codigo)
                    putExtra("nombre", producto.nombre)
                    putExtra("cantidad", producto.cantidad)
                    putExtra("stock", producto.stock)
                    putExtra("categoria", producto.departamento)
                    putExtra("tienda", producto.tienda)
                }
                startActivity(intent)
            } else if (productosSeleccionados.isEmpty()) {
                Toast.makeText(this, "Por favor, selecciona un producto para modificar.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Solo puedes modificar un producto a la vez.", Toast.LENGTH_SHORT).show()
            }
        }
        btnActualizarEstado.setOnClickListener{
            actualizarCheckin()
        }

        val btn_buscar_categoria = findViewById<Button>(R.id.btn_buscar_categoria)
        btn_buscar_categoria.setOnClickListener {
            val filtro = findViewById<Spinner>(R.id.spinner_filter).selectedItem.toString()

            buscarProductosPorCategoria(filtro)

        }

        cargarProductos()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun buscarProductosPorCategoria(categoriaBusqueda: String) {
        val productosFiltrados = listaProductos.filter { producto ->
            producto.departamento.equals(categoriaBusqueda, ignoreCase = true)
        }
        adapter = ProductoAdapter(productosFiltrados)
        findViewById<RecyclerView>(R.id.rv_product_list).adapter = adapter
    }


    private fun actualizarStock(producto: Producto, nuevoStock: Int) {
        firestore.collection("productos")
            .whereEqualTo("codigo", producto.codigo)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val documento = querySnapshot.documents[0]
                    val nuevoEstadoCheckin = false // Cambiar el estado de checkin a false

                    firestore.collection("productos")
                        .document(documento.id)
                        .update(mapOf(
                            "stock" to nuevoStock,
                            "checkin" to nuevoEstadoCheckin
                        ))
                        .addOnSuccessListener {
                            producto.stock = nuevoStock // Actualizar en la lista local
                            producto.checkin = nuevoEstadoCheckin // Actualizar el estado localmente
                            adapter.notifyDataSetChanged()
                            Log.d("Act_BD_Activity", "Stock y checkin actualizados para el producto: ${producto.codigo}")
                        }
                        .addOnFailureListener { e ->
                            Log.e("Act_BD_Activity", "Error al actualizar el stock y checkin: ", e)
                        }
                } else {
                    Log.e("Act_BD_Activity", "No se encontró el producto con código: ${producto.codigo}")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Act_BD_Activity", "Error al buscar el producto para actualizar: ", e)
            }
    }


    private fun actualizarCheckin() {
        firestore.collection("productos")
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val batch = firestore.batch()
                    querySnapshot.documents.forEach { documento ->
                        val productoRef = firestore.collection("productos").document(documento.id)
                        batch.update(productoRef, "checkin", false) // Cambiar el estado de checkin a false
                    }

                    batch.commit()
                        .addOnSuccessListener {
                            Log.d("Act_BD_Activity", "Checkin actualizado para todos los productos")
                            adapter.notifyDataSetChanged() // Notificar cambios en la lista local si es necesario
                        }
                        .addOnFailureListener { e ->
                            Log.e("Act_BD_Activity", "Error al actualizar el checkin para todos los productos: ", e)
                        }
                } else {
                    Log.e("Act_BD_Activity", "No se encontraron productos en la base de datos")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Act_BD_Activity", "Error al buscar productos para actualizar el checkin: ", e)
            }
    }

    private fun cargarCategoriasEnSpinner() {
        firestore.collection("departamento") // Nombre de tu colección en Firestore
            .get()
            .addOnSuccessListener { result ->
                val listaCategorias = mutableListOf<String>()
                for (document in result) {
                    val categoria = document.getString("nombre") // Campo donde guardas el nombre de la categoría
                    if (categoria != null) {
                        listaCategorias.add(categoria)
                    }
                }
                // Ordenar las categorías en orden alfabético
                listaCategorias.sort()
                // Configurar el Spinner con las categorías obtenidas
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listaCategorias)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerCategoria.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Error al cargar categorías: ", exception)
                Toast.makeText(this, "Error al cargar categorías", Toast.LENGTH_SHORT).show()
            }
    }

    private fun eliminarProducto(producto: Producto) {
        firestore.collection("productos")
            .whereEqualTo("codigo", producto.codigo)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val documento = querySnapshot.documents[0]
                    firestore.collection("productos")
                        .document(documento.id)
                        .delete()
                        .addOnSuccessListener {
                            Log.d("Act_BD_Activity", "Producto eliminado exitosamente")
                        }
                        .addOnFailureListener { e ->
                            Log.e("Act_BD_Activity", "Error al eliminar el producto: ", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Act_BD_Activity", "Error al buscar el producto: ", e)
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
                // Ordenar la lista por nombre de producto
                listaProductos.sortBy { it.nombre }

                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e("Act_BD_Activity", "Error al cargar productos: ", exception)
            }
    }

    private fun filtrarProductos(query: String) {
        val productosFiltrados = listaProductos.filter { producto ->
            producto.nombre.contains(query, ignoreCase = true) ||
                    producto.codigo.contains(query, ignoreCase = true) ||
                    producto.departamento.contains(query, ignoreCase = true)
        }
        adapter = ProductoAdapter(productosFiltrados)
        findViewById<RecyclerView>(R.id.rv_product_list).adapter = adapter
    }
}
