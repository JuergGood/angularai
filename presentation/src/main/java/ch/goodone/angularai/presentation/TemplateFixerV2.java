package ch.goodone.angularai.presentation;

import org.apache.poi.sl.usermodel.Placeholder;
import org.apache.poi.xslf.usermodel.*;
import org.openxmlformats.schemas.presentationml.x2006.main.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;

public class TemplateFixerV2 {
    public static void main(String[] args) throws Exception {
        String templatePath = "../doc/history/presentations/template.pptx";
        File file = new File(templatePath);
        if (!file.exists()) {
            System.out.println("File not found: " + file.getAbsolutePath());
            return;
        }

        try (XMLSlideShow ppt = new XMLSlideShow(new FileInputStream(file))) {
            // Enable slide numbers on all slides
            CTPresentation ctPresentation = ppt.getCTPresentation();
            // Note: CTPresentation doesn't have a direct "showSlideNumbers" but slide numbers are per slide/master
            
            for (XSLFSlideMaster master : ppt.getSlideMasters()) {
                fixSlideNumberPlaceholder(master);
                for (XSLFSlideLayout layout : master.getSlideLayouts()) {
                    fixSlideNumberPlaceholder(layout);
                }
            }

            // Pandoc-generated slides might be missing the placeholder entirely if it's not in the layout it uses
            // But here we are fixing the template.
            
            try (FileOutputStream out = new FileOutputStream(file)) {
                ppt.write(out);
            }
            System.out.println("Template updated successfully.");
        }
    }

    private static void fixSlideNumberPlaceholder(XSLFSheet sheet) {
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

        if (snp == null) {
            System.out.println("Adding SLIDE_NUMBER placeholder to " + sheet.getClass().getSimpleName());
            snp = sheet.createAutoShape();
            snp.setPlaceholder(Placeholder.SLIDE_NUMBER);
            // Position it bottom right
            snp.setAnchor(new java.awt.geom.Rectangle2D.Double(600, 500, 100, 30)); 
        }

        System.out.println("Ensuring field in " + sheet.getClass().getSimpleName());
        snp.clearText();
        XSLFTextParagraph p = snp.addNewTextParagraph();
        XSLFTextRun r = p.addNewTextRun();
        r.setFontFamily("Frutiger for ZKB Light");
        r.setFontSize(9.0);
        
        // Ensure the field is present at XML level
        var ctP = p.getXmlObject();
        if (ctP.getFldList().isEmpty()) {
            var fld = ctP.addNewFld();
            fld.setId("{6682B14C-E45B-4C21-A882-968B35397453}");
            fld.setType("slidenum");
            fld.setT("<#>");
            var rPr = fld.addNewRPr();
            rPr.setLang("en-US");
            rPr.addNewLatin().setTypeface("Frutiger for ZKB Light");
        }
    }
}
