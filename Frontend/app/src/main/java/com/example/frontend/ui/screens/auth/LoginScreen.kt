package com.example.frontend.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.frontend.ui.theme.NavyBlue
import com.example.frontend.ui.theme.BrightBlue

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onGoToRegister: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onLoginSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE8EDF2))
            .verticalScroll(rememberScrollState())
    ) {
        // ── Hero ─────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(NavyBlue, BrightBlue)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Cercles décoratifs
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .offset(x = 80.dp, y = (-60).dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.06f))
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(24.dp)
            ) {
                Text("🎲", fontSize = 40.sp)
                Text(
                    "Festival à Jouer",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
                Text(
                    "Connexion à votre compte",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 13.sp
                )
            }
        }

        // ── Formulaire ────────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Se connecter",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = NavyBlue
                )

                // Note: ErrorBanner might need to be created or fixed as well
                if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; viewModel.clearError() },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
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
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            viewModel.login(email, password)
                        }
                    ),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility
                                              else Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrightBlue,
                        focusedLabelColor = BrightBlue
                    )
                )

                Button(
                    onClick = { viewModel.login(email, password) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = !uiState.isLoading,
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
                        Text("Se connecter →", fontWeight = FontWeight.Bold)
                    }
                }

                TextButton(
                    onClick = onGoToRegister,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        "Pas encore de compte ? Demander l'accès",
                        color = BrightBlue,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
