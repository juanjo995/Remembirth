package com.juanjo995.remembirth.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.juanjo995.remembirth.R
import com.juanjo995.remembirth.birthday.UpcomingBirthday
import com.juanjo995.remembirth.birthday.birthdayHtml
import com.juanjo995.remembirth.birthday.upcomingBirthday
import com.juanjo995.remembirth.data.Person
import com.juanjo995.remembirth.ui.theme.RemembirthTheme

/**
 * Pantalla principal (sin la barra superior, que está en [HomeTopBar]).
 *
 * @param persons personas de la base de datos; null mientras se cargan.
 * @param onEditPerson / onDeletePerson opciones del menú de pulsación larga.
 */
@Composable
fun HomeScreen(
    persons: List<Person>?,
    onAddPersonClick: () -> Unit,
    onEditPerson: (Person) -> Unit,
    onDeletePerson: (Person) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        // Cargando (es casi instantáneo): no mostramos nada para evitar parpadeos
        persons == null -> Box(modifier.fillMaxSize())

        // Sin personas: solo el botón, centrado
        persons.isEmpty() -> Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            AddPersonButton(onAddPersonClick)
        }

        // Con personas: la lista (para añadir más, el menú de la barra superior)
        else -> BirthdayList(
            persons = persons,
            onEditPerson = onEditPerson,
            onDeletePerson = onDeletePerson,
            modifier = modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun AddPersonButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick = onClick, modifier = modifier) {
        Text(stringResource(R.string.add_person_button))
    }
}

/** Una persona junto con su próximo cumpleaños ya calculado. */
private data class PersonBirthday(val person: Person, val birthday: UpcomingBirthday)

@Composable
private fun BirthdayList(
    persons: List<Person>,
    onEditPerson: (Person) -> Unit,
    onDeletePerson: (Person) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Ordenados por días que faltan; si empatan, por nombre
    val sorted = persons
        .map { PersonBirthday(it, upcomingBirthday(it.birthDateMillis)) }
        .sortedWith(compareBy({ it.birthday.daysUntil }, { it.person.name.lowercase() }))

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        // Margen antes de la primera fila (se desplaza junto con la lista)
        contentPadding = PaddingValues(top = 8.dp),
    ) {
        items(sorted, key = { it.person.id }) { item ->
            BirthdayRow(
                item = item,
                onEdit = { onEditPerson(item.person) },
                onDelete = { onDeletePerson(item.person) },
            )
            HorizontalDivider()
        }
    }
}

/** Una fila de la lista. Al mantenerla pulsada aparece el menú Editar/Eliminar. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BirthdayRow(
    item: PersonBirthday,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    // El Box hace que el menú aparezca pegado a la fila pulsada
    Box {
        Text(
            text = birthdayText(item.person.name, item.birthday),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {}, // la pulsación normal no hace nada (de momento)
                    onLongClick = { menuExpanded = true },
                )
                .padding(horizontal = 16.dp, vertical = 16.dp),
        )
        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_edit)) },
                onClick = {
                    menuExpanded = false
                    onEdit()
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_delete)) },
                onClick = {
                    menuExpanded = false
                    onDelete()
                },
            )
        }
    }
}

/**
 * La frase del cumpleaños (ver [birthdayHtml]) convertida a texto con estilo:
 * <b> pasa a negrita y <font color> a color.
 */
@Composable
private fun birthdayText(name: String, birthday: UpcomingBirthday): AnnotatedString {
    val resources = LocalContext.current.resources
    val darkBackground = isDarkBackground()
    return AnnotatedString.fromHtml(birthdayHtml(resources, name, birthday, darkBackground))
}

/** ¿El tema actual es oscuro? Se mira el fondo, así funciona también con colores dinámicos. */
@Composable
private fun isDarkBackground(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f

@Preview(showBackground = true)
@Composable
private fun HomeScreenEmptyPreview() {
    RemembirthTheme {
        HomeScreen(
            persons = emptyList(),
            onAddPersonClick = {},
            onEditPerson = {},
            onDeletePerson = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenListPreview() {
    val day = 86_400_000L
    val now = System.currentTimeMillis()
    RemembirthTheme {
        HomeScreen(
            persons = listOf(
                Person(1, "Ana", now - 30L * 365 * day),
                Person(2, "Luis", now - 25L * 365 * day + 40 * day),
                Person(3, "Marta", now - 2L * 365 * day + 3 * day),
            ),
            onAddPersonClick = {},
            onEditPerson = {},
            onDeletePerson = {},
        )
    }
}
