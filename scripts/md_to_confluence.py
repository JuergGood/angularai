import markdown
import requests
import json
import os

# Configuration
CONFLUENCE_URL = "https://your-domain.atlassian.net/wiki/rest/api/content"
USERNAME = "your-email@example.com"
API_TOKEN = "your-api-token"
SPACE_KEY = "DOC"
PARENT_PAGE_ID = "12345" # Optional: ID of the parent page

def md_to_confluence_storage(md_file_path):
    """
    Converts a Markdown file to HTML which is compatible with Confluence Storage Format.
    Note: Standard HTML is often accepted by Confluence API, but some macros might need special tags.
    """
    with open(md_file_path, 'r', encoding='utf-8') as f:
        md_content = f.read()
    
    # Convert Markdown to HTML
    html_content = markdown.markdown(md_content, extensions=['extra', 'toc'])
    return html_content

def post_to_confluence(title, html_content, page_id=None):
    """
    Posts content to Confluence. If page_id is provided, it updates the page.
    """
    headers = {
        "Accept": "application/json",
        "Content-Type": "application/json"
    }
    
    auth = (USERNAME, API_TOKEN)
    
    data = {
        "type": "page",
        "title": title,
        "space": {"key": SPACE_KEY},
        "body": {
            "storage": {
                "value": html_content,
                "representation": "storage"
            }
        }
    }
    
    if PARENT_PAGE_ID:
        data["ancestors"] = [{"id": PARENT_PAGE_ID}]

    if page_id:
        # Update existing page (needs version increment)
        # First, get current version
        resp = requests.get(f"{CONFLUENCE_URL}/{page_id}?expand=version", auth=auth)
        version = resp.json()['version']['number'] + 1
        data["version"] = {"number": version}
        url = f"{CONFLUENCE_URL}/{page_id}"
        response = requests.put(url, data=json.dumps(data), headers=headers, auth=auth)
    else:
        # Create new page
        url = CONFLUENCE_URL
        response = requests.post(url, data=json.dumps(data), headers=headers, auth=auth)
    
    return response.status_code, response.text

if __name__ == "__main__":
    docs = [
        ("User Guide", "doc/userguide/user-guide.md"),
        ("Admin Guide", "doc/userguide/admin-guide.md"),
        ("FAQ", "doc/userguide/faq.md")
    ]
    
    print("This script is a proposal. Please configure your Confluence credentials before running.")
    # for title, path in docs:
    #     if os.path.exists(path):
    #         print(f"Converting {path}...")
    #         storage_format = md_to_confluence_storage(path)
    #         # status, text = post_to_confluence(title, storage_format)
    #         # print(f"Status: {status}")
