package com.example.inventoryandroid

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

//enum class ProviderType { BASIC }

class MainActivity : AppCompatActivity() {
    private lateinit var btnAdd: Button
    private lateinit var btn_search: Button
    private lateinit var btn_report: Button
    private lateinit var btn_act_bd: Button
    private lateinit var btn_inventory: Button
    private lateinit var btn_perfil: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Configuración del padding para Edge to Edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Vincula los botones desde el diseño
        btnAdd = findViewById(R.id.btn_add)
        btn_search = findViewById(R.id.btn_search)
        btn_report = findViewById(R.id.btn_report)
        btn_act_bd = findViewById(R.id.btn_act_bd)
        btn_inventory = findViewById(R.id.btn_inventory)
        btn_perfil = findViewById(R.id.btn_extra)

        // Configura las acciones para los botones
        btnAdd.setOnClickListener {
            val intent = Intent(this, agregarProductoActivity::class.java)
            startActivity(intent)
        }

        btn_search.setOnClickListener {
            val intent = Intent(this, BuscarActivity::class.java)
            startActivity(intent)
        }

        btn_report.setOnClickListener {
            val intent = Intent(this, MainReporteActivity::class.java)
            startActivity(intent)
        }

        btn_act_bd.setOnClickListener {
            val intent = Intent(this, Act_BD_Activity::class.java)
            startActivity(intent)
        }

        btn_inventory.setOnClickListener {
            val intent = Intent(this, InventarioActivity::class.java)
            startActivity(intent)
        }

        btn_perfil.setOnClickListener {
            val intent = Intent(this, PerfilActivity::class.java)
            startActivity(intent)
        }
    }
}
