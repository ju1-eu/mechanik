# erstellen_platzhalterbildes.py

from PIL import Image, ImageDraw, ImageFont
import os

def create_placeholder_image(filepath, text):
    img = Image.new('RGB', (50, 50), color=(155, 155, 155))
    d = ImageDraw.Draw(img)
    try:
        font = ImageFont.truetype("arial.ttf", 10)
    except IOError:
        font = ImageFont.load_default()
    text_width, text_height = d.textsize(text, font=font)
    d.text(
        ((50 - text_width) / 2, (50 - text_height) / 2),
        text,
        fill=(255, 255, 255),
        font=font
    )
    img.save(filepath)

# Definieren Sie die fehlenden Bildnamen
missing_images = [
    "car.png",
    "man.png",
    "signal_red.png",
    "signal_yellow.png",
    "signal_green.png"
]

images_dir = "images"
os.makedirs(images_dir, exist_ok=True)

for image_name in missing_images:
    path = os.path.join(images_dir, image_name)
    if not os.path.exists(path):
        # Extrahieren Sie den Namen ohne Dateiendung für das Text-Overlay
        text = os.path.splitext(image_name)[0]
        create_placeholder_image(path, text)
        print(f"Platzhalterbild erstellt: {path}")
    else:
        print(f"Bild bereits vorhanden: {path}")
