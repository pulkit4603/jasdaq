import pandas as pd
import matplotlib.pyplot as plt
import glob
import os

# Find all benchmark reports
reports = glob.glob('benchmark-reports/benchmark_*.csv')

# Load the most recent report
latest_report = max(reports, key=os.path.getctime)
data = pd.read_csv(latest_report)

# Plot latency metrics
latency_metrics = data[data['Metric'].str.contains('Latency')]
plt.figure(figsize=(10, 6))
plt.bar(latency_metrics['Metric'], latency_metrics['Value'])
plt.title('Latency Metrics')
plt.ylabel('Time (ms)')
plt.xticks(rotation=45)
plt.tight_layout()
plt.savefig('benchmark-reports/latency_chart.png')

# Plot throughput metrics
throughput_metrics = data[data['Metric'].str.contains('Throughput')]
plt.figure(figsize=(10, 6))
plt.bar(throughput_metrics['Metric'], throughput_metrics['Value'])
plt.title('Throughput Metrics')
plt.ylabel('Orders/Second')
plt.xticks(rotation=45)
plt.tight_layout()
plt.savefig('benchmark-reports/throughput_chart.png')

# Load latency distributions if available
distributions = glob.glob('benchmark-reports/*_distribution_*.csv')
if distributions:
    latest_dist = max(distributions, key=os.path.getctime)
    dist_data = pd.read_csv(latest_dist)
    
    plt.figure(figsize=(10, 6))
    plt.hist(dist_data['Latency_ns'] / 1000000, bins=50, alpha=0.75)
    plt.title('Latency Distribution')
    plt.xlabel('Latency (ms)')
    plt.ylabel('Frequency')
    plt.grid(True, alpha=0.3)
    plt.savefig('benchmark-reports/latency_distribution.png')

print("Visualization complete!")
