#!/bin/bash

# Move to project root directory where pom.xml is located
cd /home/pulkit/dev/projects/solo/jasdaq

# Compile the code first
mvn clean compile

# Create benchmark-reports directory if it doesn't exist
mkdir -p benchmark-reports

# Run only Industry Scenarios with proper memory settings
echo "Running Industry Scenarios Benchmark..."
java -Xms512m -Xmx1g -cp target/classes com.pga.jasdaq.benchmark.IndustryScenariosReport 10000

# Generate visualization if Python is available
if command -v python3 &>/dev/null; then
    echo "Generating benchmark visualization..."
    python3 benchmark_visualizer.py
    echo "Visualization complete. Check benchmark-reports directory for charts."
fi

echo "Benchmark execution completed"