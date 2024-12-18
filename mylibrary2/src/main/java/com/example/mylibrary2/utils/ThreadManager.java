package com.example.mylibrary2.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Classe responsável por configurar os processadores e gerenciar o uso de recursos pelas threads.
 */
public class ThreadManager {

    private static int configuredProcessors = Runtime.getRuntime().availableProcessors();
    private static ExecutorService executorService;

    /**
     * Configura o número de processadores disponíveis para o sistema.
     *
     * @param numCores O número de núcleos desejado.
     * @throws IllegalArgumentException Se o número de núcleos for inválido.
     */
    public static void configureProcessors(int numCores) {
        int availableProcessors = Runtime.getRuntime().availableProcessors();

        if (numCores < 1 || numCores > availableProcessors) {
            throw new IllegalArgumentException(
                    "Número de núcleos inválido. Deve estar entre 1 e " + availableProcessors);
        }

        configuredProcessors = numCores;

        if (executorService != null) {
            executorService.shutdownNow(); // Reinicializa o pool
        }
        executorService = Executors.newFixedThreadPool(configuredProcessors);

        logMessage("Número de processadores configurados: " + configuredProcessors);
    }

    /**
     * Obtém o número de processadores atualmente configurados.
     *
     * @return O número de processadores configurados.
     */
    public static int getConfiguredProcessors() {
        return configuredProcessors;
    }

    /**
     * Reseta a configuração de processadores para o máximo disponível no sistema.
     */
    public static void resetToMaxProcessors() {
        configureProcessors(Runtime.getRuntime().availableProcessors());
        logMessage("Reset para número máximo de processadores disponíveis.");
    }

    /**
     * Mede o tempo de execução de uma tarefa usando os processadores configurados.
     *
     * @param task        A tarefa a ser executada.
     * @param description Descrição da configuração (para exportação).
     * @param filePath    Caminho do arquivo CSV para exportar os resultados.
     */
    public static void measureExecutionTime(Runnable task, String description, String filePath) {
        long startTime = System.nanoTime();

        executorService.submit(task);
        executorService.shutdown();
        while (!executorService.isTerminated()) {
            // Aguarda até que todas as threads finalizem
        }

        long endTime = System.nanoTime();
        double executionTimeMs = (endTime - startTime) / 1_000_000.0;
        logMessage("Tempo de execução (" + description + "): " + executionTimeMs + " ms");

        exportToCSV(description, executionTimeMs, filePath);
    }

    /**
     * Exporta o tempo de execução para um arquivo CSV.
     *
     * @param description      Descrição da configuração.
     * @param executionTimeMs  Tempo de execução em milissegundos.
     * @param filePath         Caminho do arquivo CSV.
     */
    private static void exportToCSV(String description, double executionTimeMs, String filePath) {
        try (FileWriter writer = new FileWriter(filePath, true)) {
            writer.write(description + "," + executionTimeMs + "\n");
            logMessage("Resultados exportados para: " + filePath);
        } catch (IOException e) {
            logMessage("Erro ao exportar resultados: " + e.getMessage());
        }
    }

    /**
     * Exibe um log informativo com timestamps.
     *
     * @param message A mensagem a ser exibida.
     */
    private static void logMessage(String message) {
        System.out.printf("[%tT] %s%n", System.currentTimeMillis(), message);
    }

    /**
     * Método principal para demonstração de funcionalidades.
     */
    public static void main(String[] args) {
        String csvPath = "performance_results.csv";
        Runnable sampleTask = () -> {
            for (int i = 0; i < 1_000_000; i++) {
                Math.sqrt(i); // Simula uma carga de processamento
            }
        };

        // Teste com 1 núcleo
        configureProcessors(1);
        measureExecutionTime(sampleTask, "1 Núcleo", csvPath);

        // Teste com múltiplos núcleos
        resetToMaxProcessors();
        measureExecutionTime(sampleTask, "Máximo Núcleos", csvPath);
    }
}
