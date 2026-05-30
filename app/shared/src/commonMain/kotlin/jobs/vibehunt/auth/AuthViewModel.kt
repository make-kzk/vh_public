package jobs.vibehunt.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository = createAuthRepository(),
) : ViewModel() {
    private val _state = MutableStateFlow<AuthState>(AuthState.Loading)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    fun refreshSession() {
        viewModelScope.launch {
            _isBusy.value = true
            _errorMessage.value = null
            try {
                val user = repository.fetchMe()
                _state.value =
                    when {
                        user == null -> AuthState.Unauthenticated
                        user.role == null -> AuthState.NeedsRegistration(user)
                        else -> AuthState.Authenticated(user)
                    }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load session"
                _state.value = AuthState.Unauthenticated
            } finally {
                _isBusy.value = false
            }
        }
    }

    fun startGoogleSignIn(redirectUri: String) {
        startOAuth(OAuthProvider.GOOGLE, redirectUri)
    }

    fun startAppleSignIn(redirectUri: String) {
        startOAuth(OAuthProvider.APPLE, redirectUri)
    }

    private fun startOAuth(provider: OAuthProvider, redirectUri: String) {
        viewModelScope.launch {
            _isBusy.value = true
            _errorMessage.value = null
            try {
                val response = repository.startOAuth(provider, redirectUri)
                openOAuthAuthorizationUrl(response.authorizationUrl)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "OAuth start failed"
            } finally {
                _isBusy.value = false
            }
        }
    }

    fun completeRegistration(role: UserRole) {
        viewModelScope.launch {
            _isBusy.value = true
            _errorMessage.value = null
            try {
                val user = repository.completeRegistration(role)
                _state.value = AuthState.Authenticated(user)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Registration failed"
            } finally {
                _isBusy.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _isBusy.value = true
            try {
                repository.logout()
            } catch (_: Exception) {
                // ignore
            } finally {
                _state.value = AuthState.Unauthenticated
                _isBusy.value = false
            }
        }
    }

    fun onOAuthCallbackQuery(error: String?) {
        if (error != null) {
            _errorMessage.value = error
            _state.value = AuthState.Unauthenticated
            return
        }
        refreshSession()
    }
}

expect fun openOAuthAuthorizationUrl(url: String)

expect fun defaultAuthRedirectUri(): String
