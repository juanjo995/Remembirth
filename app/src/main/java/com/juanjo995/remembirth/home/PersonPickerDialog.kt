package com.juanjo995.remembirth.home

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.juanjo995.remembirth.R
import com.juanjo995.remembirth.data.Person
import com.juanjo995.remembirth.ui.theme.RemembirthTheme

/**
 * Diálogo para elegir una persona de la lista (se usa para editar y para eliminar).
 * El botón de confirmar solo se activa cuando hay una persona seleccionada.
 *
 * @param titleRes título del diálogo ("Editar persona", "Eliminar persona"...).
 * @param confirmRes texto del botón de confirmar ("Editar", "Eliminar"...).
 */
@Composable
fun PersonPickerDialog(
    @StringRes titleRes: Int,
    @StringRes confirmRes: Int,
    persons: List<Person>,
    onConfirm: (Person) -> Unit,
    onDismiss: () -> Unit,
) {
    // Guardamos el id (no el objeto) para que sobreviva a girar la pantalla
    var selectedId by rememberSaveable { mutableStateOf<Long?>(null) }
    val selectedPerson = persons.firstOrNull { it.id == selectedId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(titleRes)) },
        text = {
            // Con muchas personas, la lista se puede desplazar
            Column(
                Modifier
                    .selectableGroup()
                    .verticalScroll(rememberScrollState())
            ) {
                persons.forEach { person ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            // Toda la fila es pulsable, no solo el círculo
                            .selectable(
                                selected = person.id == selectedId,
                                onClick = { selectedId = person.id },
                                role = Role.RadioButton,
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = person.id == selectedId, onClick = null)
                        Spacer(Modifier.width(12.dp))
                        Text(person.name)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { selectedPerson?.let(onConfirm) },
                enabled = selectedPerson != null,
            ) {
                Text(stringResource(confirmRes))
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
private fun PersonPickerDialogPreview() {
    RemembirthTheme {
        PersonPickerDialog(
            titleRes = R.string.delete_person,
            confirmRes = R.string.menu_delete,
            persons = listOf(Person(1, "Ana", 0), Person(2, "Luis", 0)),
            onConfirm = {},
            onDismiss = {},
        )
    }
}
