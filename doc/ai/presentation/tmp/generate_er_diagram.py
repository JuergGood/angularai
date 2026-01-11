import os
import re

def parse_jpa_entities(model_dir):
    entities = {}
    relationships = []
    
    for filename in os.listdir(model_dir):
        if filename.endswith(".java") and not filename.endswith("Test.java"):
            with open(os.path.join(model_dir, filename), 'r', encoding='utf-8') as f:
                content = f.read()
            
            # Extract class name
            class_match = re.search(r'public class (\w+)', content)
            if not class_match: continue
            class_name = class_match.group(1)
            
            # Simple field extraction (private Fields)
            fields = re.findall(r'private (\w+)\s+(\w+);', content)
            # Filter out non-persistent or complex types for now, or keep them
            entities[class_name] = [f[1] for f in fields]
            
            # Extract relationships
            # @ManyToOne, @OneToMany, @OneToOne, @ManyToMany
            many_to_one = re.findall(r'@ManyToOne.*?\n\s+private\s+(\w+)\s+(\w+);', content, re.DOTALL)
            for m in many_to_one:
                relationships.append(f"{class_name} }}|--|| {m[0]} : references")
                
            one_to_many = re.findall(r'@OneToMany.*?\n\s+private\s+List<(\w+)>\s+(\w+);', content, re.DOTALL)
            for o in one_to_many:
                relationships.append(f"{class_name} ||--|{{ {o[0]} : contains")

    return entities, relationships

def generate_mermaid(entities, relationships, output_file):
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write("erDiagram\n")
        for entity, fields in entities.items():
            f.write(f"    {entity} {{\n")
            for field in fields:
                f.write(f"        string {field}\n")
            f.write("    }\n")
        
        for rel in relationships:
            f.write(f"    {rel}\n")

if __name__ == "__main__":
    model_path = os.path.join("backend", "src", "main", "java", "ch", "goodone", "angularai", "backend", "model")
    output_path = os.path.join("doc", "ai", "presentation", "../files", "er_diagram.mmd")
    
    if os.path.exists(model_path):
        entities, relationships = parse_jpa_entities(model_path)
        generate_mermaid(entities, relationships, output_path)
        print(f"ER Diagram (Mermaid) generated at {output_path}")
    else:
        print(f"Error: Model directory not found at {model_path}")
