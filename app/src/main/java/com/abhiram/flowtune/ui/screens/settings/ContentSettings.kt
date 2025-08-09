package com.abhiram.flowtune.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.toLowerCase
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.navigation.NavController
import com.abhiram.flowtune.LocalPlayerAwareWindowInsets
import com.abhiram.flowtune.NotificationPermissionPreference
import com.abhiram.flowtune.R
import com.abhiram.flowtune.constants.CountryCodeToName
import com.abhiram.flowtune.constants.LanguageCodeToName
import com.abhiram.flowtune.constants.PreferredLyricsProvider
import com.abhiram.flowtune.constants.QuickPicks
import com.abhiram.flowtune.constants.SYSTEM_DEFAULT
import com.abhiram.flowtune.ui.component.EditTextPreference
import com.abhiram.flowtune.ui.component.IconButton
import com.abhiram.flowtune.ui.component.LanguagePreference
import com.abhiram.flowtune.ui.component.ListPreference
import com.abhiram.flowtune.ui.component.LocaleManager
import com.abhiram.flowtune.ui.component.PreferenceGroupTitle
import com.abhiram.flowtune.ui.component.SliderPreference
import com.abhiram.flowtune.ui.component.SwitchPreference
import com.abhiram.flowtune.ui.utils.backToMain
import com.abhiram.flowtune.utils.rememberEnumPreference
import com.abhiram.flowtune.utils.rememberPreference
import java.net.Proxy

object PreferenceKeys {
    val ContentLanguageKey = stringPreferencesKey("content_language")
    val ContentCountryKey = stringPreferencesKey("content_country")
    val HideExplicitKey = booleanPreferencesKey("hide_explicit")
    val ProxyEnabledKey = booleanPreferencesKey("proxy_enabled")
    val ProxyTypeKey = stringPreferencesKey("proxy_type")
    val ProxyUrlKey = stringPreferencesKey("proxy_url")
    val TopSizeKey = stringPreferencesKey("top_size")
    val HistoryDurationKey = floatPreferencesKey("history_duration")
    val QuickPicksKey = stringPreferencesKey("quick_picks")
    val EnableKugouKey = booleanPreferencesKey("enable_kugou")
    val EnableLrcLibKey = booleanPreferencesKey("enable_lrclib")
    val PreferredLyricsProviderKey = stringPreferencesKey("preferred_lyrics_provider")
    val AppLanguageKey = stringPreferencesKey("app_language")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val context = LocalContext.current
    val localeManager = remember { LocaleManager(context) }

    val (contentLanguage, onContentLanguageChange) = rememberPreference(
        key = PreferenceKeys.ContentLanguageKey,
        defaultValue = "system"
    )
    val (contentCountry, onContentCountryChange) = rememberPreference(
        key = PreferenceKeys.ContentCountryKey,
        defaultValue = "system"
    )
    val (hideExplicit, onHideExplicitChange) = rememberPreference(
        key = PreferenceKeys.HideExplicitKey,
        defaultValue = false
    )
    val (proxyEnabled, onProxyEnabledChange) = rememberPreference(
        key = PreferenceKeys.ProxyEnabledKey,
        defaultValue = false
    )
    val (proxyType, onProxyTypeChange) = rememberEnumPreference(
        key = PreferenceKeys.ProxyTypeKey,
        defaultValue = Proxy.Type.HTTP
    )
    val (proxyUrl, onProxyUrlChange) = rememberPreference(
        key = PreferenceKeys.ProxyUrlKey,
        defaultValue = "host:port"
    )
    val (lengthTop, onLengthTopChange) = rememberPreference(
        key = PreferenceKeys.TopSizeKey,
        defaultValue = "50"
    )
    val (historyDuration, onHistoryDurationChange) = rememberPreference(
        key = PreferenceKeys.HistoryDurationKey,
        defaultValue = 30f
    )
    val (quickPicks, onQuickPicksChange) = rememberEnumPreference(
        key = PreferenceKeys.QuickPicksKey,
        defaultValue = QuickPicks.QUICK_PICKS
    )
    val (enableKugou, onEnableKugouChange) = rememberPreference(
        key = PreferenceKeys.EnableKugouKey,
        defaultValue = true
    )
    val (enableLrclib, onEnableLrclibChange) = rememberPreference(
        key = PreferenceKeys.EnableLrcLibKey,
        defaultValue = true
    )
    val (preferredProvider, onPreferredProviderChange) = rememberEnumPreference(
        key = PreferenceKeys.PreferredLyricsProviderKey,
        defaultValue = PreferredLyricsProvider.LRCLIB
    )
    val (selectedLanguage, setSelectedLanguage) = rememberPreference(
        key = PreferenceKeys.AppLanguageKey,
        defaultValue = "en"
    )

    Column(
        Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
            .verticalScroll(rememberScrollState()),
    ) {
        // General settings
        PreferenceGroupTitle(title = stringResource(R.string.general))

        // Hide explicit content

        NotificationPermissionPreference()

        // Language settings
       //  LanguagePreference()

        // Lyrics settings
        PreferenceGroupTitle(title = stringResource(R.string.lyrics))
        SwitchPreference(
            title = { Text(stringResource(R.string.enable_lrclib)) },
            icon = { Icon(painterResource(R.drawable.lyrics), null) },
            checked = enableLrclib,
            onCheckedChange = onEnableLrclibChange,
        )
        SwitchPreference(
            title = { Text(stringResource(R.string.enable_kugou)) },
            icon = { Icon(painterResource(R.drawable.lyrics), null) },
            checked = enableKugou,
            onCheckedChange = onEnableKugouChange,
        )
        ListPreference(
            title = { Text(stringResource(R.string.set_first_lyrics_provider)) },
            icon = { Icon(painterResource(R.drawable.lyrics), null) },
            selectedValue = preferredProvider,
            values = listOf(PreferredLyricsProvider.KUGOU, PreferredLyricsProvider.LRCLIB),
            valueText = {
                it.name.toLowerCase(androidx.compose.ui.text.intl.Locale.current)
                    .capitalize(androidx.compose.ui.text.intl.Locale.current)
            },
            onValueSelected = onPreferredProviderChange,
        )

        // Misc settings
        PreferenceGroupTitle(title = stringResource(R.string.misc))
        EditTextPreference(
            title = { Text(stringResource(R.string.top_length)) },
            icon = { Icon(painterResource(R.drawable.trending_up), null) },
            value = lengthTop,
            isInputValid = { it.toIntOrNull()?.let { num -> num > 0 } == true },
            onValueChange = onLengthTopChange,
        )
        ListPreference(
            title = { Text(stringResource(R.string.set_quick_picks)) },
            icon = { Icon(painterResource(R.drawable.home_outlined), null) },
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

