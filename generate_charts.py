import matplotlib.pyplot as plt
import numpy as np
import os

# Configuration
OUTPUT_DIR = os.path.join("docs", "images")
os.makedirs(OUTPUT_DIR, exist_ok=True)

# Style settings
plt.style.use('ggplot')
COLORS = {'Java': '#E76F00', 'Rust': '#A72145', 'Python': '#3572A5'}

def save_chart(filename):
    path = os.path.join(OUTPUT_DIR, filename)
    plt.tight_layout()
    plt.savefig(path, dpi=100) # 1200x800 approx
    print(f"Generated {path}")
    plt.close()

# 1. HFT Throughput Comparison
def plot_hft_throughput():
    labels = ['Java Standard', 'Polyglot (Rust Core)']
    values = [85, 202] # k msg/sec
    
    plt.figure(figsize=(10, 6))
    bars = plt.bar(labels, values, color=[COLORS['Java'], COLORS['Rust']])
    
    plt.title('HFT Engine Throughput (Thousands of Msg/sec)', fontsize=16)
    plt.ylabel('Messages / Second (x1000)', fontsize=12)
    plt.ylim(0, 250)
    
    for bar in bars:
        height = bar.get_height()
        plt.text(bar.get_x() + bar.get_width()/2., height,
                f'{height}k',
                ha='center', va='bottom', fontsize=14, fontweight='bold')
                
    save_chart('benchmark-hft-throughput.png')

# 2. HFT Latency (Lower is better)
def plot_hft_latency():
    labels = ['Java (GC Spikes)', 'Rust (Determinism)']
    values = [12, 0.8] # ms
    
    plt.figure(figsize=(10, 6))
    bars = plt.bar(labels, values, color=[COLORS['Java'], COLORS['Rust']])
    
    plt.title('99th Percentile Latency (Lower is Better)', fontsize=16)
    plt.ylabel('Latency (ms)', fontsize=12)
    
    for bar in bars:
        height = bar.get_height()
        plt.text(bar.get_x() + bar.get_width()/2., height,
                f'{height}ms',
                ha='center', va='bottom', fontsize=14, fontweight='bold')
                
    save_chart('benchmark-hft-latency.png')

# 3. AI Cold Start (Lower is better)
def plot_ia_cold_start():
    labels = ['Python (Docker)', 'Polyglot (Rust SIMD)']
    values = [2500, 80] # ms
    
    plt.figure(figsize=(10, 6))
    bars = plt.bar(labels, values, color=[COLORS['Python'], COLORS['Rust']])
    
    plt.title('AI Model Cold Start Time', fontsize=16)
    plt.ylabel('Time to First Inference (ms)', fontsize=12)
    plt.yscale('log') # Log scale because difference is huge
    
    for bar in bars:
        height = bar.get_height()
        plt.text(bar.get_x() + bar.get_width()/2., height,
                f'{height}ms',
                ha='center', va='bottom', fontsize=14, fontweight='bold')
                
    save_chart('benchmark-ia-start.png')

# 4. Simulation Objects (Higher is better)
def plot_sim_objects():
    labels = ['Java Pure', 'Polyglot (Rust SIMD)']
    values = [800, 5000] 
    
    plt.figure(figsize=(10, 6))
    bars = plt.bar(labels, values, color=[COLORS['Java'], COLORS['Rust']])
    
    plt.title('Max Physics Objects at 60 FPS', fontsize=16)
    plt.ylabel('Number of Objects', fontsize=12)
    
    for bar in bars:
        height = bar.get_height()
        plt.text(bar.get_x() + bar.get_width()/2., height,
                f'{height}',
                ha='center', va='bottom', fontsize=14, fontweight='bold')
                
    save_chart('benchmark-sim-objects.png')

if __name__ == "__main__":
    plot_hft_throughput()
    plot_hft_latency()
    plot_ia_cold_start()
    plot_sim_objects()
