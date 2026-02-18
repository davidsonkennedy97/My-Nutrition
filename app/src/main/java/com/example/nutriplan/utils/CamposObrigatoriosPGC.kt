package com.example.nutriplan.utils

object CamposObrigatoriosPGC {

    enum class CamposPrega {
        BICEPS, TRICEPS, PEITORAL, AXILAR_MEDIA,
        SUBESCAPULAR, ABDOMEN, SUPRAILIACA, COXA
    }

    fun obterCamposObrigatorios(metodo: String, sexo: String): Set<CamposPrega> {
        return when (metodo) {
            "Jackson & Pollock 3" -> setOf(
                CamposPrega.PEITORAL,
                CamposPrega.ABDOMEN,
                CamposPrega.COXA
            )

            "Jackson & Pollock 7" -> setOf(
                CamposPrega.PEITORAL,
                CamposPrega.AXILAR_MEDIA,
                CamposPrega.TRICEPS,
                CamposPrega.SUBESCAPULAR,
                CamposPrega.ABDOMEN,
                CamposPrega.SUPRAILIACA,
                CamposPrega.COXA
            )

            "Durnin & Womersley" -> setOf(
                CamposPrega.BICEPS,
                CamposPrega.TRICEPS,
                CamposPrega.SUBESCAPULAR,
                CamposPrega.SUPRAILIACA
            )

            "Guedes 3" -> {
                if (sexo.equals("Masculino", ignoreCase = true)) {
                    setOf(
                        CamposPrega.PEITORAL,
                        CamposPrega.ABDOMEN,
                        CamposPrega.COXA
                    )
                } else {
                    setOf(
                        CamposPrega.TRICEPS,
                        CamposPrega.SUPRAILIACA,
                        CamposPrega.COXA
                    )
                }
            }

            else -> emptySet()
        }
    }
}