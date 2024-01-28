package com.hood.cartes.carteanimee.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class ViewModel {
    /* User */
    var userId by mutableStateOf("")
    var prenomUser by mutableStateOf("")
    var nomUser by mutableStateOf("")
    var roleIdUser by mutableStateOf("")
    var roleUser by mutableStateOf("")
    /* Series */
    var series: List<Series> = listOf()

    /* Animations */
    var animations: List<Animations> = listOf()
}