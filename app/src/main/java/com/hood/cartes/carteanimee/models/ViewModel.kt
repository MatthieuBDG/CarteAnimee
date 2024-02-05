package com.hood.cartes.carteanimee.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
    var currentSerieName by mutableStateOf("")
    var series_count by mutableIntStateOf(0)
    /* Animations */
    var animations: List<Animations> = listOf()
    var animations_global by mutableIntStateOf(0)
    var animations_left by mutableIntStateOf(0)
}