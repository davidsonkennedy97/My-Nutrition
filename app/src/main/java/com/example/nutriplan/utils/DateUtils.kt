package com.example.nutriplan.utils

import java.text.SimpleDateFormat
import java.util.*

fun calcularIdade(dataNascimento: String): Int {
    if (dataNascimento.isEmpty()) return 0

    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dataNasc = sdf.parse(dataNascimento) ?: return 0

        val hoje = Calendar.getInstance()
        val nascimento = Calendar.getInstance()
        nascimento.time = dataNasc

        var idade = hoje.get(Calendar.YEAR) - nascimento.get(Calendar.YEAR)

        if (hoje.get(Calendar.DAY_OF_YEAR) < nascimento.get(Calendar.DAY_OF_YEAR)) {
            idade--
        }

        idade
    } catch (e: Exception) {
        0
    }
}