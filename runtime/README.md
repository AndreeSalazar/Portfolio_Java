# Runtime Híbrido de Alto Rendimiento

![Demo](demo.gif)

Un motor de ejecución de tareas (Task Scheduler) que delega el trabajo pesado a workers nativos.

## Arquitectura
- **Java**: Crea `EngineTask` (futuros) y los envía a la cola.
- **Rust**: `ThreadPool` que consume tareas de la cola y procesa cálculos numéricos.
- **Beneficio**: La facilidad de concurrencia de Java con la velocidad de cómputo de Rust.

## Ejecución
1. `cd rust-core && cargo build --release`
2. `cd java-control && javac -d out src/main/java/runtime/*.java`
3. `java -cp out runtime.Main`
