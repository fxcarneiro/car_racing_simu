// Caminho do arquivo: com/example/myapplication/utils/CalculationUtils.java

package com.example.mylibrary.utils;
/**
 *  Classe utilitária responsável por cálculos essenciais, como a distância entre
 *   dois pontos, usada para posicionamento e detecção de colisões na simulação.
 */

public class CalculationUtils {

    /**
     * Calcula a distância entre dois pontos no plano 2D usando a fórmula de distância euclidiana.
     *
     * Fórmula: √((x2 - x1)² + (y2 - y1)²)
     *
     * @param x1 Coordenada X do primeiro ponto.
     * @param y1 Coordenada Y do primeiro ponto.
     * @param x2 Coordenada X do segundo ponto.
     * @param y2 Coordenada Y do segundo ponto.
     * @return Distância entre os dois pontos como um valor de ponto flutuante.
     */
    public static float calculateDistance(float x1, float y1, float x2, float y2) {
        // Calcula a diferença entre as coordenadas X e Y dos dois pontos
        float deltaX = x2 - x1;
        float deltaY = y2 - y1;

        // Aplica a fórmula da distância euclidiana e retorna o valor
        return (float) Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));
    }
}
