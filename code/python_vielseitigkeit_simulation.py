import random
import time
from typing import List, Tuple
from heapq import nsmallest

# Konstanten
K_NEIGHBORS = 3
ZIEL_POSITION = 10
BEWEGUNGS_GRENZE = 1.0
SPIEL_VERZOEGERUNG = 0.1
MAX_ZUEGE = 100
HINDERNISSE = [(random.uniform(0, 10), random.uniform(0, 10)) for _ in range(5)]
BONUS_PUNKTE = [(random.uniform(0, 10), random.uniform(0, 10)) for _ in range(3)]

# Webentwicklung Simulation
def web_entwicklung() -> str:
    """
    Simuliert einen einfachen Webserver.
    
    Returns:
        Eine einfache HTTP-Antwort mit HTML-Inhalt.
    """
    return "HTTP/1.1 200 OK\nContent-Type: text/html\n\n<html><body><h1>Willkommen zur Python-Simulation!</h1></body></html>"

# Datenanalyse Simulation
def datenanalyse(daten: List[float]) -> Tuple[float, float]:
    """
    Führt eine einfache Datenanalyse durch.
    
    Args:
        daten: Liste von Fließkommazahlen für die Analyse.
    
    Returns:
        Ein Tupel mit dem Mittelwert und dem Maximum der Daten.
    """
    if not daten:
        return 0.0, 0.0
    return sum(daten) / len(daten), max(daten)

# Maschinelles Lernen Simulation
def maschinelles_lernen(trainings_daten: List[Tuple[float, float]], test_punkt: float) -> float:
    """
    Simuliert einen einfachen k-Nearest-Neighbors Algorithmus.
    
    Args:
        trainings_daten: Liste von (x, y) Tupeln als Trainingsdaten.
        test_punkt: x-Wert, für den y vorhergesagt werden soll.
    
    Returns:
        Vorhergesagter y-Wert für den test_punkt.
    """
    distanzen = [(abs(punkt[0] - test_punkt), punkt[1]) for punkt in trainings_daten]
    naechste_nachbarn = nsmallest(K_NEIGHBORS, distanzen)
    return sum(nachbar[1] for nachbar in naechste_nachbarn) / K_NEIGHBORS

# Spieleprogrammierung Simulation
class SpielObjekt:
    """
    Repräsentiert ein Objekt im Spiel mit x- und y-Koordinaten.
    """
    def __init__(self, x: float, y: float):
        self.x = x
        self.y = y

    def bewegen(self, dx: float, dy: float):
        self.x += dx
        self.y += dy

def bewegung_berechnen(befehl: str) -> Tuple[int, int]:
    bewegungen = {
        'w': (0, 1), 's': (0, -1), 'a': (-1, 0), 'd': (1, 0),
        'wa': (-1, 1), 'wd': (1, 1), 'sa': (-1, -1), 'sd': (1, -1)
    }
    return bewegungen.get(befehl, (0, 0))

def bonus_pruefen(spieler: SpielObjekt) -> int:
    for i, (x, y) in enumerate(BONUS_PUNKTE):
        if abs(spieler.x - x) < 0.5 and abs(spieler.y - y) < 0.5:
            del BONUS_PUNKTE[i]
            print("Bonus gefunden! +100 Punkte")
            return 100
    return 0

def hindernis_pruefen(spieler: SpielObjekt) -> bool:
    return any(abs(spieler.x - x) < 0.5 and abs(spieler.y - y) < 0.5 for x, y in HINDERNISSE)

def print_spielfeld(spieler: SpielObjekt, ziel: SpielObjekt):
    feld = [[' ' for _ in range(11)] for _ in range(11)]
    feld[min(10, max(0, int(spieler.y)))][min(10, max(0, int(spieler.x)))] = 'P'
    feld[min(10, max(0, int(ziel.y)))][min(10, max(0, int(ziel.x)))] = 'Z'
    for x, y in HINDERNISSE:
        feld[min(10, max(0, int(y)))][min(10, max(0, int(x)))] = 'X'
    for x, y in BONUS_PUNKTE:
        feld[min(10, max(0, int(y)))][min(10, max(0, int(x)))] = 'O'
    
    print("  " + " ".join(str(i) for i in range(11)))
    for i, zeile in enumerate(reversed(feld)):
        print(f"{10-i:2d} {' '.join(zeile)}")

