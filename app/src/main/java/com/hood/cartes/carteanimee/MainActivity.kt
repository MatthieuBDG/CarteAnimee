package com.hood.cartes.carteanimee

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import com.hood.cartes.carteanimee.models.Series

import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
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
import com.hood.cartes.carteanimee.models.User
import com.hood.cartes.carteanimee.models.UserResponse
import com.hood.cartes.carteanimee.models.ViewModel
import com.hood.cartes.carteanimee.services.ApiService
import com.hood.cartes.carteanimee.ui.theme.CarteAnimeeTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class MainActivity : ComponentActivity() {
    val viewModel = ViewModel()
    private lateinit var sessionManager: SessionManager
    private var mediaPlayer: MediaPlayer? = null
    private val baseUrl = "https://www.demineur-ligne.com/PFE/"
    private val apiService: ApiService by lazy {
        Retrofit.Builder().baseUrl("$baseUrl/api/")
            .addConverterFactory(GsonConverterFactory.create()).build()
            .create(ApiService::class.java)
    }

    @Composable
    fun MyApp() {
        val navController = rememberNavController()
        sessionManager = SessionManager(this)
        NavHost(navController = navController, startDestination = "login") {
            composable("login") { LoginScreen(navController) }
            composable("series") { SeriesScreen(navController, viewModel) }
            composable(
                route = "animations/{serieId}",
                arguments = listOf(navArgument("serieId") { type = NavType.StringType })
            ) { backStackEntry ->
                val serieId = backStackEntry.arguments?.getString("serieId")
                if (serieId != null) {
                    AnimationsScreen(navController, viewModel)
                }
            }
        }
        if (sessionManager.isLoggedIn) {
            viewModel.userId = sessionManager.userId.toString()
            viewModel.prenomUser = sessionManager.prenomUser.toString()
            viewModel.nomUser = sessionManager.nomUser.toString()
            viewModel.roleIdUser = sessionManager.roleIdUser.toString()
            viewModel.roleUser = sessionManager.roleUser.toString()

            recupSeries(navController, viewModel)
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
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun AnimationsScreen(navController: NavController, viewModel: ViewModel) {
        var currentIndex by remember { mutableIntStateOf(0) }
        val animations = viewModel.animations
        Scaffold(
            topBar = {
                // Utilisation de CenterAlignedTopAppBar au lieu de TopAppBar
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Animation",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),

                    navigationIcon = {
                        // Ajout d'une icône de déconnexion à droite de la barre d'application
                        IconButton(onClick = {
                            navController.navigate("series")
                            mediaPlayer?.apply {
                                if (isPlaying) {
                                    stop()
                                    reset()
                                }
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowBack,
                                contentDescription = "Déconnexion"
                            )
                        }
                    },
                )
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp), contentAlignment = Alignment.TopEnd
            ) {
                Button(onClick = {
                    navController.navigate("series")
                    mediaPlayer?.apply {
                        if (isPlaying) {
                            stop()
                            reset()
                        }
                    }
                }) {
                    Text("Retour au series")
                }
            }
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
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 16.dp)
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
                                .height(200.dp)
                                .clickable {
                                    mediaPlayer?.apply {
                                        if (isPlaying) {
                                            stop()
                                            reset()
                                        }
                                    }
                                    mediaPlayer = MediaPlayer().apply {
                                        try {
                                            setDataSource("$baseUrl${currentAnimation.Chemin_Audio}")
                                            prepare()
                                            start()
                                        } catch (_: IOException) {
                                        }
                                    }
                                })
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            mediaPlayer?.apply {
                                if (isPlaying) {
                                    stop()
                                    reset()
                                }
                            }
                            currentIndex = (currentIndex + 1) % animations.size
                            if (currentIndex == 0) {
                                navController.popBackStack()
                            }
                        }) {
                            Text("Animation suivante")
                        }
                    }
                } else {
                    Text("Aucune animation disponible", color = Color.Red)
                }
            }
        }
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
    @Composable
    fun LoginScreen(navController: NavController) {
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
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                )
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
                        login(email, password, navController)
                        keyboardController?.hide()
                    }),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { login(email, password, navController) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Connexion")
                }
            }
        }
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SeriesScreen(navController: NavController, viewModel: ViewModel) {
        val series = viewModel.series

        Scaffold(
            topBar = {
                // Utilisation de CenterAlignedTopAppBar au lieu de TopAppBar
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Séries",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    navigationIcon = {
                        // Ajout d'une icône de déconnexion à droite de la barre d'application
                        IconButton(onClick = {
                            sessionManager.isLoggedIn = false
                            sessionManager.userId = null
                            navController.navigate("login")
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.ExitToApp,
                                contentDescription = "Déconnexion"
                            )
                        }
                    },
                )
            }
        ) {
            Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Utilisation d'un style de texte différent pour les deux textes
                    Text(
                        text = "Bienvenue ${viewModel.prenomUser} ${viewModel.nomUser}",
                        style = MaterialTheme.typography.titleLarge.copy(color = Color.Black),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        text = "Choix de la série : ",
                        style = MaterialTheme.typography.titleLarge.copy(color = Color.Gray),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp) // Ajouté
                    ) {
                        val chunkedSeries = series.chunked(3)
                        items(chunkedSeries) { rowSeries ->
                            Row(Modifier.fillMaxWidth()) {
                                rowSeries.forEach { serie ->
                                    ElevatedCard( // Utilisation de ElevatedCard au lieu de Button
                                        shape = RoundedCornerShape(8.dp),
                                        elevation = CardDefaults.cardElevation(
                                            defaultElevation = 4.dp
                                        ),
                                        modifier = Modifier.padding(8.dp),
                                        onClick = {
                                            recupAnimations(navController, viewModel, serie.ID_Serie)
                                        }
                                    ) {
                                        Text(text = serie.Nom, modifier = Modifier.padding(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    private fun login(email: String, password: String, navController: NavController) {
        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(
                this@MainActivity,
                "Veuillez saisir une adresse email et un mot de passe.",
                Toast.LENGTH_SHORT
            ).show()
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
                        recupSeries(navController, viewModel)
                        // Après une connexion réussie
                        sessionManager.isLoggedIn = true
                        sessionManager.userId = user?.ID_User.toString()
                        sessionManager.prenomUser = user?.Prenom.toString()
                        sessionManager.nomUser = user?.Nom.toString()
                        sessionManager.roleIdUser = user?.ID_Role.toString()
                        sessionManager.roleUser = user?.Role.toString()
                        navController.navigate("series")
                    } else {
                        val errorMsg = userResponse?.error_msg ?: "Erreur inconnue"
                        Toast.makeText(this@MainActivity, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Erreur de connexion. Veuillez réessayer.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Toast.makeText(
                    this@MainActivity,
                    "Erreur de connexion. Veuillez réessayer.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun recupAnimations(
        navController: NavController, viewModel: ViewModel, serieId: String
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
                            Toast.makeText(
                                this@MainActivity,
                                "Aucune animation trouvée pour la série.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        val errorMsg = animationsResponse?.error_msg ?: "Erreur inconnue"
                        Toast.makeText(this@MainActivity, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Erreur de récupération des animations. Veuillez réessayer.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<AnimationsResponse>, t: Throwable) {
                Toast.makeText(
                    this@MainActivity,
                    "Erreur de récupération des animations. Veuillez réessayer.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun recupSeries(navController: NavController, viewModel: ViewModel) {
        if (viewModel.userId.isBlank()) {
            // Affichez le message d'erreur
            Toast.makeText(this@MainActivity, "Veuillez saisir un userId.", Toast.LENGTH_SHORT)
                .show()
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
                            Toast.makeText(
                                this@MainActivity,
                                "Aucune série trouvée pour l'utilisateur.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        // Gérez les erreurs de connexion (ex: mot de passe incorrect)
                        val errorMsg = seriesResponse?.error_msg ?: "Erreur inconnue"
                        // Affichez le message d'erreur
                        Toast.makeText(this@MainActivity, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Gestion des erreurs du réseau ou de l'API
                    Toast.makeText(
                        this@MainActivity,
                        "Erreur de connexion. Veuillez réessayer.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<SeriesResponse>, t: Throwable) {
                // Gestion des erreurs lors de l'échec de la requête
                Toast.makeText(
                    this@MainActivity,
                    "Erreur de connexion. Veuillez réessayer.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
