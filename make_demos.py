import matplotlib.pyplot as plt
import matplotlib.animation as animation
import numpy as np
import os
import random

# Configuration
PROJECTS = {
    "hft": "Motor de Datos de Alta Frecuencia",
    "ia": "Backend de IA NO-Framework",
    "os": "Sistema Operativo de Aplicación (Java)",
    "plugins": "plugin-platform",
    "runtime": "runtime",
    "sim": "sim-framework",
    "tools": "toolchain-gamedev"
}

def ensure_dir(path):
    if not os.path.exists(path):
        os.makedirs(path)

def save_anim(anim, filename):
    print(f"Saving {filename}...")
    try:
        anim.save(filename, writer='pillow', fps=30)
        print(f"Saved {filename}")
    except Exception as e:
        print(f"Error saving {filename}: {e}")

# --- 1. HFT Engine Demo ---
def create_hft_demo():
    folder = PROJECTS["hft"]
    if not os.path.exists(folder): return
    
    fig, (ax1, ax2) = plt.subplots(2, 1, figsize=(8, 6), facecolor='#1e1e1e')
    fig.suptitle('HFT Engine: Real-time Ingestion & Analysis', color='white', fontsize=14)
    
    # Style
    for ax in [ax1, ax2]:
        ax.set_facecolor('#2d2d2d')
        ax.tick_params(colors='white')
        ax.grid(True, color='#444444', linestyle='--')
        for spine in ax.spines.values(): spine.set_color('white')

    # Data
    x = np.linspace(0, 10, 200)
    line1, = ax1.plot([], [], 'c-', lw=2, label='Price Stream')
    line2, = ax1.plot([], [], 'r.', markersize=8, label='Signals')
    
    bar_x = ['Events/s', 'Latency (us)', 'Queue']
    bars = ax2.bar(bar_x, [0, 0, 0], color=['#00ff00', '#ff00ff', '#ffff00'])
    ax2.set_ylim(0, 1000)
    
    ax1.set_xlim(0, 10)
    ax1.set_ylim(-2, 2)
    ax1.legend(loc='upper right', facecolor='#2d2d2d', edgecolor='white', labelcolor='white')

    def init():
        line1.set_data([], [])
        line2.set_data([], [])
        return line1, line2, *bars

    def update(frame):
        # Price stream
        phase = frame * 0.1
        y = np.sin(x + phase) + np.random.normal(0, 0.1, len(x))
        line1.set_data(x, y)
        
        # Signals (random spikes)
        if frame % 10 == 0:
            idx = random.randint(0, len(x)-1)
            line2.set_data([x[idx]], [y[idx]])
        else:
            line2.set_data([], [])
            
        # Metrics
        bars[0].set_height(800 + random.randint(-50, 50)) # Events/s
        bars[1].set_height(45 + random.randint(-5, 10))  # Latency
        bars[2].set_height(random.randint(0, 200))        # Queue
        
        return line1, line2, *bars

    ani = animation.FuncAnimation(fig, update, frames=100, init_func=init, blit=False)
    save_anim(ani, os.path.join(folder, 'demo.gif'))
    plt.close()

# --- 2. IA Backend Demo ---
def create_ia_demo():
    folder = PROJECTS["ia"]
    if not os.path.exists(folder): return
    
    fig, ax = plt.subplots(figsize=(8, 4), facecolor='#1e1e1e')
    fig.suptitle('IA Backend: Training vs Inference Pipeline', color='white')
    ax.set_facecolor('#1e1e1e')
    ax.axis('off')
    
    # Visual elements
    # Job Queue -> Training -> Inference
    
    rects = []
    texts = []
    
    # 3 Boxes
    colors = ['#3498db', '#e74c3c', '#2ecc71']
    labels = ['Job Manager (Java)', 'Training (Python)', 'Inference (Rust)']
    
    for i in range(3):
        rect = plt.Rectangle((0.1 + i*0.3, 0.4), 0.2, 0.2, color=colors[i], alpha=0.5)
        ax.add_patch(rect)
        rects.append(rect)
        txt = ax.text(0.2 + i*0.3, 0.5, labels[i], ha='center', va='center', color='white', fontsize=9, wrap=True)
        texts.append(txt)

    # Connecting arrows
    ax.arrow(0.3, 0.5, 0.08, 0, color='white', head_width=0.02)
    ax.arrow(0.6, 0.5, 0.08, 0, color='white', head_width=0.02)
    
    status_text = ax.text(0.5, 0.2, "Status: Idle", ha='center', color='yellow', fontsize=12)
    
    # Data flow particle
    particle, = ax.plot([], [], 'wo', markersize=10)

    def update(frame):
        f = frame % 60
        
        if f < 20: # Phase 1: Java
            rects[0].set_alpha(1.0)
            rects[1].set_alpha(0.3)
            rects[2].set_alpha(0.3)
            status_text.set_text("Status: Scheduling Job")
            particle.set_data([0.2], [0.5])
            
        elif f < 40: # Phase 2: Python
            rects[0].set_alpha(0.3)
            rects[1].set_alpha(1.0)
            rects[2].set_alpha(0.3)
            status_text.set_text("Status: Training Model (Loss: {:.2f})".format(np.exp(-f/10)))
            particle.set_data([0.5], [0.5])
            
        else: # Phase 3: Rust
            rects[0].set_alpha(0.3)
            rects[1].set_alpha(0.3)
            rects[2].set_alpha(1.0)
            status_text.set_text("Status: High-Perf Inference ({} req/s)".format(int(f*100)))
            particle.set_data([0.8], [0.5])
            
        return rects + [status_text, particle]

    ani = animation.FuncAnimation(fig, update, frames=60, blit=False)
    save_anim(ani, os.path.join(folder, 'demo.gif'))
    plt.close()

