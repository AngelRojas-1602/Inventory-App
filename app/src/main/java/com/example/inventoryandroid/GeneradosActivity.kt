package com.example.inventoryandroid

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class GeneradosActivity : AppCompatActivity() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var listaReportes: MutableList<Reporte>
    private lateinit var btn_resolver: Button
    private lateinit var adapter: ReporteAdapter
    private lateinit var razon : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_generados)

        firestore = FirebaseFirestore.getInstance()

        razon = findViewById(R.id.editTextTextMultiLine)

        // Configurar RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.rv_report_list)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Cargar los reportes de Firestore
        cargarReportes()

        // Ajustar el padding para la barra de estado
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        btn_resolver = findViewById(R.id.btn_resolver)

        btn_resolver.setOnClickListener {
            // Verificar si hay un reporte seleccionado
            val reporteSeleccionado = adapter.reporteSeleccionado
            if (reporteSeleccionado != null) {
                // Eliminar el reporte de Firestore
                resolverReporte(reporteSeleccionado)

                // Eliminar el reporte de la lista local
                adapter.eliminarReporte(reporteSeleccionado)

                // Notificar al usuario
                Toast.makeText(this, "Reporte resuelto y eliminado", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Por favor, selecciona un reporte para resolver", Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun resolverReporte(reporte: Reporte) {
        val descripcion = findViewById<EditText>(R.id.editTextTextMultiLine).text.toString() // Obtenemos el texto del EditText

        firestore.collection("reportes")
            .whereEqualTo("ProductoCodigo", reporte.ProductoCodigo) // Usa un ID único si es posible
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val documento = querySnapshot.documents[0]

                    // Actualizamos el campo "resuelto" a true y añadimos la descripción
                    firestore.collection("reportes")
                        .document(documento.id)
                        .update(
                            mapOf(
                                "resuelto" to true,
                                "descripcionSolucion" to descripcion
                            )
                        )
                        .addOnSuccessListener {
                            limpiarCampos()
                            Log.d("GeneradosActivity", "Reporte resuelto exitosamente")
                            Toast.makeText(this, "Reporte marcado como resuelto", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e("GeneradosActivity", "Error al resolver el reporte: ", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("GeneradosActivity", "Error al buscar el reporte: ", e)
            }
    }



    private fun cargarReportes() {
        firestore.collection("reportes")
            .whereEqualTo("resuelto", false) // Solo cargamos reportes con "resuelto = false"
            .get()
            .addOnSuccessListener { result ->
                listaReportes = mutableListOf()
                for (document in result) {
                    val productoCodigo = document.getString("ProductoCodigo") ?: ""
                    val idUsuario = document.get("idUsuario")?.toString() ?: ""
                    val fecha = document.getString("fecha") ?: ""
                    val hora = document.getString("hora") ?: ""
                    val stock = document.getLong("stock")?.toInt() ?: 0

                    val reporte = Reporte(productoCodigo, idUsuario, fecha, hora, stock, true, "")
                    listaReportes.add(reporte)
                }

                // Configurar el adaptador con la lista de reportes
                adapter = ReporteAdapter(listaReportes) { reporte ->
                    // Aquí puedes manejar la selección si es necesario
                }

                // Establecer el adaptador del RecyclerView
                findViewById<RecyclerView>(R.id.rv_report_list).adapter = adapter
            }
            .addOnFailureListener { exception ->
                Log.e("GeneradosActivity", "Error al cargar los reportes: ", exception)
            }
    }

    private fun limpiarCampos() {
        razon.text.clear()
    }
}
