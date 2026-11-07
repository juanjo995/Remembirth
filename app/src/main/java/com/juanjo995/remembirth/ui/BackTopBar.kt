package com.juanjo995.remembirth.ui

import androidx.annotation.StringRes
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.juanjo995.remembirth.R
import com.juanjo995.remembirth.ui.theme.RemembirthTheme

/**
 * Barra superior con flecha para volver y un título
 * ("Añadir persona", "Editar persona", "Opciones"...).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackTopBar(
    @StringRes titleRes: Int,
    onBackClick: () -> Unit,
) {
    TopAppBar(
        title = { Text(stringResource(titleRes)) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = stringResource(R.string.back),
                )
            }
        },
    )
}

@Preview
@Composable
private fun BackTopBarPreview() {
    RemembirthTheme {
        BackTopBar(titleRes = R.string.add_person_title, onBackClick = {})
    }
}
