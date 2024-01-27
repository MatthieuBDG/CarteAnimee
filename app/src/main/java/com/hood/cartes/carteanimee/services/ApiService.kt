package com.hood.cartes.carteanimee.services

import com.hood.cartes.carteanimee.models.UserResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("connectaccount")
    fun connectAccount(
        @Query("email") email: String,
        @Query("mdp") password: String
    ): Call<UserResponse>
}
