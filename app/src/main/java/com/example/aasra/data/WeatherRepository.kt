package com.example.aasra.data

import com.example.aasra.model.WeatherData
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface WeatherApiService {
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current") current: String = "temperature_2m,precipitation,wind_speed_10m"
    ): WeatherResponse
}

data class WeatherResponse(
    val current: CurrentWeather
)

data class CurrentWeather(
    val temperature_2m: Double,
    val precipitation: Double,
    val wind_speed_10m: Double
)

class WeatherRepository {

    private val api: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }

    suspend fun fetchWeather(lat: Double, lon: Double, locationName: String): WeatherData {
        val response = api.getForecast(lat, lon)
        val rainfall = response.current.precipitation
        val risk = when {
            rainfall > 50 -> "severe"
            rainfall > 20 -> "high"
            rainfall > 5 -> "moderate"
            else -> "low"
        }
        return WeatherData(
            locationName = locationName,
            temperatureC = response.current.temperature_2m,
            rainfallMm = rainfall,
            windSpeedKmh = response.current.wind_speed_10m,
            condition = if (rainfall > 5) "Rain" else "Clear",
            floodRiskLevel = risk
        )
    }
}