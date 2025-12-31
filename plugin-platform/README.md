# Plataforma de Plugins Políglota

> **"Extiende tu aplicación en Java, Python o Rust sin reiniciar."**

![Plugins Demo](demo.gif)

## ❓ El Problema Real
Los sistemas monolíticos son rígidos. Para añadir una nueva funcionalidad, necesitas recompilar y redesplegar todo el servidor, lo que causa **downtime**. Además, obligas a todos los desarrolladores a usar el mismo lenguaje.

## 🛠 La Solución Arquitectónica
Un sistema de plugins agnóstico del lenguaje que permite **Hot-Swapping** (carga en caliente):

1.  **Java (Plugin Host)**: Define la interfaz `Plugin`. Usa `URLClassLoader` para cargar `.jar` externos dinámicamente.
2.  **Rust/Python (Native Plugins)**: A través de adaptadores JNI, el sistema puede cargar librerías compartidas (`.dll`/`.so`) o scripts de Python como si fueran plugins nativos de Java.

### ¿Por qué es difícil?
Gestionar el ciclo de vida (cargar/descargar) de librerías nativas es complejo porque un error de memoria en C/Rust puede tumbar la JVM. Este sistema implementa "Safe Handles" para evitar crashes.

## ⚙️ Cómo Ejecutar
Carga plugins de prueba en los 3 lenguajes:

```bash
python ../manage.py run plugins
```

## 📈 Escalabilidad
Esta arquitectura permite ecosistemas tipo "Marketplace" (como VS Code o Eclipse), donde la comunidad puede contribuir extensiones en su lenguaje preferido sin tocar el núcleo del sistema.
