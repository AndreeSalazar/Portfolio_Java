# Systems Engineering Portfolio (Java + Rust + Python)

> **"Diseño sistemas donde el runtime importa más que el framework."**

Este repositorio no es una colección de scripts; es una demostración de **ingeniería de sistemas de alto rendimiento**. 

Aquí encontrarás implementaciones de arquitecturas complejas (HFT, Motores de Física, Kernels de Aplicación) donde cada lenguaje tiene un rol crítico y justificado, no por preferencia personal, sino por **necesidad técnica**.

---

## 👨‍💻 Perfil de Ingeniería

Soy un **Ingeniero de Sistemas** enfocado en la capa de infraestructura y rendimiento. Mi valor no está en usar librerías, sino en entender cómo funcionan por dentro y construirlas cuando es necesario.

*   **Lo que construyo**: Motores de ejecución, sistemas distribuidos, herramientas de infraestructura crítica.
*   **Mi Stack**: 
    *   **Java** como *Control Plane* (Estabilidad, GC, Ecosistema).
    *   **Rust** como *Data Plane* (Determinismo, SIMD, Gestión de Memoria).
    *   **Python** como *Glue Code* (Scripting, IA, Tooling).
*   **Mi Filosofía**: "Zero-Overhead Interop". Si dos lenguajes se hablan, debe ser sin serialización costosa (JNI/FFI directo).

---

## ⚡ Quick Start (Ejecución Inmediata)

No pierdas tiempo configurando entornos. He creado un orquestador (`manage.py`) que compila, prueba y ejecuta todo el stack nativo.

**Requisitos**: Java 17+, Rust (Cargo), Python 3.8+.

```bash
# 1. Ver qué sistemas hay disponibles
python manage.py list

# 2. Ejecutar Tests Automatizados (CI Simulation)
python manage.py test

# 3. Ejecutar una demo completa (Ej: Motor de Trading HFT)
python manage.py run hft
```

---

## 🚀 Catálogo de Sistemas (Problem & Solution)

### 1. [Motor de Trading de Alta Frecuencia (HFT)](./Motor%20de%20Datos%20de%20Alta%20Frecuencia/README.md)
*   **El Problema**: Procesar millones de eventos financieros con latencia de microsegundos es imposible si el Garbage Collector pausa el sistema aleatoriamente.
*   **La Solución**: Java gestiona la red (Netty/NIO), pero pasa los bytes crudos a **Rust** para el matching.
*   **Arquitectura**: *Zero-Copy Deserialization* sobre JNI Critical Arrays.
*   **Métricas**: >200k msg/sec, latencia <1ms.

### 2. [Backend de IA "Bare-Metal"](./Backend%20de%20IA%20NO-Framework/README.md)
*   **El Problema**: Servir modelos de IA en producción suele requerir contenedores pesados (Docker) y alta latencia HTTP.
*   **La Solución**: Un servidor monolítico donde Java recibe la petición y **Rust** ejecuta la inferencia matemática (MatMul) directamente en CPU usando AVX2.
*   **Arquitectura**: Separación estricta Training (Python) vs Inference (Rust).
*   **Métricas**: 100x más rápido en Cold Start que contenedores Python.

### 3. [Kernel de Aplicación (Java-OS)](./Sistema%20Operativo%20de%20Aplicación%20(Java)/README.md)
*   **El Problema**: Ejecutar código de terceros (plugins) es inseguro y puede tumbar el servidor principal.
*   **La Solución**: Un microkernel en Java que aísla procesos y usa **Rust** para simular una MMU (Memory Management Unit) segura.
*   **Arquitectura**: Simulación de Syscalls y Aislamiento de Memoria Virtual.

### 4. [Plataforma de Plugins Políglota](./plugin-platform/README.md)
*   **El Problema**: Los sistemas monolíticos son difíciles de extender sin recompilar.
*   **La Solución**: Un sistema de carga dinámica (Hot-Swap) que permite inyectar lógica en Java, Python o Rust en tiempo de ejecución.
*   **Arquitectura**: `URLClassLoader` dinámico + FFI Bridge compartido.

### 5. [Runtime Distribuido de Alto Rendimiento](./runtime/README.md)
*   **El Problema**: Las tareas intensivas en CPU bloquean el Event Loop de los servidores web tradicionales.
*   **La Solución**: Un modelo de *Work-Stealing* donde Java despacha promesas (`Future`) y un pool de hilos en **Rust** las resuelve.
*   **Arquitectura**: Async/Await pattern cruzando fronteras de lenguaje.

---

## 📊 Performance at a Glance

| Característica | Arquitectura Típica Java | Arquitectura "Polyglot Tiered" (Este Portfolio) |
| :--- | :--- | :--- |
| **Gestión de Memoria** | Heap (GC pauses impredecibles) | **Off-Heap (Rust manual) + Stack** |
| **Cálculo Numérico** | Lento (Boxed Integers) | **SIMD / Vectorización Nativa** |
| **Interoperabilidad** | REST/JSON (Lento) | **JNI/Shared Memory (Instantáneo)** |
| **Latencia** | Variable (Jitter alto) | **Determinista (Jitter bajo)** |

---

## 🛠 Arquitectura "Polyglot Tiered"

Todos los proyectos siguen este patrón de diseño estricto:

| Capa | Tecnología | Responsabilidad |
| :--- | :--- | :--- |
| **Tier 1: Control** | **Java (JVM)** | Orquestación, APIs, Gestión de Errores, Logs. |
| **Tier 2: Core** | **Rust** | Algoritmos, Acceso a Memoria, Física, Criptografía. |
| **Tier 3: Tooling** | **Python** | Builds, Tests de Integración, Entrenamiento de Modelos. |

> Hecho con ❤️ por **Eddi Andreé Salazar Matos** - *Ingeniero de Sistemas*.
