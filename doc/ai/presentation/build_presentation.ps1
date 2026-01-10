# Automated Presentation Build Script

# 1. Generate ER Diagram from JPA Entities
Write-Host "Generating ER Diagram..." -ForegroundColor Cyan
python doc\ai\presentation\generate_er_diagram.py

# 2. Generate PowerPoint Presentation
Write-Host "Generating PowerPoint Presentation..." -ForegroundColor Cyan
python doc\ai\presentation\generate_presentation.py

Write-Host "Build Complete!" -ForegroundColor Green
