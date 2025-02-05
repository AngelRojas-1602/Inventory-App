package com.example.inventoryandroid
import com.google.firebase.Timestamp

// Data class Usuario
data class Users(
    val id: String,
    val nombre: String,
    val apellidos: String,
    val email: String,
    val admin: Boolean,
    val tienda: String
)

data class Verificado(
    val fechaHora: Timestamp,
    val productoCodigo: String,
    val productoNombre: String,
    val stock: Int,
    val userId: String,
    val userName: String
)

data class Producto(
    val nombre: String,
    val codigo: String,
    val cantidad: String,
    var stock: Int,
    val departamento : String,
    val tienda: String,
    var checkin: Boolean
)

// Data class Reportes
data class Reporte(
    val ProductoCodigo: String,
    val idUsuario: String,
    val fecha: String,
    val hora: String,
    val stock: Int,
    val resuelto: Boolean,
    val razon: String
)