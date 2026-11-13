package io.github.sjos0.spritexp.domain.usecase

/** Retângulo quadrado, em pixels da imagem de origem. */
data class SquareCrop(val x: Int, val y: Int, val side: Int)

/** Regras de entrada de imagens do SpriteXP. */
object ImageRules {

    /**
     * Mensagem exibida quando uma sprite sheet não é quadrada.
     * Imagens comuns não usam mais esta regra: qualquer proporção é recortada no centro.
     */
    const val SQUARE_ONLY_MESSAGE =
        "Apenas imagens 1:1 (quadradas) são aceitas. Escolha uma imagem com largura igual à altura."

    /** Retorna true apenas para proporção 1:1 com dimensões válidas. */
    fun isSquare(width: Int, height: Int): Boolean = width > 0 && height > 0 && width == height

    /**
     * Maior quadrado centralizado dentro de [width] x [height].
     * Imagens em 16:9, 9:16 ou qualquer outra proporção perdem as bordas da dimensão maior.
     * Retorna null para dimensões inválidas.
     */
    fun centerSquareCrop(width: Int, height: Int): SquareCrop? {
        if (width <= 0 || height <= 0) return null
        val side = minOf(width, height)
        return SquareCrop(
            x = (width - side) / 2,
            y = (height - side) / 2,
            side = side
        )
    }
}
