package com.example.nutriplan.utils

import kotlin.math.log10
import kotlin.math.pow

object CalculosMedidas {

    fun calcularIMC(peso: Float, alturaCm: Float): Float {
        val alturaMetros = alturaCm / 100f
        return peso / (alturaMetros * alturaMetros)
    }

    fun classificarIMC(imc: Float): String {
        return when {
            imc < 18.5 -> "Abaixo do peso"
            imc < 25.0 -> "Peso normal"
            imc < 30.0 -> "Sobrepeso"
            imc < 35.0 -> "Obesidade Grau I"
            imc < 40.0 -> "Obesidade Grau II"
            else -> "Obesidade Grau III"
        }
    }

    fun calcularTMB_MifflinStJeor(peso: Float, alturaCm: Float, idade: Int, sexo: String): Float {
        return if (sexo.equals("Masculino", ignoreCase = true)) {
            (10 * peso) + (6.25f * alturaCm) - (5 * idade) + 5
        } else {
            (10 * peso) + (6.25f * alturaCm) - (5 * idade) - 161
        }
    }

    fun calcularTMB_Cunningham(mlgKg: Float): Float {
        return 500 + (22 * mlgKg)
    }

    fun calcularTMB_Tinsley(peso: Float, alturaCm: Float, idade: Int, sexo: String, pgc: Float?): Float {
        if (pgc == null) {
            return calcularTMB_MifflinStJeor(peso, alturaCm, idade, sexo)
        }
        val mlg = peso * (1 - pgc / 100f)
        return 370f + (21.6f * mlg)
    }

    fun calcularGET(tmb: Float, fa: Float): Float {
        return tmb * fa
    }

    fun obterValorFA(nivel: String): Float {
        return when (nivel) {
            "Sedentário" -> 1.1f
            "Pouco Ativo" -> 1.35f
            "Ativo" -> 1.45f
            "Muito Ativo" -> 1.6f
            "Atleta" -> 1.9f
            else -> 1.1f
        }
    }

    fun calcularPGC_JacksonPollock3(
        pregaPeitoral: Float?,
        pregaAbdomen: Float?,
        pregaCoxa: Float?,
        idade: Int,
        sexo: String
    ): Float? {
        if (pregaPeitoral == null || pregaAbdomen == null || pregaCoxa == null) return null
        val soma = pregaPeitoral + pregaAbdomen + pregaCoxa
        val dc = if (sexo.equals("Masculino", ignoreCase = true)) {
            1.10938 - (0.0008267 * soma) + (0.0000016 * soma.pow(2)) - (0.0002574 * idade)
        } else {
            1.0994921 - (0.0009929 * soma) + (0.0000023 * soma.pow(2)) - (0.0001392 * idade)
        }
        return ((495.0 / dc) - 450.0).toFloat()
    }

    fun calcularPGC_JacksonPollock7(
        pregaPeitoral: Float?,
        pregaAxilarMedia: Float?,
        pregaTriceps: Float?,
        pregaSubescapular: Float?,
        pregaAbdomen: Float?,
        pregaSuprailiaca: Float?,
        pregaCoxa: Float?,
        idade: Int,
        sexo: String
    ): Float? {
        if (pregaPeitoral == null || pregaAxilarMedia == null || pregaTriceps == null ||
            pregaSubescapular == null || pregaAbdomen == null || pregaSuprailiaca == null ||
            pregaCoxa == null) return null
        val soma = pregaPeitoral + pregaAxilarMedia + pregaTriceps + pregaSubescapular +
                pregaAbdomen + pregaSuprailiaca + pregaCoxa
        val dc = if (sexo.equals("Masculino", ignoreCase = true)) {
            1.112 - (0.00043499 * soma) + (0.00000055 * soma.pow(2)) - (0.00028826 * idade)
        } else {
            1.097 - (0.00046971 * soma) + (0.00000056 * soma.pow(2)) - (0.00012828 * idade)
        }
        return ((495.0 / dc) - 450.0).toFloat()
    }

    fun calcularPGC_DurninWomersley(
        pregaBiceps: Float?,
        pregaTriceps: Float?,
        pregaSubescapular: Float?,
        pregaSuprailiaca: Float?,
        idade: Int,
        sexo: String
    ): Float? {
        if (pregaBiceps == null || pregaTriceps == null || pregaSubescapular == null ||
            pregaSuprailiaca == null) return null
        val soma = pregaBiceps + pregaTriceps + pregaSubescapular + pregaSuprailiaca
        val L = log10(soma.toDouble()).toFloat()
        val dc = if (sexo.equals("Masculino", ignoreCase = true)) {
            when {
                idade < 17 -> 1.1533f - (0.0643f * L)
                idade < 20 -> 1.1620f - (0.0630f * L)
                idade < 30 -> 1.1631f - (0.0632f * L)
                idade < 40 -> 1.1422f - (0.0544f * L)
                idade < 50 -> 1.1620f - (0.0700f * L)
                else -> 1.1715f - (0.0779f * L)
            }
        } else {
            when {
                idade < 17 -> 1.1369f - (0.0598f * L)
                idade < 20 -> 1.1549f - (0.0678f * L)
                idade < 30 -> 1.1599f - (0.0717f * L)
                idade < 40 -> 1.1423f - (0.0632f * L)
                idade < 50 -> 1.1333f - (0.0612f * L)
                else -> 1.1339f - (0.0645f * L)
            }
        }
        return ((495.0 / dc) - 450.0).toFloat()
    }

