package com.example.mylibrary2.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * MetricsCollector
 * Responsável por coletar e registrar métricas de desempenho durante a execução.
 */
public class MetricsCollector {

    /**
     * Classe interna para representar uma métrica individual.
     */
    public static class Metric {
        public String taskName;
        public long jitter;         // Jitter da tarefa (ms)
        public long responseTime;   // Tempo de resposta da tarefa (ms)
        public double processorUtilization; // Utilização do processador (%)

        public Metric(String taskName, long jitter, long responseTime, double processorUtilization) {
            this.taskName = taskName;
            this.jitter = jitter;
            this.responseTime = responseTime;
            this.processorUtilization = processorUtilization;
        }

        @Override
        public String toString() {
            return taskName + "," + jitter + "," + responseTime + "," + processorUtilization;
        }
    }

    private static final String TAG = "MetricsCollector";
    private final Context context;
    private final List<Metric> metrics;

    public MetricsCollector(Context context) {
        this.context = context;
        this.metrics = new ArrayList<>();
    }

    /**
     * Coleta uma nova métrica.
     *
     * @param taskName            Nome da tarefa/thread.
     * @param jitter              Jitter da tarefa (ms).
     * @param responseTime        Tempo de resposta da tarefa (ms).
     * @param processorUtilization Utilização do processador (%).
     */
    public void collectMetric(String taskName, long jitter, long responseTime, double processorUtilization) {
        metrics.add(new Metric(taskName, jitter, responseTime, processorUtilization));
    }

    /**
     * Exporta as métricas coletadas para um arquivo CSV com métricas acumuladas.
     *
     * @param filePath Caminho completo para o arquivo.
     * @throws IOException Caso ocorra um erro ao escrever no arquivo.
     */
    public void exportMetrics(String filePath) throws IOException {
        if (!hasStoragePermission()) {
            Log.e(TAG, "Permissões de armazenamento não concedidas. Não é possível exportar métricas.");
            throw new IOException("Permissões de armazenamento não concedidas.");
        }

        File file = new File(filePath);
        Log.d(TAG, "Caminho do arquivo de métricas: " + file.getAbsolutePath());

        // Criação do diretório pai, caso não exista
        if (file.getParentFile() != null && !file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            Log.e(TAG, "Erro ao criar diretório do arquivo: " + file.getParentFile().getAbsolutePath());
            throw new IOException("Erro ao criar diretório para " + filePath);
        }

        try (FileWriter writer = new FileWriter(file)) {
            // Cabeçalho
            writer.write("Task Name,Jitter (ms),Response Time (ms),Processor Utilization (%)\n");

            // Escrita de métricas individuais
            for (Metric metric : metrics) {
                writer.write(metric.toString() + "\n");
            }

            // Escrita de métricas acumuladas no final
            writer.write("\n=== Métricas Acumuladas ===\n");
            writer.write(String.format("Total Response Time (Ri),%d ms\n", calculateTotalResponseTime()));
            writer.write(String.format("Average Jitter (Ji),%.2f ms\n", calculateAverageJitter()));

            Log.d(TAG, "Métricas exportadas para: " + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "Erro ao salvar métricas no arquivo: " + filePath, e);
            throw e;
        }
    }

    /**
     * Verifica se as permissões de armazenamento estão concedidas.
     *
     * @return true se a permissão estiver concedida, false caso contrário.
     */
    private boolean hasStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int writePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return writePermission == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    /**
     * Calcula o tempo de resposta total (Ri).
     *
     * @return Soma total dos tempos de resposta.
     */
    public long calculateTotalResponseTime() {
        return metrics.stream().mapToLong(m -> m.responseTime).sum();
    }

    /**
     * Calcula o jitter médio (Ji).
     *
     * @return Média dos valores de jitter.
     */
    public double calculateAverageJitter() {
        return metrics.stream().mapToLong(m -> m.jitter).average().orElse(0.0);
    }

    /**
     * Exibe as métricas individuais e acumuladas no console.
     */
    public void displayMetrics() {
        System.out.printf("%-15s %-15s %-20s %-20s%n",
                "Task Name", "Jitter (ms)", "Response Time (ms)", "Processor Utilization (%)");
        for (Metric metric : metrics) {
            System.out.printf("%-15s %-15d %-20d %-20.2f%n",
                    metric.taskName, metric.jitter, metric.responseTime, metric.processorUtilization);
        }

        // Exibe métricas acumuladas
        System.out.println("\n=== Métricas Acumuladas ===");
        System.out.printf("Tempo de Resposta Total (Ri): %d ms%n", calculateTotalResponseTime());
        System.out.printf("Jitter Médio (Ji): %.2f ms%n", calculateAverageJitter());
    }

    /**
     * Limpa todas as métricas coletadas.
     */
    public void clearMetrics() {
        metrics.clear();
    }
}
