package com.juanjo995.remembirth.language

import android.content.res.Configuration
import android.content.res.Resources
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.juanjo995.remembirth.R
import com.juanjo995.remembirth.ui.theme.RemembirthTheme
import java.util.Locale

/**
 * Pantalla que se muestra la primera vez que se abre la app para elegir idioma.
 * No sabe nada de cómo se aplica el idioma: solo avisa con [onLanguageSelected].
 */
@Composable
fun LanguageSelectionScreen(
    onLanguageSelected: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier,
) {
    // rememberSaveable: conserva la selección aunque se gire la pantalla
    var selected by rememberSaveable { mutableStateOf(AppLanguage.fromSystem()) }

    // Recursos (textos) en el idioma seleccionado en el desplegable, aunque
    // todavía no se haya aplicado a la app. Se recalculan al cambiar "selected".
    val strings = localizedResources(selected)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = strings.getString(R.string.language_selection_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = strings.getString(R.string.language_selection_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(32.dp))

        LanguageDropdown(
            selected = selected,
            onSelect = { selected = it },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(32.dp))
        Button(
            onClick = { onLanguageSelected(selected) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(strings.getString(R.string.continue_button))
        }
    }
}

/** Devuelve los recursos de la app traducidos a [language]. */
@Composable
internal fun localizedResources(language: AppLanguage): Resources {
    val context = LocalContext.current
    return remember(language) {
        val config = Configuration(context.resources.configuration)
        config.setLocale(Locale.forLanguageTag(language.tag))
        context.createConfigurationContext(config).resources
    }
}

/** Campo desplegable: muestra el idioma elegido y, al pulsarlo, la lista completa. */
@Composable
internal fun LanguageDropdown(
    selected: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier) {
        OutlinedCard(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LanguageFlag(selected)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = selected.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_drop_down),
                    contentDescription = null,
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            AppLanguage.entries.forEach { language ->
                DropdownMenuItem(
                    text = { Text(language.displayName) },
                    leadingIcon = { LanguageFlag(language) },
                    onClick = {
                        onSelect(language)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
internal fun LanguageFlag(language: AppLanguage) {
    Image(
        painter = painterResource(language.flagRes),
        // El nombre del idioma ya aparece al lado, así que la imagen es decorativa
        contentDescription = null,
        modifier = Modifier.size(28.dp),
    )
}

@Preview(showBackground = true)
@Composable
private fun LanguageSelectionScreenPreview() {
    RemembirthTheme {
        LanguageSelectionScreen(onLanguageSelected = {})
    }
}
