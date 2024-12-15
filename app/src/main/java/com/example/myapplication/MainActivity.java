package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mylibrary2.utils.MetricsCollector;
import com.example.mylibrary2.utils.ThreadManager;

import java.io.File;

/**
 * MainActivity é a atividade principal que controla a interface do usuário para
 * iniciar, pausar e finalizar uma simulação de corrida.
 */
public class MainActivity extends AppCompatActivity {

    private SimulationManager simulationManager;
    private MetricsCollector metricsCollector; // Coletor de métricas
    private EditText carCountEditText;
    private Button startButton, pauseButton, finishButton;
    private LinearLayout trackContainer;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_main);

            // Inicializa os componentes de UI
            initializeUIComponents();
            initializeSimulationManager();
            setupButtonListeners();

            // Configura o número de processadores
            configureProcessors();
        } catch (Exception e) {
            handleInitializationError(e, "Erro ao iniciar a aplicação.");
        }
    }

    private void initializeUIComponents() {
        try {
            carCountEditText = findViewById(R.id.carCountEditText);
            startButton = findViewById(R.id.startButton);
            pauseButton = findViewById(R.id.pauseButton);
            finishButton = findViewById(R.id.finishButton);
            trackContainer = findViewById(R.id.trackContainer);
            Log.d(TAG, "Componentes de UI inicializados com sucesso.");
        } catch (Exception e) {
            handleInitializationError(e, "Erro ao inicializar componentes de UI.");
        }
    }

    private void initializeSimulationManager() {
        try {
            simulationManager = new SimulationManager(this);
            metricsCollector = new MetricsCollector(this); // Passa o contexto
            View trackView = simulationManager.getTrackView();
            if (trackView != null) {
                trackContainer.addView(trackView);
                Log.d(TAG, "Gerenciador de simulação inicializado com sucesso.");
            } else {
                Log.e(TAG, "Erro: o trackView é nulo.");
            }
        } catch (Exception e) {
            handleInitializationError(e, "Erro ao inicializar o gerenciador de simulação.");
        }
    }

    private void setupButtonListeners() {
        try {
            startButton.setOnClickListener(v -> startSimulation());
            pauseButton.setOnClickListener(v -> pauseSimulation());
            finishButton.setOnClickListener(v -> finishSimulation());
            Log.d(TAG, "Listeners de botões configurados com sucesso.");
        } catch (Exception e) {
            handleInitializationError(e, "Erro ao configurar os listeners de botão.");
        }
    }

    private void configureProcessors() {
        try {
            ThreadManager.configureProcessors(4); // Configura para 4 processadores
            Log.d(TAG, "Número de processadores configurados: " + ThreadManager.getConfiguredProcessors());
        } catch (Exception e) {
            handleInitializationError(e, "Erro ao configurar processadores.");
        }
    }

    private void startSimulation() {
        try {
            if (simulationManager != null) {
                if (simulationManager.isPaused()) {
                    simulationManager.resumeSimulation();
                    Log.d(TAG, "Simulação retomada.");
                } else if (!simulationManager.isRunning()) {
                    String carCountStr = carCountEditText.getText().toString();
                    Log.d(TAG, "Car count input: " + carCountStr);

                    if (!carCountStr.isEmpty() && carCountStr.matches("\\d+")) {
                        int carCount = Integer.parseInt(carCountStr);
                        simulationManager.startSimulation(carCount);

                        // Coleta inicial de métricas
                        metricsCollector.clearMetrics();
                        Log.d(TAG, "Simulação iniciada com " + carCount + " carros.");
                    } else {
                        Toast.makeText(this, "Por favor, insira um número válido de carros.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (Exception e) {
            handleInitializationError(e, "Erro ao iniciar a simulação.");
        }
    }

    private void pauseSimulation() {
        try {
            if (simulationManager != null && simulationManager.isRunning()) {
                simulationManager.pauseSimulation();
                Log.d(TAG, "Simulação pausada.");
            } else {
                Log.e(TAG, "Erro: simulationManager não está em execução.");
            }
        } catch (Exception e) {
            handleInitializationError(e, "Erro ao pausar a simulação.");
        }
    }

    private void finishSimulation() {
        try {
            if (simulationManager != null) {
                simulationManager.finishSimulation();

                // Define o caminho do arquivo para exportação de métricas
                File metricsFile = new File(getFilesDir(), "simulation_metrics.csv");

                // Exporta métricas ao finalizar
                metricsCollector.exportMetrics(metricsFile.getAbsolutePath());
                Log.d(TAG, "Simulação finalizada e métricas exportadas para " + metricsFile.getAbsolutePath());
            } else {
                Log.e(TAG, "Erro: simulationManager não foi inicializado.");
            }
        } catch (Exception e) {
            handleInitializationError(e, "Erro ao finalizar a simulação.");
        }
    }

    private void handleInitializationError(Exception e, String userMessage) {
        Log.e(TAG, userMessage + ": " + e.getMessage(), e);
        Toast.makeText(this, userMessage + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }
}
