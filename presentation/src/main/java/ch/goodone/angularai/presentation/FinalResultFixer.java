package ch.goodone.angularai.presentation;

import org.apache.poi.sl.usermodel.Placeholder;
import org.apache.poi.xslf.usermodel.*;
import org.openxmlformats.schemas.presentationml.x2006.main.*;
import org.openxmlformats.schemas.drawingml.x2006.main.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;

public class FinalResultFixer {
    public static void main(String[] args) throws Exception {
        String resultPath = args.length > 0 ? args[0] : "../doc/history/presentations/SoftwareEntwicklungAI.pptx";
        File file = new File(resultPath);
        if (!file.exists()) {
            System.out.println("File not found: " + file.getAbsolutePath());
            return;
        }

        try (XMLSlideShow ppt = new XMLSlideShow(new FileInputStream(file))) {
            // Fix Master and Layouts first
            for (XSLFSlideMaster master : ppt.getSlideMasters()) {
                System.out.println("Processing master");
                cleanupRedundantShapes(master);
                ensureSlideNumberPlaceholder(master); // Ensure Master has it too
                reassignIds(master);
                for (XSLFSlideLayout layout : master.getSlideLayouts()) {
                    System.out.println("Processing layout: " + layout.getName());
                    cleanupRedundantShapes(layout);
                    ensureSlideNumberPlaceholder(layout);
                    reassignIds(layout);
                }
            }
            
            for (XSLFSlide slide : ppt.getSlides()) {
                System.out.println("Processing slide " + slide.getSlideNumber());
                cleanupRedundantShapes(slide);
                ensureSlideNumber(slide);
                reassignIds(slide); // Reassign IDs for the slide after ensuring slide number
            }

            validatePackageIntegrity(ppt);

            try (FileOutputStream out = new FileOutputStream(file)) {
                ppt.write(out);
            }
            System.out.println("File fixed successfully: " + resultPath);
        }
    }

    private static void cleanupRedundantShapes(XSLFSheet sheet) {
        System.out.println("  Cleaning up redundant shapes in " + sheet.getClass().getSimpleName());
        java.util.List<XSLFShape> toRemove = new java.util.ArrayList<>();
        for (XSLFShape shape : sheet.getShapes()) {
            if (shape instanceof XSLFTextShape) {
                XSLFTextShape ts = (XSLFTextShape) shape;
                String text = ts.getText();
                if (text != null) {
                    // Remove shapes containing "null<#>" or multiple "<#>" if they are not the primary placeholder
                    if (text.contains("null<#>") || (text.contains("<#>") && ts.getPlaceholder() != Placeholder.SLIDE_NUMBER && ts.getShapeName().startsWith("TextBox"))) {
                        System.out.println("    Marking redundant shape for removal: " + ts.getShapeName() + " [" + text + "]");
                        toRemove.add(ts);
                    }
                }
            }
        }
        for (XSLFShape shape : toRemove) {
            sheet.removeShape(shape);
        }
    }

    private static void ensureSlideNumberPlaceholder(XSLFSheet sheet) {
        XSLFTextShape snp = null;
        for (XSLFShape shape : sheet.getShapes()) {
            if (shape instanceof XSLFTextShape) {
                XSLFTextShape ts = (XSLFTextShape) shape;
                if (ts.getPlaceholder() == Placeholder.SLIDE_NUMBER) {
                    snp = ts;
                    break;
                }
            }
        }

        if (snp != null) {
            fixDynamicField(snp);
        }
    }

    private static void reassignIds(XSLFSheet sheet) {
        System.out.println("  Reassigning IDs for " + sheet.getClass().getSimpleName());
        long currentId = 5000; // Start EVEN higher to avoid any low-range conflicts
        for (XSLFShape shape : sheet.getShapes()) {
            try {
                // Use reflection to find the CNvPr in the specific shape's XML bean
                Object ct = shape.getXmlObject();
                
                Object nvPr = null;
                // Try to find the NvPr object which contains CNvPr
                for (java.lang.reflect.Method m : ct.getClass().getMethods()) {
                    if (m.getName().startsWith("getNv") && m.getName().endsWith("Pr") && m.getParameterCount() == 0) {
                        nvPr = m.invoke(ct);
                        if (nvPr != null) break;
                    }
                }

                if (nvPr != null) {
                    Object cNvPr = nvPr.getClass().getMethod("getCNvPr").invoke(nvPr);
                    // Explicitly use the setId method of CTNonVisualDrawingProps
                    cNvPr.getClass().getMethod("setId", long.class).invoke(cNvPr, currentId++);
                }

                // If it's a graphic frame (like a table or chart), we might need to drill down
                if (shape instanceof XSLFGraphicFrame) {
                    XSLFGraphicFrame gf = (XSLFGraphicFrame) shape;
                    // Additional integrity checks for graphic frames could be added here
                }
                
            } catch (Exception e) {
                System.out.println("    Failed to reassign ID for shape: " + shape.getShapeName() + ": " + e.getMessage());
            }
        }
    }

    public static void validatePackageIntegrity(XMLSlideShow ppt) {
        System.out.println("Validating package integrity...");
        // Re-assign IDs for everything in a single global sequence
        long globalId = 10000;
        
        // Masters
        for (XSLFSlideMaster master : ppt.getSlideMasters()) {
            globalId = reassignIdsGlobally(master, globalId);
            for (XSLFSlideLayout layout : master.getSlideLayouts()) {
                globalId = reassignIdsGlobally(layout, globalId);
            }
        }
        
        // Slides
        for (XSLFSlide slide : ppt.getSlides()) {
            globalId = reassignIdsGlobally(slide, globalId);
        }
    }

