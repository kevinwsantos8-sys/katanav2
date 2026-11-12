package com.shadowkatana.client.render;

/**
 * >>> AJUSTE AQUI <<<  Todos os numeros da posicao/animacao da katana.
 * Unidades: blocos, no espaco do corpo do jogador (x negativo = lado direito do jogador,
 * y positivo = para baixo a partir do ombro, z positivo = para tras/costas, z negativo = para frente).
 * Angulos em graus.
 */
public final class KatanaPose {
    // Pose nas COSTAS (guardada)
    public static final float BACK_X = 0.02f, BACK_Y = 0.42f, BACK_Z = 0.17f;
    public static final float BACK_ROLL = 145f;   // inclinacao (cabo para cima/direita, lamina para baixo/esquerda)
    public static final float BACK_PITCH = 0f;

    // Pose na MAO direita (final da animacao de sacar)
    public static final float HAND_X = -0.36f, HAND_Y = 0.58f, HAND_Z = -0.50f;
    public static final float HAND_ROLL = 0f;
    public static final float HAND_PITCH = -70f;  // negativo = lamina aponta para frente

    // Arco da animacao (quanto a katana sobe por cima do ombro no meio do movimento)
    public static final float ARC_HEIGHT = 0.65f;
    // Giro extra da lamina no meio do saque (da o efeito de "corte")
    public static final float FLOURISH_ROLL = 50f, FLOURISH_PITCH = 25f;

    // Tamanho da katana
    public static final float SCALE = 0.55f;

    private KatanaPose() {}
}
