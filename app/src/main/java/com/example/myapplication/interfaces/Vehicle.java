// Caminho do arquivo: com/example/myapplication/interfaces/Vehicle.java

package com.example.myapplication.interfaces;

import android.graphics.Bitmap;
import com.example.myapplication.Metrics; // Importa a classe Metrics

/**
 * Interface que define o comportamento básico de um veículo em uma corrida.
 */
public interface Vehicle {

    /**
     * Inicia a corrida para o veículo.
     *
     * @param trackBitmap   O bitmap da pista, que representa visualmente o ambiente da corrida.
     * @param trackWidth    A largura da pista em pixels.
     * @param trackHeight   A altura da pista em pixels.
     */
    void startRace(Bitmap trackBitmap, int trackWidth, int trackHeight);

    /**
     * Pausa a corrida do veículo. Deve suspender temporariamente a movimentação.
     */
    void pauseRace();

    /**
     * Retoma a corrida do veículo após estar pausado.
     */
    void resumeRace();

    /**
     * Para a corrida do veículo. Esse método deve encerrar todas as atividades e liberar recursos.
     */
    void stopRace();

    /**
     * Coleta métricas de desempenho do veículo durante a corrida.
     *
     * @return Um objeto que representa as métricas coletadas (por exemplo, jitter, tempo de resposta, utilização).
     */
    Metrics collectMetrics();

    /**
     * Redefine o estado do veículo para os parâmetros iniciais.
     */
    void resetParameters();
}
