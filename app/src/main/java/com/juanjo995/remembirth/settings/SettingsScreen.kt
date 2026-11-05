package com.juanjo995.remembirth.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.juanjo995.remembirth.R
import com.juanjo995.remembirth.language.AppLanguage
import com.juanjo995.remembirth.language.ChangeLanguageDialog
import com.juanjo995.remembirth.language.LanguageFlag
import com.juanjo995.remembirth.ui.theme.RemembirthTheme

/** Pantalla de opciones: idioma y tema. */
@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var showLanguageDialog by rememberSaveable { mutableStateOf(false) }
    val currentLanguage = remember { AppLanguage.current() }
    var themeMode by remember { mutableStateOf(ThemeMode.load(context)) }

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // --- Idioma: muestra el actual; al pulsar se abre el diálogo ---
        ListItem(
            headlineContent = { Text(stringResource(R.string.settings_language)) },
            supportingContent = { Text(currentLanguage.displayName) },
            leadingContent = { LanguageFlag(currentLanguage) },
            modifier = Modifier.clickable { showLanguageDialog = true },
        )

        HorizontalDivider()

        // --- Tema: claro / oscuro / el del sistema ---
        Text(
            text = stringResource(R.string.settings_theme),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
        )
        Column(Modifier.selectableGroup()) {
            ThemeMode.entries.forEach { mode ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = mode == themeMode,
                            onClick = {
                                themeMode = mode
                                ThemeMode.save(context, mode)
                            },
                            role = Role.RadioButton,
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = mode == themeMode, onClick = null)
                    Spacer(Modifier.width(16.dp))
                    Text(stringResource(mode.labelRes))
                }
            }
        }
    }

    if (showLanguageDialog) {
        ChangeLanguageDialog(
            onLanguageSelected = { language ->
                showLanguageDialog = false
                // Guarda el idioma y recrea la Activity ya traducida
                AppLanguage.apply(context, language)
            },
            onDismiss = { showLanguageDialog = false },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    RemembirthTheme {
        SettingsScreen()
    }
}
