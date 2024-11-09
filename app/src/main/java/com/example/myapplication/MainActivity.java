package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private SimulationManager simulationManager;
    private EditText carCountEditText;
    private Button startButton, pauseButton, finishButton;
    private LinearLayout trackContainer;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_main);

            initializeUIComponents();
            initializeSimulationManager();
            setupButtonListeners();
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

    private void startSimulation() {
        try {
            if (simulationManager != null) {
                if (simulationManager.isPaused()) {
                    // Retomar a simulação caso esteja pausada
                    simulationManager.resumeSimulation();
                    Log.d(TAG, "Simulação retomada.");
                } else if (!simulationManager.isRunning()) {
                    // Iniciar uma nova simulação caso não esteja em execução
                    String carCountStr = carCountEditText.getText().toString();
                    Log.d(TAG, "Car count input: " + carCountStr);

                    if (!carCountStr.isEmpty() && carCountStr.matches("\\d+")) {
                        int carCount = Integer.parseInt(carCountStr);
                        simulationManager.startSimulation(carCount);
                        Log.d(TAG, "Simulação iniciada com " + carCount + " carros.");
                    } else {
                        Toast.makeText(this, "Por favor, insira um número válido de carros.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (NumberFormatException e) {
            handleInitializationError(e, "Número de carros inválido.");
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
                Log.d(TAG, "Simulação finalizada.");
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