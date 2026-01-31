import markdown
import json
import os

# Configuration
LANGUAGES = ["en", "de-ch"]
SOURCE_FILES = {
    "en": [
        ("readme", "README.md"),
        ("user-guide", "doc/user-guide/user-guide.md"),
        ("faq", "doc/user-guide/faq.md"),
        ("release-notes", "doc/user-guide/release-notes.md"),
        ("admin-guide", "doc/admin-guide/admin-guide.md"),
        ("android-build", "doc/development/android/android-build.md"),
        ("backend-dev", "doc/development/backend/Backend-Development.md"),
        ("postgres-setup", "doc/development/backend/postgres_setup.md"),
        ("frontend-dev", "doc/development/frontend/Frontend-Development.md"),
        ("deployment", "doc/infrastructure/Deployment.md"),
        ("md-to-confluence", "scripts/md_to_confluence.py")
    ],
    "de-ch": [
        ("readme", "README_de.md"),
        ("user-guide", "doc/user-guide/user-guide_de.md"),
        ("faq", "doc/user-guide/faq_de.md"),
        ("release-notes", "doc/user-guide/release-notes.md"),
        ("admin-guide", "doc/admin-guide/admin-guide_de.md"),
        ("md-to-confluence", "scripts/md_to_confluence.py")
    ]
}
OUTPUT_DIR = "frontend/public/assets/help"

def convert_md_to_html(md_path):
    if not os.path.exists(md_path):
        print(f"Warning: {md_path} not found.")
        return None
    with open(md_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Simple check for python files to wrap in code block
    if md_path.endswith('.py'):
        content = f"```python\n{content}\n```"
        
    html = markdown.markdown(content, extensions=['extra', 'toc', 'fenced_code'])
    return html

def main():
    for lang in LANGUAGES:
        print(f"Processing language: {lang}")
        help_data = {}
        for page_id, path in SOURCE_FILES.get(lang, []):
            print(f"  Converting {path}...")
            html_content = convert_md_to_html(path)
            if html_content:
                help_data[page_id] = html_content

        os.makedirs(OUTPUT_DIR, exist_ok=True)
        output_file = os.path.join(OUTPUT_DIR, f"help-data-{lang}.json")
        with open(output_file, 'w', encoding='utf-8') as f:
            json.dump(help_data, f, ensure_ascii=False, indent=2)
        print(f"Successfully generated {output_file}")

if __name__ == "__main__":
    main()
