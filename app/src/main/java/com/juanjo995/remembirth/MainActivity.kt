package com.juanjo995.remembirth

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.juanjo995.remembirth.data.Person
import com.juanjo995.remembirth.data.RemembirthDatabase
import com.juanjo995.remembirth.home.ConfirmDeleteDialog
import com.juanjo995.remembirth.home.HomeScreen
import com.juanjo995.remembirth.home.HomeTopBar
import com.juanjo995.remembirth.home.PersonPickerDialog
import com.juanjo995.remembirth.language.AppLanguage
import com.juanjo995.remembirth.language.LanguageSelectionScreen
import com.juanjo995.remembirth.notifications.BirthdayNotifier
import com.juanjo995.remembirth.notifications.BirthdayScheduler
import com.juanjo995.remembirth.person.AddPersonScreen
import com.juanjo995.remembirth.settings.SettingsScreen
import com.juanjo995.remembirth.settings.ThemeMode
import com.juanjo995.remembirth.ui.BackTopBar
import com.juanjo995.remembirth.ui.theme.RemembirthTheme
import kotlinx.coroutines.launch

// AppCompatActivity (en vez de ComponentActivity) permite cambiar el idioma
// de la app también en Android 12 o inferior.
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Aplica el tema guardado (claro/oscuro/sistema) antes de crear la pantalla
        AppCompatDelegate.setDefaultNightMode(ThemeMode.load(this).nightMode)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Comprobación diaria de cumpleaños para las notificaciones
        BirthdayScheduler.scheduleDaily(this)

        // Si nunca se ha elegido idioma para la app, la lista está vacía.
        // Usamos eso para saber si es el primer arranque.
        val languageChosen = !AppCompatDelegate.getApplicationLocales().isEmpty

        // Las notificaciones necesitan el idioma guardado en los ajustes. Si la app se
        // instaló antes de guardarlo, lo guardamos ahora a partir del idioma actual.
        if (languageChosen && AppLanguage.savedTag(this) == null) {
            AppLanguage.save(this, AppLanguage.current())
        }

        setContent {
            RemembirthTheme {
                if (languageChosen) {
                    RemembirthNavigation()
                } else {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        LanguageSelectionScreen(
                            onLanguageSelected = { language ->
                                // Guarda el idioma y recrea la Activity ya traducida
                                AppLanguage.apply(this@MainActivity, language)
                            },
                            modifier = Modifier.padding(innerPadding),
                        )
                    }
                }
            }
        }
    }
}

/** Pantallas de la app (sin contar la selección de idioma). */
private enum class Screen { HOME, ADD_PERSON, EDIT_PERSON, SETTINGS }

/** Para qué se abre el diálogo de elegir persona. */
private enum class PickAction { EDIT, DELETE }

/**
 * Navegación sencilla: guardamos en qué pantalla estamos y mostramos esa.
 * rememberSaveable hace que no se pierda al girar el móvil.
 */
