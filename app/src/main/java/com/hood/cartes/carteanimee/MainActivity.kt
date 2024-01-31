package com.hood.cartes.carteanimee

import android.annotation.SuppressLint
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.hood.cartes.carteanimee.models.AnimationsResponse
import com.hood.cartes.carteanimee.models.SeriesResponse
import com.hood.cartes.carteanimee.models.UserResponse
import com.hood.cartes.carteanimee.models.ViewModel
import com.hood.cartes.carteanimee.services.ApiService
import com.hood.cartes.carteanimee.ui.theme.CarteAnimeeTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {
    val viewModel = ViewModel()
    private lateinit var sessionManager: SessionManager
    private lateinit var player: ExoPlayer
    private lateinit var snackState: SnackbarHostState
    private lateinit var coroutineScope: CoroutineScope
    private lateinit var navController: NavHostController
    private var barColor: Color? = null
    private var barTitre: Color? = null
    private var Titre: Color? = null
    private val baseUrl = "https://www.demineur-ligne.com/PFE/"
    private val apiService: ApiService by lazy {
        Retrofit.Builder().baseUrl("$baseUrl/api/")
            .addConverterFactory(GsonConverterFactory.create()).build()
            .create(ApiService::class.java)
    }

    @Composable
    fun MyApp() {
        navController = rememberNavController()
        snackState = remember { SnackbarHostState() }
        coroutineScope = rememberCoroutineScope()
        player = ExoPlayer.Builder(this).build()
        sessionManager = SessionManager(this)
        barColor = Color(LocalContext.current.resources.getColor(R.color.bar))
        barTitre = Color(LocalContext.current.resources.getColor(R.color.titrebar))
        Titre = Color(LocalContext.current.resources.getColor(R.color.titre))

        NavHost(navController = navController, startDestination = "login") {
            composable("login") { LoginScreen() }
            composable("series") { SeriesScreen() }
            composable(
                route = "animations/{serieId}",
                arguments = listOf(navArgument("serieId") { type = NavType.StringType })
            ) { backStackEntry ->
                val serieId = backStackEntry.arguments?.getString("serieId")
                if (serieId != null) {
                    AnimationsScreen()
                }
            }
        }
        if (sessionManager.isLoggedIn) {
            viewModel.userId = sessionManager.userId.toString()
            viewModel.prenomUser = sessionManager.prenomUser.toString()
            viewModel.nomUser = sessionManager.nomUser.toString()
            viewModel.roleIdUser = sessionManager.roleIdUser.toString()
            viewModel.roleUser = sessionManager.roleUser.toString()

            navController.navigate("series")
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CarteAnimeeTheme {
                MyApp()
            }
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "CoroutineCreationDuringComposition")
    @Composable
    fun AnimationsScreen() {
        var currentIndex by remember { mutableIntStateOf(0) }
        val animations = viewModel.animations

        Scaffold(
            topBar = {
                // Utilisation de CenterAlignedTopAppBar au lieu de TopAppBar
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Série : ${viewModel.currentSerieName}",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = barColor!!,
                        titleContentColor = barTitre!!,
                    ),

                    navigationIcon = {
                        // Ajout d'une icône de déconnexion à droite de la barre d'application
                        IconButton(onClick = {
                            navController.navigate("series")
                            player.stop()
                            player.release()
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowBack,
                                tint = barTitre!!,
                                contentDescription = "Déconnexion"
                            )
                        }
                    },
                )
            },
            bottomBar = {
                ShowSnackBarHost(snackState)
            }
        ) {
            Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                if (animations.isNotEmpty()) {
                    val currentAnimation = animations[currentIndex]
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = currentAnimation.Nom,
                            style = MaterialTheme.typography.displaySmall.copy(Titre!!),
                            fontWeight = FontWeight.Bold

                        )
                        val imageLoader = ImageLoader.Builder(this@MainActivity).components {
                            if (SDK_INT >= 28) {
                                add(ImageDecoderDecoder.Factory())
                            } else {
                                add(GifDecoder.Factory())
                            }
                        }.build()
                        SubcomposeAsyncImage(model = "$baseUrl${currentAnimation.Chemin_Gif}",
                            imageLoader = imageLoader,
                            contentDescription = currentAnimation.Nom,
                            loading = {
                                CircularProgressIndicator()
                            },
                            modifier = Modifier
                                .fillMaxWidth() // Remplir la largeur maximale de l'écran
                                .padding(15.dp) // Ajouter une marge de 10dp de chaque côté
                                .clip(RoundedCornerShape(20.dp)) // Arrondir les coins avec un rayon de 20dp

                                .clickable {
                                    val mediaItem =
                                        MediaItem.fromUri("$baseUrl${currentAnimation.Chemin_Audio}")
                                    player.setMediaItem(mediaItem)
                                    player.prepare()
                                    player.play()
                                })
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(colors = ButtonDefaults.buttonColors(
                            barColor!!,
                            contentColor = barTitre!!
                        ), onClick = {
                            player.stop()
                            if (currentIndex < animations.size - 1) {
                                currentIndex = (currentIndex + 1) % animations.size
                            } else {
                                // Naviguer vers l'écran de série lorsque la série d'animations est terminée
                                player.release()
                                navController.navigate("series")
                            }
                        }) {
                            Text("Animation suivante")
                        }

                    }
                } else {
                    coroutineScope.launch {
                        snackState.showSnackbar(
                            "Aucune animation disponible",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun LoginScreen() {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        val keyboardController = LocalSoftwareKeyboardController.current

        Scaffold(
            topBar = {
                // Utilisation de CenterAlignedTopAppBar au lieu de TopAppBar
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Connexion à un compte utilisateur",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = barColor!!,
                        titleContentColor = barTitre!!,
                    ),
                )
            },
            bottomBar = {
                ShowSnackBarHost(snackState)
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Adresse Email") },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Mot de passe") },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        login(email, password)
                        keyboardController?.hide()
                    }),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    colors = ButtonDefaults.buttonColors(
                        barColor!!,
                        contentColor = barTitre!!
                    ),
                    onClick = { login(email, password) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Connexion")
                }

            }

        }
    }

    @Composable
    fun ShowSnackBarHost(snackState: SnackbarHostState) {
        SnackbarHost(
            hostState = snackState,
            modifier = Modifier
                .fillMaxWidth()

        ) {
            Snackbar(
                snackbarData = it,
                containerColor = barColor!!,

                )
        }
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SeriesScreen() {
        loadSeriesApi()
        val series = viewModel.series
        Scaffold(
            topBar = {
                // Utilisation de CenterAlignedTopAppBar au lieu de TopAppBar
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Séries de ${viewModel.prenomUser} ${viewModel.nomUser}",
                            maxLines = 2,
                            textAlign = TextAlign.Center,
                        )
                    },
                    actions = {
                        IconButton(onClick = {
                            navController.navigate("series")
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                tint = barTitre!!,
                                contentDescription = "Rafraichir les series"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = barColor!!,
                        titleContentColor = barTitre!!,
                    ),
                    navigationIcon = {
                        // Ajout d'une icône de déconnexion à droite de la barre d'application
                        IconButton(onClick = {
                            sessionManager.isLoggedIn = false
                            sessionManager.userId = null
                            navController.navigate("login")
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                tint = barTitre!!,
                                contentDescription = "Déconnexion"
                            )
                        }
                    },
                )
            },
            bottomBar = {
                ShowSnackBarHost(snackState)
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(80.dp))
                    Text(
                        text = "Choix de la série : ",
                        style = MaterialTheme.typography.titleLarge.copy(Titre!!),
                        modifier = Modifier.padding(bottom = 16.dp),
                        fontWeight = FontWeight.Bold
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        items(series.size) { index ->
                            val serie = series[index]
                            ElevatedCard( // Utilisation de ElevatedCard au lieu de Button
                                shape = RoundedCornerShape(10.dp),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 10.dp
                                ),

                                colors = CardDefaults.cardColors(
                                    containerColor = barColor!!,
                                ),
                                modifier = Modifier
                                    .padding(8.dp),
                                onClick = {
                                    viewModel.currentSerieName = serie.Nom
                                    recupAnimations(
                                        serie.ID_Serie
                                    )
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center

                                ) {
                                    Text(
                                        text = serie.Nom,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(20.dp),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            barTitre!!
                                        ),
                                    )

                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("CoroutineCreationDuringComposition")
    private fun loadSeriesApi() {
        coroutineScope.launch {
            snackState.showSnackbar(
                "Chargement des séries en cours...",
                duration = SnackbarDuration.Short
            )
        }
        recupSeries()
    }
    private fun login(
        email: String, password: String
    ) {
        if (email.isBlank() || password.isBlank()) {
            coroutineScope.launch {
                snackState.showSnackbar(
                    "Veuillez saisir une adresse email et un mot de passe.",
                    duration = SnackbarDuration.Short
                )
            }
            return
        }
        val call: Call<UserResponse> = apiService.connectAccount(email, password)
        call.enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    val userResponse = response.body()
                    if (userResponse?.success == true) {
                        val user = userResponse.user
                        viewModel.userId = user?.ID_User.toString()
                        viewModel.prenomUser = user?.Prenom.toString()
                        viewModel.nomUser = user?.Nom.toString()
                        viewModel.roleIdUser = user?.ID_Role.toString()
                        viewModel.roleUser = user?.Role.toString()
                        // Après une connexion réussie
                        sessionManager.isLoggedIn = true
                        sessionManager.userId = user?.ID_User.toString()
                        sessionManager.prenomUser = user?.Prenom.toString()
                        sessionManager.nomUser = user?.Nom.toString()
                        sessionManager.roleIdUser = user?.ID_Role.toString()
                        sessionManager.roleUser = user?.Role.toString()

                        navController.navigate("series")

                        coroutineScope.launch {
                            snackState.showSnackbar(
                                "Connexion réussi !",
                                duration = SnackbarDuration.Short
                            )
                        }
                    } else {
                        val errorMsg = userResponse?.error_msg ?: "Erreur inconnue"
                        // Afficher un Snackbar avec un message d'erreur
                        coroutineScope.launch {
                            snackState.showSnackbar(
                                errorMsg,
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                } else {
                    // Afficher un Snackbar avec un message d'erreur
                    coroutineScope.launch {
                        snackState.showSnackbar(
                            "Erreur de connexion. Veuillez réessayer.",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                // Afficher un Snackbar avec un message d'erreur
                coroutineScope.launch {
                    snackState.showSnackbar(
                        "Erreur de connexion. Veuillez réessayer.",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        })
    }

    private fun recupAnimations( serieId: String
    ) {
        val call: Call<AnimationsResponse> = apiService.recupAnimations(serieId)
        call.enqueue(object : Callback<AnimationsResponse> {
            override fun onResponse(
                call: Call<AnimationsResponse>, response: Response<AnimationsResponse>
            ) {
                if (response.isSuccessful) {
                    val animationsResponse = response.body()
                    if (animationsResponse?.success == true) {
                        val animations = animationsResponse.animations
                        if (!animations.isNullOrEmpty()) {

                            navController.navigate("animations/$serieId") {
                                launchSingleTop = true
                            }
                            viewModel.animations = animations
                        } else {
                            coroutineScope.launch {
                                snackState.showSnackbar(
                                    "Aucune animation trouvée pour la série.",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    } else {
                        val errorMsg = animationsResponse?.error_msg ?: "Erreur inconnue"
                        coroutineScope.launch {
                            snackState.showSnackbar(
                                errorMsg,
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                } else {
                    coroutineScope.launch {
                        snackState.showSnackbar(
                            "Erreur de récupération des animations. Veuillez réessayer.",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }

            override fun onFailure(call: Call<AnimationsResponse>, t: Throwable) {
                coroutineScope.launch {
                    snackState.showSnackbar(
                        "Erreur de récupération des animations. Veuillez réessayer.",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        })
    }


    private fun recupSeries(
    ) {
        if (viewModel.userId.isBlank()) {
            // Affichez le message d'erreur
            coroutineScope.launch {
                snackState.showSnackbar(
                    "Veuillez saisir un userId.",
                    duration = SnackbarDuration.Short
                )
            }
            return
        }
        val call: Call<SeriesResponse> = apiService.recupSeries(viewModel.userId)
        call.enqueue(object : Callback<SeriesResponse> {
            override fun onResponse(
                call: Call<SeriesResponse>, response: Response<SeriesResponse>
            ) {
                if (response.isSuccessful) {
                    val seriesResponse = response.body()
                    if (seriesResponse?.success == true) {
                        // Connexion réussie, accédez à la liste des séries dans serieResponse.series
                        val series = seriesResponse.series
                        if (!series.isNullOrEmpty()) {
                            // Mettez à jour la liste des séries dans le ViewModel
                            viewModel.series = series
                        } else {
                            // La liste des séries est vide
                            coroutineScope.launch {
                                snackState.showSnackbar(
                                    "Aucune série trouvée pour l'utilisateur.",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    } else {
                        // Gérez les erreurs de connexion (ex: mot de passe incorrect)
                        val errorMsg = seriesResponse?.error_msg ?: "Erreur inconnue"
                        // Affichez le message d'erreur
                        coroutineScope.launch {
                            snackState.showSnackbar(
                                errorMsg,
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                } else {
                    // Gestion des erreurs du réseau ou de l'API
                    coroutineScope.launch {
                        snackState.showSnackbar(
                            "Erreur de connexion. Veuillez réessayer.",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }

            override fun onFailure(call: Call<SeriesResponse>, t: Throwable) {
                // Gestion des erreurs lors de l'échec de la requête
                coroutineScope.launch {
                    snackState.showSnackbar(
                        "Erreur de connexion. Veuillez réessayer.",
                        duration = SnackbarDuration.Short
                    )
                }
            }

        })

    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        coroutineScope.launch {
            snackState.showSnackbar(
                "Impossible de faire un retour arriere",
                duration = SnackbarDuration.Short
            )
        }
    }

    override fun onStop() {
        super.onStop()
        player.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}
