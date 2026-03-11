#!/usr/bin/env bash

# docx_to_pdf.sh
# Usage: ./docx_to_pdf.sh input.docx [output_dir]

set -euo pipefail

INPUT_FILE="${1:-}"
OUTPUT_DIR="${2:-.}"

if [[ -z "$INPUT_FILE" ]]; then
  echo "Usage: $0 <input.docx> [output_dir]"
  exit 1
fi

if [[ ! -f "$INPUT_FILE" ]]; then
  echo "Error: File not found -> $INPUT_FILE"
  exit 2
fi

mkdir -p "$OUTPUT_DIR"

libreoffice --headless \
  --convert-to pdf \
  --outdir "$OUTPUT_DIR" \
  "$INPUT_FILE"

echo "✅ Conversion completed."