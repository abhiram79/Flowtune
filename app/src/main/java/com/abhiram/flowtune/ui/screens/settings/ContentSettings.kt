package com.abhiram.flowtune.ui.screens.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.abhiram.flowtube.utils.parseCookieString
import com.abhiram.flowtune.LocalPlayerAwareWindowInsets
import com.abhiram.flowtune.R
import com.abhiram.flowtune.constants.AccountChannelHandleKey
import com.abhiram.flowtune.constants.AccountEmailKey
import com.abhiram.flowtune.constants.AccountNameKey
import com.abhiram.flowtune.constants.ChipSortTypeKey
import com.abhiram.flowtune.constants.ContentCountryKey
import com.abhiram.flowtune.constants.ContentLanguageKey
import com.abhiram.flowtune.constants.CountryCodeToName
import com.abhiram.flowtune.constants.HideExplicitKey
import com.abhiram.flowtune.constants.HistoryDuration
import com.abhiram.flowtune.constants.flowtubeCookieKey
import com.abhiram.flowtune.constants.LanguageCodeToName
import com.abhiram.flowtune.constants.LibraryFilter
import com.abhiram.flowtune.constants.ProxyEnabledKey
import com.abhiram.flowtune.constants.ProxyTypeKey
import com.abhiram.flowtune.constants.ProxyUrlKey
import com.abhiram.flowtune.constants.QuickPicks
import com.abhiram.flowtune.constants.QuickPicksKey
import com.abhiram.flowtune.constants.SYSTEM_DEFAULT
import com.abhiram.flowtune.constants.SimilarContent
import com.abhiram.flowtune.constants.TopSize
import com.abhiram.flowtune.ui.component.EditTextPreference
import com.abhiram.flowtune.ui.component.IconButton
import com.abhiram.flowtune.ui.component.ListPreference
import com.abhiram.flowtune.ui.component.PreferenceEntry
import com.abhiram.flowtune.ui.component.PreferenceGroupTitle
import com.abhiram.flowtune.ui.component.SliderPreference
import com.abhiram.flowtune.ui.component.SwitchPreference
import com.abhiram.flowtune.ui.utils.backToMain
import com.abhiram.flowtune.utils.rememberEnumPreference
import com.abhiram.flowtune.utils.rememberPreference
import java.net.Proxy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val context = LocalContext.current
    val accountName by rememberPreference(AccountNameKey, "")
    val accountEmail by rememberPreference(AccountEmailKey, "")
    val accountChannelHandle by rememberPreference(AccountChannelHandleKey, "")
    val flowtubeCookie by rememberPreference(flowtubeCookieKey, "")
    val isLoggedIn =
        remember(flowtubeCookie) {
            "SAPISID" in parseCookieString(flowtubeCookie)
        }
    val (contentLanguage, onContentLanguageChange) = rememberPreference(key = ContentLanguageKey, defaultValue = "system")
    val (contentCountry, onContentCountryChange) = rememberPreference(key = ContentCountryKey, defaultValue = "system")
    val (hideExplicit, onHideExplicitChange) = rememberPreference(key = HideExplicitKey, defaultValue = false)
    val (proxyEnabled, onProxyEnabledChange) = rememberPreference(key = ProxyEnabledKey, defaultValue = false)
    val (proxyType, onProxyTypeChange) = rememberEnumPreference(key = ProxyTypeKey, defaultValue = Proxy.Type.HTTP)
    val (proxyUrl, onProxyUrlChange) = rememberPreference(key = ProxyUrlKey, defaultValue = "host:port")
    val (lengthTop, onLengthTopChange) = rememberPreference(key = TopSize, defaultValue = "50")
    val (historyDuration, onHistoryDurationChange) = rememberPreference(key = HistoryDuration, defaultValue = 30f)
    val (defaultChip, onDefaultChipChange) = rememberEnumPreference(key = ChipSortTypeKey, defaultValue = LibraryFilter.LIBRARY)
    val (quickPicks, onQuickPicksChange) = rememberEnumPreference(key = QuickPicksKey, defaultValue = QuickPicks.QUICK_PICKS)
    val (similarContentEnabled, similarContentEnabledChange) = rememberPreference(key = SimilarContent, defaultValue = true)

    Column(
        Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Top)))

        PreferenceEntry(
            title = { Text(if (isLoggedIn) accountName else stringResource(R.string.login)) },
            description =
                if (isLoggedIn) {
                    accountEmail.takeIf { it.isNotEmpty() }
                        ?: accountChannelHandle.takeIf { it.isNotEmpty() }
                } else {
                    null
                },
            icon = { Icon(painterResource(R.drawable.person), null) },
            onClick = { navController.navigate("login") },
        )

        ListPreference(
            title = { Text(stringResource(R.string.content_language)) },
            icon = { Icon(painterResource(R.drawable.language), null) },
            selectedValue = contentLanguage,
            values = listOf(SYSTEM_DEFAULT) + LanguageCodeToName.keys.toList(),
            valueText = {
                LanguageCodeToName.getOrElse(it) {
                    stringResource(R.string.system_default)
                }
            },
            onValueSelected = onContentLanguageChange,
        )
        

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PreferenceEntry(
                title = { Text(stringResource(R.string.open_supported_links)) },
                description = stringResource(R.string.configure_supported_links),
                icon = { Icon(painterResource(R.drawable.add_link), null) },
                onClick = {
                    try {
                        context.startActivity(
                            Intent(Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS, Uri.parse("package:${context.packageName}")),
                        )
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(context, R.string.intent_supported_links_not_found, Toast.LENGTH_LONG).show()
                    }
                },
            )
        }


        PreferenceGroupTitle(
            title = stringResource(R.string.proxy),
        )

        

        AnimatedVisibility(proxyEnabled) {
            Column {
                ListPreference(
                    title = { Text(stringResource(R.string.proxy_type)) },
                    selectedValue = proxyType,
                    values = listOf(Proxy.Type.HTTP, Proxy.Type.SOCKS),
                    valueText = { it.name },
                    onValueSelected = onProxyTypeChange,
                )
                EditTextPreference(
                    title = { Text(stringResource(R.string.proxy_url)) },
                    value = proxyUrl,
                    onValueChange = onProxyUrlChange,
                )
            }
        }

        EditTextPreference(
            title = { Text(stringResource(R.string.top_length)) },
            value = lengthTop,
            isInputValid = {
                val number = it.toIntOrNull()
                number != null && it.isNotEmpty() && number > 0
            },
            onValueChange = onLengthTopChange,
        )

        

        ListPreference(
            title = { Text(stringResource(R.string.set_quick_picks)) },
            selectedValue = quickPicks,
            values = listOf(QuickPicks.QUICK_PICKS, QuickPicks.LAST_LISTEN),
            valueText = {
                when (it) {
                    QuickPicks.QUICK_PICKS -> stringResource(R.string.quick_picks)
                    QuickPicks.LAST_LISTEN -> stringResource(R.string.last_song_listened)
                }
            },
            onValueSelected = onQuickPicksChange,
        )

        

        SwitchPreference(
            title = { Text(stringResource(R.string.enable_similar_content)) },
            checked = similarContentEnabled,
            onCheckedChange = similarContentEnabledChange,
        )
    }

    TopAppBar(
        title = { Text(stringResource(R.string.content)) },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain,
            ) {
                Icon(
                    painterResource(R.drawable.arrow_back),
                    contentDescription = null,
                )
            }
        },
    )
}
