# Plataforma de Plugins Políglota

![Demo](demo.gif)

Un sistema que permite cargar y ejecutar extensiones escritas en **Java**, **Rust** y **Python** dentro de la misma aplicación host.

## Arquitectura
- **Java Host**: Gestiona el ciclo de vida y registro de plugins.
- **Rust Plugin**: Compilado a librería dinámica (`.dll`/`.so`) cargada via JNI.
- **Python Plugin**: Ejecutado via Process/Binding embebido.

## Ejecución
1. `cd plugins-rust && cargo build --release`
2. `cd java-core && javac -d out src/main/java/plugins/*.java`
3. `java -cp out plugins.Main`
