#!/bin/bash
set -eo pipefail
IFS=$'\n\t'

release_targets=$(bazel query "filter(\"helm_push$\", //infrastructure/helm-chart/...)" --output label)

for target in $release_targets; do
  echo "Deploying $target"
  bazel run "$target"
done
