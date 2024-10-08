package com.hood.cartes.carteanimee.models

data class SeriesResponse(
    val success: Boolean,
    val series_count:Int,
    val series: List<Series>?, // Modifier pour une liste de Serie
    val error_msg: String?
)

data class Series(
    val ID_Serie: String,
    val Nom: String,
    val Pourcentage:Int,
    val Derniere_Animation:Int

)
