package com.example.sl_estacionamiento

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// Define ParkingService
interface ParkingService {
    @GET("estacionamiento") // La ruta en el servidor que devuelve la disponibilidad
    fun getParkingStatus(): Call<ParkingStatus>
}

// Define ParkingStatus data class
data class ParkingStatus(
    val disponible: Boolean
)

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var parkingService: ParkingService
    private var updateRunnable: Runnable? = null
    private var parkingAvailable: Boolean = false  // Variable para almacenar la disponibilidad

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val statusText = findViewById<TextView>(R.id.status_text)
        val statusIndicator = findViewById<View>(R.id.status_indicator)
        val proximityText = findViewById<TextView>(R.id.proximity_text)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Configurar Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("http://54.210.65.125:8081/") // Dirección del servidor
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        parkingService = retrofit.create(ParkingService::class.java)

        // Verificar permisos para ubicación
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            return
        }

        // Iniciar actualizaciones periódicas
        startPeriodicUpdates(statusText, statusIndicator, proximityText)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Detener actualizaciones periódicas al cerrar la actividad
        stopPeriodicUpdates()
    }

    private fun fetchParkingStatus(statusText: TextView, statusIndicator: View) {
        // Llamada al servidor
        parkingService.getParkingStatus().enqueue(object : Callback<ParkingStatus> {
            override fun onResponse(call: Call<ParkingStatus>, response: Response<ParkingStatus>) {
                val status = response.body()?.disponible ?: false
                parkingAvailable = status // Actualizar la variable con el estado del estacionamiento

                // Actualizar la interfaz según disponibilidad
                if (status) {
                    statusText.text = "Disponible"
                    statusIndicator.setBackgroundColor(getColor(R.color.green))
                } else {
                    statusText.text = "Ocupado"
                    statusIndicator.setBackgroundColor(getColor(R.color.red))
                }
            }

            override fun onFailure(call: Call<ParkingStatus>, t: Throwable) {
                statusText.text = "Error al obtener datos"
            }
        })
    }

    private fun checkUserProximity(proximityText: TextView) {
        // Coordenadas del estacionamiento (ejemplo)
        val parkingLatitude = -38.709691420561015 // Latitud del estacionamiento
        val parkingLongitude = -72.65971273692999 // Longitud del estacionamiento

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val userLatitude = location.latitude
                val userLongitude = location.longitude

                // Calcular distancia
                val distance = FloatArray(1)
                android.location.Location.distanceBetween(
                    parkingLatitude, parkingLongitude,
                    userLatitude, userLongitude,
                    distance
                )

                // Cambiar mensaje según la disponibilidad del estacionamiento y la proximidad
                if (parkingAvailable) {
                    if (distance[0] <= 50) { // Menos de 50 metros
                        proximityText.text = "Estás cerca de tu estacionamiento."
                    } else {
                        proximityText.text = "Estás lejos de tu estacionamiento."
                    }
                } else {
                    if (distance[0] <= 50) {
                        proximityText.text = "Bienvenido al estacionamiento."
                    } else {
                        proximityText.text = "Alguien más usó su espacio."
                    }
                }
            } else {
                proximityText.text = "No se pudo determinar la ubicación."
            }
        }
    }

    private fun startPeriodicUpdates(
        statusText: TextView,
        statusIndicator: View,
        proximityText: TextView
    ) {
        // Crear un Runnable que se ejecute periódicamente
        updateRunnable = Runnable {
            fetchParkingStatus(statusText, statusIndicator)
            checkUserProximity(proximityText)
            // Reprogramar después de 1 segundo
            handler.postDelayed(updateRunnable!!, 1000)
        }
        // Iniciar la primera ejecución
        handler.post(updateRunnable!!)
    }

    private fun stopPeriodicUpdates() {
        // Detener el Runnable
        updateRunnable?.let {
            handler.removeCallbacks(it)
        }
    }
}
