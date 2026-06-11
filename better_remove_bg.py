import sys
import os
from PIL import Image

def remove_checkerboard(img_path):
    out_path = img_path
    try:
        img = Image.open(img_path).convert("RGBA")
        width, height = img.size
        datas = img.getdata()
        
        # We assume the top-left corner is part of the checkerboard.
        # Actually, let's sample the top row and left column to find the two most common colors.
        edge_colors = set()
        for x in range(width):
            edge_colors.add(img.getpixel((x, 0))[:3])
            edge_colors.add(img.getpixel((x, height-1))[:3])
        for y in range(height):
            edge_colors.add(img.getpixel((0, y))[:3])
            edge_colors.add(img.getpixel((width-1, y))[:3])
            
        # The checkerboard usually consists of two colors.
        # Let's count the frequency of each color on the edge.
        color_counts = {}
        for x in range(width):
            c1 = img.getpixel((x, 0))[:3]
            c2 = img.getpixel((x, height-1))[:3]
            color_counts[c1] = color_counts.get(c1, 0) + 1
            color_counts[c2] = color_counts.get(c2, 0) + 1
        for y in range(height):
            c1 = img.getpixel((0, y))[:3]
            c2 = img.getpixel((width-1, y))[:3]
            color_counts[c1] = color_counts.get(c1, 0) + 1
            color_counts[c2] = color_counts.get(c2, 0) + 1
            
        # Get the top 2 colors
        sorted_colors = sorted(color_counts.items(), key=lambda x: x[1], reverse=True)
        bg_colors = []
        for color, count in sorted_colors:
            # Checkerboard is usually light, so r,g,b should be high and grayscale-ish
            r, g, b = color
            if r > 180 and g > 180 and b > 180 and abs(r-g) < 20 and abs(r-b) < 20:
                bg_colors.append(color)
            if len(bg_colors) == 2:
                break
                
        print(f"[{img_path}] Detected bg colors: {bg_colors}")
        
        # Now we replace these colors (and similar ones) with transparent
        def color_dist(c1, c2):
            return sum((a - b) ** 2 for a, b in zip(c1, c2)) ** 0.5

        newData = []
        for item in datas:
            rgb = item[:3]
            is_bg = False
            for bg_c in bg_colors:
                if color_dist(rgb, bg_c) < 15:  # Tolerance for anti-aliasing
                    is_bg = True
                    break
            
            if is_bg:
                newData.append((255, 255, 255, 0))
            else:
                newData.append(item)
                
        img.putdata(newData)
        img.save(out_path, "PNG")
        print(f"Saved {out_path}")
        
    except Exception as e:
        print(f"Error processing {img_path}: {e}")

images = ["repo_v2.png", "shared_v2.png", "recent_v2.png", "trash_v2.png"]
base_dir = r"src\main\resources\images"

for img in images:
    full_path = os.path.join(base_dir, img)
    if os.path.exists(full_path):
        remove_checkerboard(full_path)
    else:
        print(f"Not found: {full_path}")
