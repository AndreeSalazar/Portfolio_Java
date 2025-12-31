# Toolchain para Game Development

![Demo](demo.gif)

Una suite de herramientas de escritorio para procesar assets de videojuegos (texturas, modelos, sonidos).

## Arquitectura
- **Java (Swing)**: Provee una UI amigable y portable.
- **Rust**: Realiza operaciones costosas como compresión (LZ4/Gzip) y Hashing (SHA-256) en milisegundos.
- **Python**: Automatiza el empaquetado final del build.

## Ejecución
1. `cd rust-core && cargo build --release`
2. `cd java-tools && javac -d out src/main/java/toolchain/*.java`
3. `java -cp out toolchain.Main`
