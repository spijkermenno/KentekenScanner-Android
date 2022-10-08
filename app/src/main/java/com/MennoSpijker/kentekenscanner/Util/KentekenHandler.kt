package com.MennoSpijker.kentekenscanner.Util

import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class KentekenHandler() {
    companion object {
        private const val TAG = "KentekenHandler"

        @JvmStatic
        fun formatLicensePlate(licencePlate: String): String {
            try {
                val sideCode = getSideCodeOfLicensePlate(licencePlate)
                val newLicencePlate = licencePlate.replace("-", "").uppercase(Locale.getDefault())
                return when (sideCode) {
                    1, 2, 3, 4, 5, 6 -> newLicencePlate.substring(
                        0,
                        2
                    ) + '-' + newLicencePlate.substring(2, 4) + '-' + newLicencePlate.substring(
                        4,
                        6
                    )
                    7, 9 -> newLicencePlate.substring(0, 2) + '-' + newLicencePlate.substring(
                        2,
                        5
                    ) + '-' + newLicencePlate[5]
                    8, 10 -> newLicencePlate.substring(0, 1) + '-' + newLicencePlate.substring(
                        1,
                        4
                    ) + '-' + newLicencePlate.substring(4, 6)
                    11, 14 -> newLicencePlate.substring(0, 3) + '-' + newLicencePlate.substring(
                        3,
                        5
                    ) + '-' + newLicencePlate[5]
                    12, 13 -> newLicencePlate.substring(0, 1) + '-' + newLicencePlate.substring(
                        1,
                        3
                    ) + '-' + newLicencePlate.substring(3, 6)
                    else -> newLicencePlate
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return licencePlate
        }

        @JvmStatic
        fun getSideCodeOfLicensePlate(licensePlate: String): Int {
            var newLicensePlate = licensePlate
            newLicensePlate = newLicensePlate.replace("-", "").uppercase(Locale.getDefault())

            val patterns: ArrayList<Regex> = ArrayList()
            patterns.add(Regex("^[a-zA-Z]{2}[0-9]{2}[0-9]{2}$"))    // 1 XX-99-99
            patterns.add(Regex("^[0-9]{2}[0-9]{2}[a-zA-Z]{2}$"))    // 2 99-99-XX
            patterns.add(Regex("^[0-9]{2}[a-zA-Z]{2}[0-9]{2}$"))    // 3 99-XX-99
            patterns.add(Regex("^[a-zA-Z]{2}[0-9]{2}[a-zA-Z]{2}$")) // 4 XX-99-XX
            patterns.add(Regex("^[a-zA-Z]{2}[a-zA-Z]{2}[0-9]{2}$")) // 5 XX-XX-99
            patterns.add(Regex("^[0-9]{2}[a-zA-Z]{2}[a-zA-Z]{2}$")) // 6 99-XX-XX
            patterns.add(Regex("^[0-9]{2}[a-zA-Z]{3}[0-9]{1}$"))    // 7 99-XXX-9
            patterns.add(Regex("^[0-9]{1}[a-zA-Z]{3}[0-9]{2}$"))    // 8 9-XXX-99
            patterns.add(Regex("^[a-zA-Z]{2}[0-9]{3}[a-zA-Z]{1}$")) // 9 XX-999-X
            patterns.add(Regex("^[a-zA-Z]{1}[0-9]{3}[a-zA-Z]{2}$")) // 10 X-999-XX
            patterns.add(Regex("^[a-zA-Z]{3}[0-9]{2}[a-zA-Z]{1}$")) // 11 XXX-99-X
            patterns.add(Regex("^[a-zA-Z]{1}[0-9]{2}[a-zA-Z]{3}$")) // 12 X-99-XXX
            patterns.add(Regex("^[0-9]{1}[a-zA-Z]{2}[0-9]{3}$"))    // 13 9-XX-999
            patterns.add(Regex("^[0-9]{3}[a-zA-Z]{2}[0-9]{1}$"))    // 14 999-XX-9

            // except license-plates for diplomats
            val diplomat = Regex("^CD[ABFJNST][0-9]{1,3}$") // for example: CDB1 of CDJ45

            for (i in patterns.indices) {
                if (newLicensePlate.matches(patterns[i])) {
                    return i + 1
                }
            }
            return if (newLicensePlate.matches(diplomat)) {
                -1
            } else -2
        }

        @JvmStatic
        fun isLicensePlateValid(licensePlate: String): Boolean {
            return getSideCodeOfLicensePlate(licensePlate) >= -1
        }
    }
}