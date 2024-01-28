package com.hood.cartes.carteanimee.models

data class AnimationsResponse(
    val success: Boolean,
    val animations: List<Animations>?, // Modifier pour une liste de Serie
    val error_msg: String?
)

data class Animations(
    val ID_Animation: String,
    val Nom: String,
    val Chemin_Gif: String,
    val Chemin_Audio: String
)
