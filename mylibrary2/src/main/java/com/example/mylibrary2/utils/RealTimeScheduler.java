package com.example.mylibrary2.utils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Classe RealTimeScheduler
 * Gerencia o escalonamento de tarefas e threads em sistemas de tempo real.
 */
public class RealTimeScheduler {

    public static class Task implements Comparable<Task> {
        public String taskName;
        public long deadline;
        public int priority;
        public Runnable action;

        public Task(String taskName, long deadline, int priority, Runnable action) {
            this.taskName = taskName;
            this.deadline = deadline;
            this.priority = priority;
            this.action = action;
        }

        @Override
        public int compareTo(Task other) {
            if (this.priority != other.priority) {
                return Integer.compare(other.priority, this.priority);
            }
            return Long.compare(this.deadline, other.deadline);
        }
    }

    private final Queue<Task> taskQueue;

    public RealTimeScheduler() {
        this.taskQueue = new PriorityQueue<>();
    }

    /**
     * Ajusta dinamicamente as prioridades das tarefas usando reflexão.
     *
     * @param tasks Lista de objetos com métodos getName, getDeadlineRemaining e getDistance.
     */
    public void adjustDynamicPriorities(List<?> tasks) {
        for (Object task : tasks) {
            try {
                Method getName = task.getClass().getMethod("getName");
                Method getDeadlineRemaining = task.getClass().getMethod("getDeadlineRemaining");
                Method getDistance = task.getClass().getMethod("getDistance");

                String name = (String) getName.invoke(task);
                long deadlineRemaining = (long) getDeadlineRemaining.invoke(task);
                int distance = (int) getDistance.invoke(task);

                int newPriority;
                if (deadlineRemaining < 2000) {
                    newPriority = Thread.MAX_PRIORITY;
                } else if (distance > 1000) {
                    newPriority = Thread.NORM_PRIORITY + 2;
                } else {
                    newPriority = Thread.NORM_PRIORITY;
                }

                adjustTaskPriority(name, newPriority);
                System.out.printf("Prioridade ajustada para %s: %d%n", name, newPriority);

            } catch (Exception e) {
                System.err.println("Erro ao ajustar prioridades: " + e.getMessage());
            }
        }
    }

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

    public void scheduleTask(String taskName, long deadline, int priority, Runnable action) {
        Task task = new Task(taskName, deadline, priority, action);
        taskQueue.add(task);
        System.out.printf("Tarefa %s agendada com prioridade %d e deadline em %d ms.%n", taskName, priority, deadline);
    }

    public void executeTasks() {
        while (!taskQueue.isEmpty()) {
            Task task = taskQueue.poll();
            long currentTime = System.currentTimeMillis();

            if (currentTime > task.deadline) {
                System.out.println("Tarefa atrasada: " + task.taskName);
            } else {
                System.out.println("Executando tarefa: " + task.taskName);
                task.action.run();
            }
        }
    }
}
