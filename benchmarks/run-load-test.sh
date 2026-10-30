#!/usr/bin/env bash
# Template for one load-test run. Copy the summary it prints into results.md.
# usage: SKI_API_BASE_PATH=http://<host>:8080/server ./benchmarks/run-load-test.sh
set -euo pipefail

: "${SKI_API_BASE_PATH:?Set SKI_API_BASE_PATH to the deployed server, e.g. http://localhost:8080/server}"

cd "$(dirname "$0")/.."
mvn -q -pl client -am package

mkdir -p benchmarks/runs
run_dir="benchmarks/runs/$(date +%Y%m%d-%H%M%S)"
mkdir -p "$run_dir"

(cd "$run_dir" && java -jar ../../../client/target/client.jar | tee summary.txt)
echo "Results saved in $run_dir"
