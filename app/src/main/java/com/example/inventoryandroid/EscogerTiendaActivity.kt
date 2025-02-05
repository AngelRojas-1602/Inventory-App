package com.example.inventoryandroid

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EscogerTiendaActivity : AppCompatActivity() {
    private lateinit var rbtnSantaRosa: RadioButton
    private lateinit var rbtnBrenamiel: RadioButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.escoger_tienda)  // Asegúrate de que este layout esté correctamente definido

        // Vincular vistas
        rbtnSantaRosa = findViewById(R.id.rbtn_SantaRosa)
        rbtnBrenamiel = findViewById(R.id.rbtn_Brenamiel)

        // Obtener el UID del usuario actual
        val currentUID = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUID != null) {
            // Verificar si el usuario es admin
            checkAdminStatus(currentUID)
        } else {
            // Si el usuario no está autenticado, mostrar un mensaje de error o redirigir al login
            Toast.makeText(this, "No se pudo obtener el usuario", Toast.LENGTH_SHORT).show()
        }
    }

    // Función para verificar si el usuario es admin
    private fun checkAdminStatus(uid: String) {
        val db = FirebaseFirestore.getInstance()

        // Accedemos a la colección "users" y buscamos al usuario usando el campo "id" en lugar de "uid"
        db.collection("users").whereEqualTo("id", uid)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.documents[0] // Tomamos el primer documento (solo debe haber uno)
                    val admin = document.getBoolean("admin") ?: false
                    if (admin) {
                        // Actualizar el campo "tienda" con el valor "Administrador"
                        db.collection("users")
                            .document(document.id)
                            .update("tienda", "Administrador")
                            .addOnSuccessListener {
                                // Redirigir a la actividad principal
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error al guardar el rol: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        // Si no es admin, permitir selección de tienda
                        setupTiendaSelection()
                    }
                } else {
                    // Si no se encuentra el usuario en la base de datos
                    Toast.makeText(this, "Usuario no encontrado en la base de datos", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                // Error al obtener datos de Firestore
                Toast.makeText(this, "Error al verificar el estado de administrador", Toast.LENGTH_SHORT).show()
            }
    }

    // Configuración de la selección de tienda
    private fun setupTiendaSelection() {
        val siguienteBoton: Button = findViewById(R.id.btn_seleccionar_tienda)

        siguienteBoton.setOnClickListener {
            val tiendaSeleccionada = when {
                rbtnSantaRosa.isChecked -> "Santa Rosa"
                rbtnBrenamiel.isChecked -> "Brenamiel"
                else -> null
            }

            if (tiendaSeleccionada != null) {
                val currentUser = FirebaseAuth.getInstance().currentUser
                val db = FirebaseFirestore.getInstance()

                if (currentUser != null) {
                    // Buscar el documento por el campo "id" en la colección "users"
                    db.collection("users")
                        .whereEqualTo("id", currentUser.uid)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            if (querySnapshot.documents.isNotEmpty()) {
                                // Actualizar el campo tiendaSeleccionada en el documento correspondiente
                                val documentId = querySnapshot.documents[0].id
                                db.collection("users")
                                    .document(documentId)
                                    .update("tienda", tiendaSeleccionada)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Tienda guardada correctamente", Toast.LENGTH_SHORT).show()
                                        // Pasar a la siguiente actividad
                                        val intent = Intent(this, SimpleMortalActivity::class.java)
                                        intent.putExtra("tienda", tiendaSeleccionada)
                                        startActivity(intent)
                                        finish()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this, "Error al guardar la tienda: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                Toast.makeText(this, "No se encontró el usuario en la base de datos", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error al buscar el usuario: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Por favor selecciona una tienda.", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
