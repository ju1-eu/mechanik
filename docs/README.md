---
title: "README"
author: "Jan Unger"
date: "2024-09-29"
---

# Mechanik

## Beschreibung

Dieses Projekt behandelt die Mechanik mithilfe von Python und Jupyter Notebook.

## Installation

1. **Erstellen und Clonen des Repositories:**

  ```bash
  git config --list

  echo "# mechanik" >> README.md
  git init
  git add README.md
  git commit -m "first commit"

  # GitHub-Repository verbinden und pushen
  git branch -M main
  git remote add origin git@github.com:ju1-eu/mechanik.git
  git push -u origin main

  git tag -a v1.0 -m "Version 1.0: Initiale Veröffentlichung"
  git push origin v1.0

  # Nutzung von Git und GitHub für Zusammenarbeit
  # a) Branching-Strategie
  git checkout -b feature/neues-feature
  git add .
  git commit -m "Beschreibung des Features"
  git push -u origin feature/neues-feature
  # Pull Request auf GitHub erstellen:
  # Gehe zu deinem Repository auf GitHub.
  # Du wirst aufgefordert, einen Pull Request für den neuen Branch zu erstellen. Klicke auf "Compare & pull request".
  # Füge eine Beschreibung hinzu und klicke auf "Create pull request".

  # b) Zusammenarbeit mit anderen
  git clone git@github.com:ju1-eu/mechanik.git
  cd mechanik
  git checkout -b feature/anderes-feature

  # Git-Aliase
  git st
  git lg
  git ci -m "Nachricht"
  git co branch-name
  git co -b neuer-branch
  ```

2. **Erstellen der Conda-Umgebung:**

  ```bash
  conda update -n base -c defaults conda
  conda update --all
  conda init zsh
  source ~/.zshrc
  conda --version

  conda clean --all

  conda env update -f environment.yml
  #conda env remove -n meinenv
  #conda env create -f environment.yml

  conda install -c conda-forge jupyter_contrib_nbextensions
  jupyter contrib nbextension install --user
  # Konvertieren
  jupyter nbconvert --to python code/mein_notebook.ipynb
  jupyter nbconvert --to markdown  code/mein_notebook.ipynb  
  jupyter nbconvert --to html code/mein_notebook.ipynb
  jupyter nbconvert --to pdf code/mein_notebook.ipynb
  jupyter nbconvert --to pdf --template-file /Users/jan/daten/eBike/homofaciens/mechanik/custom_template.tplx code/mein_notebook.ipynb


  # Jupyter Notebook als Python-Skript exportieren
  cd .git/hooks
  vim pre-commit
    #!/bin/sh
    jupyter nbconvert --to script ../../code/mein_notebook.ipynb
    git add ../../code/mein_notebook.py
  chmod +x pre-commit
  ```

3. **Aktivieren der Umgebung:**

  ```bash
  conda env list
  conda activate meinenv

  # Überprüfen des Pfades und der Python-Version
  which python3
  python3 --version
  conda info
  conda update --all

  #conda env remove -n meinenv
  #conda deactivate
  ```

## Installation von Pygame mit AVX2-Unterstützung

1. SDL-Entwicklerbibliotheken installieren:

   ```bash
   brew install sdl2 sdl2_image sdl2_mixer sdl2_ttf portmidi
   ```

2. Umgebungsvariablen für SDL-Pfade setzen:

   ```bash
   export LDFLAGS="-L/usr/local/opt/sdl2/lib"
   export CPPFLAGS="-I/usr/local/opt/sdl2/include"
   ```

3. AVX2-Erkennung aktivieren:

   ```bash
   export PYGAME_DETECT_AVX2=1
   ```

4. Pygame neu installieren:

   ```bash
   pip install --upgrade pip
   pip install --no-binary :all: pygame
   ```

## Nutzung

```bash
conda env create -f environment.yml
conda env update --file environment.yml --prune

#conda activate meinenv
conda activate jupyter_fresh

conda config --add channels conda-forge
conda config --set channel_priority strict
conda update --all
#export PYGAME_DETECT_AVX2=1
#pip install --no-binary :all: pygame
# TEST
pip show pygame
conda list pytorch

#jupyter notebook
jupyter lab 
python3 ./script.py # oder

# Dokumentation konvertieren
make
make clean

conda deactivate
```

## Beitragende

- Jan Unger

## Lizenz

Dieses Projekt ist unter der MIT-Lizenz lizenziert.

## Nutzung von GitHub Features**

- **Issues:** Für Bug-Tracking und Feature-Anfragen.
- **Pull Requests:** Für Code-Reviews und Zusammenarbeit.
- **Projects:** Für die Organisation von Aufgaben im Kanban-Stil.

## Integration von CI/CD mit GitHub Actions

Continuous Integration (CI) und Continuous Deployment (CD) sind essenzielle Praktiken in der modernen Softwareentwicklung. Sie ermöglichen es Teams, Code schneller und zuverlässiger zu entwickeln, zu testen und bereitzustellen. GitHub Actions bietet eine leistungsstarke und flexible Möglichkeit, CI/CD-Pipelines direkt in GitHub-Repositories zu implementieren.

### Vorteile von CI/CD

- Frühzeitige Fehlererkennung
- Verbesserte Codequalität
- Schnellere Bereitstellung von Updates
- Erhöhte Entwicklerproduktivität

Implementiere CI/CD mit GitHub Actions, um automatische Tests und Deployments durchzuführen.

**Beispiel `.github/workflows/python-app.yml`:**

```yaml
name: Python application

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:

    runs-on: macos-latest

    steps:
    - uses: actions/checkout@v2
    - name: Set up Conda
      uses: conda-incubator/setup-miniconda@v2
      with:
        environment-file: environment.yml
        activate-environment: jupyter_fresh
        python-version: 3.12
    - name: Install pip dependencies
      run: |
        pip install -r requirements.txt
    - name: Run tests
      run: |
        pytest
```

### Erläuterung der Workflow-Schritte

1. **Trigger**: Der Workflow wird bei Pushes und Pull Requests auf den main-Branch ausgelöst.
2. **Runner**: Der Job läuft auf einem macOS-System.
3. **Checkout**: Der Repository-Code wird ausgecheckt.
4. **Conda Setup**: Conda wird mit einer spezifischen Umgebung und Python-Version eingerichtet.
5. **Dependency Installation**: Pip-Abhängigkeiten werden installiert.
6. **Tests**: Pytest wird ausgeführt, um die Tests durchzuführen.

### Anpassungsmöglichkeiten

- Für Node.js-Projekte: Ersetze Conda-Setup und pip durch Node.js-Setup und npm/yarn.
- Für Deployment: Füge einen Schritt hinzu, der den Code auf einen Server oder Cloud-Dienst deployed.

### Sicherheitshinweis

Bei der Verwendung von GitHub Actions ist es wichtig, sensible Daten wie API-Schlüssel oder Zugangsdaten als GitHub Secrets zu speichern. Diese können dann sicher in Ihren Workflows verwendet werden, ohne sie im Code preiszugeben.

**Hinweis:** Stelle sicher, dass du eine `requirements.txt`-Datei hast, wenn du zusätzliche `pip`-Pakete verwendest.

Weitere Ressourcen:
- [GitHub Actions Dokumentation](https://docs.github.com/en/actions)
- [GitHub Actions Marketplace](https://github.com/marketplace?type=actions)