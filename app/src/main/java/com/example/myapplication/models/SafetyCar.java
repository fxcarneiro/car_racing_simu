// Caminho do arquivo: com/example/myapplication/models/SafetyCar.java

package com.example.myapplication.models;

import android.graphics.Bitmap;
import android.util.Log;
import com.example.myapplication.interfaces.Vehicle;

/**
 * Classe que estende `Car` e representa o carro de segurança na pista.
 * Possui prioridade de execução mais alta e é executado em uma thread separada.
 * Funcionalidades:
 *   - Utiliza prioridade máxima para garantir uma execução mais rápida.
 *   - Inclui métodos específicos para pausar, retomar e redefinir ao estado inicial.
 */

public class SafetyCar extends Car implements Vehicle {

    private static final String TAG = "SafetyCar";
    private Thread safetyCarThread; // Thread dedicada para o Safety Car
    private volatile boolean isSafetyCarRunning = false; // Controle para execução da thread

    public SafetyCar(String name, float startX, float startY, int carColor) {
        // Inicializa o SafetyCar, passando `null` para otherCars já que ele não interage diretamente com outros carros
        super(name, startX, startY, carColor, null);
    }

    @Override
    public void startRace(Bitmap trackBitmap, int trackWidth, int trackHeight) {
        try {
            // Chama o método startRace da classe `Car`
            super.startRace(trackBitmap, trackWidth, trackHeight);

            // Inicia a thread do Safety Car, se ela não estiver ativa
            if (safetyCarThread == null || !safetyCarThread.isAlive()) {
                isSafetyCarRunning = true; // Ativa a execução da thread
                safetyCarThread = new Thread(this); // Cria nova thread para o Safety Car
                safetyCarThread.setPriority(Thread.MAX_PRIORITY); // Define prioridade alta para a thread
                safetyCarThread.start(); // Inicia a thread
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao iniciar a corrida para o Safety Car", e);
        }
    }

    @Override
    public void run() {
        long lastUpdateTime = System.currentTimeMillis();

        // Loop para manter o Safety Car em movimento enquanto `isSafetyCarRunning` estiver true
        while (isSafetyCarRunning) {
            try {
                // Verifica se o Safety Car está pausado
                if (isPaused()) {
                    Thread.sleep(100); // Aguarda enquanto estiver pausado
                    continue;
                }

                long currentTime = System.currentTimeMillis();
                double deltaTime = (currentTime - lastUpdateTime) / 1000.0; // Calcula o delta de tempo em segundos
                lastUpdateTime = currentTime;

                // Move o Safety Car constantemente
                move(deltaTime);

                // Log de movimento para indicar funcionamento (ex.: luzes de alerta)
                Log.d(TAG, getName() + " está em movimento como Safety Car.");

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Interrompe a thread se uma interrupção for detectada
                Log.e(TAG, "Thread do Safety Car interrompida", e);
            } catch (Exception e) {
                Log.e(TAG, "Erro no loop do Safety Car", e);
            }

            try {
                Thread.sleep(50); // Controle da taxa de atualização do movimento
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void stopRace() {
        super.stopRace(); // Para o Safety Car chamando o método da classe `Car`
        isSafetyCarRunning = false; // Define `isSafetyCarRunning` como false para interromper o loop de movimento
        if (safetyCarThread != null) {
            safetyCarThread.interrupt(); // Interrompe a thread do Safety Car
            Log.d(TAG, getName() + " finalizou a corrida como Safety Car.");
        }
    }

    @Override
    public void pauseRace() {
        super.pauseRace(); // Pausa o Safety Car chamando o método da classe `Car`
        Log.d(TAG, getName() + " está pausado como Safety Car.");
    }

    @Override
    public void resumeRace() {
        super.resumeRace(); // Retoma a corrida chamando o método da classe `Car`
        Log.d(TAG, getName() + " retomou a corrida como Safety Car.");
    }

    // Método para redefinir o estado inicial do Safety Car
    public void resetParameters() {
        setSpeed(initialSpeed);  // Redefine a velocidade inicial usando o método setSpeed
        setDirection(90);        // Redefine o ângulo da direção para o padrão (90 graus)
        resetAccumulatedMoveX();  // Zera o movimento acumulado em X
        resetAccumulatedMoveY();  // Zera o movimento acumulado em Y
        Log.d(TAG, "Parâmetros do Safety Car foram resetados.");
    }
}
