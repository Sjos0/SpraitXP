package io.github.sjos0.spritexp.domain.usecase

/** Regras de tamanho final do arquivo exportado, independentes da UI. */
object ExportSizing {
    const val MAX_SIDE = 8192

    fun finalSide(baseSide: Int, displayScale: Int): Int {
        require(baseSide > 0) { "O lado original precisa ser positivo" }
        require(displayScale > 0) { "A escala precisa ser positiva" }
        val result = baseSide.toLong() * displayScale
        require(result <= MAX_SIDE) {
            "A exportação excederia o limite seguro de ${MAX_SIDE}px por lado"
        }
        return result.toInt()
    }
}
