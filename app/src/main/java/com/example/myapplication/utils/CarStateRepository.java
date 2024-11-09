// Caminho do arquivo: com/example/myapplication/utils/CarStateRepository.java

package com.example.myapplication.utils;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.example.myapplication.models.Car;

import java.util.HashMap;
import java.util.Map;

public class CarStateRepository {

    private static final String TAG = "CarStateRepository";
    private static final String COLLECTION_NAME = "car_states";
    private final FirebaseFirestore firestore;

    public CarStateRepository() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    // Método para salvar o estado de um carro
    public void saveCarState(Car car) {
        Map<String, Object> carState = new HashMap<>();
        carState.put("x", car.getX());
        carState.put("y", car.getY());
        carState.put("direction", car.getDirection());
        carState.put("speed", car.getSpeed());
        carState.put("fuelTank", car.getFuelTank());
        carState.put("distance", car.getDistance());
        carState.put("penalty", car.getPenalty());
        carState.put("lapsCompleted", car.getLapsCompleted());

        firestore.collection(COLLECTION_NAME).document(car.getName())
                .set(carState)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Estado do carro salvo com sucesso: " + car.getName()))
                .addOnFailureListener(e -> Log.e(TAG, "Erro ao salvar estado do carro: " + car.getName(), e));
    }

    // Método para restaurar o estado de um carro a partir do banco de dados
    public void loadCarState(Car car, OnCarStateLoadedListener listener) {
        firestore.collection(COLLECTION_NAME).document(car.getName())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> carState = documentSnapshot.getData();
                        if (carState != null) {
                            car.setPosition(
                                    ((Number) carState.get("x")).floatValue(),
                                    ((Number) carState.get("y")).floatValue());
                            car.setDirection(((Number) carState.get("direction")).doubleValue());
                            car.setSpeed(((Number) carState.get("speed")).floatValue());
                            car.setFuelTank(((Number) carState.get("fuelTank")).intValue());
                            car.setDistance(((Number) carState.get("distance")).intValue());
                            car.setPenalty(((Number) carState.get("penalty")).intValue());
                            car.setLapsCompleted(((Number) carState.get("lapsCompleted")).intValue());
                            listener.onCarStateLoaded(car);
                        }
                    } else {
                        listener.onCarStateLoaded(null); // Nenhum estado salvo para este carro
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao carregar estado do carro: " + car.getName(), e);
                    listener.onCarStateLoaded(null);
                });
    }

    // Listener para notificar quando o estado de um carro é carregado
    public interface OnCarStateLoadedListener {
        void onCarStateLoaded(Car car);
    }
}
