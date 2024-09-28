---
title: "git_projekt"
author: "Jan Unger"
date: "2024-09-28"
---

# Git

## **1. Git-Konfigurationsbefehle verstehen**

Eine kurze Übersicht der wichtigsten Einstellungen:

- **Benutzeridentifikation**: Setzt deinen Namen und deine E-Mail-Adresse, die bei jedem Commit verwendet werden.
- **Standard-Branch**: Setzt den Standard-Branch-Namen auf `main` statt `master`.
- **Globale `.gitignore`**: Legt eine globale `.gitignore`-Datei fest, die bestimmte Dateien und Verzeichnisse in allen Repositories ignoriert.
- **Git-Aliase**: Erstellt Kurzbefehle für häufig verwendete Git-Befehle.
- **Farben aktivieren**: Verbessert die Lesbarkeit der Git-Ausgabe durch Farbgebung.
- **Standard-Editor**: Setzt den Standard-Editor für Git-Commits (z.B. Visual Studio Code).
- **Pull-Strategie**: Ändert die Pull-Strategie auf `rebase`, um eine sauberere Commit-Historie zu ermöglichen.
- **Automatisches Entfernen von gemergten Branches**: Entfernt lokale Branches, die bereits gemergt wurden.
- **Whitespace-Änderungen anzeigen**: Hebt Änderungen in Leerzeichen hervor.
- **Commit-Signierung (Optional)**: Erhöht die Sicherheit durch Signierung deiner Commits.

## **2. Umsetzung der Git-Konfiguration auf macOS**

### **a. Führe die Git-Konfigurationsbefehle aus**

```bash
# Benutzeridentifikation
git config --global user.name "Jan Unger"
git config --global user.email "esel573@gmail.com"

# Standard-Branch auf 'main' setzen
git config --global init.defaultBranch main

# Globale .gitignore festlegen
touch ~/.gitignore_global
echo -e "# macOS\n.DS_Store\n\n# Python\n__pycache__/\n*.py[cod]\n*.pyo\n*.pyd\n*.env\n.env\n*.egg\n*.egg-info/\ndist/\nbuild/" > ~/.gitignore_global
git config --global core.excludesfile ~/.gitignore_global

# Git-Aliase einrichten
git config --global alias.st status
git config --global alias.co checkout
git config --global alias.br branch
git config --global alias.ci commit
git config --global alias.lg "log --oneline --graph --decorate --all"

# Farben aktivieren
git config --global color.ui auto

# Standard-Editor festlegen (Beispiel: VS Code)
git config --global core.editor "code --wait"

# Pull-Strategie auf Rebase setzen
git config --global pull.rebase true

# Automatisches Entfernen von gemergten Branches
git config --global fetch.prune true

# Whitespace-Änderungen anzeigen
git config --global core.whitespace trailing-space,space-before-tab

# (Optional) Commit-Signierung einrichten
# git config --global user.signingkey KEYID
# git config --global commit.gpgsign true
```

**Hinweise:**

- **Commit-Signierung**: Die letzten beiden Befehle sind optional und kommentiert. Du kannst sie später aktivieren, wenn du einen GPG-Schlüssel eingerichtet hast.
- **Editor**: Stelle sicher, dass Visual Studio Code (`code`) installiert ist und der Befehl im Terminal verfügbar ist. Falls du einen anderen Editor bevorzugst, ersetze `"code --wait"` durch den entsprechenden Befehl, z.B. `"vim"` oder `"vim"`.

### **c. Optional: Automatisierung mit einem Shell-Skript**

1. **Erstelle eine neue Datei namens `git_setup.sh`:**

   ```bash
   vim git_setup.sh
   ```