# --- 3. Mini OS Demo ---
def create_os_demo():
    folder = PROJECTS["os"]
    if not os.path.exists(folder): return

    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(10, 5), facecolor='#1e1e1e')
    fig.suptitle('Mini-OS: Kernel & Memory Management', color='white')
    
    # Memory Map (Grid)
    ax1.set_title("Physical Memory (Rust Core)", color='white')
    ax1.set_facecolor('#1e1e1e')
    ax1.axis('off')
    
    mem_grid = np.zeros((10, 10))
    im = ax1.imshow(mem_grid, cmap='magma', vmin=0, vmax=1)
    
    # Process Scheduler
    ax2.set_title("Process Scheduler (Java Kernel)", color='white')
    ax2.set_facecolor('#2d2d2d')
    ax2.set_xlim(0, 100)
    ax2.set_ylim(0, 5)
    ax2.tick_params(colors='white')
    
    bars = ax2.barh([1, 2, 3, 4], [0, 0, 0, 0], color=['cyan', 'magenta', 'yellow', 'lime'])
    ax2.set_yticks([1, 2, 3, 4])
    ax2.set_yticklabels(['P1', 'P2', 'P3', 'P4'])

    def update(frame):
        # Update memory
        if frame % 5 == 0:
            x, y = random.randint(0, 9), random.randint(0, 9)
            mem_grid[x, y] = random.random()
            im.set_data(mem_grid)
            
        # Update processes
        for i, bar in enumerate(bars):
            val = (frame * (i+1)) % 100
            bar.set_width(val)
            
        return [im] + list(bars)

    ani = animation.FuncAnimation(fig, update, frames=100, blit=False)
    save_anim(ani, os.path.join(folder, 'demo.gif'))
    plt.close()

# --- 4. Sim Framework Demo ---
def create_sim_demo():
    folder = PROJECTS["sim"]
    if not os.path.exists(folder): return
    
    fig, ax = plt.subplots(figsize=(6, 6), facecolor='#1e1e1e')
    fig.suptitle('Sim Framework: Physics Engine', color='white')
    ax.set_facecolor('black')
    ax.set_xlim(0, 10)
    ax.set_ylim(0, 10)
    ax.axis('off')
    
    n_particles = 10
    pos = np.random.rand(n_particles, 2) * 10
    vel = (np.random.rand(n_particles, 2) - 0.5) * 0.5
    colors = np.random.rand(n_particles, 3)
    
    scat = ax.scatter(pos[:,0], pos[:,1], s=100, c=colors)

    def update(frame):
        nonlocal pos, vel
        pos += vel
        
        # Bounce
        for i in range(n_particles):
            if pos[i, 0] < 0 or pos[i, 0] > 10: vel[i, 0] *= -1
            if pos[i, 1] < 0 or pos[i, 1] > 10: vel[i, 1] *= -1
            
        scat.set_offsets(pos)
        return scat,

    ani = animation.FuncAnimation(fig, update, frames=100, blit=True)
    save_anim(ani, os.path.join(folder, 'demo.gif'))
    plt.close()

