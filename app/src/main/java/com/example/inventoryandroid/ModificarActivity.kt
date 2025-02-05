package com.example.inventoryandroid

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore

class ModificarActivity : AppCompatActivity() {
    private lateinit var etCodigoProducto: EditText
    private lateinit var etNombreProducto: EditText
    private lateinit var etCantidadProducto: EditText
    private lateinit var etStockProducto: EditText
    private lateinit var spinnerCategoria: Spinner
    private lateinit var spinnerTienda: Spinner
    private lateinit var btnModificar: Button

    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modificar)

        // Inicializar Firebase
        firestore = FirebaseFirestore.getInstance()

        // Referencias a los elementos del diseño
        etCodigoProducto = findViewById(R.id.et_codigo_producto)
        etNombreProducto = findViewById(R.id.et_nombre_producto)
        etCantidadProducto = findViewById(R.id.et_cantidad_producto)
        etStockProducto = findViewById(R.id.et_stock_producto)
        btnModificar = findViewById(R.id.btn_modificar_producto)

        // Referencia al Spinner
        spinnerCategoria = findViewById(R.id.spinner_filter)
        spinnerTienda = findViewById(R.id.spinner_filter_tienda)

        // Configuración dinámica del Spinner de categorías desde Firestore
        cargarCategoriasEnSpinner()

        // Recuperar los datos del Intent
        val codigo = intent.getStringExtra("codigo")
        val nombre = intent.getStringExtra("nombre")
        val cantidad = intent.getStringExtra("cantidad")
        val stock = intent.getIntExtra("stock", 0)
        // Configuración del Spinner (opcional si ya usaste android:entries)
        val categorias = resources.getStringArray(R.array.spinner_options)
        val tiendas = resources.getStringArray(R.array.spinner_options_tienda)

        // Configurar campos con los datos recibidos
        etCodigoProducto.setText(codigo)
        etNombreProducto.setText(nombre)
        etCantidadProducto.setText(cantidad.toString())
        etStockProducto.setText(stock.toString())

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categorias)
        val adapter1 = ArrayAdapter(this, android.R.layout.simple_spinner_item, tiendas)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategoria.adapter = adapter

        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTienda.adapter = adapter1



        // Botón para modificar producto
        btnModificar.setOnClickListener {
            actualizarProductoEnFirestore()
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
                // Ordenar las categorías alfabéticamente
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

    private fun actualizarProductoEnFirestore() {
        // Obtener los valores de los campos
        val codigo = etCodigoProducto.text.toString().trim()
        val nombre = etNombreProducto.text.toString().trim()
        val cantidad = etCantidadProducto.text.toString().trim()
        val stock = etStockProducto.text.toString().trim()
        val categoria = spinnerCategoria.selectedItem.toString()
        val tienda = spinnerTienda.selectedItem.toString()
        val checkin = false

        if (codigo.isEmpty() || nombre.isEmpty() || stock.isEmpty()) {
            Toast.makeText(this, "Por favor, Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        // Crear un objeto con los datos actualizados
        val productoActualizado = hashMapOf(
            "codigo" to codigo,
            "nombre" to nombre,
            "cantidad" to cantidad,
            "stock" to stock.toIntOrNull(),
            "categoria" to categoria,
            "tienda" to tienda,
            "checkin" to checkin
        )

        // Actualizar el producto en Firestore
        firestore.collection("productos")
            .document(codigo) // Usamos el ID del producto para referenciar el documento
            .set(productoActualizado)
            .addOnSuccessListener {
                Toast.makeText(this, "Producto actualizado correctamente", Toast.LENGTH_SHORT).show()
                Log.d("FirestoreSuccess", "Producto actualizado con ID: $codigo")
                finish() // Finalizar la actividad y volver a la pantalla anterior
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al actualizar el producto: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("FirestoreError", "Error al actualizar producto", e)
            }
    }
}
