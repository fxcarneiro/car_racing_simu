package com.example.mylibrary2.utils;

/**
 * Classe responsável por configurar os processadores e gerenciar o uso de recursos pelas threads.
 */
public class ThreadManager {

    private static int configuredProcessors = Runtime.getRuntime().availableProcessors();

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
        configuredProcessors = Runtime.getRuntime().availableProcessors();
        logMessage("Reset para número máximo de processadores disponíveis: " + configuredProcessors);
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
     * Define a prioridade de uma thread para usar os processadores configurados.
     *
     * @param thread A thread que terá a prioridade ajustada.
     * @param priority A prioridade da thread (valores de Process.THREAD_PRIORITY_*).
     */
    public static void setThreadPriority(Thread thread, int priority) {
        thread.setPriority(priority);
        logMessage("Prioridade da thread " + thread.getName() + " ajustada para " + priority);
    }

    /**
     * Método principal para demonstração de funcionalidades.
     */
    public static void main(String[] args) {
        // Exibe o número máximo de processadores disponíveis
        logMessage("Processadores disponíveis: " + Runtime.getRuntime().availableProcessors());

        // Configura para usar apenas 2 núcleos
        try {
            configureProcessors(2);
            logMessage("Processadores configurados: " + getConfiguredProcessors());
        } catch (IllegalArgumentException e) {
            logMessage("Erro ao configurar processadores: " + e.getMessage());
        }

        // Reseta para o máximo disponível
        resetToMaxProcessors();

        // Ajusta a prioridade de uma thread de exemplo
        Thread exampleThread = new Thread(() -> {
            logMessage("Thread de exemplo executando.");
        }, "ExampleThread");

        exampleThread.start();
        setThreadPriority(exampleThread, Thread.MAX_PRIORITY);
    }
}
