
package com.abhiram.flowtune.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.abhiram.flowtune.R
import kotlinx.coroutines.launch

// Modelo de datos mejorado con iconos y colores
data class Problema(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val imagenes: List<Int> = emptyList(),
    val iconDrawable: Int,
    val color: Color,
    val pasos: List<String> = emptyList(),
    val tips: List<String> = emptyList()
)
// Problema y preguntas con iconos y colores personalizados
val problemasComunes = listOf(
    Problema(
        id = "error_desconocido",
        titulo = "Error Desconocido",
        descripcion = "Este error puede deberse a múltiples factores. Sigue los pasos a continuación para resolverlo.",
        iconDrawable = R.drawable.error,
        color = Color(0xFFE53935),
        pasos = listOf(
            "Verifica tu conexión a Internet",
            "Reinicia la aplicación",
            "Actualiza a la última versión",
            "Borra la caché de la aplicación dentro de OpenTune:  (Ajustes > Almacenamiento > Borrar caché de canciones)",
        ),
        tips = listOf(
            "Mantén siempre actualizada la aplicación",
            "Una conexión Wi-Fi estable mejora el rendimiento"
        )
    ),
    Problema(
        id = "error_instalacion",
        titulo = "Error de Instalación",
        descripcion = "Problemas durante la instalación pueden ocurrir por varias razones.",
        iconDrawable = R.drawable.download,
        color = Color(0xFF1E88E5),
        pasos = listOf(
            "Verifica que tienes suficiente espacio de almacenamiento",
            "Comprueba que tu dispositivo es compatible",
            "Desinstala cualquier versión anterior de OpenTune",
        )
    ),
    Problema(
        id = "error_restaurar_backup",
        titulo = "Error al Restaurar Copia de Seguridad",
        descripcion = "Problemas con la restauración de datos pueden deberse a incompatibilidades.",
        iconDrawable = R.drawable.restore,
        color = Color(0xFF43A047),
        pasos = listOf(
            "Verifica que el archivo de respaldo no esté dañado",
            "Comprueba que la versión sea compatible",
            "Asegúrate de tener suficiente espacio libre",
            "Reinicia el dispositivo e intenta nuevamente"
        ),
        tips = listOf(
            "Realiza copias de seguridad regularmente",
            "Almacena tus copias en múltiples ubicaciones"
        )
    ),
    Problema(
        id = "cierre_despues_actualizar",
        titulo = "La app se cierra después de actualizar",
        descripcion = "Después de actualizar OpenTune, la aplicación se cierra inesperadamente al abrirla.",
        iconDrawable = R.drawable.update,
        color = Color(0xFFD81B60),
        pasos = listOf(
            "Cierra completamente la aplicación desde el administrador de tareas",
            "Borra la caché de la aplicación (Ajustes > Aplicaciones > OpenTune > Almacenamiento > Borrar caché)",
            "Reinicia tu dispositivo",
            "Si el problema persiste, desinstala e instala nuevamente la aplicación",
            "Asegúrate de hacer una copia de seguridad antes de desinstalar"
        ),
        tips = listOf(
            "Siempre cierra la aplicación completamente antes de actualizarla",
            "Mantén al menos 500MB de espacio libre en tu dispositivo"
        )
    ),
    Problema(
        id = "app_no_actualiza",
        titulo = "La app no se actualiza",
        descripcion = "OpenTune no se actualiza a la versión más reciente cuando intentas actualizarla.",
        iconDrawable = R.drawable.update,
        color = Color(0xFF6200EA),
        pasos = listOf(
            "Verifica tu conexión a Internet",
            "Libera espacio de almacenamiento en tu dispositivo",
            "Descarga manualmente la última versión desde los sitios oficiales",
            "Desinstala la versión actual e instala la nueva versión",
            "Asegúrate de hacer una copia de seguridad antes de desinstalar"
        ),
        tips = listOf(
            "Siempre descarga OpenTune desde fuentes oficiales",
            "Configura notificaciones para nuevas versiones"
        )
    ),
    Problema(
        id = "video_no_disponible",
        titulo = "Este video no está disponible",
        descripcion = "Al intentar reproducir un video o canción, aparece el mensaje 'Este video no está disponible'.",
        iconDrawable = R.drawable.slow_motion_video,
        color = Color(0xFF546E7A),
        pasos = listOf(
            "Verifica tu conexión a Internet",
            "El contenido puede haber sido eliminado o restringido geográficamente",
            "Intenta buscar el mismo contenido con otro nombre o de otro artista",
            "Actualiza la aplicación a la última versión",
            "Espera unas horas y vuelve a intentarlo, podría ser un problema temporal"
        ),
        tips = listOf(
            "Algunos videos pueden no estar disponibles por restricciones regionales",
            "Guarda tus favoritos para acceso sin conexión"
        )
    ),
    Problema(
        id = "video_inadecuado",
        titulo = "Este video puede ser inadecuado para algunos usuarios",
        descripcion = "Aparece una advertencia de contenido inadecuado al intentar reproducir ciertos videos.",
        iconDrawable = R.drawable.error,
        color = Color(0xFFFFA000),
        pasos = listOf(
            "Este mensaje es generado por las políticas de contenido de la plataforma original",
            "Verifica si tu cuenta tiene restricciones de edad",
            "En Ajustes > Cuenta, asegúrate de que no tienes activado el modo restringido",
            "Intenta iniciar sesión con una cuenta diferente",
            "Algunos contenidos con restricciones no pueden reproducirse en OpenTune por limitaciones de la API"
        )
    ),
    Problema(
        id = "descargas_no_inician",
        titulo = "Las descargas no inician",
        descripcion = "Al intentar descargar contenido, la descarga no comienza o queda en espera indefinidamente.",
        iconDrawable = R.drawable.download,
        color = Color(0xFF00ACC1),
        pasos = listOf(
            "Verifica tu conexión a Internet",
            "Comprueba que tienes suficiente espacio de almacenamiento",
            "Otorga los permisos de almacenamiento necesarios a la aplicación",
            "Reinicia la aplicación y vuelve a intentar la descarga",
            "Verifica que no hay restricciones de batería o datos para OpenTune",
            "Si usas una tarjeta SD, intenta cambiar la ubicación de descarga al almacenamiento interno"
        ),
        tips = listOf(
            "Descarga contenido con Wi-Fi para evitar problemas de conexión",
            "Mantén al menos 1GB de espacio libre para descargas grandes"
        )
    ),
    Problema(
        id = "descargas_no_completan",
        titulo = "Las descargas no se completan",
        descripcion = "Las descargas comienzan pero nunca terminan o se interrumpen antes de completarse.",
        iconDrawable = R.drawable.download,
        color = Color(0xFF3949AB),
        pasos = listOf(
            "Verifica que tu conexión a Internet sea estable",
            "Desactiva el ahorro de batería para OpenTune",
            "Cierra otras aplicaciones que consuman ancho de banda",
            "Intenta pausar y reanudar la descarga",
            "Reinicia el dispositivo y vuelve a intentarlo",
            "Borra la caché de la aplicación y vuelve a intentar la descarga"
        ),
        tips = listOf(
            "Las redes Wi-Fi públicas pueden ser inestables para descargas",
            "Procura no cambiar entre redes Wi-Fi y datos móviles durante la descarga"
        )
    ),
    Problema(
        id = "iniciar_sesion_bot",
        titulo = "Inicia sesión para confirmar que no eres un bot",
        descripcion = "Aparece un mensaje solicitando iniciar sesión para verificar que no eres un bot.",
        iconDrawable = R.drawable.robot,
        color = Color(0xFF757575),
        pasos = listOf(
            "Este mensaje es una medida de seguridad de los servidores",
            "Inicia sesión con tu cuenta de Google si ya tienes una configurada",
            "Si prefieres no usar tu cuenta, espera unas horas e intenta nuevamente",
            "Cambia tu red Wi-Fi o usa datos móviles",
            "Actualiza a la última versión de OpenTune",
            "Reinicia tu router o cambia de DNS"
        ),
        tips = listOf(
            "Este mensaje puede aparecer si realizas muchas búsquedas en poco tiempo",
            "Evita el uso de VPN cuando sea posible, ya que pueden activar estas verificaciones"
        )
    ),
    Problema(
        id = "upload_failed_backup",
        titulo = "'Upload failed, please try again' al intentar crear una copia de seguridad",
        descripcion = "Error al intentar subir o crear una copia de seguridad de tus datos de OpenTune.",
        iconDrawable = R.drawable.backup,
        color = Color(0xFF8E24AA),
        pasos = listOf(
            "Verifica tu conexión a Internet",
            "Asegúrate de tener espacio suficiente en tu cuenta de almacenamiento en la nube",
            "Intenta crear un archivo de respaldo más pequeño dividiendo tus datos",
            "Verifica que tienes los permisos necesarios para la aplicación",
            "Cierra la aplicación completamente y vuelve a intentarlo",
            "Intenta usar otra ubicación para almacenar la copia de seguridad"
        ),
        tips = listOf(
            "Haz copias de seguridad regulares para evitar pérdida de datos",
            "Mantén copias locales además de las copias en la nube"
        )
    ),
    Problema(
        id = "discord_rpc_falla",
        titulo = "Discord RPC a veces no funciona",
        descripcion = "La integración con Discord Rich Presence no funciona consistentemente.",
        iconDrawable = R.drawable.discord,
        color = Color(0xFF7289DA),
        pasos = listOf(
            "Asegúrate de que Discord está abierto antes de iniciar OpenTune",
            "Verifica que la opción de Discord RPC está activada en los ajustes de OpenTune",
            "Reinicia ambas aplicaciones (primero Discord, luego OpenTune)",
            "Verifica que Discord tiene permiso para ejecutarse en segundo plano",
            "Desactiva y vuelve a activar la opción de 'Actividad de juego' en Discord",
            "Actualiza ambas aplicaciones a sus últimas versiones"
        ),
        tips = listOf(
            "Las restricciones de batería pueden afectar la comunicación entre aplicaciones",
            "En algunos dispositivos, esta función puede consumir más batería"
        )
    ),
    Problema(
        id = "reproduce_sin_cuenta",
        titulo = "La app reproduce música solo cuando no tiene una cuenta de Google",
        descripcion = "OpenTune funciona correctamente sin cuenta pero deja de reproducir contenido al iniciar sesión.",
        iconDrawable = R.drawable.person,
        color = Color(0xFFEF6C00),
        pasos = listOf(
            "Cierra sesión y vuelve a iniciar sesión con tu cuenta",
            "Verifica que tu cuenta no tiene restricciones geográficas o de edad",
            "Borra los datos de la aplicación (esto eliminará tus preferencias locales)",
            "Actualiza a la última versión de OpenTune",
            "Prueba con una cuenta de Google diferente",
            "Restaura la configuración de fábrica de la aplicación en Ajustes > Avanzado"
        ),
        tips = listOf(
            "Algunas cuentas pueden tener restricciones impuestas por la plataforma original",
            "Crea una cuenta específica para usar con OpenTune si es necesario"
        )
    ),
    Problema(
        id = "no_reproduce_tras_borrar_cache",
        titulo = "Al borrar el cache la app no reproduce música e incluso se cierra",
        descripcion = "Después de borrar la caché, la aplicación presenta fallos o cierres al intentar reproducir o descargar música.",
        iconDrawable = R.drawable.playlist_add,
        color = Color(0xFFC62828),
        pasos = listOf(
            "Reinicia el dispositivo después de borrar la caché",
            "Asegúrate de que solo borraste la caché y no los datos de la aplicación",
            "Vuelve a configurar tu cuenta en la aplicación",
            "Verifica tu conexión a Internet",
            "Desinstala y vuelve a instalar la aplicación como último recurso",
            "Asegúrate de hacer una copia de seguridad antes de desinstalar"
        ),
        tips = listOf(
            "No es necesario borrar la caché regularmente",
            "Si necesitas liberar espacio, considera eliminar descargas antiguas en lugar de la caché"
        )
    ),
    Problema(
        id = "canciones_se_detienen",
        titulo = "Las canciones suenan por unos minutos y después se detienen",
        descripcion = "La reproducción de música se interrumpe después de reproducirse durante un tiempo.",
        iconDrawable = R.drawable.music_note,
        color = Color(0xFF5D4037),
        pasos = listOf(
            "Verifica que OpenTune tiene permitido ejecutarse en segundo plano",
            "Desactiva las optimizaciones de batería para la aplicación",
            "Verifica tu conexión a Internet",
            "Si estás usando Bluetooth, comprueba la configuración de ahorro de energía",
            "Intenta descargar la música para reproducción sin conexión",
            "Actualiza a la última versión de OpenTune",
            "Reinicia tu dispositivo"
        ),
        tips = listOf(
            "Algunos dispositivos tienen optimizaciones agresivas que cierran apps en segundo plano",
            "Añade OpenTune a la lista de 'apps protegidas' en la configuración de tu dispositivo"
        )
    ),
    Problema(
        id = "cierre_al_descargar_eliminar",
        titulo = "Al querer descargar o eliminar canciones la app se cierra",
        descripcion = "La aplicación se cierra inesperadamente al intentar gestionar descargas o eliminar contenido.",
        iconDrawable = R.drawable.delete,
        color = Color(0xFF0097A7),
        pasos = listOf(
            "Actualiza a la última versión de OpenTune",
            "Verifica que tienes suficiente espacio de almacenamiento",
            "Reinicia el dispositivo y vuelve a intentarlo",
            "Borra la caché de la aplicación y reiníciala",
            "Verifica que la aplicación tiene todos los permisos necesarios",
            "Si persiste, crea una copia de seguridad y reinstala la aplicación"
        ),
        tips = listOf(
            "Evita descargar o eliminar múltiples archivos simultáneamente",
            "En dispositivos con poca RAM, cierra otras aplicaciones antes de gestionar descargas"
        )
    ),
    Problema(
        id = "cierre_al_agregar_playlist",
        titulo = "La app se cierra al agregar música a una playlist",
        descripcion = "OpenTune se cierra inesperadamente al intentar añadir canciones a una lista de reproducción.",
        iconDrawable = R.drawable.playlist_add,
        color = Color(0xFF26A69A),
        pasos = listOf(
            "Actualiza a la última versión de OpenTune",
            "Reinicia la aplicación e intenta nuevamente",
            "Crea una nueva playlist e intenta añadir canciones a esta",
            "Verifica que la playlist no tiene demasiadas canciones (límite recomendado: 500)",
            "Borra la caché de la aplicación y reiníciala",
            "Como último recurso, desinstala y vuelve a instalar la aplicación"
        ),
        tips = listOf(
            "Crear múltiples playlists pequeñas puede mejorar el rendimiento",
            "Realiza copias de seguridad regulares de tus playlists importantes"
        )
    ),
    Problema(
        id = "error_radios",
        titulo = "Las canciones en las radios a veces no se reproducen o muestran 'error desconocido'",
        descripcion = "Problemas de reproducción específicamente en la función de radios de OpenTune.",
        iconDrawable = R.drawable.radio,
        color = Color(0xFF7CB342),
        pasos = listOf(
            "Verifica tu conexión a Internet",
            "Intenta reproducir una estación de radio diferente",
            "Actualiza a la última versión de OpenTune",
            "Reinicia la aplicación e intenta nuevamente",
            "Borra la caché de la aplicación",
            "Espera unas horas y vuelve a intentarlo, podría ser un problema temporal del servidor"
        ),
        tips = listOf(
            "Las radios dependen de servidores externos que pueden tener interrupciones",
            "Algunas radios pueden tener restricciones geográficas"
        )
    )
)

