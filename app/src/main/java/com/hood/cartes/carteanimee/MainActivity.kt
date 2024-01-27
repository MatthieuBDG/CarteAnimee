package com.hood.cartes.carteanimee
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hood.cartes.carteanimee.models.SeriesResponse
import com.hood.cartes.carteanimee.models.User
import com.hood.cartes.carteanimee.services.ApiService
import com.hood.cartes.carteanimee.models.UserResponse
import com.hood.cartes.carteanimee.models.ViewModel
import com.hood.cartes.carteanimee.ui.theme.CarteAnimeeTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {
    val viewModel = ViewModel()

    private val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://www.demineur-ligne.com/PFE/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
    @Composable
    fun MyApp() {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "login") {
            composable("login") { LoginScreen(navController) }
            composable("series") { SeriesScreen(navController, viewModel) }
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

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun LoginScreen(navController: NavController) {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var loggedInUser by remember { mutableStateOf<User?>(null) }
        val keyboardController = LocalSoftwareKeyboardController.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Connexion à un compte utilisateur",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
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
                keyboardActions = KeyboardActions(
                    onDone = {
                        // Appeler la fonction de connexion lorsque l'utilisateur appuie sur "Entrée" depuis le champ de mot de passe
                        login(email, password, navController)
                        keyboardController?.hide()
                    }
                ),
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
    @Composable
    fun SeriesScreen(navController: NavController, viewModel: ViewModel) {
        val series = viewModel.series

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Choisir une série",
                style = MaterialTheme.typography.titleMedium
            )
            // Afficher les options de série
            series.forEach { serie ->
                Button(
                    onClick = {
                        // Gérer la sélection de la série
                        // Par exemple, vous pouvez naviguer vers l'écran de jeu avec l'ID de série sélectionné
                        navController.navigate("game/${serie.ID_Serie}")
                    }
                ) {
                    Text(text = serie.Nom)
                }
            }
        }
    }

    // Fonction pour gérer la connexion
// Fonction pour gérer la connexion
    private fun login(email: String, password: String, navController: NavController) {
        if (email.isBlank() || password.isBlank()) {
            // Affichez le message d'erreur
            Toast.makeText(this@MainActivity, "Veuillez saisir une adresse email et un mot de passe.", Toast.LENGTH_SHORT).show()
            return
        }
        val call: Call<UserResponse> = apiService.connectAccount(email, password)
        call.enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    val userResponse = response.body()
                    if (userResponse?.success == true) {
                        // Connexion réussie, accédez à l'objet user dans userResponse.user
                        val user = userResponse.user
                        viewModel.userId = user?.ID_User.toString()
                        viewModel.prenomUser = user?.Prenom.toString()
                        viewModel.nomUser = user?.Nom.toString()
                        viewModel.roleIdUser = user?.ID_Role.toString()
                        viewModel.roleUser = user?.Role.toString()

                        // Maintenant que l'utilisateur est connecté, récupérez les séries
                        recupSeries(navController,viewModel)
                        // Puis naviguez vers la destination "series"
                        navController.navigate("series")
                    } else {
                        // Gérez les erreurs de connexion (ex: mot de passe incorrect)
                        val errorMsg = userResponse?.error_msg ?: "Erreur inconnue"
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

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                // Gestion des erreurs lors de l'échec de la requête
                Toast.makeText(
                    this@MainActivity,
                    "Erreur de connexion. Veuillez réessayer.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun recupSeries(navController: NavController, viewModel: ViewModel) {
        if (viewModel.userId.isBlank()) {
            // Affichez le message d'erreur
            Toast.makeText(this@MainActivity, "Veuillez saisir un userId.", Toast.LENGTH_SHORT).show()
            return
        }
        val call: Call<SeriesResponse> = apiService.recupSeries(viewModel.userId)
        call.enqueue(object : Callback<SeriesResponse> {
            override fun onResponse(call: Call<SeriesResponse>, response: Response<SeriesResponse>) {
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
