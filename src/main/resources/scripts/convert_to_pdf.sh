#!/bin/bash

# Script to convert a DOCX file to a PDF file

if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <docx_file>"
    exit 1
fi

DOCX_FILE="$1"

if [ ! -f "$DOCX_FILE" ]; then
    echo "Error: File '$DOCX_FILE' not found."
    exit 1
fi

# Extract the filename without extension
FILENAME=$(basename -- "$DOCX_FILE")
EXTENSION="${FILENAME##*.}"
FILENAME_NO_EXT="${FILENAME%.*}"
DIR_NAME=$(dirname -- "$DOCX_FILE")

if [ "$EXTENSION" != "docx" ]; then
    echo "Error: File must have a .docx extension."
    exit 1
fi

# Convert to PDF using unoconv or libreoffice
# Using libreoffice --headless --convert-to pdf as it's more widely available
libreoffice --headless --convert-to pdf "$DOCX_FILE" --outdir "$DIR_NAME"

PDF_FILE="$DIR_NAME/$FILENAME_NO_EXT.pdf"

if [ -f "$PDF_FILE" ]; then
    echo "Successfully converted $DOCX_FILE to $PDF_FILE"
else
    echo "Error: Failed to convert $DOCX_FILE to PDF."
    exit 1
fi
