// Caminho do arquivo: com/example/myapplication/utils/CalculationUtils.java

package com.example.mylibrary.utils;

public class CalculationUtils {

    /**
     * Calcula a distância entre dois pontos (x1, y1) e (x2, y2).
     *
     * @param x1 Coordenada X do primeiro ponto
     * @param y1 Coordenada Y do primeiro ponto
     * @param x2 Coordenada X do segundo ponto
     * @param y2 Coordenada Y do segundo ponto
     * @return Distância entre os dois pontos
     */
    public static float calculateDistance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
}
