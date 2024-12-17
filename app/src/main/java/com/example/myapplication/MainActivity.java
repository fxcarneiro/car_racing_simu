package com.example.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.utils.MetricsFileManager;
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
    private static final int STORAGE_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_main);

            // Solicita permissões, se necessário
            if (!checkAndRequestStoragePermissions()) {
                Log.e(TAG, "Permissões não concedidas. Encerrando inicialização.");
                return; // Interrompe a inicialização se permissões não forem concedidas
            }

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

    private boolean checkAndRequestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_CODE
                );
                return false; // Permissões ainda não concedidas
            }
        }
        Log.d(TAG, "Permissões de armazenamento já concedidas.");
        return true; // Permissões concedidas
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

                // Usa o MetricsFileManager para criar e verificar o arquivo
                File metricsFile = MetricsFileManager.getOrCreateMetricsFile(this, "simulation_metrics.csv");
                if (metricsFile == null || !metricsFile.exists()) {
                    Log.e(TAG, "Falha ao criar ou acessar o arquivo de métricas.");
                    Toast.makeText(this, "Erro ao exportar métricas.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Exporta métricas para o arquivo criado
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permissões de armazenamento concedidas.");
                recreate(); // Reinicia a atividade para continuar a inicialização
            } else {
                Log.e(TAG, "Permissões de armazenamento negadas.");
                Toast.makeText(this, "Permissões de armazenamento são necessárias para exportar métricas.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
