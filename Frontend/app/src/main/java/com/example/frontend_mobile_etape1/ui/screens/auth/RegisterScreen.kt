package com.example.frontend_mobile_etape1.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.frontend_mobile_etape1.ui.theme.BrightBlue
import com.example.frontend_mobile_etape1.ui.theme.NavyBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: RegisterViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onRegisterSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE8EDF2))
    ) {
        CenterAlignedTopAppBar(
            title = { Text("Créer un compte", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour"
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = NavyBlue,
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFDBEAFE))
            ) {
                Text(
                    "ℹ️  Votre compte sera créé en attente de validation par un administrateur.",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 12.sp,
                    color = NavyBlue
                )
            }

            if (uiState.isSuccess) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFD1FAE5))
                ) {
                    Text(
                        "✅  Demande envoyée ! Votre compte sera validé par un administrateur.",
                        modifier = Modifier.padding(12.dp),
                        fontSize = 12.sp,
                        color = Color(0xFF065F46)
                    )
                }
            }

            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; viewModel.clearError() },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrightBlue,
                            focusedLabelColor = BrightBlue
                        )
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; viewModel.clearError() },
                        label = { Text("Mot de passe") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (passwordVisible) VisualTransformation.None
                                              else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility
                                                  else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrightBlue,
                            focusedLabelColor = BrightBlue
                        )
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; viewModel.clearError() },
                        label = { Text("Confirmer le mot de passe") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrightBlue,
                            focusedLabelColor = BrightBlue
                        )
                    )

                    Button(
                        onClick = { viewModel.register(email, password, confirmPassword) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        enabled = !uiState.isLoading && !uiState.isSuccess,
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Demander un accès", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
