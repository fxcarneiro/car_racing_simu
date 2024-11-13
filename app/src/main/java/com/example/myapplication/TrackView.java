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

/**
 * A classe TrackView é uma extensão de View responsável por renderizar a pista e os carros
 * na tela do dispositivo, atualizando continuamente durante a simulação.
 */
@SuppressLint("ViewConstructor")
public class TrackView extends View {

    private Car[] cars;                           // Array de carros para desenhar na pista
    private final Paint trackPaint = new Paint(); // Paint para desenhar a pista
    private Bitmap trackBitmap;                   // Bitmap da imagem da pista
    private long lastUpdateTime;                  // Tempo da última atualização da tela
    private static final String TAG = "TrackView";

    /**
     * Construtor que inicializa o TrackView e carrega o bitmap da pista.
     *
     * @param context Contexto da aplicação
     * @param cars    Array inicial de carros para desenhar na pista
     */
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

    /**
     * Carrega o bitmap da pista a partir dos recursos.
     */
    private void initializeTrackBitmap() {
        try {
            // Carrega a imagem da pista (verifique se R.drawable.track existe)
            trackBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.track);
            if (trackBitmap == null) {
                Log.e(TAG, "Erro ao carregar o bitmap da pista. Verifique o recurso R.drawable.track.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao inicializar o bitmap da pista", e);
        }
    }

    /**
     * Método chamado para desenhar a pista e os carros na tela.
     */
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        try {
            super.onDraw(canvas);
            if (trackBitmap == null) {
                Log.e(TAG, "trackBitmap não foi carregado corretamente.");
                return;
            }
            drawTrack(canvas);  // Desenha o fundo da pista
            drawCars(canvas);   // Desenha os carros na pista

            // Atualiza a tela continuamente enquanto a simulação está em execução
            if (isRunning()) {
                postInvalidateOnAnimation();  // Solicita uma nova atualização de tela na próxima taxa de quadros
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao desenhar a tela", e);
        }
    }

    /**
     * Desenha a imagem da pista no canvas.
     *
     * @param canvas Canvas no qual a pista será desenhada
     */
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

    /**
     * Desenha todos os carros na tela e atualiza suas posições com base no tempo decorrido.
     *
     * @param canvas Canvas no qual os carros serão desenhados
     */
    private void drawCars(Canvas canvas) {
        try {
            long currentTime = System.currentTimeMillis();
            double deltaTime = (currentTime - lastUpdateTime) / 1000.0; // Calcula deltaTime em segundos
            lastUpdateTime = currentTime;

            // Move e desenha cada carro individualmente
            for (Car car : cars) {
                try {
                    car.move(deltaTime);  // Passa deltaTime para o método de movimentação do carro
                    car.draw(canvas);     // Desenha o carro no canvas
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao atualizar ou desenhar carro " + car.getName(), e);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao desenhar os carros", e);
        }
    }

    /**
     * Atualiza a lista de carros para ser desenhada na tela.
     *
     * @param newCars Array atualizado de carros
     */
    public void updateCars(Car[] newCars) {
        try {
            // Atualiza apenas se a lista de carros foi realmente alterada
            if (newCars != null && (cars == null || newCars.length != cars.length)) {
                this.cars = newCars;
                invalidate(); // Solicita que a tela seja redesenhada
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao atualizar a lista de carros", e);
        }
    }

    /**
     * Retorna o bitmap atual da pista.
     *
     * @return Bitmap da pista
     */
    public Bitmap getTrackBitmap() {
        try {
            return trackBitmap;
        } catch (Exception e) {
            Log.e(TAG, "Erro ao obter o bitmap da pista", e);
            return null;
        }
    }

    /**
     * Verifica se a simulação está em execução.
     *
     * @return true se a simulação estiver em execução; caso contrário, false.
     * (Esta função deve ser implementada com a lógica adequada)
     */
    private boolean isRunning() {
        return true; // Alterar conforme necessário para retornar o estado real da simulação
    }
}
