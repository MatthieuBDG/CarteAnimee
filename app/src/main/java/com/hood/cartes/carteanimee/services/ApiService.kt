package com.hood.cartes.carteanimee.services

import com.hood.cartes.carteanimee.models.AdvancementSerieResponse
import com.hood.cartes.carteanimee.models.AnimationsResponse
import com.hood.cartes.carteanimee.models.SeriesResponse
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

    @GET("recupseries")
    fun recupSeries(
        @Query("id_user") userId: String,
    ): Call<SeriesResponse>

    @GET("recupanimations")
    fun recupAnimations(
        @Query("id_serie") serieId: String,
    ): Call<AnimationsResponse>

    @GET("avancementserie")
    fun envoiAdvancementSerie(
        @Query("id_serie") serieId: String,
        @Query("id_user") userId: String,
        @Query("last_animation") animationLast: Int,
    ): Call<AdvancementSerieResponse>
}
