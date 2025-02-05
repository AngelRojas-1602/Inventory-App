package com.example.inventoryandroid

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PerfilActivity : AppCompatActivity() {

    private lateinit var etNombre: EditText
    private lateinit var etApellidos: EditText
    private lateinit var etEmail: EditText
    private lateinit var btnAccept: Button
    private lateinit var btnCancel: Button

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        // Inicializar Firestore y FirebaseAuth
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Referenciar los elementos de la interfaz
        etNombre = findViewById(R.id.et_username)
        etApellidos = findViewById(R.id.et_userlastname)
        etEmail = findViewById(R.id.et_email)
        btnAccept = findViewById(R.id.btn_accept)
        btnCancel = findViewById(R.id.btn_cancel)

        // Obtener el ID del usuario actual
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            obtenerDatosUsuario(userId)
            // Configurar botones
            btnAccept.setOnClickListener {
                actualizarDatosUsuario(userId)
                Toast.makeText(this, "Cambios aceptados", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }

        btnCancel.setOnClickListener {
            cerrarSesion()
        }
    }

    private fun obtenerDatosUsuario(userId: String) {
        firestore.collection("users")
            .whereEqualTo("id", userId) // Filtrar por el campo 'id'
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Obtener el primer documento encontrado
                    val document = querySnapshot.documents[0]
                    val nombre = document.getString("nombre") ?: "Sin nombre"
                    val apellidos = document.getString("apellidos") ?: "Sin apellidos"
                    val email = document.getString("email") ?: "Sin email"

                    // Llenar los campos con los datos obtenidos
                    etNombre.setText(nombre)
                    etApellidos.setText(apellidos)
                    etEmail.setText(email)
                } else {
                    Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Error al obtener datos del usuario", e)
                Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun actualizarDatosUsuario(userId: String) {
        // Obtener los valores de los campos de texto
        val nuevoNombre = etNombre.text.toString().trim()
        val nuevosApellidos = etApellidos.text.toString().trim()
        val nuevoEmail = etEmail.text.toString().trim()

        // Validar que los campos no estén vacíos
        if (nuevoNombre.isEmpty() || nuevosApellidos.isEmpty() || nuevoEmail.isEmpty()) {
            Toast.makeText(this, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        // Crear un mapa con los nuevos valores
        val nuevosDatos = mapOf(
            "nombre" to nuevoNombre,
            "apellidos" to nuevosApellidos,
            "email" to nuevoEmail
        )

        // Actualizar los datos en Firestore
        firestore.collection("users")
            .whereEqualTo("id", userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val documentId = querySnapshot.documents[0].id // Obtener el ID del documento
                    firestore.collection("users").document(documentId)
                        .update(nuevosDatos)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirestoreError", "Error al actualizar los datos", e)
                            Toast.makeText(this, "Error al actualizar los datos", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Error al buscar usuario para actualizar", e)
                Toast.makeText(this, "Error al buscar el usuario", Toast.LENGTH_SHORT).show()
            }
    }



    private fun cerrarSesion() {
        auth.signOut()
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
        // Redirigir al usuario a la pantalla de inicio de sesión
        val intent = Intent(this, AuthenticationActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
