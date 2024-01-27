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
import com.hood.cartes.carteanimee.models.User
import com.hood.cartes.carteanimee.services.ApiService
import com.hood.cartes.carteanimee.models.UserResponse
import com.hood.cartes.carteanimee.models.UserViewModel
import com.hood.cartes.carteanimee.ui.theme.CarteAnimeeTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {
    val userViewModel = UserViewModel()

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
            composable("series") { SeriesScreen(navController, userViewModel) }
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
                        login(email, password, navController) { user -> loggedInUser = user }
                        keyboardController?.hide()
                    }
                ),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { login(email, password, navController) { user -> loggedInUser = user } },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Connexion")
            }

            loggedInUser?.let { user ->
                Text("Salut, ${user.Prenom} ${user.Nom} ${user.ID_Role} ${user.Role} !")
                // Afficher d'autres informations de l'utilisateur si nécessaire
            }
        }
    }
    @Composable
    fun SeriesScreen(navController: NavController, userViewModel: UserViewModel) {
        // Utilisez l'ID de l'utilisateur comme bon vous semble
        val userId = userViewModel.userId
        Text(
            text = "text",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Toast.makeText(this@MainActivity, "ID de l'utilisateur : $userId", Toast.LENGTH_SHORT).show()
    }

    // Fonction pour gérer la connexion
    private fun login(email: String, password: String, navController: NavController, onSuccess: (User?) -> Unit) {
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
                        onSuccess(user)
                        userViewModel.userId = user?.ID_User.toString()
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
}
