package com.example.inventoryandroid

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

class AuthenticationActivity : AppCompatActivity() {
    private lateinit var eTemail: EditText
    private lateinit var eTpassword: EditText
    private lateinit var btn_regis: Button
    private lateinit var btn_ingre: Button
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_authentication)

        // Programar la tarea diaria
        //scheduleDailyTask(this)

        eTemail = findViewById(R.id.editTextEmailAddress)
        eTpassword = findViewById(R.id.editTextPassword)
        btn_regis = findViewById(R.id.btn_registrar)
        btn_ingre = findViewById(R.id.btn_ingresar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setup()
        setupBiometricAuthentication()
    }

    private fun setup() {
        // Ingreso de usuario
        btn_ingre.setOnClickListener {
            if (eTemail.text.isNotEmpty() && eTpassword.text.isNotEmpty()) {
                FirebaseAuth.getInstance()
                    .signInWithEmailAndPassword(eTemail.text.toString(), eTpassword.text.toString())
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            saveBiometricCredential(it.result?.user?.email ?: "")
                            goHome(it.result?.user?.email ?: "", ProviderType.BASIC)
                        } else {
                            showAlert()
                        }
                    }
            }
        }

        // Registro de usuario
        btn_regis.setOnClickListener {
            if (eTemail.text.isNotEmpty() && eTpassword.text.isNotEmpty()) {
                FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(eTemail.text.toString(), eTpassword.text.toString())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Obtener el UID y correo del nuevo usuario
                            val user = task.result?.user
                            val uid = user?.uid
                            val email = user?.email

                            // Guardar en Firestore
                            if (uid != null && email != null) {
                                saveUserToFirestore(uid, email)
                            }
                            // Redirigir al usuario a la actividad de elegir tienda
                            //goHome(email ?: "", ProviderType.BASIC)
                            Toast.makeText(this, "Usuario registrado con éxito", Toast.LENGTH_SHORT).show()
                        } else {
                            showAlert()
                        }
                    }
            }
        }
    }

    // Guardar los datos del usuario en Firestore
    private fun saveUserToFirestore(uid: String, email: String) {
        val db = FirebaseFirestore.getInstance()
        val user = hashMapOf(
            "id" to uid,
            "nombre".to (" ") ,
            "apellidos".to(" "),
            "email" to email,
            "tienda".to(" "),
            "admin" to false // El usuario se guarda como admin: false
        )

        // Guardar el usuario con el UID como ID del documento
        db.collection("users").document(uid)
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(this, "Usuario registrado con éxito", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar el usuario: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupBiometricAuthentication() {
        val biometricManager = BiometricManager.from(this)
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
            BiometricManager.BIOMETRIC_SUCCESS) {
            val executor: Executor = ContextCompat.getMainExecutor(this)

            biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    // Recuperar email almacenado y navegar al inicio
                    val sharedPref = getSharedPreferences("BiometricPrefs", MODE_PRIVATE)
                    val email = sharedPref.getString("email", null)
                    if (email != null) {
                        goHome(email, ProviderType.BASIC)
                    } else {
                        Toast.makeText(this@AuthenticationActivity, "No se encontró una cuenta asociada", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(this@AuthenticationActivity, "Error de autenticación: $errString", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(this@AuthenticationActivity, "Autenticación fallida", Toast.LENGTH_SHORT).show()
                }
            })

            promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Acceso Biométrico")
                .setSubtitle("Usa tu huella dactilar para iniciar sesión")
                .setNegativeButtonText("Cancelar")
                .build()

            // Mostrar el prompt biométrico al abrir la actividad
            biometricPrompt.authenticate(promptInfo)
        } else {
            Log.d("BiometricAuth", "La autenticación biométrica no está disponible")
        }
    }

    private fun saveBiometricCredential(email: String) {
        val sharedPref = getSharedPreferences("BiometricPrefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("email", email)
            apply()
        }
    }

    private fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error al momento de autenticar al usuario")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun goHome(email: String, provider: ProviderType) {
        val homeIntent = Intent(this, EscogerTiendaActivity::class.java).apply {
            putExtra("email", email)
            putExtra("provedoor", provider.name)
        }
        startActivity(homeIntent)
    }
}
