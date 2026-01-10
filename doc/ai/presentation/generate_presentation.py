import os
import re
from pptx import Presentation
from pptx.util import Inches, Pt

class PresentationGenerator:
    def __init__(self, plan_path, output_path, files_dir):
        self.plan_path = plan_path
        self.output_path = output_path
        self.files_dir = files_dir
        self.prs = Presentation()
        # Set slide width and height to 16:9
        self.prs.slide_width = Inches(13.33)
        self.prs.slide_height = Inches(7.5)

    def parse_plan(self):
        with open(self.plan_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        slides_raw = content.split('---')
        slides = []
        for raw in slides_raw:
            lines = [l for l in raw.strip().split('\n') if l.strip()]
            if not lines: continue
            
            slide = {'title': '', 'layout': 'title_and_content', 'content': '', 'images': [], 'subtitle': '', 'left': '', 'right': '', 'image': ''}
            
            # First line is usually the title
            if lines[0].startswith('# '):
                slide['title'] = lines[0][2:].strip()
                lines = lines[1:]
            
            # Simple Key-Value parser for the rest
            current_key = None
            for line in lines:
                if ':' in line and not line.strip().startswith('-'):
                    key, value = line.split(':', 1)
                    current_key = key.strip().lower()
                    slide[current_key] = value.strip()
                elif line.strip().startswith('-') or current_key:
                    if current_key:
                        slide[current_key] = (slide.get(current_key, '') + '\n' + line.strip()).strip()
            
            slides.append(slide)
        return slides

    def add_title_slide(self, slide_data):
        layout = self.prs.slide_layouts[0]
        slide = self.prs.slides.add_slide(layout)
        slide.shapes.title.text = slide_data['title']
        if slide_data.get('subtitle'):
            slide.placeholders[1].text = slide_data['subtitle']

    def add_content_slide(self, slide_data):
        layout = self.prs.slide_layouts[1]
        slide = self.prs.slides.add_slide(layout)
        slide.shapes.title.text = slide_data['title']
        
        body = slide.placeholders[1]
        tf = body.text_frame
        tf.text = slide_data.get('content', '')
        
        # Add image if exists
        img_path = slide_data.get('image')
        if img_path:
            full_path = os.path.join(os.path.dirname(self.plan_path), img_path)
            if os.path.exists(full_path):
                # Place image on the right if there is text, or center if not
                left = Inches(8) if slide_data.get('content') else Inches(3)
                top = Inches(2)
                height = Inches(4)
                slide.shapes.add_picture(full_path, left, top, height=height)
            else:
                print(f"Warning: Image not found at {full_path}")

    def add_two_content_slide(self, slide_data):
        # Layout 4 is usually 'Two Content'
        layout = self.prs.slide_layouts[4]
        slide = self.prs.slides.add_slide(layout)
        slide.shapes.title.text = slide_data['title']
        
        # Left content
        left_placeholder = slide.placeholders[1]
        left_placeholder.text = slide_data.get('left', '')
        
        # Right content (could be image or text)
        right_placeholder = slide.placeholders[2]
        
        right_text = slide_data.get('right', '')
        if 'image:' in right_text:
            img_match = re.search(r'image:\s*([^\n\s]+)', right_text)
            if img_match:
                img_path = img_match.group(1)
                full_path = os.path.join(os.path.dirname(self.plan_path), img_path)
                if os.path.exists(full_path):
                    # We remove the right placeholder and add a picture instead for better control
                    sp = right_placeholder.element
                    sp.getparent().remove(sp)
                    slide.shapes.add_picture(full_path, Inches(7.5), Inches(2), width=Inches(5))
                else:
                    print(f"Warning: Image not found at {full_path}")
                    right_placeholder.text = right_text
        else:
            right_placeholder.text = right_text

    def generate(self):
        slides_data = self.parse_plan()
        for data in slides_data:
            layout_type = data.get('layout', 'title_and_content')
            if layout_type == 'title':
                self.add_title_slide(data)
            elif layout_type == 'two_content':
                self.add_two_content_slide(data)
            else:
                self.add_content_slide(data)
        
        self.prs.save(self.output_path)
        print(f"Successfully generated {self.output_path}")

if __name__ == "__main__":
    # Use absolute paths relative to this script's directory
    base_dir = os.path.dirname(os.path.abspath(__file__))
    plan = os.path.join(base_dir, "presentation-slides.md")
    output = os.path.join(base_dir, "AngularAI_Presentation.pptx")
    files = os.path.join(base_dir, "files")
    
    gen = PresentationGenerator(plan, output, files)
    gen.generate()
