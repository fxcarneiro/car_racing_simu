plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}


// Opcional: configuração de buildScript caso precise de classpath para dependências adicionais
buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.2")
    }
}

