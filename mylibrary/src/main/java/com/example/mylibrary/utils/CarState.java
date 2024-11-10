// Caminho do arquivo: com/example/mylibrary/utils/CarState.java

package com.example.mylibrary.utils;

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