# --- 5. Plugin Platform Demo ---
def create_plugin_demo():
    folder = PROJECTS["plugins"]
    if not os.path.exists(folder): return
    
    fig, ax = plt.subplots(figsize=(8, 4), facecolor='#1e1e1e')
    fig.suptitle('Plugin Platform: Polyglot Execution', color='white')
    ax.set_facecolor('#1e1e1e')
    ax.axis('off')
    
    # Core System
    core = plt.Circle((0.5, 0.5), 0.15, color='#ecf0f1', alpha=0.8)
    ax.add_patch(core)
    ax.text(0.5, 0.5, "Core\n(Java)", ha='center', va='center', fontsize=10)
    
    # Plugins
    plugins = [
        {'name': 'Rust', 'color': '#e67e22', 'pos': (0.2, 0.5)},
        {'name': 'Python', 'color': '#3498db', 'pos': (0.8, 0.5)},
        {'name': 'Java', 'color': '#e74c3c', 'pos': (0.5, 0.8)},
    ]
    
    patches = []
    for p in plugins:
        c = plt.Circle(p['pos'], 0.1, color=p['color'], alpha=0.5)
        ax.add_patch(c)
        patches.append(c)
        ax.text(p['pos'][0], p['pos'][1], p['name'], ha='center', va='center', color='white')

    status = ax.text(0.5, 0.2, "Idle", ha='center', color='yellow')

    def update(frame):
        idx = (frame // 20) % 3
        
        # Reset alphas
        for p in patches: p.set_alpha(0.3)
        core.set_color('#ecf0f1')
        
        # Activate one
        patches[idx].set_alpha(1.0)
        core.set_color(plugins[idx]['color'])
        
        status.set_text(f"Executing {plugins[idx]['name']} Plugin...")
        
        return patches + [core, status]

    ani = animation.FuncAnimation(fig, update, frames=60, blit=False)
    save_anim(ani, os.path.join(folder, 'demo.gif'))
    plt.close()

# --- 6. Runtime Demo ---
def create_runtime_demo():
    folder = PROJECTS["runtime"]
    if not os.path.exists(folder): return
    
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(8, 4), facecolor='#1e1e1e')
    fig.suptitle('Hybrid Runtime: Task Scheduling', color='white')
    
    # CPU Cores
    ax1.set_title("CPU Cores (Rust Workers)", color='white', fontsize=10)
    ax1.set_facecolor('#1e1e1e')
    ax1.axis('off')
    
    cores = []
    for i in range(4):
        rect = plt.Rectangle((0.1 + (i%2)*0.4, 0.6 - (i//2)*0.4), 0.3, 0.3, color='#34495e')
        ax1.add_patch(rect)
        cores.append(rect)
        ax1.text(0.25 + (i%2)*0.4, 0.75 - (i//2)*0.4, f"Core {i}", ha='center', color='white')
        
    # Task Queue
    ax2.set_title("Task Queue (Java)", color='white', fontsize=10)
    ax2.set_facecolor('#2d2d2d')
    ax2.set_xlim(0, 1)
    ax2.set_ylim(0, 10)
    ax2.axis('off')
    
    tasks = []
    for i in range(10):
        t = plt.Rectangle((0.1, i), 0.8, 0.8, color='#2ecc71')
        ax2.add_patch(t)
        tasks.append(t)

    def update(frame):
        # Add task
        if frame % 10 == 0:
            for t in tasks:
                if t.get_y() > 8: t.set_y(0)
                
        # Assign to core
        active_core = frame % 4
        for i, c in enumerate(cores):
            c.set_color('#e74c3c' if i == active_core else '#34495e')
            
        return cores + tasks

    ani = animation.FuncAnimation(fig, update, frames=60, blit=False)
    save_anim(ani, os.path.join(folder, 'demo.gif'))
    plt.close()

# --- 7. Toolchain Demo ---
def create_toolchain_demo():
    folder = PROJECTS["tools"]
    if not os.path.exists(folder): return
    
    fig, ax = plt.subplots(figsize=(8, 4), facecolor='#1e1e1e')
    fig.suptitle('Game Dev Toolchain: Asset Pipeline', color='white')
    ax.set_facecolor('#1e1e1e')
    ax.axis('off')
    
    # Steps
    steps = ['Load (Java)', 'Compress (Rust)', 'Hash (Rust)', 'Package (Python)']
    x_pos = [0.1, 0.35, 0.6, 0.85]
    
    for i, s in enumerate(steps):
        ax.text(x_pos[i], 0.5, s, ha='center', color='white', fontsize=8, wrap=True)
        ax.add_patch(plt.Circle((x_pos[i], 0.4), 0.02, color='white'))
        
    # Asset moving
    asset = plt.Rectangle((0.08, 0.38), 0.04, 0.04, color='cyan')
    ax.add_patch(asset)
    
    progress = ax.text(0.5, 0.2, "", ha='center', color='lime')

    def update(frame):
        f = frame % 100
        x = 0.1 + (f / 100) * 0.75
        asset.set_x(x)
        
        if x < 0.2: progress.set_text("Loading File...")
        elif x < 0.45: progress.set_text("Compressing (LZ4)...")
        elif x < 0.7: progress.set_text("Calculating SHA256...")
        else: progress.set_text("Packaging Build...")
        
        return [asset, progress]

    ani = animation.FuncAnimation(fig, update, frames=100, blit=False)
    save_anim(ani, os.path.join(folder, 'demo.gif'))
    plt.close()

# --- Run All ---
if __name__ == "__main__":
    print("Generating demos...")
    create_hft_demo()
    create_ia_demo()
    create_os_demo()
    create_sim_demo()
    create_plugin_demo()
    create_runtime_demo()
    create_toolchain_demo()
    print("Done!")