2. **Füge die Git-Konfigurationsbefehle in die Datei ein:**

   ```bash
   #!/bin/bash

   # Benutzeridentifikation
   git config --global user.name "Jan Unger"
   git config --global user.email "esel573@gmail.com"

   # Standard-Branch auf 'main' setzen
   git config --global init.defaultBranch main

   # Globale .gitignore festlegen
   touch ~/.gitignore_global
   echo -e "# macOS\n.DS_Store\n\n# Python\n__pycache__/\n*.py[cod]\n*.pyo\n*.pyd\n*.env\n.env\n*.egg\n*.egg-info/\ndist/\nbuild/" > ~/.gitignore_global
   git config --global core.excludesfile ~/.gitignore_global

   # Git-Aliase einrichten
   git config --global alias.st status
   git config --global alias.co checkout
   git config --global alias.br branch
   git config --global alias.ci commit
   git config --global alias.lg "log --oneline --graph --decorate --all"

   # Farben aktivieren
   git config --global color.ui auto

   # Standard-Editor festlegen (Beispiel: VS Code: code --wait)
   git config --global core.editor "vim"

   # Pull-Strategie auf Rebase setzen
   git config --global pull.rebase true

   # Automatisches Entfernen von gemergten Branches
   git config --global fetch.prune true

   # Whitespace-Änderungen anzeigen
   git config --global core.whitespace trailing-space,space-before-tab

   # (Optional) Commit-Signierung einrichten
   # git config --global user.signingkey KEYID
   # git config --global commit.gpgsign true
   ```

3. **Speichere die Datei und mache das Skript ausführbar:**

   - **Mache das Skript ausführbar:**

     ```bash
     chmod +x git_setup.sh
     ./git_setup.sh
     ```

### **d. Überprüfe die Git-Konfiguration**

```bash
git config --list
```

## **3. SSH-Schlüssel für GitHub einrichten (Empfohlen)**

Um die Sicherheit und Benutzerfreundlichkeit zu erhöhen, ist es empfehlenswert, SSH-Schlüssel für die Authentifizierung mit GitHub zu verwenden.

### **a. Überprüfe, ob bereits ein SSH-Schlüssel existiert**

```bash
ls -al ~/.ssh
```

Suche nach Dateien wie `id_rsa.pub` oder `id_ed25519.pub`. Wenn keine SSH-Schlüssel vorhanden sind, erstelle einen neuen.

### **b. Generiere einen neuen SSH-Schlüssel**

1. **Erstelle einen neuen SSH-Schlüssel mit deiner GitHub-E-Mail-Adresse:**

   ```bash
   ssh-keygen -t ed25519 -C "esel573@gmail.com"
   ```

   **Hinweis:** Wenn dein System keine Unterstützung für Ed25519 bietet, verwende RSA:

   ```bash
   ssh-keygen -t rsa -b 4096 -C "esel573@gmail.com"
   ```

2. **Folge den Anweisungen im Terminal:**
   - Drücke `Enter`, um den Schlüssel im Standardverzeichnis zu speichern (`~/.ssh/id_ed25519`).
   - Optional: Lege ein sicheres Passwort fest oder drücke einfach `Enter`, um kein Passwort zu verwenden.

### **c. Füge den SSH-Schlüssel zum SSH-Agent hinzu**

1. **Starte den SSH-Agenten im Hintergrund:**

   ```bash
   eval "$(ssh-agent -s)"
   ```

2. **Füge deinen SSH-Schlüssel zum Agenten hinzu:**

   ```bash
   ssh-add ~/.ssh/id_ed25519
   ```

   **Hinweis:** Passe den Pfad an, falls du RSA verwendet hast (`~/.ssh/id_rsa`).

### **d. Füge den öffentlichen SSH-Schlüssel zu GitHub hinzu**

1. **Kopiere den Inhalt deines öffentlichen SSH-Schlüssels in die Zwischenablage:**

   ```bash
   pbcopy < ~/.ssh/id_ed25519.pub
   ```

   **Hinweis:** Falls du RSA verwendet hast, ersetze `id_ed25519.pub` durch `id_rsa.pub`.

