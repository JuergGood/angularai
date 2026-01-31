package ch.goodone.angularai.presentation;

import org.apache.poi.sl.usermodel.Placeholder;
import org.apache.poi.xslf.usermodel.*;
import java.io.FileInputStream;
import java.io.File;
import java.awt.geom.Rectangle2D;

public class LayoutInspector {
    public static void main(String[] args) throws Exception {
        String companyPath = "../doc/history/presentations/template-company.pptx";
        String templatePath = "../doc/history/presentations/template.pptx";
        
        inspect(companyPath, "COMPANY");
        inspect(templatePath, "ACTUAL TEMPLATE");
    }

    private static void inspect(String path, String label) throws Exception {
        File file = new File(path);
        if (!file.exists()) {
            System.out.println(label + " File not found: " + path);
            return;
        }
        System.out.println("\n--- " + label + ": " + path + " ---");
        try (XMLSlideShow ppt = new XMLSlideShow(new FileInputStream(file))) {
            for (XSLFSlideMaster master : ppt.getSlideMasters()) {
                System.out.println("Master Slide Layouts:");
                for (XSLFSlideLayout layout : master.getSlideLayouts()) {
                        System.out.println("  Layout: " + layout.getName() + " (" + layout.getType() + ")");
                        for (XSLFShape shape : layout.getShapes()) {
                            if (shape instanceof XSLFTextShape) {
                                XSLFTextShape ts = (XSLFTextShape) shape;
                                Rectangle2D anchor = ts.getAnchor();
                                System.out.println("    Shape: " + ts.getShapeName() + ", PH: " + ts.getPlaceholder() + 
                                    ", Anchor: [" + (anchor != null ? anchor.getX() : "null") + ", " + (anchor != null ? anchor.getY() : "null") + ", " + 
                                    (anchor != null ? anchor.getWidth() : "null") + ", " + (anchor != null ? anchor.getHeight() : "null") + "]");
                                if (ts.getTextParagraphs().size() > 0 && ts.getTextParagraphs().get(0).getTextRuns().size() > 0) {
                                    XSLFTextRun r = ts.getTextParagraphs().get(0).getTextRuns().get(0);
                                    System.out.println("      Font: " + r.getFontFamily() + ", Color: " + r.getFontColor());
                                }
                            }
                        }
                }
            }
        }
    }
}
