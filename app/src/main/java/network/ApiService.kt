package com.example.mediconnect_ai.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    // Defines a POST request to the "/check_symptom" endpoint
    @POST("check_symptom")
    fun checkSymptom(@Body request: SymptomRequest): Call<SymptomResponse>
}