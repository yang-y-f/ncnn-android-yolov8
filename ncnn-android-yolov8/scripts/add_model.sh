#!/usr/bin/env bash
set -euo pipefail
if [[ $# -ne 3 ]]; then
  echo "Usage: $0 <token> <path/to/yolov8{token}.param> <path/to/yolov8{token}.bin>" >&2
  exit 1
fi
TOKEN="$1"
PP="$2"
BB="$3"
APP_DIR="$(cd "$(dirname "$0")/.." && pwd)"
ASSETS_DIR="$APP_DIR/app/src/main/assets"
mkdir -p "$ASSETS_DIR"
cp -f "$PP" "$ASSETS_DIR/yolov8${TOKEN}.param"
cp -f "$BB" "$ASSETS_DIR/yolov8${TOKEN}.bin"
echo "Added model assets: yolov8${TOKEN}.param/bin"
