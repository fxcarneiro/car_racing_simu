// Caminho do arquivo: com/example/mylibrary/utils/CarStateRepository.java

package com.example.mylibrary.utils;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * ### 4.1. CarStateRepository
 * - **Descrição**: Classe responsável por salvar e carregar o estado dos carros no Firestore,
 *   possibilitando a persistência do estado entre sessões da simulação.
 * - **Funcionalidades**:
 *   - Salva o estado dos carros ao pausar ou finalizar a simulação.
 *   - Carrega o estado dos carros ao reiniciar, permitindo continuar a partir do ponto onde foi interrompido.
 *   - Implementa um listener (`OnCarStateLoadedListener`) para notificar quando o estado dos carros é
 *     carregado com sucesso.
 */

public class CarStateRepository {

    private static final String TAG = "CarStateRepository";
    private static final String COLLECTION_NAME = "car_states";  // Nome da coleção no Firestore
    private final FirebaseFirestore firestore;  // Instância do Firebase Firestore

    /**
     * Construtor que inicializa a conexão com o Firestore.
     */
    public CarStateRepository() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    /**
     * Salva o estado atual de um carro no Firestore.
     *
     * @param car Instância de CarState que contém o estado atual do carro.
     */
    public void saveCarState(CarState car) {
        // Cria um mapa de dados representando o estado atual do carro
        Map<String, Object> carState = new HashMap<>();
        carState.put("x", car.getX());
        carState.put("y", car.getY());
        carState.put("direction", car.getDirection());
        carState.put("speed", car.getSpeed());
        carState.put("fuelTank", car.getFuelTank());
        carState.put("distance", car.getDistance());
        carState.put("penalty", car.getPenalty());
        carState.put("lapsCompleted", car.getLapsCompleted());

        // Envia o estado do carro para o Firestore e trata o sucesso ou falha
        firestore.collection(COLLECTION_NAME).document(car.getName())
                .set(carState)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Estado do carro salvo com sucesso: " + car.getName()))
                .addOnFailureListener(e -> Log.e(TAG, "Erro ao salvar estado do carro: " + car.getName(), e));
    }

    /**
     * Carrega o estado salvo de um carro do Firestore e aplica os dados ao carro fornecido.
     *
     * @param car      Instância de CarState onde os dados serão aplicados.
     * @param listener Listener para notificar quando o estado do carro foi carregado.
     */
    public void loadCarState(CarState car, OnCarStateLoadedListener listener) {
        // Consulta o Firestore usando o nome do carro como ID do documento
        firestore.collection(COLLECTION_NAME).document(car.getName())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Recupera os dados do estado salvo, se disponível
                        Map<String, Object> carState = documentSnapshot.getData();
                        if (carState != null) {
                            // Aplica cada atributo do estado carregado ao carro
                            car.setPosition(
                                    ((Number) carState.get("x")).floatValue(),
                                    ((Number) carState.get("y")).floatValue());
                            car.setDirection(((Number) carState.get("direction")).doubleValue());
                            car.setSpeed(((Number) carState.get("speed")).floatValue());
                            car.setFuelTank(((Number) carState.get("fuelTank")).intValue());
                            car.setDistance(((Number) carState.get("distance")).intValue());
                            car.setPenalty(((Number) carState.get("penalty")).intValue());
                            car.setLapsCompleted(((Number) carState.get("lapsCompleted")).intValue());

                            // Notifica que o estado foi carregado com sucesso
                            listener.onCarStateLoaded(car);
                        }
                    } else {
                        // Notifica que não há estado salvo para o carro
                        listener.onCarStateLoaded(null);
                    }
                })
                .addOnFailureListener(e -> {
                    // Loga e notifica falha ao carregar o estado do carro
                    Log.e(TAG, "Erro ao carregar estado do carro: " + car.getName(), e);
                    listener.onCarStateLoaded(null);
                });
    }

    /**
     * Interface de callback para notificar quando o estado do carro foi carregado.
     */
    public interface OnCarStateLoadedListener {
        void onCarStateLoaded(CarState car);
    }
}
