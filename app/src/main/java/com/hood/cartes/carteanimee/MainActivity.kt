package com.hood.cartes.carteanimee
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hood.cartes.carteanimee.models.User
import com.hood.cartes.carteanimee.services.ApiService
import com.hood.cartes.carteanimee.models.UserResponse
import com.hood.cartes.carteanimee.ui.theme.CarteAnimeeTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {

    private val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://www.demineur-ligne.com/PFE/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CarteAnimeeTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    LoginScreen()
                }
            }
        }
    }

    @Composable
    fun LoginScreen() {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var loggedInUser by remember { mutableStateOf<User?>(null) }

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
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Mot de passe") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { login(email, password) { user -> loggedInUser = user } },
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

    // Fonction pour gérer la connexion
// Fonction pour gérer la connexion
    private fun login(email: String, password: String, onSuccess: (User?) -> Unit) {
        val call: Call<UserResponse> = apiService.connectAccount(email, password)

        call.enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    val userResponse = response.body()
                    if (userResponse?.success == true) {
                        // Connexion réussie, accédez à l'objet user dans userResponse.user
                        val user = userResponse.user
                        onSuccess(user)
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
