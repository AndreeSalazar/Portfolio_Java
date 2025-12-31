# Portfolio de Plataformas (Java + Rust + Python)

Este repositorio demuestra arquitectura de sistemas avanzada, combinando la robustez enterprise de **Java**, el rendimiento de sistema de **Rust** y la flexibilidad de **Python**.

## 🚀 Proyectos Destacados

### 1. Motor de Datos de Alta Frecuencia (HFT)
**Concepto**: Ingesta de streams, procesamiento de eventos y respuesta en tiempo real.
- **Tech**: Java (Streams/Net) + Rust (Parsing/Buffers).
- **Key**: Concurrencia lock-free, latencia mínima.

![HFT Demo](Motor%20de%20Datos%20de%20Alta%20Frecuencia/demo.gif)

---

### 2. Backend de IA NO-Framework
**Concepto**: Infraestructura de IA agnóstica de frameworks pesados para producción.
- **Tech**: Java (Job Manager) + Python (Training) + Rust (Inference).
- **Key**: Separación de training/inference, JNI/IPC fallback.

![IA Demo](Backend%20de%20IA%20NO-Framework/demo.gif)

---

### 3. Sistema Operativo de Aplicación (Java-OS)
**Concepto**: Un mini-kernel para gestionar ciclo de vida de aplicaciones y recursos.
- **Tech**: Java (Kernel/Scheduler) + Rust (Memory/IO).
- **Key**: Diseño de sistemas, gestión de procesos, no-CRUD.

![OS Demo](Sistema%20Operativo%20de%20Aplicación%20(Java)/demo.gif)

---

### 4. Framework de Simulación (Sim-Framework)
**Concepto**: Motor físico y de simulación de entornos.
- **Tech**: Java (Control) + Rust (Physics Core).

![Sim Demo](sim-framework/demo.gif)

---

## 🛠 Arquitectura General
Todos los proyectos siguen principios de diseño modular:
- **Java**: Orquestador, lógica de negocio de alto nivel, thread management.
- **Rust**: Hot-paths, gestión de memoria, parsing, operaciones costosas.
- **Python**: Scripting, glue-code, training, tooling.

### Patrones Implementados
- **JNI & IPC**: Capacidad de ejecutar código nativo en el mismo proceso (JNI) o separado (IPC/TCP) para robustez.
- **Uniform JSON API**: Comunicación estandarizada entre lenguajes.
- **Fallback Mechanisms**: Si el módulo nativo falla, el sistema degrada suavemente a implementaciones Java.

## 📦 Estructura del Repositorio
- `Motor de Datos de Alta Frecuencia/`: Engine HFT.
- `Backend de IA NO-Framework/`: Infraestructura ML.
- `Sistema Operativo de Aplicación (Java)/`: Mini-OS Kernel.
- `plugin-platform/`: Sistema de plugins multilenguaje.
- `runtime/`: Runtime de ejecución híbrido.
- `sim-framework/`: Motor de física/simulación.
- `toolchain-gamedev/`: Herramientas de desarrollo.

## 🔧 Cómo Ejecutar
Cada carpeta contiene su propio `README.md` con instrucciones detalladas de compilación y ejecución.
Generalmente:
1. `cargo build --release` en carpetas `rust-core`.
2. `javac` en carpetas `java-*`.
3. Ejecutar el `Main` de Java.

---
*Generado con ❤️ y código.*
