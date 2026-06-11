import sys
import os

def process_image(img_path):
    out_path = img_path # Overwrite original
    try:
        from rembg import remove
        from PIL import Image
        
        print(f"Processing {img_path} with rembg...")
        input_image = Image.open(img_path)
        output_image = remove(input_image)
        output_image.save(out_path)
        print(f"Saved {out_path}")
        
    except Exception as e:
        print(f"Error processing {img_path}: {e}")

images = ["repo_v2.png", "shared_v2.png", "recent_v2.png", "trash_v2.png", "icon_note_v2.png"]
base_dir = r"src\main\resources\images"

for img in images:
    full_path = os.path.join(base_dir, img)
    if os.path.exists(full_path):
        process_image(full_path)
    else:
        print(f"Not found: {full_path}")
