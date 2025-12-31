# Portfolio de Plataformas (Java + Rust + Python)

Propósito
- Demostrar arquitectura de sistemas y plataformas, no apps.
- Diseños con IPC, FFI/JNI bien hechos y runtimes/modos alternos.

Módulos
- Runtime de Ejecución Híbrido (Java Control + Rust Core): orquestación Java, núcleo Rust, modos JNI/IPC/JAVA.
- Framework de Simulación en Tiempo Real (Java + Rust): escenarios, métricas y física/colisiones nativas.
- Toolchain Profesional para Game Dev (Java): editor de assets, profiler y pipelines con núcleo Rust y scripts Python.
- Plataforma de Plugins Multilenguaje (Java Core): plugins Java, módulos nativos Rust y extensiones Python.
- Motor de Datos de Alta Frecuencia (Java + Rust): ingesta, eventos y respuesta en tiempo real.

Razones
- Arquitectura de sistemas: separación de control/ejecución, APIs JSON uniformes.
- IPC, FFI, JNI: canales duales y ABI estable.
- Diseño de runtimes: patrones JVM-lite y selección de modos.
- Concurrencia y lock-free: ring buffers y SPSC donde aplica.
- Observabilidad y UX técnico: métricas y herramientas de apoyo.
- Seguridad y estabilidad: sandboxing por IPC y control de carga nativa.

Lenguajes
- Java: threads maduros, GC estable, ecosistema enterprise, hot-reload, UI técnica, ClassLoaders.
- Rust: ejecución determinista, seguridad de memoria, performance crítica, parsing y buffers.
- Python: scripting, testing, automatización y prototipos rápidos.

Cómo ejecutar
- Cada módulo incluye instrucción de build y ejecución en su carpeta.
- Modos nativos: establecer variables de entorno para DLL/EXE y usar --enable-native-access según corresponda.

GitHub
- Incluye .gitignore y estructura ordenada para publicación.
- Sugerencia de publicación:
  - git init
  - git add .
  - git commit -m "Portfolio de Plataformas: Java + Rust + Python"
  - git branch -M main
  - git remote add origin https://github.com/<usuario>/<repo>.git
  - git push -u origin main

