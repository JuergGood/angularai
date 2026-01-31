package ch.goodone.angularai.presentation;

import org.apache.poi.xslf.usermodel.*;
import org.openxmlformats.schemas.presentationml.x2006.main.CTShape;
import java.io.FileInputStream;
import java.io.File;

public class DetailedInspector {
    public static void main(String[] args) throws Exception {
        String[] files = {"../doc/history/presentations/template-company.pptx", "../doc/history/presentations/template.pptx", "../doc/history/presentations/SoftwareEntwicklungAI.pptx"};
        
        for (String filePath : files) {
            File file = new File(filePath);
            if (!file.exists()) {
                System.out.println("File not found: " + file.getAbsolutePath());
                continue;
            }
            System.out.println("\n--- Inspecting: " + filePath + " ---");
            try (XMLSlideShow ppt = new XMLSlideShow(new FileInputStream(file))) {
                // Check Theme fonts
                if (!ppt.getSlides().isEmpty()) {
                    XSLFTheme theme = ppt.getSlides().get(0).getTheme();
                    if (theme != null) {
                       System.out.println("Major Font (Headings): " + theme.getMajorFont());
                       System.out.println("Minor Font (Body): " + theme.getMinorFont());
                    }
                }

                // Check Master fonts and IDs
                if (!ppt.getSlideMasters().isEmpty()) {
                    XSLFSlideMaster master = ppt.getSlideMasters().get(0);
                    System.out.println("Master Slide Shapes:");
                    inspectSheet(master);
                    for (XSLFSlideLayout layout : master.getSlideLayouts()) {
                        System.out.println("Layout: " + layout.getName() + " (" + layout.getType() + ") Shapes:");
                        inspectSheet(layout);
                    }
                }
                
                // Check all slides for slide number placeholder
                for (int i = 0; i < ppt.getSlides().size(); i++) {
                    XSLFSlide slide = ppt.getSlides().get(i);
                    System.out.println("Slide " + i + " Shapes:");
                    inspectSheet(slide);
                }
            }
        }
    }

    private static void inspectSheet(XSLFSheet sheet) {
        for (XSLFShape shape : sheet.getShapes()) {
            long id = -1;
            String phType = "";
            try {
                Object ct = shape.getXmlObject();
                Object nvPr = null;
                for (java.lang.reflect.Method m : ct.getClass().getMethods()) {
                    if (m.getName().startsWith("getNv") && m.getName().endsWith("Pr") && m.getParameterCount() == 0) {
                        nvPr = m.invoke(ct);
                        if (nvPr != null) break;
                    }
                }
                if (nvPr != null) {
                    Object cNvPr = nvPr.getClass().getMethod("getCNvPr").invoke(nvPr);
                    id = (Long) cNvPr.getClass().getMethod("getId").invoke(cNvPr);
                    
                    Object nvPrInner = nvPr.getClass().getMethod("getNvPr").invoke(nvPr);
                    if (nvPrInner != null) {
                        java.lang.reflect.Method getPh = nvPrInner.getClass().getMethod("getPh");
                        Object ph = getPh.invoke(nvPrInner);
                        if (ph != null) {
                            phType = " (PH Type: " + ph.getClass().getMethod("getType").invoke(ph) + ")";
                        }
                    }
                }
            } catch (Exception e) {}
            
            String phText = "";
            if (shape instanceof XSLFTextShape) {
                XSLFTextShape ts = (XSLFTextShape) shape;
                phText = ", Placeholder: " + ts.getPlaceholder() + ", Text: [" + ts.getText() + "]";
                System.out.println("  Shape Name: " + shape.getShapeName() + ", ID: " + id + phType + phText);
                
                if (ts.getPlaceholder() != null && ts.getPlaceholder().toString().contains("SLIDE_NUMBER")) {
                    System.out.println("    [FOUND SLIDE_NUMBER] Placeholder: " + ts.getPlaceholder() + ", Text: [" + ts.getText() + "], Name: " + ts.getShapeName());
                    for (XSLFTextParagraph p : ts.getTextParagraphs()) {
                        for (XSLFTextRun r : p.getTextRuns()) {
                            System.out.println("      Run font: " + r.getFontFamily() + ", text: [" + r.getRawText() + "]");
                        }
                        // Check for fields at CT level
                        if (p.getXmlObject().getFldList() != null && !p.getXmlObject().getFldList().isEmpty()) {
                            System.out.println("      [CTField found] type: " + p.getXmlObject().getFldList().get(0).getType());
                        }
                    }
                }
            } else {
                System.out.println("  Shape Name: " + shape.getShapeName() + ", ID: " + id + phType);
            }
        }
    }
}
