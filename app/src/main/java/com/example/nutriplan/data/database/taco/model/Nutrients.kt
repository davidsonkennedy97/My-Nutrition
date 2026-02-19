// app/src/main/java/com/example/nutriplan/data/taco/model/Nutrients.kt

package com.example.nutriplan.data.taco.model

import kotlin.math.pow
import kotlin.math.roundToInt

data class Nutrients(
    val proteinG: Float,
    val carbsG: Float,
    val fatG: Float,
    val fiberG: Float,
    val sodiumMg: Float,
    val potassiumMg: Float,
    val calciumMg: Float,
    val ironMg: Float,
    val vitaminCMg: Float
) {
    operator fun plus(other: Nutrients): Nutrients {
        return Nutrients(
            proteinG = this.proteinG + other.proteinG,
            carbsG = this.carbsG + other.carbsG,
            fatG = this.fatG + other.fatG,
            fiberG = this.fiberG + other.fiberG,
            sodiumMg = this.sodiumMg + other.sodiumMg,
            potassiumMg = this.potassiumMg + other.potassiumMg,
            calciumMg = this.calciumMg + other.calciumMg,
            ironMg = this.ironMg + other.ironMg,
            vitaminCMg = this.vitaminCMg + other.vitaminCMg
        )
    }

    operator fun times(factor: Float): Nutrients {
        return Nutrients(
            proteinG = this.proteinG * factor,
            carbsG = this.carbsG * factor,
            fatG = this.fatG * factor,
            fiberG = this.fiberG * factor,
            sodiumMg = this.sodiumMg * factor,
            potassiumMg = this.potassiumMg * factor,
            calciumMg = this.calciumMg * factor,
            ironMg = this.ironMg * factor,
            vitaminCMg = this.vitaminCMg * factor
        )
    }

    fun energyKcal(): Float {
        return 4 * proteinG + 4 * carbsG + 9 * fatG
    }

    fun rounded(decimals: Int = 1): Nutrients {
        fun Float.roundTo(decimals: Int): Float {
            val factor = 10f.pow(decimals)
            return (this * factor).roundToInt() / factor
        }
        return Nutrients(
            proteinG = this.proteinG.roundTo(decimals),
            carbsG = this.carbsG.roundTo(decimals),
            fatG = this.fatG.roundTo(decimals),
            fiberG = this.fiberG.roundTo(decimals),
            sodiumMg = this.sodiumMg.roundTo(decimals),
            potassiumMg = this.potassiumMg.roundTo(decimals),
            calciumMg = this.calciumMg.roundTo(decimals),
            ironMg = this.ironMg.roundTo(decimals),
            vitaminCMg = this.vitaminCMg.roundTo(decimals)
        )
    }

    companion object {
        val ZERO = Nutrients(
            proteinG = 0f,
            carbsG = 0f,
            fatG = 0f,
            fiberG = 0f,
            sodiumMg = 0f,
            potassiumMg = 0f,
            calciumMg = 0f,
            ironMg = 0f,
            vitaminCMg = 0f
        )
    }
}