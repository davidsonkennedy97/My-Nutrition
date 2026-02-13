package com.example.nutriplan.core

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Cria um Context "localizado" (pt ou en) para ler strings do resources
 * mesmo que o celular esteja em outro idioma.
 */
fun contextWithLocale(base: Context, languageCode: String): Context {
    val locale = Locale(languageCode)
    Locale.setDefault(locale)

    val config = Configuration(base.resources.configuration)
    config.setLocale(locale)

    return base.createConfigurationContext(config)
}