@Composable
private fun RemembirthNavigation() {
    var currentScreen by rememberSaveable { mutableStateOf(Screen.HOME) }
    // Diálogo de elegir persona abierto (y para qué), o null si está cerrado
    var pickAction by rememberSaveable { mutableStateOf<PickAction?>(null) }
    // Id de la persona que se está editando
    var editingPersonId by rememberSaveable { mutableStateOf<Long?>(null) }
    // Id de la persona pendiente de confirmar su eliminación (pulsación larga)
    var confirmDeleteId by rememberSaveable { mutableStateOf<Long?>(null) }

    val context = LocalContext.current
    val personDao = remember { RemembirthDatabase.getInstance(context).personDao() }
    // Las operaciones de base de datos son "suspend": se lanzan en una corrutina
    val scope = rememberCoroutineScope()

    // Lista de personas que se actualiza sola cuando cambia la base de datos.
    // null hasta que llega la primera lectura.
    val persons by remember { personDao.getAll() }.collectAsState(initial = null)

    // Permiso de notificaciones (Android 13+): se pide cuando ya hay alguna persona
    // guardada, que es cuando el usuario entiende para qué sirve.
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) BirthdayScheduler.checkNow(context)
    }
    val hasPersons = !persons.isNullOrEmpty()
    LaunchedEffect(hasPersons) {
        if (hasPersons &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            when (currentScreen) {
                // En la principal, la barra solo cuando ya hay personas
                // (la primera vez solo se ve el botón "Añadir persona")
                Screen.HOME -> if (!persons.isNullOrEmpty()) {
                    HomeTopBar(
                        onAddClick = { currentScreen = Screen.ADD_PERSON },
                        onEditClick = { pickAction = PickAction.EDIT },
                        onDeleteClick = { pickAction = PickAction.DELETE },
                        onOptionsClick = { currentScreen = Screen.SETTINGS },
                    )
                }

                Screen.ADD_PERSON -> BackTopBar(
                    titleRes = R.string.add_person_title,
                    onBackClick = { currentScreen = Screen.HOME },
                )

                Screen.EDIT_PERSON -> BackTopBar(
                    titleRes = R.string.edit_person,
                    onBackClick = { currentScreen = Screen.HOME },
                )

                Screen.SETTINGS -> BackTopBar(
                    titleRes = R.string.menu_options,
                    onBackClick = { currentScreen = Screen.HOME },
                )
            }
        },
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)

        when (currentScreen) {
            Screen.HOME -> HomeScreen(
                persons = persons,
                onAddPersonClick = { currentScreen = Screen.ADD_PERSON },
                onEditPerson = { person ->
                    editingPersonId = person.id
                    currentScreen = Screen.EDIT_PERSON
                },
                onDeletePerson = { person -> confirmDeleteId = person.id },
                modifier = contentModifier,
            )

            Screen.ADD_PERSON -> {
                // Botón/gesto "atrás" del sistema: volver al inicio en vez de cerrar la app
                BackHandler { currentScreen = Screen.HOME }
                AddPersonScreen(
                    onSave = { name, birthDateMillis ->
                        scope.launch {
                            val id = personDao.insert(Person(name = name, birthDateMillis = birthDateMillis))
                            BirthdayScheduler.checkNow(context, id) // ¿hay que avisar ya?
                            currentScreen = Screen.HOME
                        }
                    },
                    modifier = contentModifier,
                )
            }

            Screen.EDIT_PERSON -> {
                BackHandler { currentScreen = Screen.HOME }
                val person = persons?.firstOrNull { it.id == editingPersonId }
                if (person != null) {
                    // Mismo formulario, pero con los datos actuales ya rellenos
                    AddPersonScreen(
                        initialName = person.name,
                        initialBirthDateMillis = person.birthDateMillis,
                        onSave = { name, birthDateMillis ->
                            scope.launch {
                                personDao.update(
                                    person.copy(name = name, birthDateMillis = birthDateMillis)
                                )
                                BirthdayScheduler.checkNow(context, person.id) // la fecha puede haber cambiado
                                currentScreen = Screen.HOME
                            }
                        },
                        modifier = contentModifier,
                    )
                }
            }

            Screen.SETTINGS -> {
                BackHandler { currentScreen = Screen.HOME }
                SettingsScreen(modifier = contentModifier)
            }
        }
    }

    // Diálogo para elegir a quién editar o eliminar
    val action = pickAction
    val currentPersons = persons
    if (action != null && !currentPersons.isNullOrEmpty()) {
        PersonPickerDialog(
            titleRes = if (action == PickAction.EDIT) R.string.edit_person else R.string.delete_person,
            confirmRes = if (action == PickAction.EDIT) R.string.menu_edit else R.string.menu_delete,
            persons = currentPersons,
            onConfirm = { person ->
                pickAction = null
                when (action) {
                    PickAction.EDIT -> {
                        editingPersonId = person.id
                        currentScreen = Screen.EDIT_PERSON
                    }
                    PickAction.DELETE -> scope.launch {
                        personDao.delete(person)
                        BirthdayNotifier.cancel(context, person)
                    }
                }
            },
            onDismiss = { pickAction = null },
        )
    }

    // Confirmación al eliminar desde la pulsación larga
    val personToDelete = persons?.firstOrNull { it.id == confirmDeleteId }
    if (personToDelete != null) {
        ConfirmDeleteDialog(
            personName = personToDelete.name,
            onConfirm = {
                confirmDeleteId = null
                scope.launch {
                    personDao.delete(personToDelete)
                    BirthdayNotifier.cancel(context, personToDelete)
                }
            },
            onDismiss = { confirmDeleteId = null },
        )
    }
}
