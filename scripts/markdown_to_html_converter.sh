#!/bin/bash

# Konfigurationsdatei laden
CONFIG_FILE="markdown_converter_config.sh"
if [ -f "$CONFIG_FILE" ]; then
    source "$CONFIG_FILE"
else
    # Standardkonfiguration
    INPUT_DIR="."
    OUTPUT_DIR="."
    INDEX_FILE="index.html"
    CSS_FILE="markdown_styles.css"
    MAX_WIDTH="800px"
    LANGUAGE="de"
    DEBUG=false
fi

# Header-Inhalt
HEADER_CONTENT='
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <link rel="stylesheet" href="'$CSS_FILE'">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/prism/1.24.1/themes/prism.min.css"/>
  <link href="https://fonts.googleapis.com/css2?family=Fira+Code&display=swap" rel="stylesheet">
  <link href="https://fonts.googleapis.com/css2?family=STIX+Two+Math&display=swap" rel="stylesheet">
  <script src="https://cdnjs.cloudflare.com/ajax/libs/prism/1.24.1/prism.min.js"></script>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/prism/1.24.1/components/prism-markdown.min.js"></script>
  <script src="https://polyfill.io/v3/polyfill.min.js?features=es6"></script>
  <script id="MathJax-script" async src="https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-mml-chtml.js"></script>
'

# Funktion zur Fehlerbehandlung
error_exit() {
    echo "Fehler: $1" >&2
    exit 1
}

# Überprüfung der Abhängigkeiten
command -v pandoc >/dev/null 2>&1 || error_exit "Pandoc ist nicht installiert. Bitte installieren Sie Pandoc und versuchen Sie es erneut."

[ -f "$CSS_FILE" ] || echo "Warnung: CSS-Datei $CSS_FILE nicht gefunden."

# Funktion zur Titelgenerierung
generate_title() {
    echo "$1" | sed -e 's/^[._]*//' -e 's/-/ /g' -e 's/\.html//g'
}

# Debug-Funktion
debug_output() {
    [ "$DEBUG" = true ] && echo "DEBUG: $1"
}

# Funktion zur sicheren Dateinamenkonvertierung
sanitize_filename() {
    echo "$1" | sed -e 's/^[._]*//' -e 's/[^A-Za-z0-9._-]/_/g'
}

# Funktion zur Konvertierung einer einzelnen Datei
convert_file() {
    local mdfile="$1"
    local htmlfile="${OUTPUT_DIR}/$(sanitize_filename "$(basename "${mdfile%.md}.html")")"
    local title=$(generate_title "$(basename "$htmlfile")")
    
    debug_output "Konvertiere $mdfile zu $htmlfile"
    
    # Konvertiere Markdown zu HTML
    if ! content=$(pandoc "$mdfile" -f markdown-implicit_figures -t html --lua-filter=markdown_pdf_image_handler.lua --no-highlight --mathjax --wrap=none); then
        echo "Fehler bei der Konvertierung von $mdfile. Überspringe diese Datei."
        return 1
    fi
    
    # Erstelle die vollständige HTML-Datei
    cat << EOF > "$htmlfile"
<!DOCTYPE html>
<html lang="$LANGUAGE">
<head>
  <title>$title</title>
  $HEADER_CONTENT
</head>
<body>
<div class="container" style="max-width: $MAX_WIDTH;">
$content
</div>
<script>
  document.addEventListener("DOMContentLoaded", (event) => {
    if (typeof Prism !== 'undefined') Prism.highlightAll();
    if (typeof MathJax !== 'undefined') MathJax.typeset();
  });
</script>
</body>
</html>
EOF

    echo "Konvertiert $mdfile zu $htmlfile"
    return 0
}

# Hauptfunktion
main() {
    local converted_files=()
    
    # Erstelle Output-Verzeichnis, falls es nicht existiert
    mkdir -p "$OUTPUT_DIR" || error_exit "Konnte Output-Verzeichnis nicht erstellen"

    # Konvertiere nur Markdown-Dateien im aktuellen Verzeichnis
    for mdfile in *.md; do
        if [[ -f "$mdfile" ]]; then
            if convert_file "$mdfile"; then
                converted_files+=("$(sanitize_filename "${mdfile%.md}.html")")
            fi
        fi
    done

    # Überprüfen, ob HTML-Dateien erstellt wurden
    if [ ${#converted_files[@]} -eq 0 ]; then
        error_exit "Keine HTML-Dateien wurden erstellt. Beenden."
    fi

    # Erstelle Index-Datei
    local index_file="${OUTPUT_DIR}/${INDEX_FILE}"
    cat <<EOL > "$index_file"
<!DOCTYPE html>
<html lang="$LANGUAGE">
<head>
  <title>Inhaltsverzeichnis</title>
  $HEADER_CONTENT
</head>
<body>
<div class="container" style="max-width: $MAX_WIDTH;">
  <h1>Inhaltsverzeichnis</h1>
  <ul>
EOL

    # Füge Links zu konvertierten Dateien hinzu (ohne Duplikate)
    local added_files=()
    for file in "${converted_files[@]}"; do
        if [[ ! " ${added_files[@]} " =~ " ${file} " ]]; then
            title=$(generate_title "$file")
            echo "    <li><a href=\"$file\">$title</a></li>" >> "$index_file"
            added_files+=("$file")
        fi
    done

    # Schließe Index-Datei
    cat <<EOL >> "$index_file"
  </ul>
</div>
<script>
  document.addEventListener('DOMContentLoaded', (event) => {
    Prism.highlightAll();
    MathJax.typeset();
  });
</script>
</body>
</html>
EOL

    echo "Index-Datei $index_file wurde erstellt."
}

# Führe das Hauptprogramm aus
main