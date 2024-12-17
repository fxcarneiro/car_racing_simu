package com.example.myapplication.utils;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * Classe para gerenciar a criação e validação de arquivos de métricas.
 */
public class MetricsFileManager {

    private static final String TAG = "MetricsFileManager";

    /**
     * Garante a criação do arquivo de métricas.
     *
     * @param context  Contexto da aplicação.
     * @param fileName Nome do arquivo de métricas.
     * @return O arquivo criado ou validado.
     */
    public static File getOrCreateMetricsFile(Context context, String fileName) {
        if (context == null) {
            Log.e(TAG, "Contexto é nulo. Não é possível acessar diretórios.");
            return null;
        }

        File dir = context.getExternalFilesDir(null); // Diretório externo privado
        if (dir == null) {
            Log.e(TAG, "Falha ao acessar o diretório externo. Verifique permissões ou espaço no armazenamento.");
            return null;
        }

        if (!dir.exists()) {
            boolean dirCreated = dir.mkdirs();
            if (!dirCreated) {
                Log.e(TAG, "Falha ao criar o diretório: " + dir.getAbsolutePath());
                return null;
            } else {
                Log.d(TAG, "Diretório criado com sucesso: " + dir.getAbsolutePath());
            }
        }

        File file = new File(dir, fileName);
        try {
            if (!file.exists()) {
                boolean fileCreated = file.createNewFile();
                if (!fileCreated) {
                    Log.e(TAG, "Falha ao criar o arquivo: " + file.getAbsolutePath());
                    return null;
                } else {
                    Log.d(TAG, "Arquivo criado com sucesso: " + file.getAbsolutePath());
                }
            } else {
                Log.d(TAG, "Arquivo já existente: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            Log.e(TAG, "Erro ao criar ou acessar o arquivo: " + fileName, e);
            return null;
        }

        return file;
    }
}
