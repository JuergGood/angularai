# Pandoc Reference PPTX – Recommended Setup

Edit `template.pptx` in PowerPoint:

## Slide Master
- Define consistent margins
- Add subtle page border on:
  - Title and Content
  - Two Content

## Layouts to Maintain
- Title Slide
- Title and Content
- Two Content

Do NOT rename layouts after setup.

## Fonts & Colors
- Use corporate font as default
- Avoid per-slide overrides

## Image Placeholders
- Ensure Two Content layout has:
  - Two OBJECT/BODY placeholders
  - Clear left/right alignment

Save and use with:
pandoc slides.md --reference-doc=template.pptx
