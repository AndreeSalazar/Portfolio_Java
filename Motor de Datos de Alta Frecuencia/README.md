# Motor de Datos de Alta Frecuencia (Java + Rust)

![Demo](demo.gif)

Un motor que ingiere streams, procesa eventos y responde en tiempo real.

Arquitectura
- Java: Streams, networking, gestión de vida y orquestación de pipelines.
- Rust: Parsing ultra rápido de CSV, buffers y camino hacia SIMD.
- Modos: JNI en proceso e IPC por socket, con fallback JAVA.

Demostración
- Ingesta SPSC con ring buffer lock-free simple.
- Métricas: throughput por segundo y último notional calculado.

Cómo ejecutar
1) Compilar Java:
   - mkdir "Motor de Datos de Alta Frecuencia/java-core/out"
   - javac -d "Motor de Datos de Alta Frecuencia/java-core/out" "Motor de Datos de Alta Frecuencia/java-core/src/main/java/hf/HFRustAdapter.java" "Motor de Datos de Alta Frecuencia/java-core/src/main/java/hf/HFEngine.java" "Motor de Datos de Alta Frecuencia/java-core/src/main/java/hf/HFMain.java"
2) Compilar Rust:
   - cd "Motor de Datos de Alta Frecuencia/rust-core" && cargo build
3) Ejecutar:
   - Java fallback: java -cp "Motor de Datos de Alta Frecuencia/java-core/out" hf.HFMain
   - JNI: set HF_NATIVE_LIB=ABSOLUTE_PATH_TO\\"Motor de Datos de Alta Frecuencia\\rust-core\\target\\debug\\hf_core.dll" && java --enable-native-access=ALL-UNNAMED -cp "Motor de Datos de Alta Frecuencia/java-core/out" hf.HFMain
   - IPC: set HF_NATIVE_LIB= && set HF_IPC_BIN=ABSOLUTE_PATH_TO\\"Motor de Datos de Alta Frecuencia\\rust-core\\target\\debug\\hf-ipc.exe" && java -cp "Motor de Datos de Alta Frecuencia/java-core/out" hf.HFMain

Casos de uso
- Trading, Telemetría, IoT, Monitoreo industrial.

WOW
- Concurrencia y pipelines en Java + parsing rápido en Rust = plataforma silenciosa y eficiente.

