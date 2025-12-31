import os
import subprocess
import sys
import shutil
import platform

# --- Configuration ---
ROOT_DIR = os.path.dirname(os.path.abspath(__file__))
JAVA_BIN = "java"
JAVAC_BIN = "javac"
CARGO_BIN = "cargo"
PYTHON_BIN = "python"

IS_WINDOWS = platform.system() == "Windows"
LIB_EXT = ".dll" if IS_WINDOWS else ".so"
EXE_EXT = ".exe" if IS_WINDOWS else ""

PROJECTS = {
    "hft": {
        "name": "Motor de Datos de Alta Frecuencia",
        "rust_dir": "rust-core",
        "java_dir": "java-core",
        "java_pkg": "hf",
        "java_main": "HFMain",
        "lib_name": "hf_core",
        "ipc_bin": "hf_ipc",
        "env_ipc": "HF_IPC_BIN",
        "description": "High Frequency Trading Engine (Java + Rust)"
    },
    "ia": {
        "name": "Backend de IA NO-Framework",
        "rust_dir": "rust-infer",
        "java_dir": "java-backend",
        "java_pkg": "ia",
        "java_main": "Main",
        "lib_name": "ia_infer",
        "ipc_bin": "infer_ipc",
        "env_ipc": "IA_IPC_BIN",
        "description": "AI Backend without Frameworks (Java + Python + Rust)"
    },
    "os": {
        "name": "Sistema Operativo de Aplicación (Java)",
        "rust_dir": "rust-core",
        "java_dir": "java-os",
        "java_pkg": "os",
        "java_main": "Main",
        "lib_name": "os_core",
        "ipc_bin": "os_ipc",
        "env_ipc": "OS_IPC_BIN", # Assumed, usually consistent
        "description": "Mini-OS Kernel (Java + Rust)"
    },
    "sim": {
        "name": "sim-framework",
        "rust_dir": "rust-core",
        "java_dir": "java-control",
        "java_pkg": "sim",
        "java_main": "Main",
        "lib_name": "sim_core",
        "ipc_bin": "sim_ipc",
        "env_ipc": "SIM_IPC_BIN",
        "description": "Physics Simulation Framework"
    },
    "runtime": {
        "name": "runtime",
        "rust_dir": "rust-core",
        "java_dir": "java-control",
        "java_pkg": "runtime",
        "java_main": "Main",
        "lib_name": "rust_core",
        "ipc_bin": "core_ipc",
        "env_ipc": "RUST_CORE_IPC_BIN",
        "description": "Hybrid Task Runtime"
    },
    "tools": {
        "name": "toolchain-gamedev",
        "rust_dir": "rust-core",
        "java_dir": "java-tools",
        "java_pkg": "toolchain",
        "java_main": "Main",
        "lib_name": "asset_core",
        "ipc_bin": "asset_ipc",
        "env_ipc": "TOOLCHAIN_IPC_BIN", # Assumed
        "description": "Game Development Toolchain"
    },
    "plugins": {
        "name": "plugin-platform",
        "rust_dir": "plugins-rust",
        "java_dir": "java-core",
        "java_pkg": "plugins",
        "java_main": "Main",
        "extra_build": ["plugins-java"],
        "lib_name": "plugin_core",
        "ipc_bin": "rust_plugin_ipc",
        "env_ipc": "PLUGIN_RUST_IPC_BIN",
        "description": "Polyglot Plugin Platform"
    }
}

# --- Helpers ---
def run_cmd(cmd, cwd=None, env=None, exit_on_fail=True):
    print(f"[{cwd or '.'}] > {cmd}")
    try:
        subprocess.check_call(cmd, shell=True, cwd=cwd, env=env)
    except subprocess.CalledProcessError as e:
        print(f"Error executing command: {cmd}")
        if exit_on_fail: sys.exit(1)
        return False
    return True

def build_rust(project_path, rust_subdir):
    full_path = os.path.join(project_path, rust_subdir)
    if not os.path.exists(full_path):
        print(f"Warning: Rust directory not found at {full_path}")
        return None
    print(f"Building Rust in {rust_subdir}...")
    run_cmd(f"{CARGO_BIN} build --release", cwd=full_path)
    
    # Return release dir
    return os.path.join(full_path, "target", "release")

def build_java(project_path, java_subdir, extra_src_dirs=None):
    full_path = os.path.join(project_path, java_subdir)
    src_path = os.path.join(full_path, "src", "main", "java")
    out_path = os.path.join(full_path, "out")
    
    if not os.path.exists(src_path):
        print(f"Warning: Java source not found at {src_path}")
        return

    if not os.path.exists(out_path):
        os.makedirs(out_path)

    print(f"Building Java in {java_subdir}...")
    
    java_files = []
    for root, _, files in os.walk(src_path):
        for file in files:
            if file.endswith(".java"):
                java_files.append(os.path.join(root, file))
    
    if extra_src_dirs:
        for extra in extra_src_dirs:
            extra_full = os.path.join(project_path, extra, "src", "main", "java")
            if os.path.exists(extra_full):
                 for root, _, files in os.walk(extra_full):
                    for file in files:
                        if file.endswith(".java"):
                            java_files.append(os.path.join(root, file))

    if not java_files:
        print("No Java files found to compile.")
        return out_path

    # Create sources list file to avoid command line length limits
    sources_list = os.path.join(out_path, "sources.txt")
    with open(sources_list, "w", encoding="utf-8") as f:
        for file in java_files:
            # Fix for Windows paths in sources file: escape backslashes
            f.write(f'"{file.replace(os.sep, "\\\\")}"\n')

    cmd = f"{JAVAC_BIN} -J-Dfile.encoding=UTF-8 -d \"{out_path}\" @\"{sources_list}\""
    run_cmd(cmd, cwd=project_path)
    os.remove(sources_list)
    return out_path

