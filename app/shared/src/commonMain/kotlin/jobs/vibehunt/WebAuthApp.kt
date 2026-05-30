package jobs.vibehunt

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import jobs.vibehunt.auth.AuthState
import jobs.vibehunt.auth.AuthViewModel
import jobs.vibehunt.auth.currentAuthPath
import jobs.vibehunt.ui.auth.CallbackScreen
import jobs.vibehunt.ui.auth.HomeScreen
import jobs.vibehunt.ui.auth.LoginScreen
import jobs.vibehunt.ui.auth.RoleSelectionScreen
import jobs.vibehunt.ui.theme.VibeHuntTheme

@Composable
fun WebAuthApp(viewModel: AuthViewModel = viewModel { AuthViewModel() }) {
    val authState by viewModel.state.collectAsState()
    val isBusy by viewModel.isBusy.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val path = remember { currentAuthPath() }

    LaunchedEffect(Unit) {
        when {
            path.endsWith("/auth/callback") -> Unit
            path.endsWith("/auth/role") -> viewModel.refreshSession()
            else -> viewModel.refreshSession()
        }
    }

    VibeHuntTheme {
        when {
            path.endsWith("/auth/callback") -> {
                CallbackScreen(viewModel = viewModel)
                when (val state = authState) {
                    is AuthState.NeedsRegistration -> RoleSelectionScreen(state.user, viewModel, isBusy, errorMessage)
                    is AuthState.Authenticated -> HomeScreen(state.user, viewModel)
                    AuthState.Unauthenticated ->
                        if (!isBusy) {
                            LoginScreen(viewModel, isBusy, errorMessage)
                        }
                    else -> LoadingBox()
                }
            }
            path.endsWith("/auth/role") -> {
                when (val state = authState) {
                    is AuthState.NeedsRegistration ->
                        RoleSelectionScreen(state.user, viewModel, isBusy, errorMessage)
                    is AuthState.Authenticated -> HomeScreen(state.user, viewModel)
                    AuthState.Loading, AuthState.Unauthenticated -> LoadingBox()
                }
            }
            else -> {
                when (val state = authState) {
                    AuthState.Loading -> LoadingBox()
                    AuthState.Unauthenticated ->
                        LoginScreen(viewModel, isBusy, errorMessage)
                    is AuthState.NeedsRegistration ->
                        RoleSelectionScreen(state.user, viewModel, isBusy, errorMessage)
                    is AuthState.Authenticated -> HomeScreen(state.user, viewModel)
                }
            }
        }
    }
}

@Composable
private fun LoadingBox() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
