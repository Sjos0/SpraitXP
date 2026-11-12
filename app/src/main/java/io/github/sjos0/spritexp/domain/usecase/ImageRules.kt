package io.github.sjos0.spritexp.domain.usecase

/** Regras de entrada de imagens do SpriteXP. */
object ImageRules {

    /** Mensagem exibida quando a imagem não é quadrada. */
    const val SQUARE_ONLY_MESSAGE =
        "Apenas imagens 1:1 (quadradas) são aceitas. Escolha uma imagem com largura igual à altura."

    /** Retorna true apenas para proporção 1:1 com dimensões válidas. */
    fun isSquare(width: Int, height: Int): Boolean = width > 0 && height > 0 && width == height
}
