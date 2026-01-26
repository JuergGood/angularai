import markdown
from docx import Document
from htmldocx import HtmlToDocx
import os
import sys

def print_usage():
    """
    Prints the usage instructions for the script.
    """
    print("Usage: python scripts\\md_to_docx.py <input_file.md> <output_file.docx>")
    print("Example: python scripts\\md_to_docx.py doc\\ai\\tech.md doc\\ai\\tech.docx")

def convert_md_to_docx(md_path, docx_path):
    """
    Converts a Markdown file to a DOCX file using HTML as an intermediate format.
    
    Args:
        md_path (str): Path to the source Markdown file.
        docx_path (str): Path to the destination DOCX file.
    
    Returns:
        bool: True if conversion was successful, False otherwise.
    """
    if not os.path.exists(md_path):
        print(f"Error: Input file '{md_path}' not found.")
        return False
    
    try:
        # Read the Markdown content
        with open(md_path, 'r', encoding='utf-8') as f:
            md_content = f.read()
        
        # Convert Markdown to HTML
        # We use extensions like 'extra' for tables, 'toc' for table of contents, and 'fenced_code' for code blocks
        html_content = markdown.markdown(md_content, extensions=['extra', 'toc', 'fenced_code'])
        
        # Convert HTML to DOCX
        document = Document()
        new_parser = HtmlToDocx()
        
        # Add the converted HTML content to the Word document
        new_parser.add_html_to_document(html_content, document)
        
        # Save the document to the specified path
        document.save(docx_path)
        print(f"Successfully converted '{md_path}' to '{docx_path}'")
        return True
    except Exception as e:
        print(f"An error occurred during conversion: {e}")
        return False

if __name__ == "__main__":
    # Check if correct number of arguments are provided
    if len(sys.argv) == 3:
        input_file = sys.argv[1]
        output_file = sys.argv[2]
        convert_md_to_docx(input_file, output_file)
    elif len(sys.argv) == 1:
        # Default behavior if no arguments provided (for backward compatibility or convenience)
        # However, following the 'usage printout' request, we should probably encourage arguments
        print("No arguments provided. Using default paths.")
        convert_md_to_docx('doc/ai/technical-fixes-summary.md', 'doc/ai/technical-fixes-summary.docx')
    else:
        # Invalid number of arguments
        print_usage()
        sys.exit(1)
