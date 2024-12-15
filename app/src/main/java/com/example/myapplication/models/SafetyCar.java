package com.example.myapplication.models;

import android.graphics.Bitmap;
import android.util.Log;

import com.example.mylibrary2.utils.MetricsCollector;
import com.example.mylibrary2.utils.RealTimeScheduler;

/**
 * Classe que estende `Car` e representa o carro de segurança na pista.
 * Possui prioridade de execução mais alta e é executado em uma thread separada.
 */
public class SafetyCar extends Car {

    private static final String TAG = "SafetyCar";
    private Thread safetyCarThread; // Thread dedicada para o Safety Car
    private volatile boolean isSafetyCarRunning = false; // Controle para execução da thread
    private final RealTimeScheduler scheduler; // Escalonador para gerenciar tarefas críticas

    /**
     * Construtor do SafetyCar.
     *
     * @param name             Nome do SafetyCar
     * @param startX           Posição inicial no eixo X
     * @param startY           Posição inicial no eixo Y
     * @param carColor         Cor do SafetyCar
     * @param metricsCollector Coletor de métricas compartilhado
     */
    public SafetyCar(String name, float startX, float startY, int carColor, MetricsCollector metricsCollector) {
        super(name, startX, startY, carColor, null, metricsCollector); // Passa null para lista de outros carros
        this.scheduler = new RealTimeScheduler(); // Inicializa o escalonador
    }

    @Override
    public void startRace(Bitmap trackBitmap, int trackWidth, int trackHeight) {
        try {
            super.startRace(trackBitmap, trackWidth, trackHeight);

            // Adiciona tarefa ao escalonador com alta prioridade e deadline curto
            scheduler.scheduleTask(
                    "SafetyCar Movement",
                    System.currentTimeMillis() + 5000, // Deadline de 5 segundos
                    10, // Alta prioridade
                    this::collectAndMove // Ação a ser executada
            );

            if (safetyCarThread == null || !safetyCarThread.isAlive()) {
                isSafetyCarRunning = true;
                safetyCarThread = new Thread(this);
                safetyCarThread.setPriority(Thread.MAX_PRIORITY);
                safetyCarThread.start();
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao iniciar a corrida para o Safety Car", e);
        }
    }

    @Override
    public void run() {
        long lastUpdateTime = System.currentTimeMillis();

        while (isSafetyCarRunning) {
            try {
                if (isPaused()) {
                    Thread.sleep(100);
                    continue;
                }

                long currentTime = System.currentTimeMillis();
                double deltaTime = (currentTime - lastUpdateTime) / 1000.0;
                lastUpdateTime = currentTime;

                // Coleta métricas antes de mover
                collectAndMove();

                Thread.sleep(50); // Taxa de atualização do movimento
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Log.e(TAG, "Thread do Safety Car interrompida", e);
            } catch (Exception e) {
                Log.e(TAG, "Erro no loop do Safety Car", e);
            }
        }
    }

    private void collectAndMove() {
        try {
            long jitter = System.currentTimeMillis() % 100; // Exemplo de cálculo de jitter
            long responseTime = (long) (Math.random() * 200); // Simula tempo de resposta
            double utilization = Math.random() * 100; // Simula utilização do processador

            // Adiciona métricas ao coletor
            metricsCollector.collectMetric(getName(), jitter, responseTime, utilization);

            // Move o Safety Car
            move(0.05); // Passa deltaTime simulado
            Log.d(TAG, getName() + " moveu com métricas coletadas.");
        } catch (Exception e) {
            Log.e(TAG, "Erro ao coletar métricas ou mover o Safety Car", e);
        }
    }

    @Override
    public void stopRace() {
        super.stopRace();
        isSafetyCarRunning = false;
        if (safetyCarThread != null) {
            safetyCarThread.interrupt();
            Log.d(TAG, getName() + " finalizou a corrida como Safety Car.");
        }

        // Exporta métricas coletadas
        try {
            metricsCollector.exportMetrics("safety_car_metrics.csv");
            Log.d(TAG, "Métricas exportadas para 'safety_car_metrics.csv'.");
        } catch (Exception e) {
            Log.e(TAG, "Erro ao exportar métricas do Safety Car", e);
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

    public void resetParameters() {
        setSpeed(initialSpeed);
        setDirection(90);
        resetAccumulatedMoveX();
        resetAccumulatedMoveY();
        Log.d(TAG, "Parâmetros do Safety Car foram resetados.");
    }
}
