import markdown
import json
import os

# Configuration
SOURCE_FILES = [
    ("readme", "README.md"),
    ("user-guide", "doc/userguide/user-guide.md"),
    ("admin-guide", "doc/userguide/admin-guide.md"),
    ("faq", "doc/userguide/faq.md")
]
OUTPUT_FILE = "frontend/src/assets/help/help-data.json"

def convert_md_to_html(md_path):
    if not os.path.exists(md_path):
        print(f"Warning: {md_path} not found.")
        return None
    with open(md_path, 'r', encoding='utf-8') as f:
        content = f.read()
    html = markdown.markdown(content, extensions=['extra', 'toc'])
    return html

def main():
    help_data = {}
    for page_id, path in SOURCE_FILES:
        print(f"Converting {path}...")
        html_content = convert_md_to_html(path)
        if html_content:
            help_data[page_id] = html_content

    os.makedirs(os.path.dirname(OUTPUT_FILE), exist_ok=True)
    with open(OUTPUT_FILE, 'w', encoding='utf-8') as f:
        json.dump(help_data, f, ensure_ascii=False, indent=2)
    print(f"Successfully generated {OUTPUT_FILE}")

if __name__ == "__main__":
    main()
