package com.example.mylibrary2.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Classe utilitária para manipulação e verificação de deadlines em sistemas de tempo real.
 */
public class DeadlineUtils {

    public static boolean isDelayed(long currentTime, long deadline) {
        return currentTime > deadline;
    }

    public static long calculateDeadline(long startTime, long duration) {
        return startTime + duration;
    }

    public static long calculateJitter(long expectedStartTime, long actualStartTime) {
        return Math.abs(actualStartTime - expectedStartTime);
    }

    public static long calculateMaxResponseTime(
            long jitter,
            long executionTime,
            List<Long> interferingTasks,
            List<Long> periods) {

        if (interferingTasks.size() != periods.size()) {
            throw new IllegalArgumentException("As listas de tarefas interferentes e períodos devem ter o mesmo tamanho.");
        }

        long interferenceSum = 0;
        for (int i = 0; i < interferingTasks.size(); i++) {
            long period = periods.get(i);
            long execution = interferingTasks.get(i);
            interferenceSum += Math.ceil((double) executionTime / period) * execution;
        }

        return jitter + executionTime + interferenceSum;
    }

    public static double calculateProcessorUtilization(List<Long> executionTimes, List<Long> periods) {
        if (executionTimes.size() != periods.size()) {
            throw new IllegalArgumentException("As listas de tempos de execução e períodos devem ter o mesmo tamanho.");
        }

        double utilization = 0.0;
        for (int i = 0; i < executionTimes.size(); i++) {
            utilization += (double) executionTimes.get(i) / periods.get(i);
        }

        return utilization;
    }

    /**
     * Exporta os resultados de utilização do processador para um arquivo CSV.
     *
     * @param executionTimes Lista de tempos de execução das tarefas.
     * @param periods        Lista de períodos das tarefas.
     * @param outputPath     Caminho do arquivo CSV.
     * @throws IOException Se ocorrer um erro ao escrever no arquivo.
     */
    public static void exportProcessorUtilizationToCSV(
            List<Long> executionTimes, List<Long> periods, String outputPath) throws IOException {

        try (FileWriter writer = new FileWriter(outputPath)) {
            writer.write("Tarefa,Execução,Período,Utilização\n");

            for (int i = 0; i < executionTimes.size(); i++) {
                double utilization = (double) executionTimes.get(i) / periods.get(i);
                writer.write(String.format("%d,%d,%d,%.2f\n", i + 1, executionTimes.get(i), periods.get(i), utilization));
            }

            System.out.println("Utilização exportada para o arquivo: " + outputPath);
        }
    }

    /**
     * Método principal para testes da classe.
     */
    public static void main(String[] args) {
        // Exemplo de utilização
        List<Long> executionTimes = List.of(10L, 20L, 30L);
        List<Long> periods = List.of(50L, 100L, 150L);

        // Cálculo de utilização do processador
        double utilization = calculateProcessorUtilization(executionTimes, periods);
        System.out.printf("Utilização total do processador: %.2f%n", utilization);

        // Exportação de dados para CSV
        try {
            exportProcessorUtilizationToCSV(executionTimes, periods, "processor_utilization.csv");
        } catch (IOException e) {
            System.err.println("Erro ao exportar dados: " + e.getMessage());
        }
    }
}
