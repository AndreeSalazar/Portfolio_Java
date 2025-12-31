# Sistema Operativo de Aplicación (Java)

![Demo](demo.gif)

## Qué es
Un mini-OS para aplicaciones que gestiona lifecycle, procesos, recursos y eventos.

## Qué demuestras
- **Pensamiento de sistema**: Diseño de kernel lógico y scheduling.
- **Rust**: Módulos críticos de memoria y IO.
- **Nivel sistema, no CRUD**: Arquitectura orientada a eventos y gestión de recursos.

## Arquitectura
- **Java Kernel**: Scheduler (Round Robin), Event Bus, Process Table.
- **Rust Core**: Gestión de memoria física simulada, IO operations.
- **Modos**: JNI (in-process), IPC (socket TCP), JAVA (mock fallback).

## Ejecución
1. Compilar Rust: `cd rust-core && cargo build --release`
2. Compilar Java: `cd java-os && javac -d out src/main/java/os/*.java`
3. Ejecutar: `java -cp out os.Main`
