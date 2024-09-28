"""
Geschrieben für www.HomoFaciens.de
Das Programm demonstriert die Ursache der Rollreibung.
Copyright (C) 2010 Norbert Heinz

Dieses Programm ist freie Software. Sie können es unter den Bedingungen der GNU General Public License, wie von der Free Software Foundation veröffentlicht, weitergeben und/oder modifizieren, entweder gemäß Version 3 der Lizenz oder (nach Ihrer Option) jeder späteren Version.

Die Veröffentlichung dieses Programms erfolgt in der Hoffnung, dass es Ihnen von Nutzen sein wird, aber OHNE IRGENDEINE GARANTIE, sogar ohne die implizite Garantie der MARKTREIFE oder der VERWENDBARKEIT FÜR EINEN BESTIMMTEN ZWECK. Details finden Sie in der GNU General Public License.

Sie sollten ein Exemplar der GNU General Public License zusammen mit diesem Programm erhalten haben. Falls nicht, siehe <http://www.gnu.org/licenses/>.
"""
import tkinter as tk
import math
import time

class RollreibungsSimulation:
    def __init__(self, master):
        self.master = master
        master.title("Rollreibungssimulation")

        # Konfigurationsvariablen
        self.druck = tk.DoubleVar(value=1.8)           # Druck in bar
        self.groesse = tk.DoubleVar(value=1.0)         # Größe (Skalierung des Rads)
        self.geschwindigkeit = tk.DoubleVar(value=20.0)  # Geschwindigkeit in U/min

        # GUI-Elemente
        tk.Label(master, text="Druck (bar):").grid(row=0, column=0, padx=10, pady=5)
        tk.Entry(master, textvariable=self.druck).grid(row=0, column=1, padx=10, pady=5)

        tk.Label(master, text="Größe (X):").grid(row=1, column=0, padx=10, pady=5)
        tk.Entry(master, textvariable=self.groesse).grid(row=1, column=1, padx=10, pady=5)

        tk.Label(master, text="Geschwindigkeit (U/min):").grid(row=2, column=0, padx=10, pady=5)
        tk.Entry(master, textvariable=self.geschwindigkeit).grid(row=2, column=1, padx=10, pady=5)

        self.start_button = tk.Button(master, text="Start", command=self.toggle_simulation)
        self.start_button.grid(row=3, column=0, columnspan=2, pady=10)

        self.canvas = tk.Canvas(master, width=400, height=300, bg="white")
        self.canvas.grid(row=4, column=0, columnspan=2, padx=10, pady=10)

        # Simulationsvariablen
        self.is_running = False
        self.wheel_angle = 0
        self.last_time = time.time()

    def toggle_simulation(self):
        if self.is_running:
            self.is_running = False
            self.start_button.config(text="Start")
            print("Simulation gestoppt.")
        else:
            self.is_running = True
            self.start_button.config(text="Stop")
            self.last_time = time.time()  # Reset der Zeit beim Start
            print("Simulation gestartet.")
            self.update_simulation()

    def update_simulation(self):
        if not self.is_running:
            return

        current_time = time.time()
        dt = current_time - self.last_time
        self.last_time = current_time

        # Berechnung der Radrotation
        rpm = self.geschwindigkeit.get()
        self.wheel_angle += rpm * 2 * math.pi * dt / 60

        # Zeichnen
        self.draw_wheel()

        self.master.after(50, self.update_simulation)  # Aktualisierung alle 50 ms

    def draw_wheel(self):
        self.canvas.delete("all")
        
        # Straße
        self.canvas.create_rectangle(0, 250, 400, 300, fill="gray")

        # Rad
        center_x, center_y = 200, 200
        radius = 50 * self.groesse.get()
        flatness = 0.1666666 * (self.druck.get() - 0.8) + 0.8

        # Reifenumriss
        points = []
        for i in range(360):
            angle = math.radians(i)
            x = center_x + radius * math.cos(angle + self.wheel_angle)
            y = center_y + radius * math.sin(angle + self.wheel_angle)
            if y > center_y:
                y = min(y, center_y + radius * flatness)
            points.extend([x, y])

        self.canvas.create_polygon(points, outline="black", fill="black")

        # Radnabe
        self.canvas.create_oval(center_x - 10, center_y - 10, center_x + 10, center_y + 10, fill="gray")

        # Markierung für Rotation
        mark_x = center_x + radius * 0.7 * math.cos(self.wheel_angle)
        mark_y = center_y + radius * 0.7 * math.sin(self.wheel_angle)
        self.canvas.create_oval(mark_x - 5, mark_y - 5, mark_x + 5, mark_y + 5, fill="red")

# Hauptteil des Programms
if __name__ == "__main__":
    root = tk.Tk()
    simulation = RollreibungsSimulation(root)
    root.mainloop()
