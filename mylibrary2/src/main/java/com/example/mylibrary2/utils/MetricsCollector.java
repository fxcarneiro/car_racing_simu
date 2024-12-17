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
        public String taskName;     // Nome da tarefa/thread
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
    private final Context context;  // Contexto do aplicativo
    private final List<Metric> metrics; // Lista de métricas coletadas

    /**
     * Construtor da classe MetricsCollector.
     *
     * @param context O contexto da aplicação para determinar diretórios de gravação.
     */
    public MetricsCollector(Context context) {
        this.context = context;
        this.metrics = new ArrayList<>();
    }

    /**
     * Adiciona uma métrica à lista de métricas.
     *
     * @param taskName            Nome da tarefa/thread.
     * @param jitter              Jitter da tarefa (ms).
     * @param responseTime        Tempo de resposta da tarefa (ms).
     * @param processorUtilization Utilização do processador (%).
     */
    public void collectMetric(String taskName, long jitter, long responseTime, double processorUtilization) {
        Metric metric = new Metric(taskName, jitter, responseTime, processorUtilization);
        metrics.add(metric);
    }

    /**
     * Exporta as métricas coletadas para um arquivo CSV.
     * O arquivo será salvo no diretório externo do aplicativo, se permitido.
     *
     * @param filePath Caminho completo para o arquivo de métricas.
     * @throws IOException Caso ocorra um erro ao gravar o arquivo.
     */
    public void exportMetrics(String filePath) throws IOException {
        if (!hasStoragePermission()) {
            Log.e(TAG, "Permissões de armazenamento não concedidas. Não é possível exportar métricas.");
            throw new IOException("Permissões de armazenamento não concedidas.");
        }

        File file = new File(filePath); // Garante que o caminho seja usado corretamente
        Log.d(TAG, "Caminho do arquivo de métricas: " + file.getAbsolutePath());

        // Cria o diretório pai, se necessário
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
            Log.e(TAG, "Erro ao criar diretório do arquivo: " + parentDir.getAbsolutePath());
            throw new IOException("Erro ao criar diretório para " + filePath);
        }

        try (FileWriter writer = new FileWriter(file)) {
            writer.write("Task Name,Jitter (ms),Response Time (ms),Processor Utilization (%)\n");
            for (Metric metric : metrics) {
                writer.write(metric.toString() + "\n");
            }
            Log.d(TAG, "Métricas exportadas para: " + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "Erro ao salvar métricas no arquivo: " + filePath, e);
            throw e;
        }
    }

    /**
     * Verifica se as permissões de armazenamento estão concedidas.
     *
     * @return true se as permissões estão concedidas, false caso contrário.
     */
    private boolean hasStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int writePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return writePermission == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    /**
     * Exibe todas as métricas no console.
     */
    public void displayMetrics() {
        System.out.printf("%-15s %-15s %-20s %-20s%n",
                "Task Name", "Jitter (ms)", "Response Time (ms)", "Processor Utilization (%)");
        for (Metric metric : metrics) {
            System.out.printf("%-15s %-15d %-20d %-20.2f%n",
                    metric.taskName, metric.jitter, metric.responseTime, metric.processorUtilization);
        }
    }

    /**
     * Limpa as métricas coletadas.
     */
    public void clearMetrics() {
        metrics.clear();
    }

    /**
     * Identifica métricas que excedem limites de jitter ou tempo de resposta.
     *
     * @param jitterLimit       Limite de jitter permitido (ms).
     * @param responseTimeLimit Limite de tempo de resposta permitido (ms).
     * @return Lista de métricas que excedem os limites especificados.
     */
    public List<Metric> findOutliers(long jitterLimit, long responseTimeLimit) {
        List<Metric> outliers = new ArrayList<>();
        for (Metric metric : metrics) {
            if (metric.jitter > jitterLimit || metric.responseTime > responseTimeLimit) {
                outliers.add(metric);
            }
        }
        return outliers;
    }
}