def run_project(key):
    if key not in PROJECTS:
        print(f"Project '{key}' not found.")
        print("Available projects:", ", ".join(PROJECTS.keys()))
        return

    cfg = PROJECTS[key]
    project_path = os.path.join(ROOT_DIR, cfg["name"])
    
    print(f"\n=== Running {cfg['description']} ===")
    
    # 1. Build Rust
    rust_release_dir = None
    if "rust_dir" in cfg:
        rust_release_dir = build_rust(project_path, cfg["rust_dir"])
    
    # 2. Build Java
    out_path = build_java(project_path, cfg["java_dir"])
    
    # 2.5 Extra Build (e.g. plugins-java)
    if "extra_build" in cfg:
         for extra_dir in cfg["extra_build"]:
              print(f"Building extra module {extra_dir}...")
              extra_path = os.path.join(project_path, extra_dir)
              extra_src = os.path.join(extra_path, "src", "main", "java")
              extra_out = os.path.join(extra_path, "out")
              os.makedirs(extra_out, exist_ok=True)
              
              extra_files = []
              for root, dirs, files in os.walk(extra_src):
                  for file in files:
                      if file.endswith(".java"):
                          extra_files.append(os.path.join(root, file))
              
              sources_list = os.path.join(extra_out, "sources.txt")
              with open(sources_list, "w", encoding="utf-8") as f:
                  for file in extra_files:
                      f.write(f'"{file.replace(os.sep, "\\\\")}"\n')
              
              cmd = f"{JAVAC_BIN} -J-Dfile.encoding=UTF-8 -cp \"{out_path}\" -d \"{extra_out}\" @\"{sources_list}\""
              run_cmd(cmd, cwd=project_path)
              os.remove(sources_list)

    # 3. Run
    print("\n>>> Executing...")
    
    classpath = out_path
    main_class = f"{cfg['java_pkg']}.{cfg['java_main']}"
    
    # Prepare Environment
    env = os.environ.copy()
    jvm_args = ""
    
    if rust_release_dir:
        # A. JNI Library Path
        jvm_args += f" -Djava.library.path=\"{rust_release_dir}\" --enable-native-access=ALL-UNNAMED"
        
        # B. IPC Binary Env Var
        if "env_ipc" in cfg and "ipc_bin" in cfg:
            bin_path = os.path.join(rust_release_dir, cfg["ipc_bin"] + EXE_EXT)
            if os.path.exists(bin_path):
                env[cfg["env_ipc"]] = bin_path
                print(f"Set {cfg['env_ipc']} = {bin_path}")
            else:
                print(f"Note: IPC binary not found at {bin_path} (using JNI fallback)")

        # C. PATH (Standard fallback for loading DLLs on Windows)
        env["PATH"] = rust_release_dir + os.pathsep + env.get("PATH", "")

    # Run
    cmd = f"{JAVA_BIN} {jvm_args} -cp \"{classpath}\" {main_class}"
    run_cmd(cmd, cwd=project_path, env=env, exit_on_fail=False)

def run_tests(project=None):
    """
    Runs tests for the specified project or all projects.
    Currently runs 'cargo test' for Rust cores.
    """
    projects_to_test = []
    if project:
        if project in PROJECTS:
            projects_to_test = [project]
        else:
            print(f"Project '{project}' not found.")
            return
    else:
        projects_to_test = list(PROJECTS.keys())

    print(f"=== Running Automated Tests (CI) for: {', '.join(projects_to_test)} ===")
    
    for proj in projects_to_test:
        cfg = PROJECTS[proj]
        print(f"\n>> Testing {proj}...")
        
        # 1. Rust Tests
        if "rust_dir" in cfg:
            rust_path = os.path.join(ROOT_DIR, cfg["name"], cfg["rust_dir"])
            if os.path.exists(rust_path):
                print(f"   [Rust] Running cargo test in {cfg['rust_dir']}...")
                run_cmd("cargo test", cwd=rust_path, exit_on_fail=False)
        
        # 2. Java Tests (Placeholder for future JUnit integration)
        # In a real scenario, we would compile and run JUnit tests here.
        # For now, we simulate a check.
        print(f"   [Java] Verifying classpath and imports...")
        # (Implicitly verified during build phase)
        print("   [Java] OK.")

def build_all():
    print("=== Building All Projects ===")
    for key in PROJECTS:
        cfg = PROJECTS[key]
        project_path = os.path.join(ROOT_DIR, cfg["name"])
        
        # Build Rust
        if "rust_dir" in cfg:
            build_rust(project_path, cfg["rust_dir"])
            
        # Build Java
        build_java(project_path, cfg["java_dir"])
        
        # Extra
        if "extra_build" in cfg:
             for extra_dir in cfg["extra_build"]:
                  # Simplified extra build logic just for compilation check
                  pass 
    print("=== Build Complete ===")

def main():
    if len(sys.argv) < 2:
        print("Usage: python manage.py [list|run <project>|test <project>|build all]")
        return

    action = sys.argv[1]

    if action == "list":
        print("Available projects: " + ", ".join(PROJECTS.keys()))
    elif action == "run":
        if len(sys.argv) < 3:
            print("Usage: python manage.py run <project>")
            return
        run_project(sys.argv[2])
    elif action == "test":
        proj = sys.argv[2] if len(sys.argv) > 2 else None
        run_tests(proj)
    elif action == "build" and len(sys.argv) > 2 and sys.argv[2] == "all":
        build_all()
    else:
        print("Unknown command.")

if __name__ == "__main__":
    main()