2. **Gehe zu GitHub:**
   - Melde dich bei [GitHub](https://github.com/) an.
   - Navigiere zu **Settings** > **SSH and GPG keys**.
   - Klicke auf **"New SSH key"**.
   - Füge den kopierten Schlüssel in das Feld **"Key"** ein und gib ihm einen aussagekräftigen Titel.
   - Klicke auf **"Add SSH key"**.

3. **Überprüfe die SSH-Verbindung zu GitHub:**

   ```bash
   ssh -T git@github.com
   ```

   **Erwartete Ausgabe:**

   ```
   Hi JanUnger! You've successfully authenticated, but GitHub does not provide shell access.
   ```

## **4. GitHub-Repository erstellen und verbinden**

Falls du dies noch nicht getan hast, erstelle ein Repository auf GitHub und verbinde es mit deinem lokalen Projekt.

### **a. GitHub-Repository erstellen**

1. **Gehe zu [GitHub](https://github.com/) und melde dich an.**
2. **Klicke auf das Plus-Symbol (`+`) oben rechts und wähle "New repository".**
3. **Fülle die erforderlichen Informationen aus:**
   - **Repository-Name:** `mechanik` (oder ein anderer Name deiner Wahl).
   - **Beschreibung:** Optional, z.B. "Simulation der Rollreibung".
   - **Sichtbarkeit:** Wähle zwischen "Public" oder "Private".
   - **README hinzufügen:** Aktiviere **"Initialize this repository with a README"**.
4. **Klicke auf "Create repository".**

### **b. Lokales Git-Repository initialisieren und verbinden**

1. **Navigiere zu deinem Projektordner im Terminal:**

   ```bash
   echo "# mechanik" >> README.md
   git init
   git add README.md
   git commit -m "first commit"
   git branch -M main
   git remote add origin git@github.com:ju1-eu/mechanik.git
   git push -u origin main
   ```

## **5. Jupyter Notebook als Python-Skript exportieren**

Um dein Jupyter Notebook (`mein_notebook.ipynb`) als Python-Skript zu exportieren, verwende den folgenden Befehl:

```bash
jupyter nbconvert --to script mein_notebook.ipynb
```

Dies erstellt eine Datei namens `mein_notebook.py` im selben Verzeichnis.

### **Automatisches Exportieren bei jedem Commit (Optional)**

Um sicherzustellen, dass dein Python-Skript immer auf dem neuesten Stand ist, kannst du einen Git Hook einrichten, der das Notebook automatisch bei jedem Commit exportiert.

1. **Navigiere zum Git-Hooks-Verzeichnis:**

   ```bash
   cd .git/hooks
   ```

2. **Erstelle einen `pre-commit` Hook:**

   ```bash
   vim pre-commit
   ```

3. **Füge folgendes Skript hinzu:**

   ```bash
   #!/bin/sh
   jupyter nbconvert --to script ../mein_notebook.ipynb
   git add ../mein_notebook.py
   ```

   **Hinweis:** Passe den Pfad zu deinem Notebook entsprechend an.

4. **Speichere die Datei und mache den Hook ausführbar:**

   - **Speichern und Beenden in `vim`:** Drücke `Ctrl + O`, dann `Enter`, und dann `Ctrl + X`.
   - **Mache den Hook ausführbar:**

     ```bash
     chmod +x pre-commit
     ```

Jetzt wird bei jedem Commit dein Notebook automatisch als Python-Skript exportiert und hinzugefügt.

## **6. Weitere nützliche Git-Befehle und Best Practices**

### **a. Git-Aliase nutzen**

Dank der zuvor eingerichteten Aliase kannst du häufig verwendete Git-Befehle kürzer schreiben:

- **Status anzeigen:**

  ```bash
  git st
  ```

- **Checkout zu einem Branch:**

  ```bash
  git co branch-name
  ```

- **Branch erstellen und wechseln:**

  ```bash
  git co -b neuer-branch
  ```

- **Commit erstellen:**

  ```bash
  git ci -m "Nachricht"
  ```

- **Commit-Historie anzeigen:**

  ```bash
  git lg
  ```

### **b. Branching und Pull Requests**

Für eine saubere Entwicklung ist es empfehlenswert, neue Features in separaten Branches zu entwickeln und diese über Pull Requests zu mergen.

1. **Neuen Branch erstellen:**

   ```bash
   git checkout -b feature/neues-feature
   ```

2. **Änderungen vornehmen und committen:**

   ```bash
   git add .
   git commit -m "Beschreibung der Änderungen"
   ```

3. **Branch zu GitHub pushen:**

   ```bash
   git push -u origin feature/neues-feature
   ```

4. **Pull Request auf GitHub erstellen:**
   - Gehe zu deinem Repository auf GitHub.
   - Du siehst eine Meldung, dass ein neuer Branch gepusht wurde. Klicke auf **"Compare & pull request"**.
   - Füge eine Beschreibung hinzu und klicke auf **"Create pull request"**.

### **c. Globale `.gitignore` erweitern**

Falls du zusätzliche Dateien oder Verzeichnisse global ignorieren möchtest, bearbeite die globale `.gitignore`-Datei:

```bash
vim ~/.gitignore_global
```

Füge weitere Einträge hinzu, z.B.:

```
# Virtual Environment
venv/
.env

# IDEs
.vscode/
.idea/
```

### **d. Farben und Lesbarkeit optimieren**

Mit aktiviertem Farbmodus sind Git-Ausgaben besser lesbar:

```bash
git config --global color.ui auto
```

### **e. Commit-Nachrichten formatieren**

Verwende klare und präzise Commit-Nachrichten, die den Zweck der Änderungen beschreiben. Zum Beispiel:

```bash
git commit -m "Füge Rollreibungssimulation mit Tkinter hinzu"
```

## **7. Fehlerbehebung**

Falls du auf Probleme stößt, hier einige häufige Lösungen:

### **a. Git verwendet immer noch die alte Version**

Falls trotz der Installation über Homebrew Git immer noch die alte Version angezeigt wird, überprüfe deine `PATH`-Variable:

1. **Überprüfe den aktuellen Git-Pfad:**

   ```bash
   which git
   ```

   **Erwartete Ausgabe:**

   ```
   /usr/local/bin/git
   ```

2. **Falls der Pfad falsch ist, füge Homebrew zu deinem `PATH` hinzu:**

   Öffne deine `~/.zshrc`-Datei:

   ```bash
   vim ~/.zshrc
   ```

   Füge am Anfang der Datei folgende Zeile hinzu:

   ```zsh
   export PATH="/usr/local/bin:$PATH"
   ```

   **Speichern und schließen:**
   - In `vim`: Drücke `Ctrl + O`, dann `Enter`, und dann `Ctrl + X`.

3. **Lade die geänderte `~/.zshrc` neu:**

   ```bash
   source ~/.zshrc
   ```

4. **Überprüfe erneut die Git-Version:**

   ```bash
   git --version
   which git
   ```

   **Erwartete Ausgabe:**

   ```
   git version 2.46.2
   /usr/local/bin/git
   ```

### **b. SSH-Verbindung zu GitHub funktioniert nicht**

Falls du Probleme mit der SSH-Verbindung zu GitHub hast:

1. **Überprüfe den SSH-Agenten:**

   ```bash
   eval "$(ssh-agent -s)"
   ```

2. **Füge deinen SSH-Schlüssel hinzu:**

   ```bash
   ssh-add ~/.ssh/id_ed25519
   ```

3. **Teste die SSH-Verbindung:**

   ```bash
   ssh -T git@github.com
   ```

   **Erwartete Ausgabe:**

   ```
   Hi JanUnger! You've successfully authenticated, but GitHub does not provide shell access.
   ```

### **c. `.gitignore` funktioniert nicht wie erwartet**

Stelle sicher, dass die `.gitignore`-Datei korrekt konfiguriert ist und dass du Dateien nicht bereits zum Repository hinzugefügt hast. Git ignoriert nur Dateien, die **noch nicht verfolgt** werden.

**Beispiel:**

Wenn du eine Datei bereits hinzugefügt hast, lösche sie aus dem Tracking:

```bash
git rm --cached dateiname
```

## **8. Zusammenfassung**

Durch die Anpassung deiner Git-Konfiguration kannst du deine Arbeitsabläufe effizienter gestalten und die Zusammenarbeit mit anderen erleichtern. Hier ist eine Zusammenfassung der Schritte, die du durchgeführt hast:

1. **Git-Konfiguration setzen**: Benutzername, E-Mail, Standard-Branch, globale `.gitignore`, Aliase, Farben, Editor, Pull-Strategie, automatische Bereinigung und Whitespace-Änderungen.
2. **SSH-Schlüssel einrichten**: Für sichere und einfache Authentifizierung mit GitHub.
3. **GitHub-Repository erstellen und verbinden**: Lokales Repository mit GitHub verbinden.
4. **Jupyter Notebook exportieren**: Dein Notebook als Python-Skript für bessere Integration und Versionierung.
5. **Best Practices anwenden**: Klare Commits, Branching-Strategien und `.gitignore`-Datei verwenden.
6. **Fehlerbehebung**: Lösungen für häufige Probleme bereitstellen.