def spiel_simulation() -> None:
    """
    Simuliert ein interaktives Spiel mit erweiterten Funktionen.
    
    Der Spieler steuert ein Objekt, um ein Ziel zu erreichen, mit Hindernissen und Bonuspunkten.
    """
    spieler = SpielObjekt(0, 0)
    ziel = SpielObjekt(ZIEL_POSITION, ZIEL_POSITION)
    zuege = 0
    punkte = 0
    start_zeit = time.time()
    
    print("Steuern Sie den Spieler zum Ziel (10, 10)!")
    print("Befehle: 'w' (hoch), 's' (runter), 'a' (links), 'd' (rechts)")
    print("         'wa' (links-oben), 'wd' (rechts-oben), 'sa' (links-unten), 'sd' (rechts-unten)")
    print("         'q' (beenden)")
    print("Hindernisse: X, Bonuspunkte: O")
    
    while zuege < MAX_ZUEGE:
        print_spielfeld(spieler, ziel)
        abstand_x = abs(spieler.x - ziel.x)
        abstand_y = abs(spieler.y - ziel.y)
        print(f"Spieler Position: ({spieler.x:.2f}, {spieler.y:.2f})")
        print(f"Entfernung zum Ziel: ({abstand_x:.2f}, {abstand_y:.2f})")
        print(f"Aktuelle Punkte: {punkte}")
        befehl = input("Ihr Zug: ").lower()
        
        if befehl == 'q':
            print("Spiel beendet.")
            return
        
        dx, dy = bewegung_berechnen(befehl)
        if dx == dy == 0:
            print("Ungültige Eingabe. Bitte verwenden Sie 'w', 'a', 's', 'd', 'wa', 'wd', 'sa', 'sd' oder 'q'.")
            continue
        
        laenge = random.uniform(0.5, 1.5)
        alte_position = (spieler.x, spieler.y)
        spieler.bewegen(dx * laenge, dy * laenge)
        zuege += 1
        
        print(f"Bewegung: ({(spieler.x - alte_position[0]):.2f}, {(spieler.y - alte_position[1]):.2f})")
        
        punkte += bonus_pruefen(spieler)
        if hindernis_pruefen(spieler):
            print("Sie haben ein Hindernis getroffen! Zurück zur vorherigen Position.")
            spieler.x, spieler.y = alte_position
        
        if abstand_x <= BEWEGUNGS_GRENZE and abstand_y <= BEWEGUNGS_GRENZE:
            spieldauer = time.time() - start_zeit
            punkte += max(0, 1000 - zuege * 10)  # Bonus für schnelles Erreichen des Ziels
            print(f"Ziel erreicht in {zuege} Zügen und {spieldauer:.2f} Sekunden!")
            print(f"Endpunktzahl: {punkte}")
            return
    
    print(f"Maximale Anzahl von Zügen ({MAX_ZUEGE}) erreicht. Spiel beendet.")

# Hauptfunktion zur Demonstration der Vielseitigkeit von Python
def python_vielseitigkeit_demo():
    print("Python Vielseitigkeits-Simulation")
    print("=================================")
    
    # Webentwicklung
    print("\n1. Webentwicklung:")
    print(web_entwicklung())
    
    # Datenanalyse
    print("\n2. Datenanalyse:")
    daten = [random.uniform(0, 100) for _ in range(10)]
    mittelwert, maximum = datenanalyse(daten)
    print(f"Daten: {daten}")
    print(f"Mittelwert: {mittelwert:.2f}, Maximum: {maximum:.2f}")
    
    # Maschinelles Lernen
    print("\n3. Maschinelles Lernen:")
    trainings_daten = [(i, i**2) for i in range(10)]
    test_punkt = 4.5
    vorhersage = maschinelles_lernen(trainings_daten, test_punkt)
    print(f"Trainingsdaten: {trainings_daten}")
    print(f"Vorhersage für {test_punkt}: {vorhersage:.2f}")
    
    # Spieleprogrammierung
    print("\n4. Spieleprogrammierung:")
    spiel_simulation()

if __name__ == "__main__":
    python_vielseitigkeit_demo()

# Dokumentation:
"""
Diese erweiterte Python-Simulation demonstriert die Vielseitigkeit von Python in verschiedenen Anwendungsbereichen:

1. Webentwicklung: Simuliert einen einfachen Webserver, der eine HTML-Antwort zurückgibt.
2. Datenanalyse: Führt eine einfache statistische Analyse (Mittelwert und Maximum) auf zufällig generierten Daten durch.
3. Maschinelles Lernen: Implementiert einen vereinfachten k-Nearest-Neighbors Algorithmus für Regressionsaufgaben.
4. Spieleprogrammierung: Simuliert ein interaktives 2D-Spiel, in dem der Benutzer einen Spieler zum Ziel steuern muss,
   während er Hindernissen ausweicht und Bonuspunkte sammelt.

Die Simulation zeigt, wie Python in diesen verschiedenen Bereichen eingesetzt werden kann, 
von der Verarbeitung von Strings und Listen bis hin zur Implementierung von Klassen und der 
Verwendung von Benutzereingaben. Sie demonstriert auch Pythons Lesbarkeit und die Möglichkeit, 
komplexe Konzepte in relativ wenigen Zeilen Code auszudrücken.

Das Spiel am Ende bietet nun eine visuelle Darstellung des Spielfelds, Hindernisse, Bonuspunkte,
und ein Punktesystem, was es interaktiver und herausfordernder macht.

Um die Simulation auszuführen, führen Sie einfach dieses Skript aus. Es werden nacheinander 
Beispiele aus jedem der vier Bereiche ausgeführt und die Ergebnisse in der Konsole ausgegeben.
"""