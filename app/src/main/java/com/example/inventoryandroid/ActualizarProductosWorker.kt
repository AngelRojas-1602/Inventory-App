package com.example.inventoryandroid

import android.content.Context
import android.widget.Toast
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.google.common.util.concurrent.SettableFuture

import com.google.common.util.concurrent.ListenableFuture

class ActualizarProductosWorker(appContext: Context, workerParams: WorkerParameters) : ListenableWorker(appContext, workerParams) {

    // Instancia de Firestore
    private val db = FirebaseFirestore.getInstance()

    override fun startWork(): ListenableFuture<Result> {
        // Crear una instancia de SettableFuture
        val future = SettableFuture.create<Result>()

        // Obtener la referencia de la colección 'productos'
        val productosRef = db.collection("productos")

        // Obtener todos los productos de la colección
        productosRef.get()
            .addOnSuccessListener { querySnapshot ->
                // Iterar sobre los documentos (productos) recuperados
                for (document in querySnapshot) {
                    // Actualizar el campo 'checkin' de cada producto a true
                    document.reference.update("checkin", false)
                        .addOnSuccessListener {
                            // Log para indicar que se actualizó correctamente el producto
                            println("Producto ${document.id} actualizado correctamente")
                        }
                        .addOnFailureListener { e ->
                            // Log para indicar que hubo un error al actualizar el producto
                            println("Error al actualizar el producto ${document.id}: $e")
                        }
                }

                // Si todos los productos fueron procesados correctamente, establecer el resultado como éxito
                future.set(Result.success())
            }
            .addOnFailureListener { e ->
                // Si hubo un error al obtener los productos
                future.set(Result.failure())
                println("Error al obtener productos: $e")
            }

        return future
    }
}
