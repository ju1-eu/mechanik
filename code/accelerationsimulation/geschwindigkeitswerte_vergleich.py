# Importieren der notwendigen Bibliotheken
import matplotlib.pyplot as plt

# Definieren der Objekte und ihrer typischen Geschwindigkeiten in m/s
objekte = [
    "Schneckentempo",
    "Fußgänger",
    "100m Läufer",
    "Brieftaube",
    "Lastkraftwagen",
    "Fiat 126",
    "Gepard",
    "Thrust SSC",
    "Space Shuttle"  # Hinzugefügt
]

# Typische Geschwindigkeiten in m/s
# Für Brieftaube wird der Durchschnitt der Spanne (13 - 27 m/s) berechnet
geschwindigkeiten = [
    0.0008,  # Schneckentempo
    1.4,     # Fußgänger
    10.44,   # 100m Läufer
    (13 + 27) / 2,  # Brieftaube (Durchschnitt)
    22,      # Lastkraftwagen
    29,      # Fiat 126
    30,      # Gepard
    341,     # Thrust SSC
    15.7     # Space Shuttle
]

# Farben definieren: Blau für alle positiven Geschwindigkeiten
farben = ['blue'] * len(geschwindigkeiten)

# Erstellen der Abbildung und der Achsen
fig, ax = plt.subplots(figsize=(12, 8))

# Erstellen des Balkendiagramms
bars = ax.barh(objekte, geschwindigkeiten, color=farben)

# Beschriften der Achsen und des Titels
ax.set_xlabel('Geschwindigkeit (m/s)', fontsize=14)
ax.set_title('Typische Geschwindigkeitswerte verschiedener Fahrzeuge und Objekte', fontsize=16)

# Hinzufügen von Beschriftungen zu den Balken
for bar in bars:
    breite = bar.get_width()
    # Position der Beschriftung leicht neben dem Balken platzieren
    position_x = breite + 0.5  # Leicht neben dem Balken platzieren
    ax.text(position_x, bar.get_y() + bar.get_height()/2, f'{breite:.2f} m/s', va='center', ha='left', fontsize=12)

# Anpassen der Layouts für bessere Darstellung
plt.tight_layout()

# Speichern des Diagramms als PNG-Datei
plt.savefig('geschwindigkeitswerte-vergleich.svg')
plt.savefig('geschwindigkeitswerte-vergleich.png', dpi=300)

# Anzeigen des Diagramms
plt.show()
