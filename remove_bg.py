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
        
    except ImportError:
        print("rembg not installed, using fallback Pillow method.")
        from PIL import Image
        img = Image.open(img_path).convert("RGBA")
        datas = img.getdata()
        
        newData = []
        for item in datas:
            # Check if pixel is white or light gray (checkerboard)
            if item[0] > 200 and item[1] > 200 and item[2] > 200:
                newData.append((255, 255, 255, 0))
            else:
                newData.append(item)
                
        img.putdata(newData)
        img.save(out_path, "PNG")
        print(f"Saved {out_path} using fallback")

images = ["repo_new.png", "shared_new.png", "recent_new.png", "trash_new.png", "icon_note_v2.png"]
base_dir = r"src\main\resources\images"

for img in images:
    full_path = os.path.join(base_dir, img)
    if os.path.exists(full_path):
        process_image(full_path)
    else:
        print(f"Not found: {full_path}")
