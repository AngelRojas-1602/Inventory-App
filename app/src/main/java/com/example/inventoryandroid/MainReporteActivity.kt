package com.example.inventoryandroid

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainReporteActivity : AppCompatActivity() {
    private lateinit var btn_generar: Button
    private lateinit var btn_generados: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main_reporte)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btn_generar = findViewById(R.id.btn_zona_reporte1)
        btn_generados = findViewById(R.id.btn_zona_reporte2)

        btn_generar.setOnClickListener {
            val intent = Intent(this, ReporteActivity::class.java)
            startActivity(intent)
        }

        btn_generados.setOnClickListener {
            val intent = Intent(this, GeneradosActivity::class.java)
            startActivity(intent)
        }

    }
}