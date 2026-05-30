package jobs.vibehunt.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import jobs.vibehunt.auth.AuthViewModel
import jobs.vibehunt.auth.readAuthQueryParam
import jobs.vibehunt.ui.adaptive.AdaptiveContent

@Composable
fun CallbackScreen(viewModel: AuthViewModel) {
    LaunchedEffect(Unit) {
        val error = readAuthQueryParam("error")
        viewModel.onOAuthCallbackQuery(error)
    }

    AdaptiveContent {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator()
            Text(
                text = "Finishing sign-in…",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}
