// Caminho do arquivo: com/example/myapplication/TrackView.java

package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.myapplication.models.Car;

@SuppressLint("ViewConstructor")
public class TrackView extends View {

    private Car[] cars;
    private final Paint trackPaint = new Paint();
    private Bitmap trackBitmap;
    private long lastUpdateTime;
    private static final String TAG = "TrackView";

    public TrackView(Context context, Car[] cars) {
        super(context);
        try {
            this.cars = cars;
            initializeTrackBitmap();
            lastUpdateTime = System.currentTimeMillis();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao inicializar TrackView", e);
        }
    }

    private void initializeTrackBitmap() {
        try {
            trackBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.track);
            if (trackBitmap == null) {
                Log.e(TAG, "Erro ao carregar o bitmap da pista. Verifique o recurso R.drawable.track.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao inicializar o bitmap da pista", e);
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        try {
            super.onDraw(canvas);
            if (trackBitmap == null) {
                Log.e(TAG, "trackBitmap não foi carregado corretamente.");
                return;
            }
            drawTrack(canvas);
            drawCars(canvas);

            // Evita atualizações se a simulação estiver pausada ou terminada
            if (isRunning()) {
                postInvalidateOnAnimation();  // Atualiza a tela de acordo com a taxa de quadros
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao desenhar a tela", e);
        }
    }

    private void drawTrack(Canvas canvas) {
        try {
            if (trackBitmap != null) {
                Rect destRect = new Rect(0, 0, canvas.getWidth(), canvas.getHeight());
                canvas.drawBitmap(trackBitmap, null, destRect, trackPaint);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao desenhar a pista", e);
        }
    }

    private void drawCars(Canvas canvas) {
        try {
            long currentTime = System.currentTimeMillis();
            double deltaTime = (currentTime - lastUpdateTime) / 1000.0; // Calcula o deltaTime em segundos
            lastUpdateTime = currentTime;

            for (Car car : cars) {
                try {
                    car.move(deltaTime);  // Passa deltaTime para o método de movimentação
                    car.draw(canvas);
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao atualizar ou desenhar carro " + car.getName(), e);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao desenhar os carros", e);
        }
    }

    public void updateCars(Car[] newCars) {
        try {
            // Atualiza apenas se houver uma mudança real na lista de carros
            if (newCars != null && (cars == null || newCars.length != cars.length)) {
                this.cars = newCars;
                invalidate();
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao atualizar a lista de carros", e);
        }
    }

    public Bitmap getTrackBitmap() {
        try {
            return trackBitmap;
        } catch (Exception e) {
            Log.e(TAG, "Erro ao obter o bitmap da pista", e);
            return null;
        }
    }

    // Método auxiliar para verificar o estado da simulação
    private boolean isRunning() {
        // Implemente essa função com a lógica para determinar se a simulação está em execução.
        return true; // Mude conforme necessário
    }
}
