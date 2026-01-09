import os
try:
    from pptx import Presentation
    from pptx.util import Inches, Pt
    from pptx.enum.text import PP_ALIGN
except ImportError:
    print("Error: python-pptx library not found. Please install it using: pip install python-pptx")
    exit(1)

def add_slide(prs, title, content):
    slide_layout = prs.slide_layouts[1] # Bullet point layout
    slide = prs.slides.add_slide(slide_layout)
    slide.shapes.title.text = title
    
    tf = slide.placeholders[1].text_frame
    tf.text = content[0]
    
    for point in content[1:]:
        p = tf.add_paragraph()
        p.text = point
        p.level = 0

def create_presentation():
    prs = Presentation()

    # Slide 1: Title
    title_slide_layout = prs.slide_layouts[0]
    slide = prs.slides.add_slide(title_slide_layout)
    title = slide.shapes.title
    subtitle = slide.placeholders[1]
    title.text = "Accelerating Full-Stack Development with Junie AI"
    subtitle.text = "A Case Study on the AngularAI Project Ecosystem"

    # Slide 2: Application Overview
    add_slide(prs, "Application Overview: The 'AngularAI' Ecosystem", [
        "Unified Experience: Seamless task management across Web and Mobile.",
        "Web App: Modern Angular dashboard using Material Design.",
        "Android App: Native experience built with Jetpack Compose.",
        "Features: Auth, Task Management, User Administration, Profile Settings."
    ])

    # Slide 3: Architecture Overview
    add_slide(prs, "Architecture Overview", [
        "Client Layer: Angular (Web) & Jetpack Compose (Android).",
        "API Layer: Spring Boot REST API (Stateless).",
        "Persistence: PostgreSQL (AWS RDS) managed via Flyway.",
        "Infrastructure: Containerized deployment on AWS ECS Fargate.",
        "Communication: HTTPS / REST / Basic Auth."
    ])

    # Slide 4: Backend Excellence: Spring Boot 4 & JPA
    add_slide(prs, "Backend Excellence: Spring Boot 4 & JPA", [
        "Clean Code: DTO-based communication (fromEntity mapping).",
        "Security: RBAC with Spring Security and BCrypt.",
        "Efficiency: Automated database migrations with Flyway.",
        "Junie's Role: Scaffolding controllers, repositories, and optimizing JPA queries."
    ])

    # Slide 5: Modern Frontend: Angular & Material
    add_slide(prs, "Modern Frontend: Angular & Material", [
        "Reactive State: Angular Signals for fine-grained updates.",
        "Standalone Architecture: Modular, tree-shakable components.",
        "UX/UI: Material 3 (indigo-pink) for a professional look.",
        "Junie's Role: UI scaffolding and Signal-based service generation."
    ])

    # Slide 6: Native Power: Android Jetpack Compose
    add_slide(prs, "Native Power: Android Jetpack Compose", [
        "Declarative UI: Modern Kotlin-based UI development.",
        "Offline First: Local caching with Room Database.",
        "Reactive Flow: Coroutines and Flow for data sync.",
        "Junie's Role: Translating web requirements to native mobile components."
    ])

    # Slide 7: Local Development Setup
    add_slide(prs, "Local Development Setup: Integrated Ecosystem", [
        "IntelliJ IDEA + Junie: Full-stack core (Spring Boot & Angular).",
        "Android Studio + Gemini: Native mobile dev in Emulator.",
        "Docker on Windows: Local PostgreSQL persistence.",
        "Test Client: Validating endpoints locally or on AWS cloud.",
        "Hot Reloading: Instant feedback for Web, Mobile, and Backend."
    ])

    # Slide 8: AI-Driven Development with Junie
    add_slide(prs, "AI-Driven Development with Junie", [
        "Context Awareness: Multi-language project understanding.",
        "Unit Test Generation: Automated JUnit 5 and Vitest coverage.",
        "Command Execution: Direct terminal interaction for builds/deploys.",
        "Productivity: 40% reduction in repetitive coding tasks."
    ])

    # Slide 8: Case Study: Seamless Database Evolution
    add_slide(prs, "Case Study: Seamless Database Evolution", [
        "Problem: Syncing schema changes between Backend and Mobile.",
        "Process: Modify JPA Entity -> Junie generates Flyway SQL.",
        "Sync: Junie updates Android Room Entity and Migration scripts.",
        "Result: Consistent data state across all platforms."
    ])

    # Slide 9: Infrastructure & AWS Deployment
    add_slide(prs, "Infrastructure & AWS Deployment", [
        "Docker: Identical dev/prod environments via Compose.",
        "AWS Fargate: Serverless container orchestration.",
        "CI/CD: Automated PowerShell scripts for ECR/ECS.",
        "Junie's Role: Infrastructure as Code (Task Definitions) and log analysis."
    ])

    # Slide 10: Conclusion
    add_slide(prs, "Conclusion: The Productivity Multiplier", [
        "Speed: Faster time-to-market for full-stack features.",
        "Quality: Consistent standards and high test coverage.",
        "Integration: Bridging the gap between Web, Mobile, and Backend.",
        "Motto: Build smarter, not harder, with Junie."
    ])

    output_path = "AngularAI_Junie_Presentation.pptx"
    prs.save(output_path)
    print(f"Presentation saved to {os.path.abspath(output_path)}")

if __name__ == "__main__":
    create_presentation()