val preguntasFrecuentes = listOf(
    Problema(
        id = "como_descargar_musica",
        titulo = "¿Cómo descargar música?",
        descripcion = "Descarga tus canciones favoritas para escucharlas sin conexión.",
        iconDrawable = R.drawable.download,
        color = Color(0xFF7B1FA2),
        pasos = listOf(
            "Busca la canción que deseas descargar",
            "Pulsa el icono de descarga junto a la canción",
            "Espera a que la descarga finalice",
            "Accede a tu biblioteca para encontrar la canción descargada"
        ),
        tips = listOf(
            "Descarga música con Wi-Fi para ahorrar datos",
            "Configura la calidad de descarga en los ajustes"
        )
    ),
    Problema(
        id = "como_sincronizar_letras",
        titulo = "¿Cómo sincronizar letras?",
        descripcion = "Disfruta de las letras sincronizadas mientras escuchas tus canciones.",
        iconDrawable = R.drawable.lyrics,
        color = Color(0xFFFF9800),
        pasos = listOf(
            "Abre la canción que deseas reproducir",
            "Pulsa en la caratula de la canción",
            "Edita o añade letras manualmente",
            "Si están disponibles, se mostrarán automáticamente sincronizadas"
        ),
        tips = listOf(
            "No todas las canciones tienen letras disponibles",
            "Puedes desactivar las letras en la configuración"
        )
    ),
    Problema(
        id = "como_crear_playlist",
        titulo = "¿Cómo crear una playlist?",
        descripcion = "Organiza tu música creando listas de reproducción personalizadas.",
        iconDrawable = R.drawable.playlist_add,
        color = Color(0xFF00897B),
        pasos = listOf(
            "Ve a la sección 'Tu biblioteca'",
            "Pulsa en 'Crear playlist'",
            "Asigna un nombre a tu playlist",
            "Añade canciones buscándolas o desde tus favoritos",
            "Guarda los cambios"
        )
    ),
    Problema(
        id = "es_de_paga",
        titulo = "¿OpenTune es de paga?",
        descripcion = "Información sobre el modelo de negocio de OpenTune.",
        iconDrawable = R.drawable.attach_money,
        color = Color(0xFF4CAF50),
        pasos = listOf(
            "No, OpenTune es completamente gratuito",
            "No existen versiones premium o de paga",
            "Todas las funcionalidades están disponibles sin costo",
            "El proyecto se mantiene gracias a donaciones voluntarias",
            "No contiene publicidad ni compras dentro de la aplicación"
        )
    ),
    Problema(
        id = "es_seguro",
        titulo = "¿OpenTune es seguro?",
        descripcion = "Información sobre la seguridad de la aplicación.",
        iconDrawable = R.drawable.security,
        color = Color(0xFF3F51B5),
        pasos = listOf(
            "OpenTune es seguro y respeta tu privacidad",
            "El código fuente está disponible para revisión pública",
            "No recopila datos personales innecesarios",
            "Las conexiones con servidores se realizan de forma segura",
            "Se recomienda descargar la aplicación únicamente de fuentes oficiales",
            "El proyecto es mantenido por una comunidad activa que monitorea posibles vulnerabilidades"
        )
    ),
    Problema(
        id = "permisos_app",
        titulo = "¿Qué permisos pide OpenTune para funcionar y por qué?",
        descripcion = "Explicación de los permisos requeridos por la aplicación.",
        iconDrawable = R.drawable.lock_open,
        color = Color(0xFF607D8B),
        pasos = listOf(
            "Almacenamiento: Para guardar descargas y caché",
            "Internet: Para reproducir contenido en línea",
            "Modificar/eliminar tarjeta SD: Para gestionar descargas",
            "Ejecutarse al inicio: Para restaurar la reproducción tras reiniciar",
            "Control de vibración: Para retroalimentación táctil",
            "Impedir que el dispositivo entre en reposo: Para reproducción continua",
            "Todos los permisos son necesarios para funcionalidades específicas y no se usan para recopilar datos"
        )
    ),
    Problema(
        id = "funciona_sin_cuenta",
        titulo = "¿OpenTune funciona sin cuenta de Google?",
        descripcion = "Información sobre el uso sin cuenta de Google.",
        iconDrawable = R.drawable.person,
        color = Color(0xFFFF5722),
        pasos = listOf(
            "Sí, OpenTune puede utilizarse sin una cuenta de Google",
            "La mayoría de las funciones están disponibles sin iniciar sesión",
            "Sin cuenta, no podrás sincronizar tus playlists con YouTube Music",
            "Para acceder a tus playlists de YouTube Music, necesitarás iniciar sesión",
            "La reproducción y descarga de música funciona completamente sin cuenta"
        )
    ),
    Problema(
        id = "seguridad_cuenta_google",
        titulo = "¿Es seguro usar mi cuenta de Google en OpenTune?",
        descripcion = "Información sobre la seguridad de iniciar sesión con Google.",
        iconDrawable = R.drawable.verified_user,
        color = Color(0xFF673AB7),
        pasos = listOf(
            "OpenTune maneja las credenciales de forma segura",
            "Nunca se comparten tus credenciales con terceros",
            "La aplicación utiliza métodos de autenticación estándar de Google",
            "Las credenciales se almacenan de forma encriptada",
            "Puedes cerrar sesión en cualquier momento desde la configuración",
            "Si tienes preocupaciones, puedes usar la aplicación sin iniciar sesión"
        )
    ),
    Problema(
        id = "ban_cuenta_google",
        titulo = "¿Puedo ser baneado de Google por usar mi cuenta en OpenTune?",
        descripcion = "Información sobre posibles consecuencias en tu cuenta de Google.",
        iconDrawable = R.drawable.block,
        color = Color(0xFFE91E63),
        pasos = listOf(
            "OpenTune utiliza APIs públicas para acceder a los servicios",
            "No hay reportes confirmados de baneos por el simple uso de la aplicación",
            "Sin embargo, el uso excesivo o automatizado podría ser detectado",
            "Si te preocupa, considera usar una cuenta secundaria",
            "Evita realizar demasiadas descargas simultáneas o en muy poco tiempo",
            "Mantén actualizada la aplicación para proteger tu cuenta"
        ),
        tips = listOf(
            "Considera usar una cuenta separada si tienes preocupaciones",
            "Respeta los términos de servicio de las plataformas subyacentes"
        )
    ),
    Problema(
        id = "donde_reportar",
        titulo = "¿Dónde puedo hacer sugerencias o reportes de errores?",
        descripcion = "Canales oficiales para comunicarte con los desarrolladores.",
        iconDrawable = R.drawable.feedback,
        color = Color(0xFF795548),
        pasos = listOf(
            "En GitHub: Puedes crear 'issues' en el repositorio oficial",
            "En Discord: Hay un canal específico para reportes y sugerencias",
            "En Telegram: Grupo oficial de soporte",
            "Por correo electrónico: Contacto disponible en la web oficial",
            "Dentro de la app: Menú > Ajustes > Reportar problema",
            "Proporciona detalles específicos del problema, incluyendo capturas de pantalla y pasos para reproducirlo"
        )
    ),
    Problema(
        id = "sincronizar_playlists",
        titulo = "¿Puedo sincronizar mis playlists de YouTube Music?",
        descripcion = "Información sobre la sincronización de listas de reproducción.",
        iconDrawable = R.drawable.sync,
        color = Color(0xFFAB47BC),
        pasos = listOf(
            "Sí, puedes sincronizar tus playlists de YouTube Music",
            "Inicia sesión con tu cuenta de Google",
            "Ve a 'Tu biblioteca' > 'Playlists'",
            "Pulsa en 'Sincronizar playlists'",
            "Selecciona las playlists que deseas sincronizar",
            "Espera a que se complete el proceso de sincronización"
        ),
        tips = listOf(
            "La sincronización puede tardar más tiempo para playlists grandes",
            "Mantén activa la aplicación durante la sincronización"
        )
    ),
    Problema(
        id = "cambios_reflejados_ytmusic",
        titulo = "¿Los cambios que haga en las playlists se verán reflejados en YouTube Music?",
        descripcion = "Información sobre la sincronización bidireccional.",
        iconDrawable = R.drawable.compare_arrows,
        color = Color(0xFF009688),
        pasos = listOf(
            "Sí, los cambios se sincronizan en ambas direcciones",
            "Al añadir canciones a una playlist en OpenTune, aparecerán en YouTube Music",
            "Al eliminar canciones, también se eliminarán en YouTube Music",
            "Al crear nuevas playlists, se crearán también en YouTube Music",
            "La sincronización puede tardar unos minutos en completarse",
            "Asegúrate de tener conexión a Internet para que los cambios se sincronicen"
        )
    ),
    Problema(
        id = "apoyo_monetario",
        titulo = "¿Hay alguna forma de apoyar el proyecto de manera monetaria?",
        descripcion = "Información sobre cómo contribuir al desarrollo de OpenTune.",
        iconDrawable = R.drawable.done,
        color = Color(0xFFD32F2F),
        pasos = listOf(
            "Sí, puedes hacer donaciones a través de varios métodos",
            "PayPal: Enlace disponible en la web oficial",
            "GitHub Sponsors: Apoya directamente a los desarrolladores",
            "Patreon: Suscripción mensual con beneficios especiales",
            "Cryptocurrency: Direcciones disponibles en el repositorio",
            "Dentro de la app: Menú > Ajustes > Apoyar el proyecto"
        ),
        tips = listOf(
            "Incluso pequeñas donaciones ayudan a mantener el proyecto",
            "También puedes contribuir con código, traducciones o diseño"
        )
    ),
//    Problema(
//        id = "archivos_locales",
//        titulo = "¿Puedo reproducir archivos locales en OpenTune?",
//        descripcion = "Información sobre la reproducción de música almacenada en el dispositivo.",
//        iconDrawable = R.drawable.folder_music,
//        color = Color(0xFF388E3C),
//        pasos = listOf(
//            "Sí, OpenTune puede reproducir archivos locales",
//            "Ve a 'Tu biblioteca' > 'Archivos locales'",
//            "Configura las carpetas a escanear en Ajustes > Archivos locales",
//            "Los formatos soportados incluyen MP3, FLAC, WAV, OGG y más",
//            "Puedes crear playlists mezclando archivos locales y contenido en línea",
//            "Las estadísticas de reproducción se mantienen para archivos locales"
//        ),
//        tips = listOf(
//            "Organiza tus archivos en carpetas para una mejor experiencia",
//            "Los archivos con metadatos correctos se mostrarán con carátulas y detalles"
//        )
//    ),
//    Problema(
//        id = "descargar_musica",
//        titulo = "¿Puedo descargar música?",
//        descripcion = "Información sobre la descarga de música para escuchar sin conexión.",
//        iconDrawable = R.drawable.file_download,
//        color = Color(0xFF1976D2),
//        pasos = listOf(
//            "Sí, puedes descargar música para escuchar sin conexión",
//            "Busca la canción que deseas descargar",
//            "Pulsa el icono de descarga junto a la canción o en la pantalla de reproducción",
//            "Selecciona la calidad de descarga (si se muestra esta opción)",
//            "Espera a que la descarga se complete",
//            "Accede a 'Tu biblioteca' > 'Descargas' para encontrar el contenido descargado"
//        ),
//        tips = listOf(
//            "Utiliza Wi-Fi para descargas grandes para evitar consumo de datos",
//            "Puedes descargar playlists completas manteniendo pulsada la playlist"
//        )
//    ),
//    Problema(
//        id = "lanzamiento_playstore",
//        titulo = "¿Se planea un lanzamiento en Google Play Store en algún momento?",
//        descripcion = "Información sobre la disponibilidad de OpenTune en la tienda oficial de Google.",
//        iconDrawable = R.drawable.google_play,
//        color = Color(0xFF43A047),
//        pasos = listOf(
//            "OpenTune no está disponible en Google Play Store actualmente",
//            "Debido a las políticas de Google, es poco probable que se apruebe su publicación",
//            "La aplicación seguirá distribuyéndose a través de los canales oficiales",
//            "Las actualizaciones deben instalarse manualmente o a través de aplicaciones de terceros",
//            "Esta situación permite mantener todas las funcionalidades sin restricciones",
//            "Sigue los canales oficiales para estar al tanto de novedades sobre distribución"
//        )
//    ),
//    Problema(
//        id = "version_pc",
//        titulo = "¿Hay versión de PC?",
//        descripcion = "Información sobre la disponibilidad de OpenTune para computadoras.",
//        iconDrawable = R.drawable.laptop,
//        color = Color(0xFF5E35B1),
//        pasos = listOf(
//            "Actualmente no existe una versión oficial para PC",
//            "Puedes usar emuladores de Android como BlueStacks para ejecutar OpenTune en PC",
//            "Existen alternativas de código abierto similares para PC que podrías considerar",
//            "El desarrollo se centra actualmente en la versión para Android",
//            "Sigue los canales oficiales para actualizaciones sobre posibles versiones para PC"
//        ),
//        tips = listOf(
//            "Al usar emuladores, asegúrate de descargar OpenTune desde fuentes oficiales",
//            "Algunas funciones pueden tener limitaciones en emuladores"
//        )
//    ),
//    Problema(
//        id = "soporte_android_auto",
//        titulo = "¿Tiene soporte para Android Auto?",
//        descripcion = "Información sobre la compatibilidad con Android Auto.",
//        iconDrawable = R.drawable.directions_car,
//        color = Color(0xFF00BCD4),
//        pasos = listOf(
//            "Sí, OpenTune es compatible con Android Auto",
//            "Asegúrate de tener la última versión de OpenTune instalada",
//            "Conecta tu teléfono al sistema de Android Auto de tu vehículo",
//            "OpenTune debería aparecer entre las aplicaciones de música disponibles",
//            "Si no aparece, verifica en Ajustes > Aplicaciones > OpenTune > Android Auto",
//            "Algunas funciones pueden estar limitadas mientras usas Android Auto por seguridad"
//        ),
//        tips = listOf(
//            "Descarga tu música favorita para usar cuando no tengas conexión estable",
//            "Las playlists son la forma más fácil de navegar mientras conduces"
//        )
//    ),
//    Problema(
//        id = "requisitos_minimos",
//        titulo = "¿Cuáles son los requisitos mínimos para ejecutar OpenTune?",
//        descripcion = "Información sobre los requerimientos de sistema para usar la aplicación.",
//        iconDrawable = R.drawable.memory,
//        color = Color(0xFFFF4081),
//        pasos = listOf(
//            "Android 5.0 (Lollipop) o superior",
//            "Al menos 2GB de RAM (4GB recomendado para mejor rendimiento)",
//            "Mínimo 50MB de espacio de almacenamiento para la aplicación",
//            "Espacio adicional para descargas (varía según uso)",
//            "Conexión a Internet para streaming (Wi-Fi recomendado)",
//            "No requiere root ni permisos especiales"
//        )
//    ),
//    Problema(
//        id = "sitios_oficiales",
//        titulo = "¿Cuáles son los sitios donde puedo obtener OpenTune de manera oficial?",
//        descripcion = "Información sobre las fuentes oficiales para descargar la aplicación.",
//        iconDrawable = R.drawable.verified,
//        color = Color(0xFF1565C0),
//        pasos = listOf(
//            "GitHub: Repositorio oficial del proyecto",
//            "Sitio web oficial: Descargas verificadas",
//            "Canal de Telegram oficial",
//            "Discord oficial del proyecto",
//            "No descargues la aplicación de tiendas de aplicaciones no oficiales o sitios desconocidos",
//            "Verifica siempre la firma digital de la aplicación"
//        ),
//        tips = listOf(
//            "Siempre verifica que la URL sea la correcta antes de descargar",
//            "Configura notificaciones en GitHub o Telegram para nuevas versiones"
//        )
//    ),
//    Problema(
//        id = "crear_copia_seguridad",
//        titulo = "¿Cómo creo una copia de seguridad?",
//        descripcion = "Información sobre cómo respaldar tus datos en OpenTune.",
//        iconDrawable = R.drawable.backup,
//        color = Color(0xFF9C27B0),
//        pasos = listOf(
//            "Ve a Ajustes > Copia de seguridad",
//            "Selecciona 'Crear copia de seguridad'",
//            "Elige qué datos quieres incluir (playlists, descargas, configuración, etc.)",
//            "Selecciona la ubicación donde guardar el archivo de respaldo",
//            "Espera a que se complete el proceso",
//            "Guarda el archivo en un lugar seguro (nube, otro dispositivo, etc.)"
//        ),
//        tips = listOf(
//            "Realiza copias de seguridad regularmente, especialmente antes de actualizar",
//            "Las copias de seguridad de playlists no incluyen los archivos de música"
//        )
//    ),
//    Problema(
//        id = "compatibilidad_equalizadores",
//        titulo = "¿La app es compatible con equalizadores como Poweramp, Wavelet o Viper4Android?",
//        descripcion = "Información sobre la compatibilidad con aplicaciones de ecualización de audio.",
//        iconDrawable = R.drawable.equalizer,
//        color = Color(0xFFFF6F00),
//        pasos = listOf(
//            "Sí, OpenTune es compatible con la mayoría de equalizadores externos",
//            "Para Poweramp Equalizer: Habilita 'Procesar audio de otras apps' en sus ajustes",
//            "Para Wavelet: Asegúrate de que OpenTune esté en su lista de aplicaciones",
//            "Para Viper4Android: Requiere dispositivo rooteado y configuración específica",
//            "En OpenTune, ve a Ajustes > Audio > Ecualización externa",
//            "Activa la opción 'Permitir procesamiento de audio externo'"
//        ),
//        tips = listOf(
//            "Algunos equalizadores pueden aumentar el consumo de batería",
//            "Si experimentas cortes o latencia, prueba con configuraciones menos intensivas"
//        )
//    ),
//    Problema(
//        id = "como_crear_playlist",
//        titulo = "¿Cómo creo una playlist?",
//        descripcion = "Instrucciones para crear listas de reproducción personalizadas.",
//        iconDrawable = R.drawable.playlist_add,
//        color = Color(0xFF00897B),
//        pasos = listOf(
//            "Ve a la sección 'Tu biblioteca'",
//            "Pulsa en 'Playlists'",
//            "Selecciona 'Crear nueva playlist'",
//            "Asigna un nombre a tu playlist",
//            "Opcionalmente, añade una descripción e imagen de portada",
//            "Guarda la playlist",
//            "Ahora puedes añadir canciones a tu nueva playlist"
//        ),
//        tips = listOf(
//            "Puedes crear playlists temáticas para diferentes ocasiones",
//            "Las playlists se pueden sincronizar con tu cuenta de YouTube Music"
//        )
//    ),
//    Problema(
//        id = "como_anadir_canciones_playlist",
//        titulo = "¿Cómo añado canciones al playlist?",
//        descripcion = "Instrucciones para agregar música a tus listas de reproducción.",
//        iconDrawable = R.drawable.playlist_add_check,
//        color = Color(0xFF00796B),
//        pasos = listOf(
//            "Método 1 - Desde la canción:",
//            "- Mantén pulsada la canción que quieres añadir",
//            "- Selecciona 'Añadir a playlist'",
//            "- Elige la playlist destino",
//            "Método 2 - Desde la playlist:",
//            "- Abre la playlist donde quieras añadir canciones",
//            "- Pulsa el botón '+' o 'Añadir canciones'",
//            "- Busca y selecciona las canciones a añadir"
//        ),
//        tips = listOf(
//            "Puedes añadir varias canciones a la vez seleccionándolas con pulsación larga",
//            "También puedes añadir canciones directamente desde los resultados de búsqueda"
//        )
//    ),
//    Problema(
//        id = "como_ver_letras",
//        titulo = "¿Cómo veo las letras?",
//        descripcion = "Instrucciones para visualizar letras de canciones mientras escuchas música.",
//        iconDrawable = R.drawable.lyrics,
//        color = Color(0xFFFF9800),
//        pasos = listOf(
//            "Reproduce la canción deseada",
//            "En la pantalla de reproducción, desliza hacia arriba o pulsa el botón de letras",
//            "Las letras se mostrarán sincronizadas con la música si están disponibles",
//            "Si no hay letras disponibles, puedes añadirlas manualmente",
//            "Para añadir letras: Pulsa en 'Añadir letras' y pega el texto",
//            "Para editar letras existentes: Pulsa en el ícono de edición"
//        ),
//        tips = listOf(
//            "Puedes activar el modo automático de letras en Ajustes > Reproductor",
//            "La sincronización de letras puede variar dependiendo de la disponibilidad"
//        )
//    )
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProblemSolverScreen(navController: NavController) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var problemaSeleccionado by remember { mutableStateOf<Problema?>(null) }
    var showSearchBar by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Problema>>(emptyList()) }
    val uriHandler = LocalUriHandler.current

    val allProblems = remember { problemasComunes + preguntasFrecuentes }

    // Efecto para buscar
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            searchResults = allProblems.filter {
                it.titulo.contains(searchQuery, ignoreCase = true) ||
                        it.descripcion.contains(searchQuery, ignoreCase = true)
            }
        } else {
            searchResults = emptyList()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(320.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerContentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // Header con logo y título
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.support_agent),
                            contentDescription = null,
                            modifier = Modifier
                                .size(32.dp)
                                .align(Alignment.Center),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Centro de Ayuda",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "OpenTune",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.height(8.dp))

                // Sección de problemas comunes
                CategoryHeader(
                    iconDrawable = R.drawable.bug_report,
                    title = "Problemas Comunes"
                )

                LazyColumn(
                    modifier = Modifier.height(180.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(problemasComunes) { problema ->
                        ProblemaDrawerItem(
                            problema = problema,
                            isSelected = problemaSeleccionado?.id == problema.id,
                            onClick = {
                                problemaSeleccionado = problema
                                scope.launch { drawerState.close() }
                            }
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.height(8.dp))

                // Sección de preguntas frecuentes
                CategoryHeader(
                    iconDrawable = R.drawable.help,
                    title = "Preguntas Frecuentes"
                )

                LazyColumn(
                    modifier = Modifier.height(220.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(preguntasFrecuentes) { pregunta ->
                        ProblemaDrawerItem(
                            problema = pregunta,
                            isSelected = problemaSeleccionado?.id == pregunta.id,
                            onClick = {
                                problemaSeleccionado = pregunta
                                scope.launch { drawerState.close() }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Footer con opciones adicionales
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                ListItem(
                    headlineContent = { Text("Contactar Soporte") },
                    leadingContent = {
                        Image(
                            painter = painterResource(id = R.drawable.email),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                        )
                    },
                    modifier = Modifier.clickable {
                        uriHandler.openUri("mailto:cervantesarturo254@gmail.com") // Cambia el enlace aquí
                    }
                )

                ListItem(
                    headlineContent = { Text("Documentación") },
                    leadingContent = {
                        Image(
                            painter = painterResource(id = R.drawable.menu_book),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                        )
                    },
                    modifier = Modifier.clickable {
                        uriHandler.openUri("https://opentune.gitbook.io/undefined") // Cambia el enlace aquí
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (showSearchBar) {
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onSearch = { showSearchBar = false },
                        active = true,
                        onActiveChange = { showSearchBar = it },
                        placeholder = { Text("Buscar solución...") },
                        leadingIcon = {
                            IconButton(onClick = { showSearchBar = false }) {
                                Image(
                                    painter = painterResource(id = R.drawable.arrow_back),
                                    contentDescription = "Atrás"
                                )
                            }
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Image(
                                        painter = painterResource(id = R.drawable.close),
                                        contentDescription = "Limpiar"
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LazyColumn {
                            if (searchResults.isNotEmpty()) {
                                items(searchResults) { resultado ->
                                    ListItem(
                                        headlineContent = { Text(resultado.titulo) },
                                        supportingContent = { Text(resultado.descripcion) },
                                        leadingContent = {
                                            Image(
                                                painter = painterResource(id = resultado.iconDrawable),
                                                contentDescription = null,
                                                colorFilter = ColorFilter.tint(resultado.color)
                                            )
                                        },
                                        modifier = Modifier.clickable {
                                            problemaSeleccionado = resultado
                                            showSearchBar = false
                                        }
                                    )
                                }
                            } else if (searchQuery.isNotEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("No se encontraron resultados")
                                    }
                                }
                            }
                        }
                    }
                } else {
                    LargeTopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = "Centro de Ayuda",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Text(
                                    text = "Encuentra soluciones rápidas",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        navigationIcon = {
                            Row {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Image(
                                        painter = painterResource(id = R.drawable.arrow_back),
                                        contentDescription = "Atrás",
                                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                                    )
                                }
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Image(
                                        painter = painterResource(id = R.drawable.menu),
                                        contentDescription = "Menú",
                                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                                    )
                                }
                            }
                        },
                        actions = {
                            IconButton(onClick = { showSearchBar = true }) {
                                Image(
                                    painter = painterResource(id = R.drawable.search),
                                    contentDescription = "Buscar",
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                problemaSeleccionado?.let {
                    DetalleProblema(it)
                } ?: WelcomeContent {
                    scope.launch { drawerState.open() }
                }
            }
        }
    }
}

@Composable
fun CategoryHeader(iconDrawable: Int, title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = iconDrawable),
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ProblemaDrawerItem(
    problema: Problema,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = problema.iconDrawable),
                contentDescription = null,
                colorFilter = ColorFilter.tint(problema.color),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = problema.titulo,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun WelcomeContent(onOpenDrawer: () -> Unit) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Ilustración
        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.support_agent),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
                modifier = Modifier.size(100.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "¿En qué podemos ayudarte?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Selecciona una opción en el menú para encontrar soluciones a problemas comunes o respuestas a preguntas frecuentes.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Tarjetas de categorías
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CategoryCard(
                iconDrawable = R.drawable.bug_report,
                title = "Problemas",
                onClick = onOpenDrawer
            )
            CategoryCard(
                iconDrawable = R.drawable.help,
                title = "Preguntas",
                onClick = onOpenDrawer
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Soporte directo
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "¿Necesitas ayuda personalizada?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Contacta directamente con nuestro equipo de soporte",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                FilledTonalButton(
                    onClick = {
                        uriHandler.openUri("https://opentune.netlify.app/from") // O cambia el link por Telegram, etc.
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.email),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Contactar Soporte")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun CategoryCard(
    iconDrawable: Int,
    title: String,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .width(150.dp)
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = iconDrawable),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DetalleProblema(problema: Problema) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cabecera
        item {
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    problema.color.copy(alpha = 0.7f),
                                    MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Top // Cambiado a Top para mejor alineación
                    ) {
                        Image(
                            painter = painterResource(id = problema.iconDrawable),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                            modifier = Modifier.size(64.dp)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(
                            modifier = Modifier.weight(1f) // Añadido para que la columna ocupe el espacio disponible
                        ) {
                            Text(
                                text = problema.titulo,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2, // Limitar a 2 líneas
                                overflow = TextOverflow.Ellipsis // Añadir elipsis si se corta
                            )

                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + expandVertically()
                            ) {
                                Text(
                                    text = problema.descripcion,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    maxLines = 3, // Limitar a 3 líneas para la descripción
                                    overflow = TextOverflow.Ellipsis // Añadir elipsis si se corta
                                )
                            }
                        }
                    }
                }
            }
        }

        // Pasos para resolver
        if (problema.pasos.isNotEmpty()) {
            item {
                Text(
                    text = "Pasos para resolver",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            items(problema.pasos.size) { index ->
                StepItem(
                    step = index + 1,
                    description = problema.pasos[index],
                    color = problema.color
                )
            }
        }

        // Tips adicionales
        if (problema.tips.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Consejos útiles",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(problema.tips) { tip ->
                TipItem(tip = tip)
            }
        }

        // Imágenes si existen
        if (problema.imagenes.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Referencias visuales",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(problema.imagenes) { imagenRes ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Image(
                        painter = painterResource(id = imagenRes),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        // Footer con botones de acción
        item {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { /* Acción */ },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.share),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Compartir")
                }

                FilledTonalButton(
                    onClick = { /* Acción */ },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = problema.color.copy(alpha = 0.2f),
                        contentColor = problema.color
                    )
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.thumb_up),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(problema.color)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("¿Te fue útil?")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun StepItem(step: Int, description: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = step.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun TipItem(tip: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Image(
            painter = painterResource(id = R.drawable.lightbulb),
            contentDescription = null,
            colorFilter = ColorFilter.tint(Color(0xFFFFC107)),
            modifier = Modifier
                .padding(top = 2.dp)
                .size(20.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = tip,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}