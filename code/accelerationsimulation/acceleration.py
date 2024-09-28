# Geschrieben für www.HomoFaciens.de
# Ursprünglich von Norbert Heinz (2009)
# Von Java nach Python übersetzt und optimiert durch Jan Unger mit Unterstützung von ChatGPT (2024)
#
# Dieses Programm demonstriert eine Beschleunigungssimulation.
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

import tkinter as tk
from tkinter import ttk, messagebox
from tkinter import PhotoImage
from PIL import Image, ImageTk

# Versuch, Resampling zu importieren, sonst auf Image.LANCZOS setzen
try:
    from PIL import Resampling
except ImportError:
    Resampling = None

import matplotlib
from matplotlib.backends.backend_tkagg import FigureCanvasTkAgg
import matplotlib.pyplot as plt
import pygame
import time
import math
import os

# Pygame-Mixer für Sounds initialisieren mit Fehlerbehandlung
try:
    pygame.mixer.init()
except pygame.error as e:
    messagebox.showerror("Pygame Fehler", f"Pygame Mixer konnte nicht initialisiert werden: {e}")

class AccelerationApp(tk.Tk):
    def __init__(self):
        super().__init__()
        self.title("Beschleunigungssimulation")
        self.geometry("1200x800")
        self.resizable(False, False)

        # Spracheinstellungen (Standard auf Deutsch)
        self.language = "Deutsch"

        # Variablen initialisieren
        self.version_number = "v1.0"
        self.is_running = False
        self.graph_active = -1
        self.max_graphs = 5
        self.coords_count = 350
        self.x_max = 1.0
        self.y_max = 1.0
        self.coord_step = 0.0
        self.simulation_speed = 1.0
        self.timer_pause = 25  # Millisekunden
        self.timer = None
        self.simulation_time = 0
        self.start_time = 0

        # Datenstrukturen für Graphen
        self.x_data = []
        self.y_data = []
        self.line, = plt.subplots(figsize=(5, 4))[1].plot([], [], 'r-')  # Placeholder, wird später überschrieben

        # GUI-Komponenten initialisieren
        self.create_widgets()

        # Bilder und Sounds laden
        self.load_assets()

    def create_widgets(self):
        # Hauptframes erstellen
        steuerungs_frame = ttk.Frame(self, padding="10")
        steuerungs_frame.pack(side=tk.LEFT, fill=tk.Y)

        graph_frame = ttk.Frame(self, padding="10")
        graph_frame.pack(side=tk.TOP, fill=tk.BOTH, expand=True)

        szenen_frame = ttk.Frame(self, padding="10")
        szenen_frame.pack(side=tk.BOTTOM, fill=tk.BOTH, expand=True)

        # Steuerungspanel
        ttk.Label(steuerungs_frame, text="Beschleunigungssimulation", font=("Arial", 16)).pack(pady=10)

        # Radiobuttons für Formeln mit Tooltips
        self.formula_var = tk.StringVar(value="s_at_t")
        sat_rb = ttk.Radiobutton(
            steuerungs_frame,
            text="s = (a / 2) * t²",
            variable=self.formula_var,
            value="s_at_t",
            command=self.reset_values
        )
        sat_rb.pack(anchor=tk.W, pady=5)
        self.create_tooltip(sat_rb, "Berechnet die Distanz basierend auf der Beschleunigung und Zeit.")

        vat_rb = ttk.Radiobutton(
            steuerungs_frame,
            text="v = a * t",
            variable=self.formula_var,
            value="v_at_t",
            command=self.reset_values
        )
        vat_rb.pack(anchor=tk.W, pady=5)
        self.create_tooltip(vat_rb, "Berechnet die Geschwindigkeit basierend auf der Beschleunigung und Zeit.")

        ttk.Separator(steuerungs_frame, orient='horizontal').pack(fill='x', pady=10)

        # Auswahl-Buttons mit Tooltips
        self.choice_var = tk.StringVar(value="zeit")
        choice1_rb = ttk.Radiobutton(
            steuerungs_frame,
            text="Zeit",
            variable=self.choice_var,
            value="zeit",
            command=self.update_labels
        )
        choice1_rb.pack(anchor=tk.W, pady=5)
        self.create_tooltip(choice1_rb, "Wählen Sie, ob die Zeit als Basis für die Berechnungen dienen soll.")

        choice2_rb = ttk.Radiobutton(
            steuerungs_frame,
            text="Beschleunigung",
            variable=self.choice_var,
            value="beschleunigung",
            command=self.update_labels
        )
        choice2_rb.pack(anchor=tk.W, pady=5)
        self.create_tooltip(choice2_rb, "Wählen Sie, ob die Beschleunigung als Basis für die Berechnungen dienen soll.")

        choice3_rb = ttk.Radiobutton(
            steuerungs_frame,
            text="Distanz/Geschwindigkeit",
            variable=self.choice_var,
            value="distanz_geschwindigkeit",
            command=self.update_labels
        )
        choice3_rb.pack(anchor=tk.W, pady=5)
        self.create_tooltip(choice3_rb, "Wählen Sie, ob die Distanz oder Geschwindigkeit als Basis dienen soll.")

        ttk.Separator(steuerungs_frame, orient='horizontal').pack(fill='x', pady=10)

        # Eingabefelder
        self.val01_label = ttk.Label(steuerungs_frame, text="Zeit (s):")
        self.val01_label.pack(anchor=tk.W)
        self.val01 = tk.DoubleVar(value=25.0)
        val01_entry = ttk.Entry(steuerungs_frame, textvariable=self.val01)
        val01_entry.pack(fill=tk.X, pady=5)
        self.create_tooltip(val01_entry, "Geben Sie den Wert für die erste Variable ein.")

        self.val02_label = ttk.Label(steuerungs_frame, text="Distanz (m):")
        self.val02_label.pack(anchor=tk.W)
        self.val02 = tk.DoubleVar(value=55.0)
        val02_entry = ttk.Entry(steuerungs_frame, textvariable=self.val02)
        val02_entry.pack(fill=tk.X, pady=5)
        self.create_tooltip(val02_entry, "Geben Sie den Wert für die zweite Variable ein.")

        ttk.Separator(steuerungs_frame, orient='horizontal').pack(fill='x', pady=10)

        # Start-/Stop-Button
        self.calc_button = ttk.Button(steuerungs_frame, text="Start", command=self.toggle_simulation)
        self.calc_button.pack(pady=10)
        self.create_tooltip(self.calc_button, "Starten oder stoppen Sie die Simulation.")

        # Quellen- und Versionslabels
        self.source_label = ttk.Label(
            steuerungs_frame,
            text="Quelle & Info: www.HomoFaciens.de",
            font=("Arial", 8)
        )
        self.source_label.pack(side=tk.BOTTOM, pady=5)
        self.version_label = ttk.Label(
            steuerungs_frame,
            text=self.version_number,
            font=("Arial", 8)
        )
        self.version_label.pack(side=tk.BOTTOM)

        # Graph-Bereich mit matplotlib
        self.fig, self.ax = plt.subplots(figsize=(5, 4))
        self.ax.set_xlabel("Zeit (s)")
        self.ax.set_ylabel("Distanz (m)")
        self.ax.set_title("Graph")
        self.line, = self.ax.plot([], [], 'r-')  # Initial leere Linie

        self.canvas = FigureCanvasTkAgg(self.fig, master=graph_frame)
        self.canvas.draw()
        self.canvas.get_tk_widget().pack(side=tk.TOP, fill=tk.BOTH, expand=True)

        # Szenenbereich mit Canvas
        self.scene_canvas = tk.Canvas(szenen_frame, width=900, height=200, bg="white")
        self.scene_canvas.pack()

        # Initiale Objekte auf dem Canvas erstellen
        self.background_img_id = None
        self.sun_img_id = None
        self.moon_img_id = None
        self.vehicle_id = self.scene_canvas.create_rectangle(50, 150, 100, 180, fill="blue")
        self.vehicle_label = self.scene_canvas.create_text(75, 165, text="Fahrzeug", fill="white")
        self.sun_moon_id = None  # Platzhalter für Sonne/Mond

    def create_tooltip(self, widget, text):
        tooltip = ToolTip(widget, text)

    def load_assets(self):
        # Bilder laden (Platzhalter verwendet)
        self.images = {}
        image_names = [
            "car", "truck", "dove", "man",
            "background", "sun", "moon",
            "signal_red", "signal_yellow", "signal_green"
        ]
        for name in image_names:
            try:
                path = os.path.join("images", f"{name}.png")
                img = Image.open(path)
                # Verwenden Sie Resampling.LANCZOS, falls verfügbar, sonst Image.LANCZOS
                if Resampling:
                    resample = Resampling.LANCZOS
                else:
                    resample = Image.LANCZOS
                img = img.resize((50, 50), resample)
                self.images[name] = ImageTk.PhotoImage(img)
            except Exception as e:
                print(f"Fehler beim Laden des Bildes {name}: {e}")
                self.images[name] = None  # Platzhalter für fehlende Bilder

        # Hintergrundbild setzen
        if self.images.get("background"):
            self.background_img_id = self.scene_canvas.create_image(450, 100, image=self.images["background"])

        # Sounds laden (Platzhalter verwendet)
        self.sounds = {}
        sound_names = ["truck", "car", "dove", "man", "thrustSSC"]  # Stellen Sie sicher, dass die Namen übereinstimmen
        for name in sound_names:
            try:
                path = os.path.join("sounds", f"{name}.wav")
                self.sounds[name] = pygame.mixer.Sound(path)
            except Exception as e:
                print(f"Fehler beim Laden des Sounds {name}: {e}")
                self.sounds[name] = None

    def update_labels(self):
        choice = self.choice_var.get()
        formula = self.formula_var.get()

        if choice == "zeit":
            if formula == "s_at_t":
                self.val01_label.config(text="Zeit (s):")
                self.val02_label.config(text="Distanz (m):")
            elif formula == "v_at_t":
                self.val01_label.config(text="Zeit (s):")
                self.val02_label.config(text="Beschleunigung (m/s²):")
        elif choice == "beschleunigung":
            if formula == "s_at_t":
                self.val01_label.config(text="Beschleunigung (m/s²):")
                self.val02_label.config(text="Distanz (m):")
            elif formula == "v_at_t":
                self.val01_label.config(text="Beschleunigung (m/s²):")
                self.val02_label.config(text="Geschwindigkeit (m/s):")
        elif choice == "distanz_geschwindigkeit":
            if formula == "s_at_t":
                self.val01_label.config(text="Beschleunigung (m/s²):")
                self.val02_label.config(text="Zeit (s):")
            elif formula == "v_at_t":
                self.val01_label.config(text="Beschleunigung (m/s²):")
                self.val02_label.config(text="Zeit (s):")

        self.reset_values()

    def reset_values(self):
        self.graph_active = -1
        self.ax.clear()
        self.ax.set_xlabel(self.ax.get_xlabel())
        self.ax.set_ylabel(self.ax.get_ylabel())
        self.ax.set_title("Graph")
        self.line.set_data([], [])
        self.canvas.draw()
        self.x_data = []
        self.y_data = []
        self.is_running = False
        self.calc_button.config(text="Start")
        if self.timer:
            self.after_cancel(self.timer)
            self.timer = None
        pygame.mixer.stop()

    def toggle_simulation(self):
        if not self.is_running:
            # Simulation starten
            try:
                val1 = float(self.val01.get())
                val2 = float(self.val02.get())
            except ValueError:
                messagebox.showerror("Ungültige Eingabe", "Bitte geben Sie gültige numerische Werte ein.")
                return

            formula = self.formula_var.get()
            choice = self.choice_var.get()

            # Berechnungen basierend auf Formel und Auswahl durchführen
            try:
                if formula == "s_at_t":
                    if choice == "zeit":
                        # s = (a / 2) * t²
                        acceleration = val1
                        distance = val2
                        x_max = math.sqrt(2 * distance / acceleration) if acceleration != 0 else 10
                        y_max = distance
                        x_label = "Zeit (s)"
                        y_label = "Distanz (m)"
                    elif choice == "beschleunigung":
                        # a = 2s / t²
                        distance = val1
                        time_val = val2
                        acceleration = 2 * distance / (time_val ** 2) if time_val != 0 else 0
                        x_max = time_val
                        y_max = acceleration
                        x_label = "Zeit (s)"
                        y_label = "Beschleunigung (m/s²)"
                    elif choice == "distanz_geschwindigkeit":
                        # s = (a / 2) * t²
                        acceleration = val1
                        time_val = val2
                        distance = (acceleration / 2) * (time_val ** 2)
                        x_max = time_val
                        y_max = distance
                        x_label = "Zeit (s)"
                        y_label = "Distanz (m)"
                elif formula == "v_at_t":
                    if choice == "zeit":
                        # v = a * t
                        acceleration = val1
                        speed = val2
                        x_max = speed / acceleration if acceleration != 0 else 10
                        y_max = speed
                        x_label = "Zeit (s)"
                        y_label = "Geschwindigkeit (m/s)"
                    elif choice == "beschleunigung":
                        # a = v / t
                        speed = val1
                        time_val = val2
                        acceleration = speed / time_val if time_val != 0 else 0
                        x_max = time_val
                        y_max = acceleration
                        x_label = "Zeit (s)"
                        y_label = "Beschleunigung (m/s²)"
                    elif choice == "distanz_geschwindigkeit":
                        # v = a * t
                        acceleration = val1
                        time_val = val2
                        speed = acceleration * time_val
                        x_max = time_val
                        y_max = speed
                        x_label = "Zeit (s)"
                        y_label = "Geschwindigkeit (m/s)"
            except Exception as e:
                messagebox.showerror("Berechnungsfehler", f"Ein Fehler ist bei den Berechnungen aufgetreten: {e}")
                return

            # Graph-Labels aktualisieren
            self.ax.set_xlabel(x_label)
            self.ax.set_ylabel(y_label)
            self.ax.set_title("Graph")

            # Graph zurücksetzen
            self.ax.clear()
            self.ax.set_xlabel(x_label)
            self.ax.set_ylabel(y_label)
            self.ax.set_title("Graph")
            self.line, = self.ax.plot([], [], 'r-')  # Neue Linie erstellen
            self.canvas.draw()

            # Simulationsparameter setzen
            self.x_max = x_max
            self.y_max = y_max
            self.simulation_time = 0
            self.start_time = time.time()
            self.is_running = True
            self.calc_button.config(text="Stop")

            # Timer starten
            self.timer = self.after(self.timer_pause, self.update_simulation)

        else:
            # Simulation stoppen
            self.is_running = False
            self.calc_button.config(text="Start")
            if self.timer:
                self.after_cancel(self.timer)
                self.timer = None
            pygame.mixer.stop()

    def update_simulation(self):
        if not self.is_running:
            return

        current_time = time.time()
        self.simulation_time = current_time - self.start_time

        # Physik basierend auf Formel und Auswahl aktualisieren
        formula = self.formula_var.get()
        choice = self.choice_var.get()

        try:
            if formula == "s_at_t":
                if choice == "zeit":
                    acceleration = float(self.val01.get())
                    t = self.simulation_time
                    s = (acceleration / 2) * (t ** 2)
                    data_point = (t, s)
                elif choice == "beschleunigung":
                    distance = float(self.val01.get())
                    t = self.simulation_time
                    acceleration = 2 * distance / (t ** 2) if t != 0 else 0
                    s = (acceleration / 2) * (t ** 2)
                    data_point = (t, acceleration)
                elif choice == "distanz_geschwindigkeit":
                    acceleration = float(self.val01.get())
                    t = self.simulation_time
                    s = (acceleration / 2) * (t ** 2)
                    data_point = (t, s)
            elif formula == "v_at_t":
                if choice == "zeit":
                    acceleration = float(self.val01.get())
                    t = self.simulation_time
                    v = acceleration * t
                    data_point = (t, v)
                elif choice == "beschleunigung":
                    speed = float(self.val01.get())
                    t = self.simulation_time
                    acceleration = speed / t if t != 0 else 0
                    v = acceleration * t
                    data_point = (t, acceleration)
                elif choice == "distanz_geschwindigkeit":
                    acceleration = float(self.val01.get())
                    t = self.simulation_time
                    v = acceleration * t
                    data_point = (t, v)
        except Exception as e:
            messagebox.showerror("Simulationsfehler", f"Ein Fehler ist während der Simulation aufgetreten: {e}")
            self.toggle_simulation()
            return

        # Daten zum Graphen hinzufügen
        self.x_data.append(data_point[0])
        self.y_data.append(data_point[1])
        self.line.set_data(self.x_data, self.y_data)
        self.ax.relim()
        self.ax.autoscale_view()
        self.update_graph()

        # Szene aktualisieren (Fahrzeugposition)
        self.update_scene(data_point[1])

        # Nächste Aktualisierung planen
        if self.simulation_time < self.x_max:
            self.timer = self.after(self.timer_pause, self.update_simulation)
        else:
            self.toggle_simulation()

    def update_graph(self):
        self.canvas.draw()

    def update_scene(self, value):
        # Berechnen Sie die x-Position basierend auf dem Wert
        x_position = 50 + (value / self.y_max) * 800  # Skalierung auf Canvas-Breite (900px)
        x_position = min(max(x_position, 50), 850)  # Begrenzen auf Canvas-Bereich

        # Fahrzeug bewegen
        self.scene_canvas.coords(self.vehicle_id, x_position, 150, x_position + 50, 180)
        self.scene_canvas.coords(self.vehicle_label, x_position + 25, 165)

        # Sonne oder Mond basierend auf Simulationszeit zeichnen
        if self.simulation_time < self.x_max / 2:
            if self.images.get("sun"):
                if self.sun_img_id:
                    self.scene_canvas.coords(self.sun_img_id, 50 + self.simulation_time * 800 / self.x_max, 50)
                else:
                    self.sun_img_id = self.scene_canvas.create_image(
                        50 + self.simulation_time * 800 / self.x_max,
                        50,
                        image=self.images["sun"]
                    )
        else:
            if self.images.get("moon"):
                adjusted_time = self.simulation_time - self.x_max / 2
                if self.moon_img_id:
                    self.scene_canvas.coords(
                        self.moon_img_id,
                        50 + adjusted_time * 800 / self.x_max,
                        50
                    )
                else:
                    self.moon_img_id = self.scene_canvas.create_image(
                        50 + adjusted_time * 800 / self.x_max,
                        50,
                        image=self.images["moon"]
                    )

        # Sound basierend auf Simulation aktualisieren
        if self.simulation_time < self.x_max:
            if self.sounds.get("car") and not pygame.mixer.get_busy():
                self.sounds["car"].play(-1)  # Endlos schleifen
        else:
            if self.sounds.get("car"):
                self.sounds["car"].stop()

    def on_closing(self):
        if self.is_running and self.timer:
            self.after_cancel(self.timer)
        pygame.mixer.quit()
        self.destroy()

class ToolTip:
    """
    Ein einfaches Tooltip-Widget für Tkinter.
    """
    def __init__(self, widget, text):
        self.widget = widget
        self.text = text
        self.tip_window = None
        self.widget.bind("<Enter>", self.show_tip)
        self.widget.bind("<Leave>", self.hide_tip)

    def show_tip(self, event=None):
        if self.tip_window or not self.text:
            return
        x, y, cx, cy = self.widget.bbox("insert")
        x = x + self.widget.winfo_rootx() + 25
        y = y + self.widget.winfo_rooty() + 20
        self.tip_window = tw = tk.Toplevel(self.widget)
        tw.wm_overrideredirect(True)  # Entfernt Fensterrahmen
        tw.wm_geometry(f"+{x}+{y}")
        label = tk.Label(tw, text=self.text, justify='left',
                         background="#ffffe0", relief='solid', borderwidth=1,
                         font=("Arial", "10", "normal"))
        label.pack(ipadx=1)

    def hide_tip(self, event=None):
        tw = self.tip_window
        self.tip_window = None
        if tw:
            tw.destroy()

if __name__ == "__main__":
    app = AccelerationApp()
    app.protocol("WM_DELETE_WINDOW", app.on_closing)
    app.mainloop()
