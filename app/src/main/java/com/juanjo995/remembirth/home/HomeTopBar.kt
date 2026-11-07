package com.juanjo995.remembirth.home

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.juanjo995.remembirth.R
import com.juanjo995.remembirth.ui.theme.RemembirthTheme

/**
 * Barra superior de la pantalla principal: título y menú de tres puntos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    onAddClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onOptionsClick: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text(stringResource(R.string.app_name)) },
        actions = {
            // El Box hace que el menú se despliegue pegado al botón
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_more_vert),
                        contentDescription = stringResource(R.string.more_options),
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    MenuItem(R.string.menu_add, onAddClick) { menuExpanded = false }
                    MenuItem(R.string.menu_edit, onEditClick) { menuExpanded = false }
                    MenuItem(R.string.menu_delete, onDeleteClick) { menuExpanded = false }
                    MenuItem(R.string.menu_options, onOptionsClick) { menuExpanded = false }
                }
            }
        },
    )
}

/** Opción del menú: al pulsarla cierra el menú y ejecuta la acción. */
@Composable
private fun MenuItem(@StringRes textRes: Int, onClick: () -> Unit, closeMenu: () -> Unit) {
    DropdownMenuItem(
        text = { Text(stringResource(textRes)) },
        onClick = {
            closeMenu()
            onClick()
        },
    )
}

@Preview
@Composable
private fun HomeTopBarPreview() {
    RemembirthTheme {
        HomeTopBar(
            onAddClick = {},
            onEditClick = {},
            onDeleteClick = {},
            onOptionsClick = {},
        )
    }
}