    private static long reassignIdsGlobally(XSLFSheet sheet, long startId) {
        long currentId = startId;
        for (XSLFShape shape : sheet.getShapes()) {
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
                    cNvPr.getClass().getMethod("setId", long.class).invoke(cNvPr, currentId++);
                }
            } catch (Exception e) {}
        }
        return currentId;
    }

    private static void ensureSlideNumber(XSLFSlide slide) {
        XSLFTextShape snp = null;
        for (XSLFShape shape : slide.getShapes()) {
            if (shape instanceof XSLFTextShape) {
                XSLFTextShape ts = (XSLFTextShape) shape;
                if (ts.getPlaceholder() == Placeholder.SLIDE_NUMBER) {
                    snp = ts;
                    break;
                }
                // Also check for shapes that look like slide numbers (often created by Pandoc as plain text shapes)
                if (ts.getText() != null && ts.getText().contains("<#>")) {
                    System.out.println("  Found text shape containing <#>, marking as SLIDE_NUMBER placeholder.");
                    ts.setPlaceholder(Placeholder.SLIDE_NUMBER);
                    snp = ts;
                    break;
                }
            }
        }

        if (snp == null) {
            System.out.println("  Slide number placeholder not found, searching in layout...");
            // Find it from layout
            XSLFSlideLayout layout = slide.getSlideLayout();
            XSLFTextShape layoutSnp = null;
            for (XSLFShape shape : layout.getShapes()) {
                if (shape instanceof XSLFTextShape) {
                    XSLFTextShape ts = (XSLFTextShape) shape;
                    if (ts.getPlaceholder() == Placeholder.SLIDE_NUMBER) {
                        layoutSnp = ts;
                        break;
                    }
                }
            }
            
            if (layoutSnp != null) {
                System.out.println("  Creating slide number placeholder from layout...");
                
                int maxId = 0;
                for (XSLFShape s : slide.getShapes()) {
                    try {
                        CTShape c = (CTShape) s.getXmlObject();
                        if (c.getNvSpPr() != null && c.getNvSpPr().getCNvPr() != null) {
                            maxId = Math.max(maxId, (int) c.getNvSpPr().getCNvPr().getId());
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                }
                // Use a very high starting point for new shapes to avoid any conflict with POI's internal low-number counters
                int nextId = Math.max(maxId + 1, 2000);

                snp = slide.createAutoShape();
                CTShape ctShape = (CTShape) snp.getXmlObject();
                if (ctShape.getNvSpPr() != null && ctShape.getNvSpPr().getCNvPr() != null) {
                    ctShape.getNvSpPr().getCNvPr().setId(nextId);
                }
                
                snp.setPlaceholder(Placeholder.SLIDE_NUMBER);
                snp.setAnchor(layoutSnp.getAnchor());
                
                System.out.println("  Assigned unique shape ID: " + nextId);
            } else {
                System.out.println("  Fallback: Creating slide number placeholder at default position...");
                
                int maxId = 0;
                for (XSLFShape s : slide.getShapes()) {
                    try {
                        CTShape c = (CTShape) s.getXmlObject();
                        if (c.getNvSpPr() != null && c.getNvSpPr().getCNvPr() != null) {
                            maxId = Math.max(maxId, (int) c.getNvSpPr().getCNvPr().getId());
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                }
                int nextId = Math.max(maxId + 1, 2000);

                snp = slide.createAutoShape();
                CTShape ctShape = (CTShape) snp.getXmlObject();
                if (ctShape.getNvSpPr() != null && ctShape.getNvSpPr().getCNvPr() != null) {
                    ctShape.getNvSpPr().getCNvPr().setId(nextId);
                }
                
                snp.setPlaceholder(Placeholder.SLIDE_NUMBER);
                snp.setAnchor(new java.awt.geom.Rectangle2D.Double(650, 500, 50, 30));

                System.out.println("  Assigned unique shape ID (fallback): " + nextId);
            }
        } else {
            System.out.println("  Found existing slide number placeholder.");
        }

        // Only clear and set dynamic field if it's NOT already a dynamic field
        boolean alreadyDynamic = false;
        for (XSLFTextParagraph p : snp.getTextParagraphs()) {
            if (p.getXmlObject().getFldList() != null && !p.getXmlObject().getFldList().isEmpty()) {
                for (CTTextField fld : p.getXmlObject().getFldList()) {
                    if ("slidenum".equals(fld.getType())) {
                        alreadyDynamic = true;
                        break;
                    }
                }
            }
        }

        if (!alreadyDynamic) {
            fixDynamicField(snp);
        } else {
            System.out.println("  Placeholder is already a dynamic field.");
        }
    }

    private static void fixDynamicField(XSLFTextShape snp) {
        System.out.println("  Configuring dynamic slide number field...");
        snp.clearText();
        XSLFTextParagraph p = snp.addNewTextParagraph();
        
        CTTextParagraph ctP = p.getXmlObject();
        CTTextField fld = ctP.addNewFld();
        fld.setId("{6682B14C-E45B-4C21-A882-968B35397453}");
        fld.setType("slidenum");
        fld.setT("<#>");
        
        CTTextCharacterProperties rPr = fld.addNewRPr();
        rPr.setLang("en-US");
        rPr.setSz(900); // 9.0pt
        CTTextFont latin = rPr.addNewLatin();
        latin.setTypeface("Frutiger for ZKB Light");
        rPr.setDirty(false);
        rPr.setSmtClean(false);
        // Re-set text to ensure it's not literal 'slide number' or similar
        fld.setT("<#>"); 
        
        // Also ensure the paragraph properties are minimal
        if (!ctP.isSetPPr()) ctP.addNewPPr();
        CTTextParagraphProperties pPr = ctP.getPPr();
        pPr.setAlgn(STTextAlignType.R); // Right align by default for slide numbers in many templates
    }
}
