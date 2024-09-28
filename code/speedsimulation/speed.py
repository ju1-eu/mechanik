"""
# Geschrieben für www.HomoFaciens.de
# Ursprünglich von Norbert Heinz (2009)
# Von Java nach Python übersetzt durch Jan Unger mit Unterstützung von ChatGPT (2024)
#
# Dieses Programm demonstriert Zusammenhänge zwischen Zeit, Entfernung und Geschwindigkeit.
#
# Dieses Programm ist freie Software: Sie können es unter den Bedingungen
# der GNU General Public License, wie von der Free Software Foundation,
# Version 3 der Lizenz oder (nach Ihrer Wahl) jeder neueren
# veröffentlichten Version, weiter verteilen und/oder modifizieren.
#
# Dieses Programm wird in der Hoffnung bereitgestellt, dass es nützlich sein wird, jedoch
# OHNE JEDE GEWÄHRLEISTUNG; sogar ohne die implizite Gewährleistung der
# MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN BESTIMMTEN ZWECK. Siehe die
# GNU General Public License für weitere Einzelheiten.
#
# Sie sollten eine Kopie der GNU General Public License zusammen mit diesem
# Programm erhalten haben. Wenn nicht, siehe <https://www.gnu.org/licenses/>.
"""

import tkinter as tk
from tkinter import ttk, messagebox
import matplotlib.pyplot as plt
from matplotlib.backends.backend_tkagg import FigureCanvasTkAgg
import pygame
import time
from decimal import Decimal, InvalidOperation

# Initialisiere Pygame für Sound
pygame.mixer.init()

# Funktion zum Laden von Sounddateien
def lade_sound(name):
    try:
        return pygame.mixer.Sound(name)
    except pygame.error as e:
        print(f"Fehler beim Laden des Sounds {name}: {e}")
        return None

