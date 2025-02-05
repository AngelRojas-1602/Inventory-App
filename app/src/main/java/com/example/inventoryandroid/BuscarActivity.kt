package com.example.inventoryandroid

import android.content.Intent
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue


class BuscarActivity : AppCompatActivity() {

    private lateinit var spinnerCategoria: Spinner

    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_buscar)

        firestore = FirebaseFirestore.getInstance()

        // Cargar las opciones del spinner desde el archivo strings.xml
        spinnerCategoria = findViewById<Spinner>(R.id.spinner_filter)

        // Configuración dinámica del Spinner de categorías desde Firestore
        cargarCategoriasEnSpinner()

        val spinnerOptions = resources.getStringArray(R.array.spinner_options)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategoria.adapter = adapter

        // Llamada a la función para cargar los productos de Firebase
        // Obtener el UID del usuario actual
        val currentUID = FirebaseAuth.getInstance().currentUser?.uid

        val btn_actualizar = findViewById<Button>(R.id.actualizar_Lista)

        if (currentUID != null) {
            // Verificar si el usuario es admin
            cargarProductos(currentUID)
            val btnNext = findViewById<Button>(R.id.btn_next)
            btnNext.setOnClickListener {
                val textoBusqueda = findViewById<EditText>(R.id.et_search_product).text.toString()

                buscarProductosPorNombre(currentUID, textoBusqueda)
            }

            val btn_buscar_categoria = findViewById<Button>(R.id.btn_buscar_categoria)

            btn_buscar_categoria.setOnClickListener {
                val filtro = findViewById<Spinner>(R.id.spinner_filter).selectedItem.toString()
                buscarProductosPorCategoria(currentUID, filtro)
            }

            btn_actualizar.setOnClickListener {

            }
        } else {
            // Si el usuario no está autenticado, mostrar un mensaje de error o redirigir al login
            Toast.makeText(this, "No se pudo obtener el usuario", Toast.LENGTH_SHORT).show()
        }

        // Manejar los márgenes de la pantalla
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btn_buscar_checkin = findViewById<Button>(R.id.btn_buscar_checkin)


        btn_buscar_checkin.setOnClickListener {
            // Obtener los productos seleccionados del RecyclerView
            val productosSeleccionados =
                (findViewById<RecyclerView>(R.id.rv_product_list).adapter as ProductoAdapter).obtenerProductosSeleccionados()

            if (productosSeleccionados.isNotEmpty()) {
                // Obtener el UID del usuario autenticado
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

                // Obtener el nombre del usuario desde Firestore
                val db = FirebaseFirestore.getInstance()

                // Empezar transacción para asegurar la consistencia de los datos
                db.collection("users")
                    .whereEqualTo("id", userId) // Busca por el UID del usuario
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        if (!querySnapshot.isEmpty) {
                            // Si el usuario existe, podemos continuar
                            val userName = querySnapshot.documents.first().getString("nombre") ?: "Desconocido"  // Obtener nombre del usuario

                            // Aquí estamos accediendo a la colección de productos
                            val productosRef = db.collection("productos")
                            val verificadosRef = db.collection("verificados")  // Colección donde se guardarán los movimientos

                            // Iterar sobre los productos seleccionados y actualizarlos
                            for (producto in productosSeleccionados) {
                                val productoCodigo = producto.codigo
                                val productoNombre = producto.nombre
                                val productoStock = producto.stock  // Suponiendo que cada producto tiene el stock

                                // Empezar una transacción para actualizar los productos y guardar la verificación
                                db.runTransaction { transaction ->
                                    val productoDocRef = productosRef.document(productoCodigo)

                                    // Actualizar el campo 'checkin' a true (el valor en la base de datos será modificado a 'true')
                                    transaction.update(productoDocRef, "checkin", true)

                                    // Crear el objeto que se va a almacenar en 'verificados'
                                    val movimiento = hashMapOf(
                                        "userId" to userId,
                                        "userName" to userName,
                                        "productoNombre" to productoNombre,
                                        "productoCodigo" to productoCodigo,
                                        "stock" to productoStock,
                                        "fechaHora" to FieldValue.serverTimestamp()  // Fecha y hora actual
                                    )

                                    // Guardar el movimiento en la colección 'verificados'
                                    val movimientoRef = verificadosRef.document()
                                    transaction.set(movimientoRef, movimiento)

                                }.addOnSuccessListener {
                                    // Mostrar un Toast cuando el producto se actualiza correctamente
                                    Toast.makeText(applicationContext, "Producto ${productoCodigo} actualizado correctamente", Toast.LENGTH_SHORT).show()
                                }.addOnFailureListener { e ->
                                    // Mostrar un Toast si ocurre un error al actualizar
                                    Toast.makeText(applicationContext, "Error al actualizar el producto: ${productoCodigo}", Toast.LENGTH_SHORT).show()
                                }
                            }

                            // Mensaje de éxito para el usuario
                            Toast.makeText(applicationContext, "Productos actualizados correctamente", Toast.LENGTH_SHORT).show()

                        } else {
                            // Manejar el caso donde no se encuentra el usuario
                            Toast.makeText(applicationContext, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        // Manejar cualquier error al buscar el usuario
                        Toast.makeText(applicationContext, "Error al obtener los datos del usuario", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // Manejar caso cuando no se han seleccionado productos
                Toast.makeText(applicationContext, "No se han seleccionado productos", Toast.LENGTH_SHORT).show()
            }

        }



        val btn_buscar_reportar = findViewById<Button>(R.id.btn_buscar_reportar)
        btn_buscar_reportar.setOnClickListener {
            val intent = Intent(this, ReporteActivity::class.java)
            startActivity(intent)
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

    // Función para obtener los productos de Firebase
    private fun cargarProductos(uid: String) {
        val userDb = FirebaseFirestore.getInstance().collection("users")
        val productDb = FirebaseFirestore.getInstance().collection("productos")

        // Consultar los datos del usuario por su UID
        userDb.whereEqualTo("id", uid)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val userDocument = documents.documents[0] // Documento del usuario
                    val isAdmin = userDocument.getBoolean("admin") ?: false
                    val userTienda = userDocument.getString("tienda") ?: ""

                    // Construir la consulta de productos
                    val query = if (isAdmin) {
                        productDb.whereEqualTo("checkin", false) // Solo productos con checkin = false
                    } else {
                        productDb.whereEqualTo("tienda", userTienda) // Filtra por tienda del usuario
                            .whereEqualTo("checkin", false) // Solo productos con checkin = false
                    }

                    // Obtener los productos filtrados
                    query.get()
                        .addOnSuccessListener { result ->
                            val listaProductos = result.map { document ->
                                Producto(
                                    document.getString("nombre") ?: "",
                                    document.getString("codigo") ?: "",
                                    document.getString("cantidad") ?: "",
                                    document.getLong("stock")?.toInt() ?: 0,
                                    document.getString("categoria") ?: "",
                                    document.getString("tienda") ?: "",
                                    document.getBoolean("checkin") ?: false // Asegúrate de que el campo 'checkin' esté correctamente mapeado
                                )
                            }

                            // Configurar el RecyclerView
                            val recyclerView = findViewById<RecyclerView>(R.id.rv_product_list)
                            recyclerView.layoutManager = LinearLayoutManager(this)
                            recyclerView.adapter = ProductoAdapter(listaProductos)
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firebase", "Error obteniendo productos: ", e)
                            Toast.makeText(this, "Error al cargar los productos", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Usuario no encontrado en la base de datos", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error obteniendo datos del usuario: ", e)
                Toast.makeText(this, "Error al verificar el usuario", Toast.LENGTH_SHORT).show()
            }
    }


    private fun buscarProductosPorNombre(uid: String, nombreBusqueda: String) {
        val userDb = FirebaseFirestore.getInstance().collection("users")
        val productDb = FirebaseFirestore.getInstance().collection("productos")

        userDb.whereEqualTo("id", uid)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val userDocument = documents.documents[0]
                    val isAdmin = userDocument.getBoolean("admin") ?: false
                    val userTienda = userDocument.getString("tienda") ?: ""

                    // Construir la consulta de productos
                    val query = if (isAdmin) {
                        productDb.whereEqualTo("checkin", false) // Solo productos con checkin = false
                            .whereGreaterThanOrEqualTo("nombre", nombreBusqueda)
                            .whereLessThanOrEqualTo("nombre", nombreBusqueda + "\uf8ff")
                    } else {
                        productDb.whereEqualTo("tienda", userTienda) // Filtra por tienda del usuario
                            .whereEqualTo("checkin", false) // Solo productos con checkin = false
                            .whereGreaterThanOrEqualTo("nombre", nombreBusqueda)
                            .whereLessThanOrEqualTo("nombre", nombreBusqueda + "\uf8ff")
                    }

                    query.get()
                        .addOnSuccessListener { result ->
                            val listaProductos = result.map { document ->
                                Producto(
                                    document.getString("nombre") ?: "",
                                    document.getString("codigo") ?: "",
                                    document.getString("cantidad") ?: "",
                                    document.getLong("stock")?.toInt() ?: 0,
                                    document.getString("categoria") ?: "",
                                    document.getString("tienda") ?: "",
                                    false
                                )
                            }

                            val recyclerView = findViewById<RecyclerView>(R.id.rv_product_list)
                            recyclerView.layoutManager = LinearLayoutManager(this)
                            recyclerView.adapter = ProductoAdapter(listaProductos)
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firebase", "Error buscando productos por nombre: ", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error obteniendo datos del usuario: ", e)
            }
    }

    private fun buscarProductosPorCategoria(uid: String, categoriaBusqueda: String) {
        val userDb = FirebaseFirestore.getInstance().collection("users")
        val productDb = FirebaseFirestore.getInstance().collection("productos")

        userDb.whereEqualTo("id", uid)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val userDocument = documents.documents[0]
                    val isAdmin = userDocument.getBoolean("admin") ?: false
                    val userTienda = userDocument.getString("tienda") ?: ""

                    val query = if (isAdmin) {
                        productDb.whereEqualTo("categoria", categoriaBusqueda) // Filtra por categoría
                            .whereEqualTo("checkin", false) // Solo productos con checkin = false
                    } else {
                        productDb.whereEqualTo("tienda", userTienda) // Filtra por tienda del usuario
                            .whereEqualTo("categoria", categoriaBusqueda) // Filtra por categoría
                            .whereEqualTo("checkin", false) // Solo productos con checkin = false
                    }


                    query.get()
                        .addOnSuccessListener { result ->
                            val listaProductos = result.map { document ->
                                Producto(
                                    document.getString("nombre") ?: "",
                                    document.getString("codigo") ?: "",
                                    document.getString("cantidad") ?: "",
                                    document.getLong("stock")?.toInt() ?: 0,
                                    document.getString("categoria") ?: "",
                                    document.getString("tienda") ?: "",
                                    false
                                )
                            }

                            val recyclerView = findViewById<RecyclerView>(R.id.rv_product_list)
                            recyclerView.layoutManager = LinearLayoutManager(this)
                            recyclerView.adapter = ProductoAdapter(listaProductos)
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firebase", "Error buscando productos por categoría: ", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error obteniendo datos del usuario: ", e)
            }
    }



}





