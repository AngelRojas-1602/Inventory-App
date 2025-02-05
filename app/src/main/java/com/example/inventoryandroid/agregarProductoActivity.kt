package com.example.inventoryandroid

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream

class agregarProductoActivity : AppCompatActivity() {

    private lateinit var etCodigoProducto: EditText
    private lateinit var etNombreProducto: EditText
    private lateinit var etCantidadProducto: EditText
    private lateinit var etStockProducto: EditText
    private lateinit var spinnerCategoria: Spinner
    private lateinit var spinnerTienda: Spinner
    private lateinit var btnModificarProducto: Button
    private lateinit var btnAgregar: Button

    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_producto)

        // Inicializar Firebase
        firestore = FirebaseFirestore.getInstance()

        // Referencias a los elementos del diseño
        etCodigoProducto = findViewById(R.id.et_codigo_producto)
        etNombreProducto = findViewById(R.id.et_nombre_producto)
        etCantidadProducto = findViewById(R.id.et_cantidad_producto)
        etStockProducto = findViewById(R.id.et_stock_producto)
        btnModificarProducto = findViewById(R.id.btn_modificar_producto)
        btnAgregar = findViewById(R.id.btn_cargar_json)

        // Referencia al Spinner
        spinnerCategoria = findViewById(R.id.spinner_filter)
        spinnerTienda = findViewById(R.id.spinner_filter_tienda)

        // Configuración dinámica del Spinner de categorías desde Firestore
        cargarCategoriasEnSpinner()

        // Configuración del Spinner (opcional si ya usaste android:entries)
        val categorias = resources.getStringArray(R.array.spinner_options)
        val tiendas = resources.getStringArray(R.array.spinner_options_tienda)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categorias)
        val adapter1 = ArrayAdapter(this, android.R.layout.simple_spinner_item, tiendas)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategoria.adapter = adapter

        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTienda.adapter = adapter1

        // Botón para enviar datos
        btnModificarProducto.setOnClickListener {
            agregarProducto()
        }

        btnAgregar.setOnClickListener {
            cargarProductosDesdeJSON()
        }
    }

    private fun cargarProductosDesdeJSON() {
        try {
            // Leer el archivo JSON desde la carpeta assets
            val inputStream: InputStream = assets.open("productos.json")
            val json = inputStream.bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(json)

            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)

                // Parsear los datos del JSON
                val codigo = jsonObject.getString("codigo")
                val nombre = jsonObject.getString("nombre")
                val cantidad = jsonObject.optString("cantidad", " ") // Default "0" si no está
                val stock = jsonObject.optInt("stock", 0) // Default 0 si no está
                val categoria = jsonObject.getString("categoria")
                val tienda = jsonObject.getString("tienda")
                val checkin = jsonObject.optBoolean("checkin", false)

                // Guardar en Firestore
                guardarProductoEnFirestore(codigo, nombre, cantidad, stock, categoria, tienda, checkin)
            }
            Toast.makeText(this, "Productos cargados desde JSON", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("JSONError", "Error al cargar productos desde JSON", e)
            Toast.makeText(this, "Error al cargar JSON: ${e.message}", Toast.LENGTH_SHORT).show()
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

    private fun agregarProducto() {
        // Validar campos
        val codigo = etCodigoProducto.text.toString().trim()
        val nombre = etNombreProducto.text.toString().trim()
        val cantidad = etCantidadProducto.text.toString().trim()
        val stock = etStockProducto.text.toString().trim()
        val categoria = spinnerCategoria.selectedItem.toString()
        val tienda = spinnerTienda.selectedItem.toString()
        val checkin = false;

        if (codigo.isEmpty() || nombre.isEmpty() || stock.isEmpty() ) {
            Toast.makeText(this, "Por favor, Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        // Guardar los datos en Firestore
        guardarProductoEnFirestore(codigo, nombre, cantidad, stock.toIntOrNull(), categoria, tienda, checkin)

    }

    private fun guardarProductoEnFirestore(
        codigo: String,
        nombre: String,
        cantidad: String,
        stock: Int?,
        categoria: String,
        tienda: String,
        checkin: Boolean
    ) {
        val producto = hashMapOf(
            "codigo" to codigo,
            "nombre" to nombre,
            "cantidad" to cantidad,
            "stock" to stock,
            "categoria" to categoria,
            "tienda" to tienda,
            "checkin" to checkin
        )

        // Usamos el 'codigo' como el ID del documento
        firestore.collection("productos")
            .document(codigo) // Usamos el código como ID
            .set(producto) // Guardamos el producto con ese ID
            .addOnSuccessListener {
                Toast.makeText(this, "Producto agregado correctamente", Toast.LENGTH_SHORT).show()
                Log.d("FirestoreSuccess", "Producto agregado con ID: ${codigo}")
                limpiarCampos()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar el producto: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("FirestoreError", "Error al guardar producto", e)
            }
    }

    private fun limpiarCampos() {
        etCodigoProducto.text.clear()
        etNombreProducto.text.clear()
        etCantidadProducto.text.clear()
        etStockProducto.text.clear()

    }
}


