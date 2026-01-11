import os
import re
from pptx import Presentation
from pptx.util import Inches, Pt

class PresentationGenerator:
    def __init__(self, plan_path, output_path, files_dir, template_path=None):
        self.plan_path = plan_path
        self.output_path = output_path
        self.files_dir = files_dir
        if template_path and os.path.exists(template_path):
            self.prs = Presentation(template_path)
            print(f"Using template: {template_path}")
        else:
            self.prs = Presentation()
            # Set slide width and height to 16:9 for default presentation
            self.prs.slide_width = Inches(13.33)
            self.prs.slide_height = Inches(7.5)
            if template_path:
                print(f"Warning: Template not found at {template_path}. Using default.")

    def parse_plan(self):
        with open(self.plan_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        slides_raw = content.split('---')
        slides = []
        for raw in slides_raw:
            lines = raw.strip().split('\n')
            if not lines or (len(lines) == 1 and not lines[0].strip()): continue
            
            slide = {'title': '', 'layout': 'title_and_content', 'content': '', 'images': [], 'subtitle': '', 'left': '', 'right': '', 'image': ''}
            
            # First line is usually the title
            if lines[0].startswith('# '):
                slide['title'] = lines[0][2:].strip()
                lines = lines[1:]
            
            current_key = None
            for line in lines:
                if not line.strip():
                    if current_key:
                        slide[current_key] += '\n'
                    continue
                
                # Check for key: value pattern
                # Avoid matching lines that are just list items (e.g., "- item")
                # Also check for indentation to decide if it's a new top-level key or sub-content
                is_indented = line.startswith('  ') or line.startswith('\t')
                match = re.match(r'^(\w+):\s*(.*)', line.strip())
                
                if match and not line.strip().startswith('-') and not is_indented:
                    current_key = match.group(1).lower()
                    value = match.group(2).strip()
                    if value == '|':
                        slide[current_key] = ''
                    else:
                        slide[current_key] = value
                elif current_key:
                    # Append to current key's value
                    # Keep indentation but remove trailing spaces
                    clean_line = line.rstrip()
                    
                    # Special handling for bullets: remove the hyphen but keep the level
                    # We expect 2 spaces per level.
                    # "- item" -> level 0
                    # "  - subitem" -> level 1
                    
                    match_bullet = re.match(r'^(\s*)-\s*(.*)', clean_line)
                    if match_bullet:
                        indent = match_bullet.group(1)
                        content = match_bullet.group(2)
                        clean_line = indent + content
                    
                    if slide[current_key]:
                        slide[current_key] += '\n' + clean_line
                    else:
                        slide[current_key] = clean_line
            
            # Clean up trailing/leading whitespace in all values
            for k in slide:
                if isinstance(slide[k], str):
                    slide[k] = slide[k].strip()
            
            slides.append(slide)
        return slides

    def add_title_slide(self, slide_data):
        layout = self.prs.slide_layouts[0]
        slide = self.prs.slides.add_slide(layout)
        slide.shapes.title.text = slide_data['title']
        if slide_data.get('subtitle'):
            # Some title layouts have subtitle in placeholder 1
            if len(slide.placeholders) > 1:
                slide.placeholders[1].text = slide_data['subtitle']

    def add_text_to_frame(self, text_frame, text):
        text_frame.clear()  # Clear default paragraph
        lines = text.split('\n')
        
        # Determine base indentation (minimum indentation of non-empty lines)
        non_empty_lines = [l for l in lines if l.strip()]
        if not non_empty_lines:
            return
            
        base_indent = min(len(l) - len(l.lstrip()) for l in non_empty_lines)
        
        for i, line in enumerate(lines):
            if not line.strip(): continue
            
            # Determine level by leading spaces (2 spaces = 1 level)
            stripped = line.lstrip()
            indent = len(line) - len(stripped)
            # Relative to base_indent
            level = (indent - base_indent) // 2
            
            # PowerPoint supports up to 5 levels (0-4), we cap it
            level = max(0, min(level, 4))
            
            if i == 0 or not text_frame.paragraphs:
                p = text_frame.paragraphs[0] if text_frame.paragraphs else text_frame.add_paragraph()
            else:
                p = text_frame.add_paragraph()
            
            p.text = stripped
            p.level = level
            # Set font size based on level
            p.font.size = Pt(max(20 - level * 2, 12))

    def add_content_slide(self, slide_data):
        # Use 'Title and Content' layout
        layout = self.prs.slide_layouts[1]
        slide = self.prs.slides.add_slide(layout)
        slide.shapes.title.text = slide_data['title']
        
        body = slide.placeholders[1]
        self.add_text_to_frame(body.text_frame, slide_data.get('content', ''))
        
        # Add image if exists
        img_path = slide_data.get('image')
        if img_path:
            full_path = os.path.join(os.path.dirname(self.plan_path), img_path)
            if not os.path.exists(full_path):
                # Try relative to base_dir if plan_path relative didn't work
                full_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), img_path)
            
            if os.path.exists(full_path):
                # We use fixed positioning but try to avoid the header
                # Title and Content layout usually has the body starting around Inches(1.5)
                left = Inches(8) if slide_data.get('content') else Inches(3)
                top = Inches(1.5) 
                width = Inches(5)
                slide.shapes.add_picture(full_path, left, top, width=width)
            else:
                print(f"Warning: Image not found at {full_path}")

    def add_two_content_slide(self, slide_data):
        # Layout 3 is 'Two Content' in the template
        layout = self.prs.slide_layouts[3]
        slide = self.prs.slides.add_slide(layout)
        slide.shapes.title.text = slide_data['title']
        
        # Left content
        left_placeholder = slide.placeholders[1]
        self.add_text_to_frame(left_placeholder.text_frame, slide_data.get('left', ''))
        
        # Right content (could be image or text)
        right_placeholder = slide.placeholders[2]
        
        right_text = slide_data.get('right', '')
        if 'image:' in right_text:
            img_match = re.search(r'image:\s*([^\n\s]+)', right_text)
            if img_match:
                img_path = img_match.group(1)
                full_path = os.path.join(os.path.dirname(self.plan_path), img_path)
                if os.path.exists(full_path):
                    # Use the placeholder's position and size for the image
                    left = right_placeholder.left
                    top = right_placeholder.top
                    width = right_placeholder.width
                    height = right_placeholder.height
                    
                    # We remove the right placeholder to avoid "Click to add text"
                    sp = right_placeholder.element
                    sp.getparent().remove(sp)
                    
                    slide.shapes.add_picture(full_path, left, top, width=width)
                else:
                    print(f"Warning: Image not found at {full_path}")
                    self.add_text_to_frame(right_placeholder.text_frame, right_text)
        else:
            self.add_text_to_frame(right_placeholder.text_frame, right_text)

    def generate(self):
        slides_data = self.parse_plan()
        
        # Map layout names to indices (standard PowerPoint layouts)
        layout_map = {
            'title': 0,
            'title_and_content': 1,
            'section_header': 2,
            'two_content': 3,
            'comparison': 4,
            'title_only': 5,
            'blank': 6,
            'content_with_caption': 7,
            'picture_with_caption': 8
        }
        
        for i, data in enumerate(slides_data):
            layout_type = data.get('layout', 'title_and_content').lower()
            
            if layout_type == 'title':
                self.add_title_slide(data)
            elif layout_type == 'two_content':
                self.add_two_content_slide(data)
            elif layout_type == 'title_and_content':
                self.add_content_slide(data)
            elif layout_type in layout_map:
                # Generic handler for other layouts
                idx = layout_map[layout_type]
                layout = self.prs.slide_layouts[idx]
                slide = self.prs.slides.add_slide(layout)
                
                if slide.shapes.title:
                    slide.shapes.title.text = data['title']
                
                # Try to fill placeholders
                for ph in slide.placeholders:
                    if ph.placeholder_format.type in (2, 7): # Body (2) or Object (7)
                        content = data.get('content') or data.get('left') or data.get('right')
                        if content:
                            self.add_text_to_frame(ph.text_frame, content)
            else:
                self.add_content_slide(data)
        
        try:
            self.prs.save(self.output_path)
            print(f"Successfully generated {self.output_path}")
        except PermissionError:
            base, ext = os.path.splitext(self.output_path)
            alt_path = f"{base}_new{ext}"
            print(f"Warning: Could not save to {self.output_path} (file might be open). Saving to {alt_path} instead.")
            self.prs.save(alt_path)

if __name__ == "__main__":
    # Use absolute paths relative to this script's directory
    base_dir = os.path.dirname(os.path.abspath(__file__))
    plan = os.path.join(base_dir, "presentation-slides-de.md")
    output = os.path.join(base_dir, "generated/SoftwareDevelopmentWithAI.pptx")
    files = os.path.join(base_dir, "files")
    template = os.path.join(base_dir, "template.pptx")
    
    # Ensure generated directory exists
    os.makedirs(os.path.dirname(output), exist_ok=True)
    
    gen = PresentationGenerator(plan, output, files, template)
    gen.generate()
