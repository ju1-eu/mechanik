# pip install numpy matplotlib
# beschleunigung.py
import numpy as np
import matplotlib.pyplot as plt

# Daten der Objekte
vehicles = [
    {
        'name': 'Fiat 126 (Beschleunigung)',
        'acceleration': 0.59,
        'initial_speed': 0,
        'total_time': 47
    },
    {
        'name': 'Porsche Carrera GT',
        'acceleration': 7.31,
        'initial_speed': 0,
        'total_time': 3.8
    },
    {
        'name': 'Fiat 126 (Bremsen)',
        'acceleration': -7.41,  # Negative Beschleunigung beim Bremsen
        'initial_speed': 27.78,  # 100 km/h in m/s
        'distance': 52  # Gegebener Bremsweg in Metern
    },
    {
        'name': 'Thrust SSC',
        'acceleration': 17.36,
        'initial_speed': 0,
        'total_time': 16
    },
    {
        'name': 'Space Shuttle',
        'acceleration': 40,
        'initial_speed': 0,
        'total_time': 60  # Angenommene Dauer für die Simulation
    }
]

def simulate_motion(vehicle):
    if 'total_time' not in vehicle:
        # Berechne die Zeit für den Fiat 126 beim Bremsen
        v0 = vehicle['initial_speed']
        a = vehicle['acceleration']
        s = vehicle['distance']
        # Verwende die Formel: s = v0 * t + 0.5 * a * t^2
        # Dies ist eine quadratische Gleichung in t: 0.5 * a * t^2 + v0 * t - s = 0
        # Löse die Gleichung für t
        coeffs = [0.5 * a, v0, -s]
        roots = np.roots(coeffs)
        # Wähle die positive Lösung für t
        t_positive = roots[roots > 0]
        if t_positive.size == 0:
            raise ValueError(f"Keine positive Zeitlösung für {vehicle['name']}")
        vehicle['total_time'] = t_positive[0]
    t = np.linspace(0, vehicle['total_time'], num=500)
    v0 = vehicle['initial_speed']
    a = vehicle['acceleration']
    v = v0 + a * t
    s = v0 * t + 0.5 * a * t**2
    return t, v, s


# Geschwindigkeit-Zeit-Diagramm
# B5 Größe in Zoll (250 x 176 mm)
width_in = 250 / 25.4
height_in = 176 / 25.4
plt.figure(figsize=(width_in, height_in))
for vehicle in vehicles:
    t, v, s = simulate_motion(vehicle)
    plt.plot(t, v, label=vehicle['name'])
plt.title('Geschwindigkeit über Zeit')
plt.xlabel('Zeit (s)')
plt.ylabel('Geschwindigkeit (m/s)')
plt.legend()
plt.grid(True)
# Speichern als SVG
plt.savefig("geschwindigkeit-zeit-diagramm.svg", format="svg")
# Speichern als PNG
plt.savefig("geschwindigkeit-zeit-diagramm.png", format="png", dpi=300)
plt.show()

# Weg-Zeit-Diagramm
# B5 Größe in Zoll (250 x 176 mm)
width_in = 250 / 25.4
height_in = 176 / 25.4
plt.figure(figsize=(width_in, height_in))
for vehicle in vehicles:
    t, v, s = simulate_motion(vehicle)
    plt.plot(t, s, label=vehicle['name'])
plt.title('Weg über Zeit')
plt.xlabel('Zeit (s)')
plt.ylabel('Weg (m)')
plt.legend()
plt.grid(True)
# Speichern als SVG
plt.savefig("weg-zeit-diagramm.svg", format="svg")
# Speichern als PNG
plt.savefig("weg-zeit-diagramm.png", format="png", dpi=300)
plt.show()
