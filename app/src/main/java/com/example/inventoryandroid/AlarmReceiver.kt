package com.example.inventoryandroid

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import java.util.*

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        // Disparar el trabajo de WorkManager cuando se activa la alarma
        val workRequest = OneTimeWorkRequest.Builder(ActualizarProductosWorker::class.java)
            .build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }
}

fun scheduleDailyTask(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, AlarmReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context, 0, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
    ) // FLAG_NO_CREATE devuelve null si no existe el PendingIntent

    if (pendingIntent == null) {
        // No existe un PendingIntent, así que programa el AlarmManager
        val newPendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Obtener la hora de las 3 AM
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 3)  // 3 AM
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Si la hora ya pasó hoy, programar para el día siguiente
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        // Configurar el AlarmManager para ejecutar la alarma a las 3 AM todos los días
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,  // Esto hace que se repita cada 24 horas
            newPendingIntent
        )
    }
}

