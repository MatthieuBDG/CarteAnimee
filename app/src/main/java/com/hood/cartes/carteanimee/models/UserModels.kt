package com.hood.cartes.carteanimee.models

data class UserResponse(
    val success: Boolean,
    val user: User?,
    val error_msg: String?
)
data class User(
    val ID_User: String,
    val Prenom: String,
    val Nom: String,
    val Email: String,
    val ID_Role: String,
    val Role: String
)
