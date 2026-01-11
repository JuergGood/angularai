# Pandoc PPTX Authoring Checklist (Corporate)

## Slide Structure
- Use exactly one `# Heading` per slide
- Do NOT use `---` for slide breaks
- Keep max 5–6 bullets per slide

## Bullets
- Use `-` for bullets
- Max depth: 3 levels
- Avoid long sentences

## Images
- Use relative paths
- Prefer placing images BEFORE text in a column
- One image per column

## Two-column Layout
Use Pandoc columns:

::: columns
::: column
Left content
:::
::: column
Right content
:::
:::

## Speaker Notes
Use:
::: notes
Text here
:::

## Corporate Safety
- No model version numbers on slides
- No customer data
- Neutral wording

## Export Command
pandoc slides.md --reference-doc=template.pptx -o output.pptx