# Klasse für die Hauptanwendung
class GeschwindigkeitSimulator(tk.Tk):
    def __init__(self):
        super().__init__()
        self.title("Geschwindigkeit Simulator")
        self.geometry("1200x800")
        self.resizable(False, False)

        # Spracheinstellung
        self.sprache = "Deutsch"  # Standardmäßig Deutsch

        # GUI-Komponenten erstellen
        self.erzeuge_widgets()

        # Sounddateien laden
        self.sounds = {
            "auto": lade_sound("sounds/car.wav"),
            "lkw": lade_sound("sounds/truck.wav"),
            "taube": lade_sound("sounds/dove.wav"),
            "mann": lade_sound("sounds/man.wav"),
            "thrustSSC": lade_sound("sounds/thrustSSC.wav")
        }

        # Simulationseinstellungen
        self.ist_laueft = False
        self.simulations_step = 0  # Aktueller Schritt in der Simulation

        # Initialisiere Diagrammdaten
        self.x_daten = []
        self.y_daten = []
        self.max_x = 1.0
        self.max_y = 1.0

    def erzeuge_widgets(self):
        # Rahmen für die Steuerung
        steuerungs_frame = ttk.Frame(self)
        steuerungs_frame.pack(side=tk.LEFT, fill=tk.Y, padx=10, pady=10)

        # Radiobuttons zur Auswahl der zu berechnenden Größe
        self.param_var = tk.StringVar(value="geschwindigkeit")
        ttk.Label(steuerungs_frame, text="Wählen Sie die zu berechnende Größe:").pack(anchor=tk.W)
        ttk.Radiobutton(steuerungs_frame, text="Geschwindigkeit", variable=self.param_var, value="geschwindigkeit").pack(anchor=tk.W)
        ttk.Radiobutton(steuerungs_frame, text="Fahrzeit", variable=self.param_var, value="fahrzeit").pack(anchor=tk.W)
        ttk.Radiobutton(steuerungs_frame, text="Strecke", variable=self.param_var, value="strecke").pack(anchor=tk.W)

        # Spinner für Eingaben mit Unterstützung von Dezimalzahlen
        ttk.Label(steuerungs_frame, text="Geschwindigkeit (m/s):").pack(anchor=tk.W, pady=(10,0))
        self.geschwindigkeit_var = tk.StringVar(value="55.0")
        self.geschwindigkeit_spin = ttk.Spinbox(
            steuerungs_frame, 
            from_=0.0, 
            to=350.0, 
            textvariable=self.geschwindigkeit_var, 
            increment=0.1, 
            width=10,
            format="%.1f"
        )
        self.geschwindigkeit_spin.pack(anchor=tk.W)

        ttk.Label(steuerungs_frame, text="Fahrzeit (s):").pack(anchor=tk.W, pady=(10,0))
        self.fahrzeit_var = tk.StringVar(value="25.0")
        self.fahrzeit_spin = ttk.Spinbox(
            steuerungs_frame, 
            from_=0.0, 
            to=50.0, 
            textvariable=self.fahrzeit_var, 
            increment=0.1, 
            width=10,
            format="%.1f"
        )
        self.fahrzeit_spin.pack(anchor=tk.W)

        ttk.Label(steuerungs_frame, text="Strecke (m):").pack(anchor=tk.W, pady=(10,0))
        self.strecke_var = tk.StringVar(value="55.0")
        self.strecke_spin = ttk.Spinbox(
            steuerungs_frame, 
            from_=0.0, 
            to=1000.0, 
            textvariable=self.strecke_var, 
            increment=0.1, 
            width=10,
            format="%.1f"
        )
        self.strecke_spin.pack(anchor=tk.W)

        # Start/Stop Button
        self.start_stop_button = ttk.Button(steuerungs_frame, text="Start", command=self.toggle_simulation)
        self.start_stop_button.pack(pady=20)

        # Versions- und Quellenangabe
        ttk.Label(steuerungs_frame, text="Quelle & Info: www.HomoFaciens.de").pack(anchor=tk.W, pady=(50,0))
        ttk.Label(steuerungs_frame, text="Version: v1.2").pack(anchor=tk.W)

        # Diagramm
        self.fig, self.ax = plt.subplots(figsize=(6,5))
        self.canvas = FigureCanvasTkAgg(self.fig, master=self)
        self.canvas.get_tk_widget().pack(side=tk.TOP, fill=tk.BOTH, expand=True)
        self.ax.set_xlabel("Zeit (s)")
        self.ax.set_ylabel("Strecke (m)")
        self.ax.grid(True)

        self.plot, = self.ax.plot([], [], 'r-')  # Initial leere Linie

    def toggle_simulation(self):
        if not self.ist_laueft:
            self.start_simulation()
        else:
            self.stop_simulation()

    def start_simulation(self):
        # Validiere Eingaben
        if not self.validiere_eingaben():
            return

        self.ist_laueft = True
        self.start_stop_button.config(text="Stop")
        self.plot.set_data([], [])
        self.ax.relim()
        self.ax.autoscale_view()
        self.canvas.draw()

        # Starte die Simulation
        self.simulations_step = 0
        self.simuliere()

        # Starte den Soundeffekt
        if self.sounds["auto"]:
            self.sounds["auto"].play(-1)  # Endlos abspielen

    def stop_simulation(self):
        self.ist_laueft = False
        self.start_stop_button.config(text="Start")
        # Stoppe den Soundeffekt
        if self.sounds["auto"]:
            self.sounds["auto"].stop()

    def validiere_eingaben(self):
        param = self.param_var.get()
        try:
            geschwindigkeit = float(self.geschwindigkeit_var.get())
            fahrzeit = float(self.fahrzeit_var.get())
            strecke = float(self.strecke_var.get())
        except ValueError:
            messagebox.showerror("Fehler", "Bitte geben Sie gültige Dezimalzahlen ein.")
            return False

        # Überprüfe auf Division durch Null und negative Werte
        if param == "geschwindigkeit":
            if fahrzeit <= 0:
                messagebox.showerror("Fehler", "Fahrzeit muss größer als null sein.")
                return False
            if strecke < 0:
                messagebox.showerror("Fehler", "Strecke darf nicht negativ sein.")
                return False
        elif param == "fahrzeit":
            if geschwindigkeit <= 0:
                messagebox.showerror("Fehler", "Geschwindigkeit muss größer als null sein.")
                return False
            if strecke < 0:
                messagebox.showerror("Fehler", "Strecke darf nicht negativ sein.")
                return False
        elif param == "strecke":
            if geschwindigkeit < 0:
                messagebox.showerror("Fehler", "Geschwindigkeit darf nicht negativ sein.")
                return False
            if fahrzeit < 0:
                messagebox.showerror("Fehler", "Fahrzeit darf nicht negativ sein.")
                return False
        return True

    def simuliere(self):
        if not self.ist_laueft:
            return

        param = self.param_var.get()
        try:
            geschwindigkeit = float(self.geschwindigkeit_var.get())
            fahrzeit = float(self.fahrzeit_var.get())
            strecke = float(self.strecke_var.get())
        except ValueError:
            messagebox.showerror("Fehler", "Ungültige Eingaben während der Simulation.")
            self.stop_simulation()
            return

        # Berechnungen basierend auf der Auswahl
        if param == "geschwindigkeit":
            # v = s / t
            berechnete_geschwindigkeit = strecke / fahrzeit
            self.geschwindigkeit_var.set(f"{berechnete_geschwindigkeit:.1f}")
            ergebnis = f"Geschwindigkeit = {berechnete_geschwindigkeit:.2f} m/s"
        elif param == "fahrzeit":
            # t = s / v
            berechnete_fahrzeit = strecke / geschwindigkeit
            self.fahrzeit_var.set(f"{berechnete_fahrzeit:.1f}")
            ergebnis = f"Fahrzeit = {berechnete_fahrzeit:.2f} s"
        elif param == "strecke":
            # s = v * t
            berechnete_strecke = geschwindigkeit * fahrzeit
            self.strecke_var.set(f"{berechnete_strecke:.1f}")
            ergebnis = f"Strecke = {berechnete_strecke:.2f} m"

        print(ergebnis)

        # Aktualisiere das Diagramm
        self.x_daten = list(range(0, int(fahrzeit) + 1))
        self.y_daten = [geschwindigkeit * t for t in self.x_daten]
        self.plot.set_data(self.x_daten, self.y_daten)
        self.ax.set_xlim(0, fahrzeit)
        self.ax.set_ylim(0, max(self.y_daten) + 10)
        self.canvas.draw()

        # Starte die Simulationsschritte
        if self.simulations_step < len(self.x_daten):
            self.simulations_step += 1
            # Aktualisiere das Diagramm schrittweise (kumulativ)
            current_x = self.x_daten[:self.simulations_step]
            current_y = self.y_daten[:self.simulations_step]
            self.plot.set_data(current_x, current_y)
            self.ax.set_xlim(0, max(current_x) + 1)
            self.ax.set_ylim(0, max(current_y) + 10)
            self.canvas.draw()
            # Setze den nächsten Simulationsschritt
            self.after(1000, self.simuliere)  # Warte 1 Sekunde
        else:
            # Simulation beendet
            self.stop_simulation()

# Hauptprogramm
if __name__ == "__main__":
    app = GeschwindigkeitSimulator()
    app.mainloop()
