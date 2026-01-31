package ch.goodone.angularai.presentation;

import org.apache.poi.sl.usermodel.Placeholder;
import org.apache.poi.xslf.usermodel.*;
import org.openxmlformats.schemas.drawingml.x2006.main.*;
import org.openxmlformats.schemas.presentationml.x2006.main.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;

public class FinalTemplateFixer {
    public static void main(String[] args) throws Exception {
        String templatePath = "../doc/history/presentations/template.pptx";
        File file = new File(templatePath);
        if (!file.exists()) {
            System.out.println("File not found: " + templatePath);
            return;
        }

        try (XMLSlideShow ppt = new XMLSlideShow(new FileInputStream(file))) {
            // 1. Force Theme Fonts
            for (XSLFSlideMaster master : ppt.getSlideMasters()) {
                XSLFTheme theme = master.getTheme();
                if (theme != null) {
                    CTOfficeStyleSheet styleSheet = theme.getXmlObject();
                    CTBaseStyles themeElements = styleSheet.getThemeElements();
                    CTFontScheme fontScheme = themeElements.getFontScheme();
                    
                    // Major Font (Headings)
                    CTFontCollection major = fontScheme.getMajorFont();
                    major.getLatin().setTypeface("Frutiger for ZKB Light");
                    major.getEa().setTypeface("Frutiger for ZKB Light");
                    major.getCs().setTypeface("Frutiger for ZKB Light");
                    
                    // Minor Font (Body)
                    CTFontCollection minor = fontScheme.getMinorFont();
                    minor.getLatin().setTypeface("Frutiger for ZKB Light");
                    minor.getEa().setTypeface("Frutiger for ZKB Light");
                    minor.getCs().setTypeface("Frutiger for ZKB Light");
                    
                    System.out.println("Updated Theme fonts to Frutiger for ZKB Light");
                }
                
                // 2. Fix Master and Layouts slide numbers
                fixSlideNumbers(master);
                for (XSLFSlideLayout layout : master.getSlideLayouts()) {
                    fixSlideNumbers(layout);
                }
            }

            // 3. Fix existing slides if any
            for (XSLFSlide slide : ppt.getSlides()) {
                fixSlideNumbers(slide);
            }

            try (FileOutputStream out = new FileOutputStream(file)) {
                ppt.write(out);
            }
            System.out.println("Template updated successfully.");
        }
    }

    private static void fixSlideNumbers(XSLFSheet sheet) {
        XSLFTextShape snp = null;
        for (XSLFShape shape : sheet.getShapes()) {
            if (shape instanceof XSLFTextShape) {
                XSLFTextShape ts = (XSLFTextShape) shape;
                if (ts.getPlaceholder() == Placeholder.SLIDE_NUMBER || (ts.getText() != null && ts.getText().contains("<#>"))) {
                    snp = ts;
                    break;
                }
            }
        }

        if (snp != null) {
            System.out.println("Fixing slide number in " + sheet.getClass().getSimpleName());
            
            // Clear existing runs and ensure one run with the correct font and field
            snp.clearText();
            XSLFTextParagraph p = snp.addNewTextParagraph();
            
            CTTextParagraph ctP = p.getXmlObject();
            CTTextField ctF = ctP.addNewFld();
            ctF.setId("{6682B14C-E45B-4C21-A882-968B35397453}");
            ctF.setType("slidenum");
            ctF.setT("<#>");
            
            CTTextCharacterProperties ctRPr = ctF.addNewRPr();
            ctRPr.setLang("en-US");
            ctRPr.setSz(900);
            ctRPr.setSmtClean(false);
            ctRPr.setDirty(false);
            CTTextFont latinFont = ctRPr.addNewLatin();
            latinFont.setTypeface("Frutiger for ZKB Light");
        }
    }
}
