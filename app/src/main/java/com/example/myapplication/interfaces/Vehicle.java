// Caminho do arquivo: com/example/myapplication/Vehicle.java

package com.example.myapplication.interfaces;

import android.graphics.Bitmap;

public interface Vehicle {
    void startRace(Bitmap trackBitmap, int trackWidth, int trackHeight);
    void pauseRace();
    void resumeRace();
    void stopRace();
}

