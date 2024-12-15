package com.example.mylibrary2.utils;

import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Classe RealTimeScheduler
 * Gerencia o escalonamento de tarefas e threads em sistemas de tempo real.
 */
public class RealTimeScheduler {

    /**
     * Classe interna para representar uma tarefa escalonável.
     */
    public static class Task implements Comparable<Task> {
        public String taskName;
        public long deadline;   // Deadline da tarefa
        public int priority;    // Prioridade da tarefa
        public Runnable action; // Ação associada à tarefa

        public Task(String taskName, long deadline, int priority, Runnable action) {
            this.taskName = taskName;
            this.deadline = deadline;
            this.priority = priority;
            this.action = action;
        }

        @Override
        public int compareTo(Task other) {
            // Escalonamento baseado em prioridade e deadline
            if (this.priority != other.priority) {
                return Integer.compare(other.priority, this.priority); // Maior prioridade primeiro
            }
            return Long.compare(this.deadline, other.deadline); // Menor deadline primeiro
        }
    }

    private final Queue<Task> taskQueue; // Fila de tarefas escalonáveis

    /**
     * Construtor do RealTimeScheduler.
     */
    public RealTimeScheduler() {
        this.taskQueue = new PriorityQueue<>();
    }

    /**
     * Agenda uma nova tarefa para escalonamento.
     *
     * @param taskName Nome da tarefa.
     * @param deadline Deadline da tarefa (em milissegundos).
     * @param priority Prioridade da tarefa.
     * @param action   Ação associada à tarefa.
     */
    public void scheduleTask(String taskName, long deadline, int priority, Runnable action) {
        Task task = new Task(taskName, deadline, priority, action);
        taskQueue.add(task);
        System.out.printf("Tarefa %s agendada com prioridade %d e deadline em %d ms.%n", taskName, priority, deadline);
    }

    /**
     * Executa as tarefas escalonadas na fila, respeitando as prioridades e deadlines.
     */
    public void executeTasks() {
        while (!taskQueue.isEmpty()) {
            Task task = taskQueue.poll(); // Pega a próxima tarefa
            long currentTime = System.currentTimeMillis();

            if (currentTime > task.deadline) {
                System.out.println("Tarefa atrasada: " + task.taskName);
            } else {
                System.out.println("Executando tarefa: " + task.taskName);
                task.action.run();
            }
        }
    }

    /**
     * Verifica se uma tarefa ainda é escalonável com base no deadline e no tempo atual.
     *
     * @param deadline Deadline da tarefa.
     * @return true se a tarefa é escalonável, false caso contrário.
     */
    public boolean isTaskScalable(long deadline) {
        long currentTime = System.currentTimeMillis();
        return currentTime <= deadline;
    }

    /**
     * Ajusta a prioridade de uma tarefa com base no atraso ou dependência.
     *
     * @param taskName Nome da tarefa a ser ajustada.
     * @param newPriority Nova prioridade da tarefa.
     */
    public void adjustTaskPriority(String taskName, int newPriority) {
        for (Task task : taskQueue) {
            if (task.taskName.equals(taskName)) {
                taskQueue.remove(task);
                task.priority = newPriority;
                taskQueue.add(task);
                System.out.printf("Prioridade da tarefa %s ajustada para %d.%n", taskName, newPriority);
                break;
            }
        }
    }

    /**
     * Ajusta a velocidade do carro com base no atraso.
     *
     * @param currentSpeed Velocidade atual do carro.
     * @param isDelayed    Indica se o carro está atrasado.
     * @return A nova velocidade ajustada.
     */
    public float adjustSpeed(float currentSpeed, boolean isDelayed) {
        if (isDelayed) {
            return currentSpeed + 10; // Aumenta a velocidade em 10
        }
        return Math.max(0, currentSpeed - 5); // Reduz a velocidade em 5, sem ir abaixo de 0
    }

    /**
     * Limpa todas as tarefas da fila.
     */
    public void clearTasks() {
        taskQueue.clear();
    }

    /**
     * Método principal para demonstração.
     */
    public static void main(String[] args) {
        RealTimeScheduler scheduler = new RealTimeScheduler();

        // Adicionando tarefas simuladas
        scheduler.scheduleTask("Task1", System.currentTimeMillis() + 5000, 1, () -> {
            System.out.println("Task1 executada.");
        });
        scheduler.scheduleTask("Task2", System.currentTimeMillis() + 3000, 2, () -> {
            System.out.println("Task2 executada.");
        });
        scheduler.scheduleTask("Task3", System.currentTimeMillis() + 1000, 3, () -> {
            System.out.println("Task3 executada.");
        });

        // Executando as tarefas
        scheduler.executeTasks();
    }
}
