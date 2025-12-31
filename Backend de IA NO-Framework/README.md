# Backend de IA NO-Framework (Java + Python + Rust)

Qué es
- Java gestiona jobs y orquestación.
- Python entrena modelos rápidamente.
- Rust ejecuta inferencia con baja latencia.

Arquitectura
- Modos: JNI para ejecución en proceso, IPC por socket y fallback JAVA.
- API JSON uniforme para training/inference.

Cómo ejecutar
1) Entrenamiento Python:
   - python "Backend de IA NO-Framework/python-train/train.py"
2) Compilar Java:
   - mkdir "Backend de IA NO-Framework/java-backend/out"
   - javac -d "Backend de IA NO-Framework/java-backend/out" "Backend de IA NO-Framework/java-backend/src/main/java/ia/IARustAdapter.java" "Backend de IA NO-Framework/java-backend/src/main/java/ia/JobManager.java" "Backend de IA NO-Framework/java-backend/src/main/java/ia/Main.java"
3) Compilar Rust:
   - cd "Backend de IA NO-Framework/rust-infer" && cargo build
4) Ejecutar:
   - JAVA fallback: java -cp "Backend de IA NO-Framework/java-backend/out" ia.Main
   - JNI: set IA_NATIVE_LIB=ABSOLUTE_PATH_TO\\"Backend de IA NO-Framework\\rust-infer\\target\\debug\\ia_infer.dll" && java --enable-native-access=ALL-UNNAMED -cp "Backend de IA NO-Framework/java-backend/out" ia.Main
   - IPC: set IA_NATIVE_LIB= && set IA_IPC_BIN=ABSOLUTE_PATH_TO\\"Backend de IA NO-Framework\\rust-infer\\target\\debug\\infer-ipc.exe" && java -cp "Backend de IA NO-Framework/java-backend/out" ia.Main

Demostración
- Separación de training (Python) e inference (Rust).
- Respuesta en tiempo real con pipeline simple.

Casos de uso
- Empresas reales, IA embebida y edge computing.

WOW
- Llevar IA a producción con orquestación, training separado e inferencia eficiente.

