# pip install pygame matplotlib
# geschwindigkeit.py
import pygame
import matplotlib.pyplot as plt
import matplotlib.animation as animation
import sys
import math

# Daten für die Objekte
objects = {
    "Fußgänger": 1.4,
    "Lastkraftwagen": 22,
    "Fiat 126": 29,
    "Gepard": 30,
    "Thrust SSC": 341
}

# Zeitparameter
total_time = 20  # Sekunden
time_steps = 100
dt = total_time / time_steps

# Initialisiere Pygame
pygame.init()

# Fenstergröße
WIDTH, HEIGHT = 800, 600
window = pygame.display.set_mode((WIDTH, HEIGHT))
pygame.display.set_caption("Geschwindigkeitssimulation")

# Farben
WHITE = (255, 255, 255)
BLACK = (0, 0, 0)
GREY = (200, 200, 200)
RED = (255, 0, 0)
GREEN = (0, 255, 0)
BLUE = (0, 0, 255)
YELLOW = (255, 255, 0)

# Schrift
font = pygame.font.SysFont(None, 24)

# Skalierung: 1 Meter = 5 Pixel
SCALE = 5

# Leitpfosten alle 50 Meter
POST_INTERVAL = 50  # Meter

# Simulationklasse
class SimObject:
    def __init__(self, name, speed, color, y_pos):
        self.name = name
        self.speed = speed  # m/s
        self.color = color
        self.x = 0
        self.y = y_pos
        self.size = 20
        self.wobble_angle = 0

    def update(self, dt):
        self.x += self.speed * dt
        # Simuliere Wackeln (für Fiat 126)
        if self.name == "Fiat 126":
            self.wobble_angle += self.speed * dt * 10  # Arbiträrer Faktor für Wackeln

    def draw(self, surface, camera_x):
        screen_x = WIDTH - (self.x - camera_x) * SCALE
        screen_y = self.y
        if screen_x < -self.size or screen_x > WIDTH + self.size:
            return  # Objekt außerhalb des Bildschirms
        if self.name == "Fiat 126":
            # Wackel-Effekt durch Rotation
            wobble = math.sin(math.radians(self.wobble_angle)) * 5
            pygame.draw.rect(surface, self.color, 
                             (screen_x, screen_y + wobble, self.size, self.size))
        else:
            pygame.draw.rect(surface, self.color, 
                             (screen_x, screen_y, self.size, self.size))
        # Beschriftung
        label = font.render(self.name, True, BLACK)
        surface.blit(label, (screen_x, screen_y - 20))

# Erstelle Simulationsobjekte
sim_objects = []
y_start = 100
y_gap = 80
colors = [RED, GREEN, BLUE, YELLOW, GREY]
for i, (name, speed) in enumerate(objects.items()):
    sim_objects.append(SimObject(name, speed, colors[i % len(colors)], y_start + i * y_gap))

# Beobachterkamera (wird mit dem Fiat 126 synchronisiert)
camera_x = 0

# Hauptschleife
clock = pygame.time.Clock()
running = True
current_time = 0

while running:
    dt_seconds = clock.tick(60) / 1000  # Delta time in Sekunden
    current_time += dt_seconds
    for event in pygame.event.get():
        if event.type == pygame.QUIT:
            running = False

    # Aktualisiere Objekte
    for obj in sim_objects:
        obj.update(dt_seconds)

    # Synchronisiere die Kamera mit dem Fiat 126
    for obj in sim_objects:
        if obj.name == "Fiat 126":
            camera_x = obj.x
            break

    # Zeichne Hintergrund
    window.fill(WHITE)

    # Zeichne Leitpfosten
    num_posts = WIDTH // (POST_INTERVAL * SCALE) + 2
    for i in range(num_posts):
        post_x = WIDTH - (i * POST_INTERVAL * SCALE) + (camera_x % (POST_INTERVAL * SCALE))
        pygame.draw.line(window, BLACK, (post_x, 50), (post_x, HEIGHT - 50), 2)

    # Zeichne Objekte
    for obj in sim_objects:
        obj.draw(window, camera_x)

    # Zeichne Zeit
    time_label = font.render(f"Zeit: {current_time:.2f} s", True, BLACK)
    window.blit(time_label, (10, 10))

    pygame.display.flip()

    # Beende die Simulation nach der Gesamtzeit
    if current_time > total_time:
        running = False

pygame.quit()

# Matplotlib Plot
# B5 Größe in Zoll (250 x 176 mm)
width_in = 250 / 25.4
height_in = 176 / 25.4
plt.figure(figsize=(width_in, height_in))
for name, speed in objects.items():
    times = [t * dt for t in range(time_steps + 1)]
    distances = [speed * t for t in times]
    plt.plot(times, distances, label=name)

plt.title("Weg-Zeit-Diagramm")
plt.xlabel("Zeit (s)")
plt.ylabel("Strecke (m)")
plt.legend()
plt.grid(True)
# Speichern als SVG
plt.savefig("weg-zeit-diagramm.svg", format="svg")
# Speichern als PNG
plt.savefig("weg-zeit-diagramm.png", format="png", dpi=300)
plt.show()

