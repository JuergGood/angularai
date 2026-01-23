import markdown
import json
import os

# Configuration
LANGUAGES = ["en", "de-ch"]
SOURCE_FILES = {
    "en": [
        ("readme", "README.md"),
        ("user-guide", "doc/userguide/user-guide.md"),
        ("admin-guide", "doc/userguide/admin-guide.md"),
        ("faq", "doc/userguide/faq.md"),
        ("release-notes", "doc/userguide/release-notes.md"),
        ("android-build-instructions", "doc/ai/android/android-build-instructions.md"),
        ("aws-setup", "doc/ai/aws/aws_setup.md"),
        ("postgres-setup", "doc/ai/backend/postgres_setup.md"),
        ("aws-fargate-config", "doc/ai/aws/aws_fargate_config.md"),
        ("aws-create-target-group", "doc/ai/aws/aws_create_target_group.md"),
        ("aws-alb-troubleshooting", "doc/ai/aws/aws_alb_troubleshooting.md"),
        ("aws-ecs-push-instructions", "doc/ai/aws/aws_ecs_push_instructions.md"),
        ("md-to-confluence", "scripts/md_to_confluence.py")
    ],
    "de-ch": [
        ("readme", "README_de.md"),
        ("user-guide", "doc/userguide/user-guide_de.md"),
        ("admin-guide", "doc/userguide/admin-guide_de.md"),
        ("faq", "doc/userguide/faq_de.md"),
        ("release-notes", "doc/userguide/release-notes.md"),
        ("android-build-instructions", "doc/ai/android/android-build-instructions.md"),
        ("aws-setup", "doc/ai/aws/aws_setup.md"),
        ("postgres-setup", "doc/ai/backend/postgres_setup.md"),
        ("aws-fargate-config", "doc/ai/aws/aws_fargate_config.md"),
        ("aws-create-target-group", "doc/ai/aws/aws_create_target_group.md"),
        ("aws-alb-troubleshooting", "doc/ai/aws/aws_alb_troubleshooting.md"),
        ("aws-ecs-push-instructions", "doc/ai/aws/aws_ecs_push_instructions.md"),
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
