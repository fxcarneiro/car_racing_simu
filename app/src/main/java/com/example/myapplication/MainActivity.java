// Caminho do arquivo: com/example/myapplication/MainActivity.java

package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

/**
 * MainActivity é a atividade principal que controla a interface do usuário para
 * iniciar, pausar e finalizar uma simulação de corrida.
 *
 * A classe interage com o SimulationManager para gerenciar a simulação de corrida.
 * Permite ao usuário definir o número de carros e controlar o estado da simulação
 * através de botões na interface gráfica. A classe também lida com possíveis erros
 * e fornece feedback através de mensagens de log e toast.
 */
public class MainActivity extends AppCompatActivity {

    private SimulationManager simulationManager;  // Gerenciador de simulação
    private EditText carCountEditText;            // Campo de entrada para a quantidade de carros
    private Button startButton, pauseButton, finishButton; // Botões de controle da simulação
    private LinearLayout trackContainer;          // Container para exibir a pista
    private static final String TAG = "MainActivity";

    /**
     * Método de ciclo de vida onCreate inicializa a atividade e configura a interface.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_main);

            // Inicializa componentes de UI, gerenciador de simulação e listeners
            initializeUIComponents();
            initializeSimulationManager();
            setupButtonListeners();
        } catch (Exception e) {
            handleInitializationError(e, "Erro ao iniciar a aplicação.");
        }
    }

    /**
     * Inicializa os componentes de interface do usuário, como campos de texto e botões.
     */
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

    /**
     * Inicializa o SimulationManager e adiciona a pista (TrackView) ao layout.
     */
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

    /**
     * Configura os listeners para os botões, associando cada botão a uma ação de controle.
     */
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

    /**
     * Inicia ou retoma a simulação, dependendo do estado atual.
     * Lê o número de carros inserido pelo usuário e, se válido, inicia a simulação.
     */
    private void startSimulation() {
        try {
            if (simulationManager != null) {
                if (simulationManager.isPaused()) {
                    // Retoma a simulação caso esteja pausada
                    simulationManager.resumeSimulation();
                    Log.d(TAG, "Simulação retomada.");
                } else if (!simulationManager.isRunning()) {
                    // Inicia nova simulação se não estiver em execução
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

    /**
     * Pausa a simulação se estiver em execução.
     */
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

    /**
     * Finaliza a simulação e reinicia os parâmetros.
     */
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

    /**
     * Trata e exibe erros de inicialização com mensagens de log e toast.
     *
     * @param e Exceção capturada.
     * @param userMessage Mensagem para exibir ao usuário.
     */
    private void handleInitializationError(Exception e, String userMessage) {
        Log.e(TAG, userMessage + ": " + e.getMessage(), e);
        Toast.makeText(this, userMessage + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }
}
