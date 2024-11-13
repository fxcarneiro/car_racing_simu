// Caminho do arquivo: com/example/mylibrary/utils/CarState.java

package com.example.mylibrary.utils;
/**
 * CarState (Interface)
 * Define métodos para acessar e modificar o estado do carro, incluindo informações
 * como posição, direção, velocidade, combustível, distância, penalidades e voltas completas.
 * Funcionalidade: Fornece uma estrutura comum para manipular e armazenar o estado dos carros,
 * facilitando a persistência no banco de dados.
 */

public interface CarState {
    String getName();
    float getX();
    float getY();
    double getDirection();
    float getSpeed();
    int getFuelTank();
    int getDistance();
    int getPenalty();
    int getLapsCompleted();

    void setPosition(float x, float y);
    void setDirection(double direction);
    void setSpeed(float speed);
    void setFuelTank(int fuelTank);
    void setDistance(int distance);
    void setPenalty(int penalty);
    void setLapsCompleted(int lapsCompleted);
}
