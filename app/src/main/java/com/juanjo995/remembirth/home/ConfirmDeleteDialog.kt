package com.juanjo995.remembirth.home

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.juanjo995.remembirth.R
import com.juanjo995.remembirth.ui.theme.RemembirthTheme

/** Pregunta "¿Eliminar a <nombre>?" antes de borrar. */
@Composable
fun ConfirmDeleteDialog(
    personName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { Text(stringResource(R.string.delete_confirm, personName)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.menu_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_button))
            }
        },
    )
}

@Preview
@Composable
private fun ConfirmDeleteDialogPreview() {
    RemembirthTheme {
        ConfirmDeleteDialog(personName = "Ana", onConfirm = {}, onDismiss = {})
    }
}
