package com.example.inventoryandroid

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ReporteActivity : AppCompatActivity() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var listaProductos: MutableList<Producto>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_reporte)

        firestore = FirebaseFirestore.getInstance()

        // Inicializar SearchView
        val searchView = findViewById<SearchView>(R.id.search_view_reporte)
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

        val btnAccept = findViewById<Button>(R.id.btn_accept)
        val etStockQuantity = findViewById<EditText>(R.id.et_stock_quantity)

        // Configurar el evento click del botón btn_accept
        btnAccept.setOnClickListener {
            // Obtener el valor ingresado por el usuario para el stock
            val stockValue = etStockQuantity.text.toString().toIntOrNull()

            if (stockValue != null) {
                // Filtrar los productos seleccionados según el stock
                val productosSeleccionados =
                    (findViewById<RecyclerView>(R.id.rv_product_list).adapter as ProductoAdapter)
                        .obtenerProductosSeleccionados()

                if (productosSeleccionados.isNotEmpty()) {
                    // Obtener el UID del usuario que está actualmente autenticado
                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

                    val db = FirebaseFirestore.getInstance()
                    db.collection("users")
                        .whereEqualTo("id", userId)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            if (querySnapshot.documents.isNotEmpty()) {
                                val usuarioDocument = querySnapshot.documents[0]
                                val nombreUsuario = usuarioDocument.getString("nombre") ?: "Usuario desconocido"

                                // Obtener la fecha y hora actuales
                                val fechaHoraActual = Calendar.getInstance()
                                val formatoFecha =
                                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // Formato de fecha
                                val formatoHora =
                                    SimpleDateFormat("HH:mm", Locale.getDefault()) // Formato de hora

                                val fecha = formatoFecha.format(fechaHoraActual.time)
                                val hora = formatoHora.format(fechaHoraActual.time)

                                // Crear el reporte para cada producto seleccionado
                                productosSeleccionados.forEach { producto ->
                                    producto.stock = stockValue

                                    val productoCodigoConcatenado = "${producto.departamento} ${producto.nombre}  ${producto.cantidad}"

                                    val reporte = Reporte(
                                        ProductoCodigo = productoCodigoConcatenado,
                                        idUsuario = nombreUsuario, // Usar el nombre del usuario
                                        fecha = fecha,             // Fecha actual
                                        hora = hora,               // Hora actual
                                        stock = stockValue,
                                        resuelto = false,
                                        razon = ""
                                    )

                                    // Registrar el reporte en Firestore
                                    registrarReporte(reporte)

                                    //actualizarProductoEnFirestore(producto)
                                }
                                etStockQuantity.text.clear()
                                Toast.makeText(this, "Reporte generado con éxito", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(
                                    this,
                                    "No se encontró el usuario en la base de datos",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "Error al obtener el nombre del usuario: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    Toast.makeText(
                        this,
                        "No se encontraron productos con stock menor o igual a $stockValue",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(this, "Por favor ingresa un valor de stock válido", Toast.LENGTH_SHORT).show()
            }
        }


        // Cargar productos desde Firestore
        cargarProductos()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun cargarProductos() {
        firestore.collection("productos")
            .get()
            .addOnSuccessListener { result ->
                listaProductos = mutableListOf()
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

    private fun filtrarProductos(query: String) {
        val productosFiltrados = listaProductos.filter { producto ->
            producto.nombre.contains(query, ignoreCase = true) ||
                    producto.codigo.contains(query, ignoreCase = true) ||
                    producto.departamento.contains(query, ignoreCase = true)
        }
        actualizarRecyclerView(productosFiltrados)
    }

    fun registrarReporte(reporte: Reporte) {
        // Crear un mapa con los datos del reporte
        val reporteData = hashMapOf(
            "ProductoCodigo" to reporte.ProductoCodigo,
            "idUsuario" to reporte.idUsuario,
            "fecha" to reporte.fecha,
            "hora" to reporte.hora,
            "stock" to reporte.stock,
            "resuelto" to reporte.resuelto,
            "razon" to reporte.razon
        )

        // Referencia a la colección 'reportes' en Firestore
        val db = FirebaseFirestore.getInstance()
        db.collection("reportes")
            .add(reporteData)
            .addOnSuccessListener { documentReference ->
                // Log exitoso con el ID del documento generado
                Log.d("RegistrarReporte", "Reporte registrado con ID: ${documentReference.id}")
                Toast.makeText(this, "Reporte registrado exitosamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                // Log de error detallado
                Log.w("RegistrarReporte", "Error al registrar el reporte: ${e.message}")
                Toast.makeText(this, "Error al registrar el reporte: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun actualizarRecyclerView(lista: List<Producto>) {
        val recyclerView = findViewById<RecyclerView>(R.id.rv_product_list)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ProductoAdapter(lista)
    }
}
