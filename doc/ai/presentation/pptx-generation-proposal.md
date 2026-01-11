Generate a pptx in following steps steps

Step 2 Image generation
Action: generate png images out of input file formats drawio, mmd, puml

input /files/input
output /files/generated


Step 4 Presentation generation
generate pptx from md and png files
Support all formats in MS PowerPoint, such as 'Two Content', 'Title and conten', ...
Support bullet lists with 3 levels on two content lides left and right

input
template for presentation template-pptx.pptx 
content of presentation presentation-slides-de.md
embedded files in presentation folder /files/images and /files/images

optional styling templates are given in doc/ai/presentation/Musterfolien.pptx

output 
generated/SoftwareDevelopmentWithAI.pptx

Modify current generation script generate_presentation.py