    fun calcularPGC_Guedes3(
        pregaPeitoral: Float?,
        pregaAbdomen: Float?,
        pregaCoxa: Float?,
        pregaTriceps: Float?,
        pregaSuprailiaca: Float?,
        sexo: String
    ): Float? {
        val soma = if (sexo.equals("Masculino", ignoreCase = true)) {
            if (pregaPeitoral == null || pregaAbdomen == null || pregaCoxa == null) return null
            pregaPeitoral + pregaAbdomen + pregaCoxa
        } else {
            if (pregaTriceps == null || pregaSuprailiaca == null || pregaCoxa == null) return null
            pregaTriceps + pregaSuprailiaca + pregaCoxa
        }
        val L = log10(soma.toDouble()).toFloat()
        val densidade = if (sexo.equals("Masculino", ignoreCase = true)) {
            1.17136f - (0.06706f * L)
        } else {
            1.16650f - (0.07063f * L)
        }
        return ((495.0 / densidade) - 450.0).toFloat()
    }

    fun classificarPGC(pgc: Float, sexo: String, idade: Int): String {
        return if (sexo.equals("Masculino", ignoreCase = true)) {
            when {
                idade < 30 -> when {
                    pgc < 10 -> "Excelente"
                    pgc < 14 -> "Bom"
                    pgc < 20 -> "Médio"
                    pgc < 25 -> "Ruim"
                    else -> "Muito Ruim"
                }
                idade < 40 -> when {
                    pgc < 12 -> "Excelente"
                    pgc < 16 -> "Bom"
                    pgc < 22 -> "Médio"
                    pgc < 27 -> "Ruim"
                    else -> "Muito Ruim"
                }
                idade < 50 -> when {
                    pgc < 14 -> "Excelente"
                    pgc < 18 -> "Bom"
                    pgc < 24 -> "Médio"
                    pgc < 29 -> "Ruim"
                    else -> "Muito Ruim"
                }
                idade < 60 -> when {
                    pgc < 16 -> "Excelente"
                    pgc < 20 -> "Bom"
                    pgc < 26 -> "Médio"
                    pgc < 31 -> "Ruim"
                    else -> "Muito Ruim"
                }
                else -> when {
                    pgc < 18 -> "Excelente"
                    pgc < 22 -> "Bom"
                    pgc < 28 -> "Médio"
                    pgc < 33 -> "Ruim"
                    else -> "Muito Ruim"
                }
            }
        } else {
            when {
                idade < 30 -> when {
                    pgc < 16 -> "Excelente"
                    pgc < 20 -> "Bom"
                    pgc < 28 -> "Médio"
                    pgc < 35 -> "Ruim"
                    else -> "Muito Ruim"
                }
                idade < 40 -> when {
                    pgc < 17 -> "Excelente"
                    pgc < 21 -> "Bom"
                    pgc < 29 -> "Médio"
                    pgc < 36 -> "Ruim"
                    else -> "Muito Ruim"
                }
                idade < 50 -> when {
                    pgc < 18 -> "Excelente"
                    pgc < 23 -> "Bom"
                    pgc < 31 -> "Médio"
                    pgc < 38 -> "Ruim"
                    else -> "Muito Ruim"
                }
                idade < 60 -> when {
                    pgc < 19 -> "Excelente"
                    pgc < 25 -> "Bom"
                    pgc < 33 -> "Médio"
                    pgc < 40 -> "Ruim"
                    else -> "Muito Ruim"
                }
                else -> when {
                    pgc < 20 -> "Excelente"
                    pgc < 27 -> "Bom"
                    pgc < 35 -> "Médio"
                    pgc < 42 -> "Ruim"
                    else -> "Muito Ruim"
                }
            }
        }
    }

    fun calcularMLG(peso: Float, pgc: Float): Float {
        return peso * (1 - pgc / 100f)
    }

    fun calcularMassaMuscular(peso: Float, altura: Float, idade: Int): Float {
        val alturaMetros = altura / 100f
        val alturaQuadrado = alturaMetros * alturaMetros
        return (0.566f * alturaQuadrado) - (0.098f * idade) + (0.226f * peso) - 1.28f
    }

    fun calcularMassaGordura(peso: Float, pgc: Float): Float {
        return (peso * pgc) / 100f
    }

    fun calcularMassaOssea(altura: Float): Float {
        val alturaMetros = altura / 100f
        return alturaMetros * alturaMetros * 1.2f
    }

    fun calcularMassaResidual(peso: Float): Float {
        return peso * 0.241f
    }

    fun calcularRCQ(cintura: Float?, quadril: Float?): Float? {
        if (cintura == null || quadril == null || quadril == 0f) return null
        return cintura / quadril
    }

    fun classificarRCQ(rcq: Float, sexo: String): String {
        return if (sexo.equals("Masculino", ignoreCase = true)) {
            when {
                rcq < 0.83 -> "Baixo"
                rcq < 0.88 -> "Moderado"
                rcq < 0.95 -> "Alto"
                else -> "Muito Alto"
            }
        } else {
            when {
                rcq < 0.72 -> "Baixo"
                rcq < 0.75 -> "Moderado"
                rcq < 0.82 -> "Alto"
                else -> "Muito Alto"
            }
        }
    }

    fun calcularFaixaPesoIdeal(alturaCm: Float): Pair<Float, Float> {
        val alturaMetros = alturaCm / 100f
        val pesoMin = 18.5f * (alturaMetros * alturaMetros)
        val pesoMax = 24.9f * (alturaMetros * alturaMetros)
        return Pair(pesoMin, pesoMax)
    }
}