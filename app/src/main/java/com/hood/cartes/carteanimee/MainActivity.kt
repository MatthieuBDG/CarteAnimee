package com.hood.cartes.carteanimee

import android.annotation.SuppressLint
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Refresh
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.hood.cartes.carteanimee.models.AdvancementSerieResponse
import com.hood.cartes.carteanimee.models.AnimationsResponse
import com.hood.cartes.carteanimee.models.Series
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
    private var isLoggedInBlockExecuted = false
    private lateinit var player: ExoPlayer
    private lateinit var snackState: SnackbarHostState
    private lateinit var coroutineScope: CoroutineScope
    private lateinit var navController: NavHostController
    private var barColor: Color? = null
    private var barTitre: Color? = null
    private var titre: Color? = null
    private lateinit var isLoadingSerie: MutableState<Boolean>
    private lateinit var seriesError: MutableState<String>
    private lateinit var seriesList: MutableState<List<Series>>
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
        isLoadingSerie = remember { mutableStateOf(false) }
        seriesError = remember { mutableStateOf("") }
        seriesList = remember { mutableStateOf(viewModel.series) }
        sessionManager = SessionManager(this)
        barColor = Color(ContextCompat.getColor(applicationContext, R.color.bar))
        barTitre = Color(ContextCompat.getColor(applicationContext, R.color.titrebar))
        titre = Color(ContextCompat.getColor(applicationContext, R.color.titre))

        NavHost(navController = navController, startDestination = "login") {
            composable("login") { LoginScreen() }
            composable("series") { SeriesScreen() }
            composable("userInfo") { UserInfoScreen() }
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

        if (sessionManager.isLoggedIn && !isLoggedInBlockExecuted) {
            isLoggedInBlockExecuted = true
            viewModel.userId = sessionManager.userId.toString()
            viewModel.prenomUser = sessionManager.prenomUser.toString()
            viewModel.emailUser = sessionManager.emailUser.toString()
            viewModel.nomUser = sessionManager.nomUser.toString()
            viewModel.roleIdUser = sessionManager.roleIdUser.toString()
            viewModel.roleUser = sessionManager.roleUser.toString()
            loadSeriesApiWithMessage()
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
        player = ExoPlayer.Builder(this).build()

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
                            recupSeries()
                            navController.navigate("series")
                            player.stop()
                            player.release()
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowBack,
                                tint = barTitre!!,
                                contentDescription = "Retour Series"
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
                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier.fillMaxHeight(0.90f)

                    ) {
                        if (animations.isNotEmpty()) {
                            val currentAnimation = animations[currentIndex]
                            var currentGifPath by remember { mutableStateOf(currentAnimation.Chemin_Gif_Reel) }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Spacer(modifier = Modifier.height(40.dp))
                                Text(
                                    text = currentAnimation.Nom,
                                    style = MaterialTheme.typography.displaySmall.copy(titre!!),
                                    fontWeight = FontWeight.Bold

                                )
                                val imageLoader =
                                    ImageLoader.Builder(this@MainActivity).components {
                                        if (SDK_INT >= 28) {
                                            add(ImageDecoderDecoder.Factory())
                                        } else {
                                            add(GifDecoder.Factory())
                                        }
                                    }.build()
                                SubcomposeAsyncImage(model = "$baseUrl${currentGifPath}",
                                    imageLoader = imageLoader,
                                    contentDescription = currentAnimation.Nom,
                                    loading = {
                                        CircularProgressIndicator(
                                            modifier = Modifier.fillMaxWidth(),
                                            color = titre!!,
                                            trackColor = barTitre!!,
                                        )
                                    },
                                    modifier = Modifier
                                        .padding(15.dp) // Ajouter une marge de 10dp de chaque côté
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(20.dp)) // Arrondir les coins avec un rayon de 20dp
                                        .clickable {
                                            val mediaItem =
                                                MediaItem.fromUri("$baseUrl${currentAnimation.Chemin_Audio}")
                                            player.setMediaItem(mediaItem)
                                            player.prepare()
                                            player.play()
                                        })
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Button(
                                        colors = ButtonDefaults.buttonColors(
                                            barColor!!,
                                            contentColor = barTitre!!
                                        ),
                                        onClick = {

                                            // Logique pour changer l'image GIF ici
                                            currentGifPath =
                                                if (currentGifPath == currentAnimation.Chemin_Gif_Reel) {
                                                    currentAnimation.Chemin_Gif_Fictif
                                                } else {
                                                    currentAnimation.Chemin_Gif_Reel
                                                }
                                        }
                                    ) {
                                        if (currentGifPath == currentAnimation.Chemin_Gif_Reel) {
                                            Text("Image dessin animé")
                                        } else {
                                            Text("Image réel")
                                        }
                                        Icon(
                                            imageVector = Icons.Rounded.Refresh,
                                            tint = barTitre!!,
                                            contentDescription = null
                                        )
                                    }
                                }
                                // Boutons pour l'animation suivante et précédente dans une Row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = if (currentIndex > 0) Arrangement.SpaceBetween else Arrangement.Center
                                ) {
                                    if (currentIndex > 0) {
                                        Button(
                                            colors = ButtonDefaults.buttonColors(
                                                barColor!!,
                                                contentColor = barTitre!!
                                            ),
                                            onClick = {
                                                player.stop()
                                                if (currentIndex > 0) {
                                                    avancementSerie(
                                                        viewModel.serieId,
                                                        viewModel.userId,
                                                        viewModel.animations_pass
                                                    )
                                                    currentIndex--
                                                    currentGifPath =
                                                        animations[currentIndex].Chemin_Gif_Reel
                                                    viewModel.animations_pass--

                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.KeyboardArrowLeft,
                                                tint = barTitre!!,
                                                contentDescription = "Précédente"
                                            )
                                            Text("Précédente")
                                        }
                                    }
                                    Button(
                                        colors = ButtonDefaults.buttonColors(
                                            barColor!!,
                                            contentColor = barTitre!!
                                        ),
                                        onClick = {
                                            player.stop()
                                            if (currentIndex < animations.size - 1) {
                                                currentIndex = (currentIndex + 1) % animations.size
                                                currentGifPath =
                                                    animations[currentIndex].Chemin_Gif_Reel
                                                avancementSerie(
                                                    viewModel.serieId,
                                                    viewModel.userId,
                                                    viewModel.animations_pass
                                                )
                                                viewModel.animations_pass++
                                            } else {
                                                player.release()
                                                // Naviguer vers l'écran de série lorsque la série d'animations est terminée
                                                navController.navigate("series")
                                                avancementSerie(
                                                    viewModel.serieId,
                                                    viewModel.userId,
                                                    viewModel.animations_global
                                                )
                                                recupSeries()
                                                viewModel.animations_pass = 1
                                            }
                                        }
                                    ) {
                                        if (viewModel.animations_pass != viewModel.animations_global) {
                                            Text("Suivante")
                                            Icon(
                                                imageVector = Icons.Rounded.KeyboardArrowRight,
                                                tint = barTitre!!,
                                                contentDescription = "Suivante"
                                            )
                                        } else {
                                            Text("Terminer")
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                tint = Color.Green,
                                                contentDescription = "Suivante"
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            coroutineScope.launch {
                                snackState.showSnackbar(
                                    "Aucune animation disponible",
                                    duration = SnackbarDuration.Short,
                                    withDismissAction = true
                                )
                            }
                        }
                    }
                    if (viewModel.animations_global > 1) {
                        Text(
                            text = "Animations : ${viewModel.animations_pass} sur ${viewModel.animations_global}",
                            style = MaterialTheme.typography.titleMedium.copy(titre!!),
                        )
                    } else {
                        Text(
                            text = "Animation : ${viewModel.animations_pass} sur ${viewModel.animations_global}",
                            style = MaterialTheme.typography.titleMedium.copy(titre!!),
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
        var passwordVisibility by remember { mutableStateOf(false) }
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
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = "$baseUrl/assets/img/fond/fond.jpg",
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(radiusX = 3.dp, radiusY = 3.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(3.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.9f),
                                shape = RoundedCornerShape(5.dp)
                            ),
                        horizontalAlignment = Alignment.Start
                    ) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Adresse Email") },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 7.dp, end = 7.dp)
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
                            visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 7.dp, end = 7.dp),
                            trailingIcon = {
                                IconButton(
                                    onClick = { passwordVisibility = !passwordVisibility },
                                ) {
                                    Icon(
                                        imageVector = if (passwordVisibility) Icons.Default.Info else Icons.Default.Info,
                                        contentDescription = if (passwordVisibility) "Hide Password" else "Show Password"
                                    )
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            colors = ButtonDefaults.buttonColors(
                                barColor!!,
                                contentColor = barTitre!!
                            ),
                            onClick = { login(email, password) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 7.dp, end = 7.dp, bottom = 5.dp)
                        ) {
                            Text("Connexion")
                        }
                    }
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
    fun UserInfoScreen() {
        Scaffold(
            topBar = {
                // Utilisation de CenterAlignedTopAppBar au lieu de TopAppBar
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Informations utilisateur",
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
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowBack,
                                tint = barTitre!!,
                                contentDescription = "Retour Series"
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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    ElevatedCard(
                        // Utilisation de ElevatedCard au lieu de Button
                        shape = RoundedCornerShape(10.dp),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 10.dp
                        ),

                        colors = CardDefaults.cardColors(
                            containerColor = barColor!!,
                        ),
                        modifier = Modifier
                            .padding(8.dp),
                    ) {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()

                        ) {
                            Text(
                                text = "Prénom : ${viewModel.prenomUser}",
                                style = MaterialTheme.typography.titleLarge.copy(barTitre!!),
                                modifier = Modifier.padding(10.dp),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Nom : ${viewModel.nomUser}",
                                style = MaterialTheme.typography.titleLarge.copy(barTitre!!),
                                modifier = Modifier.padding(10.dp),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Email : ${viewModel.emailUser}",
                                style = MaterialTheme.typography.titleLarge.copy(barTitre!!),
                                modifier = Modifier.padding(10.dp),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Rôle : ${viewModel.roleUser}",
                                style = MaterialTheme.typography.titleLarge.copy(barTitre!!),
                                modifier = Modifier.padding(10.dp),
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                modifier = Modifier.padding(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    Color.Red,
                                    contentColor = barTitre!!
                                ),
                                onClick = {
                                    sessionManager.isLoggedIn = false
                                    sessionManager.userId = null
                                    navController.navigate("login")
                                },
                            ) {
                                Text("Deconnexion")
                            }
                        }
                    }
                }
            }
        }
    }


    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SeriesScreen() {
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
                        IconButton(
                            onClick = {
                                navController.navigate("userInfo")
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AccountCircle,
                                tint = barTitre!!,
                                contentDescription = "Déconnexion"
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
                            loadSeriesApiWithMessage()
                        }) {

                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                tint = barTitre!!,
                                contentDescription = "Rafraîchir les series"
                            )
                        }
                    },
                )
            },
            bottomBar = {
                ShowSnackBarHost(snackState)
            }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = "$baseUrl/assets/img/fond/fond.jpg",
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(radiusX = 3.dp, radiusY = 3.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (isLoadingSerie.value && seriesError.value.isEmpty()) {
                            CircularProgressIndicator(
                                modifier = Modifier.width(64.dp),
                                color = titre!!,
                                trackColor = barTitre!!,
                            )
                        } else if (seriesError.value.isNotEmpty()) {
                            Text(
                                text = seriesError.value,
                                style = MaterialTheme.typography.titleLarge.copy(titre!!),
                                modifier = Modifier.padding(16.dp),
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            if (seriesList.value.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(80.dp))
                                Text(
                                    text = "Choix de la série : ",
                                    style = MaterialTheme.typography.titleLarge.copy(titre!!),
                                    modifier = Modifier.padding(16.dp),
                                    fontWeight = FontWeight.Bold
                                )

                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    verticalArrangement = Arrangement.spacedBy(20.dp),
                                    modifier = Modifier.fillMaxHeight(0.85f),
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                ) {
                                    items(seriesList.value.size) { index ->
                                        val serie = seriesList.value[index]

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
                                                viewModel.serieId = serie.ID_Serie
                                                avancementSerie(
                                                    viewModel.serieId,
                                                    viewModel.userId,
                                                    0
                                                )
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
                                                    if (serie.Pourcentage > 0) {
                                                        "${index + 1}. ${serie.Nom}\n${serie.Pourcentage}%"
                                                    } else {
                                                        "${index + 1}. ${serie.Nom}"
                                                    },
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.padding(15.dp),
                                                    style = MaterialTheme.typography.titleLarge.copy(
                                                        barTitre!!
                                                    )
                                                )


                                            }
                                        }
                                    }
                                }
                                Text(
                                    text = "Totales des séries : ${viewModel.series_count}",
                                    style = MaterialTheme.typography.titleMedium.copy(titre!!),
                                    modifier = Modifier.padding(20.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                }
            }
        }
    }

    @SuppressLint("CoroutineCreationDuringComposition")
    private fun loadSeriesApiWithMessage() {
        coroutineScope.launch {
            snackState.showSnackbar(
                "Chargement des séries en cours...",
                duration = SnackbarDuration.Short,
                withDismissAction = true
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
                    duration = SnackbarDuration.Short,
                    withDismissAction = true
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

                        coroutineScope.launch {
                            snackState.showSnackbar(
                                "Connexion réussi !",
                                duration = SnackbarDuration.Short,
                                withDismissAction = true
                            )
                        }
                        val user = userResponse.user

                        viewModel.userId = user?.ID_User.toString()
                        viewModel.prenomUser = user?.Prenom.toString()
                        viewModel.emailUser = user?.Email.toString()
                        viewModel.nomUser = user?.Nom.toString()
                        viewModel.roleIdUser = user?.ID_Role.toString()
                        viewModel.roleUser = user?.Role.toString()
                        // Après une connexion réussie
                        sessionManager.isLoggedIn = true
                        sessionManager.userId = user?.ID_User.toString()
                        sessionManager.prenomUser = user?.Prenom.toString()
                        sessionManager.emailUser = user?.Email.toString()
                        sessionManager.nomUser = user?.Nom.toString()
                        sessionManager.roleIdUser = user?.ID_Role.toString()
                        sessionManager.roleUser = user?.Role.toString()
                        loadSeriesApiWithMessage()
                        navController.navigate("series")
                    } else {
                        val errorMsg = userResponse?.error_msg ?: "Erreur inconnue"
                        // Afficher un Snackbar avec un message d'erreur
                        coroutineScope.launch {
                            snackState.showSnackbar(
                                errorMsg,
                                duration = SnackbarDuration.Short,
                                withDismissAction = true
                            )
                        }
                    }
                } else {
                    // Afficher un Snackbar avec un message d'erreur
                    coroutineScope.launch {
                        snackState.showSnackbar(
                            "Erreur de connexion. Veuillez réessayer.",
                            duration = SnackbarDuration.Short,
                            withDismissAction = true

                        )
                    }
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                // Afficher un Snackbar avec un message d'erreur
                coroutineScope.launch {
                    snackState.showSnackbar(
                        "Erreur de connexion. Veuillez réessayer.",
                        duration = SnackbarDuration.Short,
                        withDismissAction = true
                    )
                }
            }
        })
    }

    private fun recupAnimations(
        serieId: String
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
                            viewModel.animations_pass = 1
                            viewModel.animations_global = animationsResponse.animations_count
                        } else {
                            coroutineScope.launch {
                                snackState.showSnackbar(
                                    "Aucune animation trouvée pour la série.",
                                    duration = SnackbarDuration.Short,
                                    withDismissAction = true
                                )
                            }
                        }
                    } else {
                        val errorMsg = animationsResponse?.error_msg ?: "Erreur inconnue"
                        coroutineScope.launch {
                            snackState.showSnackbar(
                                errorMsg,
                                duration = SnackbarDuration.Short,
                                withDismissAction = true
                            )
                        }
                    }
                } else {
                    coroutineScope.launch {
                        snackState.showSnackbar(
                            "Erreur de récupération des animations. Veuillez réessayer.",
                            duration = SnackbarDuration.Short,
                            withDismissAction = true
                        )
                    }
                }
            }

            override fun onFailure(call: Call<AnimationsResponse>, t: Throwable) {
                coroutineScope.launch {
                    snackState.showSnackbar(
                        "Erreur de récupération des animations. Veuillez réessayer.",
                        duration = SnackbarDuration.Short,
                        withDismissAction = true
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
                    duration = SnackbarDuration.Short,
                    withDismissAction = true
                )
            }
            return
        }
        isLoadingSerie.value = true
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
                            seriesList.value = series
                            viewModel.series_count = seriesResponse.series_count

                            isLoadingSerie.value = false
                            seriesError.value = ""
                            println("seriesEmpty.value: ${seriesError.value}")
                            coroutineScope.launch {
                                if (seriesResponse.series_count > 1) {
                                    snackState.showSnackbar(
                                        "${seriesResponse.series_count} séries récupérées",
                                        duration = SnackbarDuration.Short,
                                        withDismissAction = true
                                    )
                                } else {
                                    snackState.showSnackbar(
                                        "${seriesResponse.series_count} série récupérée",
                                        duration = SnackbarDuration.Short,
                                        withDismissAction = true
                                    )
                                }
                            }
                        } else {
                            seriesError.value = "Aucune série trouvée pour l'utilisateur."
                            println("seriesError.value: ${seriesError.value}")
                            // La liste des séries est vide
                            coroutineScope.launch {
                                snackState.showSnackbar(
                                    "Aucune série trouvée pour l'utilisateur.",
                                    duration = SnackbarDuration.Short,
                                    withDismissAction = true
                                )
                            }
                        }
                    } else {
                        // Gérez les erreurs de connexion (ex: mot de passe incorrect)
                        val errorMsg = seriesResponse?.error_msg ?: "Erreur inconnue"
                        seriesError.value = errorMsg
                        println("seriesError.value: ${seriesError.value}")
                        // Affichez le message d'erreur
                        coroutineScope.launch {
                            snackState.showSnackbar(
                                errorMsg,
                                duration = SnackbarDuration.Short,
                                withDismissAction = true
                            )
                        }
                    }
                } else {
                    // Gestion des erreurs du réseau ou de l'API
                    coroutineScope.launch {
                        snackState.showSnackbar(
                            "Erreur de connexion. Veuillez réessayer.",
                            duration = SnackbarDuration.Short,
                            withDismissAction = true
                        )
                    }
                }
            }

            override fun onFailure(call: Call<SeriesResponse>, t: Throwable) {
                // Gestion des erreurs lors de l'échec de la requête
                coroutineScope.launch {
                    snackState.showSnackbar(
                        "Erreur de connexion. Veuillez réessayer.",
                        duration = SnackbarDuration.Short,
                        withDismissAction = true
                    )
                }
            }

        })

    }

    private fun avancementSerie(
        serieId: String, userId: String, animationLast: Int
    ) {
        if (serieId.isBlank() || userId.isBlank()) {
            coroutineScope.launch {
                snackState.showSnackbar(
                    "Veuillez saisir un serieId et userId.",
                    duration = SnackbarDuration.Short,
                    withDismissAction = true
                )
            }
            return
        }
        val call: Call<AdvancementSerieResponse> =
            apiService.envoiAdvancementSerie(serieId, userId, animationLast)
        call.enqueue(object : Callback<AdvancementSerieResponse> {
            override fun onResponse(
                call: Call<AdvancementSerieResponse>,
                response: Response<AdvancementSerieResponse>
            ) {
                if (response.isSuccessful) {
                    val advancementSerieResponse = response.body()
                    if (advancementSerieResponse?.success == false) {
                        val errorMsg = advancementSerieResponse.error_msg ?: "Erreur inconnue"
                        // Afficher un Snackbar avec un message d'erreur
                        coroutineScope.launch {
                            snackState.showSnackbar(
                                errorMsg,
                                duration = SnackbarDuration.Short,
                                withDismissAction = true
                            )
                        }
                    }
                } else {
                    // Afficher un Snackbar avec un message d'erreur
                    coroutineScope.launch {
                        snackState.showSnackbar(
                            "Erreur de connexion. Veuillez réessayer.",
                            duration = SnackbarDuration.Short,
                            withDismissAction = true

                        )
                    }
                }
            }

            override fun onFailure(call: Call<AdvancementSerieResponse>, t: Throwable) {
                // Afficher un Snackbar avec un message d'erreur
                coroutineScope.launch {
                    snackState.showSnackbar(
                        "Erreur de connexion. Veuillez réessayer.",
                        duration = SnackbarDuration.Short,
                        withDismissAction = true
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
                duration = SnackbarDuration.Short,
                withDismissAction = true
            )
        }
    }
    /*
        override fun onStop() {
            super.onStop()
            player.stop()
        }

        override fun onDestroy() {
            super.onDestroy()
            player.release()
        }
     */
}
