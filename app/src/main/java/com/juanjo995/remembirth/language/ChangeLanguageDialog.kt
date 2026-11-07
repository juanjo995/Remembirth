package com.juanjo995.remembirth.language

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.juanjo995.remembirth.R
import com.juanjo995.remembirth.ui.theme.RemembirthTheme

/**
 * Diálogo para cambiar el idioma desde el menú. Usa el mismo desplegable
 * que la pantalla del primer arranque, con el idioma actual preseleccionado.
 * Igual que allí, los textos del diálogo se traducen al elegir en el desplegable.
 */
@Composable
fun ChangeLanguageDialog(
    onLanguageSelected: (AppLanguage) -> Unit,
    onDismiss: () -> Unit,
) {
    var selected by rememberSaveable { mutableStateOf(AppLanguage.current()) }
    val strings = localizedResources(selected)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.getString(R.string.menu_change_language)) },
        text = {
            LanguageDropdown(
                selected = selected,
                onSelect = { selected = it },
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = { onLanguageSelected(selected) }) {
                Text(strings.getString(R.string.ok_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.getString(R.string.cancel_button))
            }
        },
    )
}

@Preview
@Composable
private fun ChangeLanguageDialogPreview() {
    RemembirthTheme {
        ChangeLanguageDialog(onLanguageSelected = {}, onDismiss = {})
    }
}
