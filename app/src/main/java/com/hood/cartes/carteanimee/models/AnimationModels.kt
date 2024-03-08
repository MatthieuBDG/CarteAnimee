package com.hood.cartes.carteanimee.models

data class AnimationsResponse(
    val success: Boolean,
    val animations_count:Int,
    val animations: List<Animations>?, // Modifier pour une liste de Serie
    val error_msg: String?
)

data class Animations(
    val ID_Animation: String,
    val Nom: String,
    val Chemin_Gif_Reel: String,
    val Chemin_Gif_Fictif: String,
    val Chemin_Audio: String
)
