package com.example.mylibrary2.utils;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Classe para demonstrar a Lei de Amdahl, calculando o impacto do paralelismo
 * no desempenho de sistemas de múltiplos processadores.
 * Inclui métodos para geração de resultados e exportação para CSV.
 */
public class AmdahlLaw {

    /**
     * Calcula o ganho de desempenho teórico com base na Lei de Amdahl.
     *
     * @param parallelFraction Fração do código que é paralelizável (entre 0 e 1).
     * @param numProcessors    Número de processadores disponíveis.
     * @return O speedup (ganho de desempenho) esperado.
     * @throws IllegalArgumentException Se a fração paralelizável não estiver no intervalo [0, 1].
     */
    public double calculateSpeedup(double parallelFraction, int numProcessors) {
        if (parallelFraction < 0 || parallelFraction > 1) {
            throw new IllegalArgumentException("A fração paralelizável deve estar entre 0 e 1.");
        }
        if (numProcessors < 1) {
            throw new IllegalArgumentException("O número de processadores deve ser pelo menos 1.");
        }

        // Fórmula da Lei de Amdahl
        return 1 / ((1 - parallelFraction) + (parallelFraction / numProcessors));
    }

    /**
     * Gera uma tabela de resultados para diferentes configurações de núcleos.
     *
     * @param parallelFraction Fração paralelizável do código.
     * @param maxProcessors    Número máximo de processadores para teste.
     * @return Uma matriz de resultados contendo o número de processadores e o speedup correspondente.
     */
    public double[][] generateResultsTable(double parallelFraction, int maxProcessors) {
        double[][] results = new double[maxProcessors][2];

        for (int processors = 1; processors <= maxProcessors; processors++) {
            results[processors - 1][0] = processors;
            results[processors - 1][1] = calculateSpeedup(parallelFraction, processors);
        }

        return results;
    }

    /**
     * Exibe os resultados em formato de texto.
     *
     * @param results Matriz de resultados gerada pelo método generateResultsTable.
     */
    public void printResults(double[][] results) {
        System.out.println("Processadores\tSpeedup");
        for (double[] row : results) {
            System.out.printf("%d\t\t%.2f%n", (int) row[0], row[1]);
        }
    }

    /**
     * Exporta os resultados para um arquivo CSV.
     *
     * @param results     Matriz de resultados gerada pelo método generateResultsTable.
     * @param outputPath  Caminho do arquivo CSV onde os resultados serão salvos.
     * @throws IOException Se ocorrer um erro ao escrever no arquivo.
     */
    public void exportToCSV(double[][] results, String outputPath) throws IOException {
        try (FileWriter writer = new FileWriter(outputPath)) {
            writer.write("Processadores,Speedup\n");
            for (double[] row : results) {
                writer.write(String.format("%d,%.2f%n", (int) row[0], row[1]));
            }
            System.out.println("Resultados exportados para o arquivo: " + outputPath);
        }
    }

    /**
     * Método principal para testes da classe.
     */
    public static void main(String[] args) {
        AmdahlLaw amdahl = new AmdahlLaw();

        try {
            // Configurações para o teste
            double parallelFraction = 0.8; // 80% do código é paralelizável
            int maxProcessors = 16;       // Teste com até 16 processadores

            // Gera e exibe os resultados
            double[][] results = amdahl.generateResultsTable(parallelFraction, maxProcessors);
            amdahl.printResults(results);

            // Exporta os resultados para um arquivo CSV
            String outputPath = "amdahl_results.csv";
            amdahl.exportToCSV(results, outputPath);

            // Exemplo de cálculo direto
            double speedup = amdahl.calculateSpeedup(0.9, 8);
            System.out.printf("Speedup com 90%% paralelizável e 8 processadores: %.2f%n", speedup);

        } catch (IOException e) {
            System.err.println("Erro ao exportar resultados: " + e.getMessage());
        }
    }
}
