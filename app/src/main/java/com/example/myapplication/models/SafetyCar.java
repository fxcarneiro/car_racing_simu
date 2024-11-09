// Caminho do arquivo: com/example/myapplication/models/SafetyCar.java

package com.example.myapplication.models;

import android.graphics.Bitmap;
import android.util.Log;
import com.example.myapplication.interfaces.Vehicle;

public class SafetyCar extends Car implements Vehicle {

    private static final String TAG = "SafetyCar";
    private Thread safetyCarThread;
    private volatile boolean isSafetyCarRunning = false; // Variável para controlar a execução da thread

    public SafetyCar(String name, float startX, float startY, int carColor) {
        super(name, startX, startY, carColor, null); // null para otherCars pois SafetyCar não interage diretamente com outros
    }

    @Override
    public void startRace(Bitmap trackBitmap, int trackWidth, int trackHeight) {
        try {
            super.startRace(trackBitmap, trackWidth, trackHeight);

            // Inicializa e inicia a thread do Safety Car, se ainda não estiver ativa
            if (safetyCarThread == null || !safetyCarThread.isAlive()) {
                isSafetyCarRunning = true; // Ativa a variável de controle
                safetyCarThread = new Thread(this);
                safetyCarThread.setPriority(Thread.MAX_PRIORITY); // Define a prioridade máxima para a thread
                safetyCarThread.start();
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao iniciar a corrida para o Safety Car", e);
        }
    }

    @Override
    public void run() {
        long lastUpdateTime = System.currentTimeMillis();

        // Loop de execução para manter o Safety Car em movimento
        while (isSafetyCarRunning) {
            try {
                if (isPaused()) {  // Usando o método isPaused() ao invés da variável direta
                    Thread.sleep(100);
                    continue;
                }

                long currentTime = System.currentTimeMillis();
                double deltaTime = (currentTime - lastUpdateTime) / 1000.0;
                lastUpdateTime = currentTime;

                // Movimentação constante do Safety Car
                move(deltaTime);

                // Log para simulação de comportamento (exemplo: luzes de alerta)
                Log.d(TAG, getName() + " está em movimento como Safety Car.");

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Log.e(TAG, "Thread do Safety Car interrompida", e);
            } catch (Exception e) {
                Log.e(TAG, "Erro no loop do Safety Car", e);
            }

            try {
                Thread.sleep(50); // Controle de taxa de atualização
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void stopRace() {
        super.stopRace();
        isSafetyCarRunning = false; // Desativa a execução da thread
        if (safetyCarThread != null) {
            safetyCarThread.interrupt();
            Log.d(TAG, getName() + " finalizou a corrida como Safety Car.");
        }
    }

    @Override
    public void pauseRace() {
        super.pauseRace();
        Log.d(TAG, getName() + " está pausado como Safety Car.");
    }

    @Override
    public void resumeRace() {
        super.resumeRace();
        Log.d(TAG, getName() + " retomou a corrida como Safety Car.");
    }

    // Método para redefinir os parâmetros do Safety Car ao estado inicial
    public void resetParameters() {
        setSpeed(initialSpeed);  // Usa o método setSpeed para redefinir a velocidade inicial
        setDirection(90);        // Redefine o ângulo para o padrão
        resetAccumulatedMoveX();  // Zera o movimento acumulado em X usando o método público
        resetAccumulatedMoveY();  // Zera o movimento acumulado em Y usando o método público
        Log.d(TAG, "Parâmetros do Safety Car foram resetados.");
    }
}
