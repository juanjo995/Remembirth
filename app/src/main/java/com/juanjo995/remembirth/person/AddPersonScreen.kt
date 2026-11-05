package com.juanjo995.remembirth.person

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.juanjo995.remembirth.R
import com.juanjo995.remembirth.ui.theme.RemembirthTheme
import java.text.DateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Pantalla para añadir (o editar) una persona: nombre y fecha de nacimiento.
 * Para editar, se pasan los datos actuales en [initialName] e [initialBirthDateMillis].
 *
 * La fecha se guarda como milisegundos UTC (medianoche del día elegido),
 * que es lo que devuelve el DatePicker de Material 3. null = sin elegir.
 *
 * La pantalla no guarda nada: al pulsar OK avisa con [onSave] y quien la usa
 * decide qué hacer (guardarlo en la base de datos).
 */
@Composable
fun AddPersonScreen(
    onSave: (name: String, birthDateMillis: Long) -> Unit,
    modifier: Modifier = Modifier,
    initialName: String = "",
    initialBirthDateMillis: Long? = null,
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    var birthDateMillis by rememberSaveable { mutableStateOf(initialBirthDateMillis) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    // Evita guardar dos veces si se pulsa OK muy rápido (guardar tarda unos milisegundos)
    var saving by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding() // deja sitio al teclado para que no tape los campos
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.name_label)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words, // "juan" -> "Juan"
                imeAction = ImeAction.Done,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(16.dp))

        BirthDateField(
            dateMillis = birthDateMillis,
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(32.dp))

        // Solo se puede pulsar cuando hay nombre y fecha
        val date = birthDateMillis
        Button(
            onClick = {
                if (date != null && !saving) {
                    saving = true
                    onSave(name.trim(), date)
                }
            },
            enabled = name.isNotBlank() && date != null && !saving,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.save_button))
        }
    }

    if (showDatePicker) {
        BirthDatePickerDialog(
            initialDateMillis = birthDateMillis,
            onDateSelected = { birthDateMillis = it },
            onDismiss = { showDatePicker = false },
        )
    }
}

/**
 * Campo de solo lectura que muestra la fecha elegida. No se escribe en él:
 * al tocarlo se abre el calendario.
 */
@Composable
private fun BirthDateField(
    dateMillis: Long?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = LocalConfiguration.current.locales[0]
    val text = dateMillis?.let { formatDate(it, locale) } ?: ""

    Box(modifier) {
        OutlinedTextField(
            value = text,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.birth_date_label)) },
            trailingIcon = {
                Icon(painterResource(R.drawable.ic_calendar), contentDescription = null)
            },
            modifier = Modifier.fillMaxWidth(),
        )
        // Capa transparente encima: un TextField se "come" los toques,
        // así que capturamos el clic aquí para abrir el calendario.
        Box(
            Modifier
                .matchParentSize()
                .clickable(onClick = onClick)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BirthDatePickerDialog(
    initialDateMillis: Long?,
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit,
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateMillis,
        // Solo años pasados: al elegir año en el calendario no aparecen 2027, 2028...
        yearRange = DatePickerDefaults.YearRange.first..Calendar.getInstance().get(Calendar.YEAR),
        selectableDates = PastOrTodayDates,
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
                onDismiss()
            }) {
                Text(stringResource(R.string.ok_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_button))
            }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}

/** No se puede nacer en el futuro: solo deja elegir hasta hoy. */
@OptIn(ExperimentalMaterial3Api::class)
private object PastOrTodayDates : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean =
        utcTimeMillis <= System.currentTimeMillis()
}

/** Formatea la fecha según el idioma: "5 de noviembre de 1995" / "November 5, 1995". */
private fun formatDate(utcMillis: Long, locale: Locale): String {
    val format = DateFormat.getDateInstance(DateFormat.LONG, locale)
    // El DatePicker trabaja en UTC; si usáramos la zona del móvil
    // en algunos países saldría el día anterior.
    format.timeZone = TimeZone.getTimeZone("UTC")
    return format.format(Date(utcMillis))
}

@Preview(showBackground = true)
@Composable
private fun AddPersonScreenPreview() {
    RemembirthTheme {
        AddPersonScreen(onSave = { _, _ -> })
    }
}
