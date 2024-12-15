// Caminho do arquivo: com/example/myapplication/Metrics.java

package com.example.myapplication;

/**
 * Classe que representa métricas básicas de desempenho de um veículo.
 */
public class Metrics {
    public long jitter;         // Jitter do veículo em milissegundos
    public long responseTime;   // Tempo de resposta em milissegundos
    public double utilization;  // Utilização como porcentagem

    /**
     * Construtor da classe Metrics.
     *
     * @param jitter         O jitter do veículo em milissegundos.
     * @param responseTime   O tempo de resposta em milissegundos.
     * @param utilization    A utilização do veículo como porcentagem.
     */
    public Metrics(long jitter, long responseTime, double utilization) {
        this.jitter = jitter;
        this.responseTime = responseTime;
        this.utilization = utilization;
    }
}
