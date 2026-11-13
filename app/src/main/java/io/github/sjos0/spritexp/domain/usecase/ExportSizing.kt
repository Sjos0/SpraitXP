package io.github.sjos0.spritexp.domain.usecase

/**
 * Regras de tamanho final do arquivo exportado, independentes da UI.
 * O export multiplica o lado do quadro convertido por [displayScale] (nearest-neighbor).
 */
object ExportSizing {
    const val MAX_SIDE = 8192

    /** Lado final (base × escala), com validação de limites. */
    fun finalSide(baseSide: Int, displayScale: Int): Int {
        require(baseSide > 0) { "O lado original precisa ser positivo" }
        require(displayScale > 0) { "A escala precisa ser positiva" }
        val result = baseSide.toLong() * displayScale
        require(result <= MAX_SIDE) {
            "A exportação excederia o limite seguro de ${MAX_SIDE}px por lado"
        }
        return result.toInt()
    }

    /**
     * Garante que largura e altura finais (após escala) cabem no limite.
     * @return o fator de escala se for seguro para o upscale.
     */
    fun requireSafeScale(width: Int, height: Int, displayScale: Int): Int {
        require(width > 0 && height > 0) { "O quadro precisa ter tamanho positivo" }
        require(displayScale > 0) { "A escala precisa ser positiva" }
        finalSide(maxOf(width, height), displayScale)
        return displayScale
    }
}

