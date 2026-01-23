package com.ericho.myhospital

data class HospitalWaitTime(
    val name: String,
    val t1wt: String,
    val t2wt: String,
    val t3p50: String,
    val t3p95: String,
    val t45p50: String,
    val t45p95: String,
) {
    fun t3p50Minutes(): Int = parseTimeToMinutes(t3p50)

    private fun parseTimeToMinutes(raw: String): Int {
        val trimmed = raw.trim().lowercase()
        val number = trimmed.split(" ").firstOrNull()?.toDoubleOrNull() ?: return Int.MAX_VALUE
        return when {
            "hour" in trimmed -> (number * 60).toInt()
            "minute" in trimmed -> number.toInt()
            else -> Int.MAX_VALUE
        }
    }
}
