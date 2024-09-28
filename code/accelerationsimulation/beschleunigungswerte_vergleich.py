# Importieren der notwendigen Bibliotheken
import matplotlib.pyplot as plt

# Definieren der Objekte und ihrer Beschleunigungswerte in m/s²
objekte = [
    "Fiat 126 mit Originalmotor (17 kW)",
    "Porsche Carrera GT (450 kW)",
    "Fiat 126 (Bremsen)",
    "Thrust SSC",
    "Space Shuttle",
    "Formel 1 Rennwagen",
    "Saturn V Rakete",
    "Freier Fall im Vakuum",
    "Geplanter Hyperloop-Zug"
]

beschleunigungen = [
    0.59,    # Fiat 126 mit Originalmotor
    7.31,    # Porsche Carrera GT
    -7.41,   # Fiat 126 beim Bremsen
    17.36,   # Thrust SSC
    15.7,    # Space Shuttle
    11.11,   # Formel 1 Rennwagen
    16.3,    # Saturn V Rakete
    9.81,    # Freier Fall im Vakuum
    0.77     # Geplanter Hyperloop-Zug
]

# Definieren der Farben: Rot für negative Beschleunigung (Verzögerung), Blau für positive
farben = ['blue' if a >= 0 else 'red' for a in beschleunigungen]

# Erstellen der Abbildung und der Achsen
fig, ax = plt.subplots(figsize=(12, 8))

# Erstellen des Balkendiagramms
bars = ax.barh(objekte, beschleunigungen, color=farben)

# Hinzufügen einer vertikalen Linie für die Erdbeschleunigung (9,81 m/s²)
ax.axvline(x=9.81, color='green', linestyle='--', linewidth=2, label='Erdbeschleunigung (9,81 m/s²)')

# Beschriften der Achsen und des Titels
ax.set_xlabel('Beschleunigung (m/s²)', fontsize=14)
ax.set_title('Typische Beschleunigungswerte verschiedener Fahrzeuge und Objekte', fontsize=16)

# Hinzufügen einer Legende
ax.legend()

# Hinzufügen von Beschriftungen zu den Balken
for bar in bars:
    width = bar.get_width()
    if width >= 0:
        label_x_pos = width + 0.2
        ha = 'left'
    else:
        label_x_pos = width - 0.2
        ha = 'right'
    ax.text(label_x_pos, bar.get_y() + bar.get_height()/2, f'{width}', va='center', ha=ha, fontsize=12)

# Anpassen der Layouts für bessere Darstellung
plt.tight_layout()

# Speichern des Diagramms als PNG-Datei und SVG
plt.savefig('beschleunigungswerte-vergleich.png', dpi=300)
plt.savefig('beschleunigungswerte-vergleich.svg')

# Anzeigen des Diagramms
plt.show()
