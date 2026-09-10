package com.apkinves.toolbox.core.util

/** Diff línea a línea entre dos textos (LCS clásico), útil para comparar versiones de un mensaje o documento. */
object TextDiff {
    enum class LineType { EQUAL, ADDED, REMOVED }
    data class DiffLine(val type: LineType, val text: String)

    fun diff(oldText: String, newText: String): List<DiffLine> {
        val oldLines = oldText.lines()
        val newLines = newText.lines()
        val n = oldLines.size
        val m = newLines.size

        // Tabla LCS (longitud del subsecuencia común más larga).
        val lcs = Array(n + 1) { IntArray(m + 1) }
        for (i in n - 1 downTo 0) {
            for (j in m - 1 downTo 0) {
                lcs[i][j] = if (oldLines[i] == newLines[j]) lcs[i + 1][j + 1] + 1
                else maxOf(lcs[i + 1][j], lcs[i][j + 1])
            }
        }

        val result = mutableListOf<DiffLine>()
        var i = 0
        var j = 0
        while (i < n && j < m) {
            when {
                oldLines[i] == newLines[j] -> {
                    result += DiffLine(LineType.EQUAL, oldLines[i])
                    i++; j++
                }
                lcs[i + 1][j] >= lcs[i][j + 1] -> {
                    result += DiffLine(LineType.REMOVED, oldLines[i])
                    i++
                }
                else -> {
                    result += DiffLine(LineType.ADDED, newLines[j])
                    j++
                }
            }
        }
        while (i < n) { result += DiffLine(LineType.REMOVED, oldLines[i]); i++ }
        while (j < m) { result += DiffLine(LineType.ADDED, newLines[j]); j++ }
        return result
    }
}
