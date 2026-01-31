package ch.goodone.angularai.presentation;

import org.apache.poi.sl.usermodel.Placeholder;
import org.apache.poi.xslf.usermodel.*;
import org.openxmlformats.schemas.drawingml.x2006.main.*;
import org.openxmlformats.schemas.presentationml.x2006.main.*;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.FileOutputStream;
import java.io.File;

public class TemplateCreator {
    private static final Color ZKB_BLUE = new Color(0, 60, 211); // #003cd3

    /* 
     * REFERENCES to template-company.pptx:
     * - Master Fonts: [Master -> Theme -> FontScheme -> Major/Minor: "Frutiger for ZKB Light"]
     * - Title Slide: [Layout: "Titelfolie weiss" (TITLE_ONLY)]
     *   - Title: [Shape: "Titel 1", ID: 2, PH Type: title, Anchor: (24.08, 148.44, 715, 373.9)]
     *   - Subtitle: [Shape: "Untertitel 2", ID: 3, PH Type: subTitle, Anchor: (24.08, 22.66, 715, 67.9)]
     *   - Background Image: [Shape: "Bildplatzhalter 1", ID: 4, Anchor: (0, 0, 720, 540)]
     * - Title and Content: [Layout: "Titel und Inhalt" (TITLE_ONLY)]
     *   - Title: [Shape: "Titel 1", ID: 2, PH Type: title, Anchor: (24.08, 22.66, 801.7, 67.9)]
     *   - Content: [Shape: "Textplatzhalter 4", ID: 5, PH Type: body, Anchor: (24.08, 148.44, 801.7, 373.9)]
     * - Two Content: [Layout: "2 Spalten" (TITLE_ONLY)]
     *   - Title: [Shape: "Titel 1", ID: 2, PH Type: title, Anchor: (24.08, 22.66, 801.7, 67.9)]
     *   - Content 1: [Shape: "Inhaltsplatzhalter 4", ID: 5, PH Type: obj, Anchor: (24.08, 148.44, 470.7, 373.9)]
     *   - Content 2: [Shape: "Inhaltsplatzhalter 5", ID: 6, PH Type: obj, Anchor: (517.45, 148.44, 470.7, 373.9)]
     * - Slide Number: [PH Type: sldNum, ID: 2, Anchor: (24.08, 535.41, 24.08, 15.01)]
     *   - Found in "template-company.pptx" -> Slide Master -> Shape: "Foliennummernplatzhalter 2"
     *   - Character Properties: [Font: "Frutiger for ZKB Light", Size: 9pt, Color: ZKB Blue (#003cd3)]
     * - Footer: [PH Type: ftr, ID: 3, Anchor: (48.16, 535.41, 777.62, 15.01)]
     *   - Found in "template-company.pptx" -> Slide Master -> Shape: "Fuzeilenplatzhalter 3"
     *   - Character Properties: [Font: "Frutiger for ZKB Light", Size: 9pt, Color: Grey (#808080)]
     *   - Note: The footer text in the company template is usually empty or contains generic metadata.
     */

    public static void main(String[] args) throws Exception {
        org.apache.poi.util.IOUtils.setByteArrayMaxOverride(200000000); // Allow up to 200MB
        String outputPath = "../doc/history/presentations/template.pptx";
        String referencePath = "../doc/history/presentations/reference.pptx";
        File file = new File(outputPath);
        File refFile = new File(referencePath);
        
        XMLSlideShow ppt;
        if (refFile.exists()) {
            System.out.println("Using reference.pptx as base...");
            ppt = new XMLSlideShow(new java.io.FileInputStream(refFile));
        } else {
            System.out.println("reference.pptx not found, creating from scratch (Pandoc might fail)...");
            ppt = new XMLSlideShow();
        }

        try {
            // Setup Master
            XSLFSlideMaster master = ppt.getSlideMasters().get(0);
            
            // Clean all existing slides from reference.pptx to start fresh
            System.out.println("Cleaning existing slides in main pass: " + ppt.getSlides().size());
            while (ppt.getSlides().size() > 0) {
                ppt.removeSlide(0);
            }
            
            // Clean up notes slides and other residual parts
            for (XSLFSlideMaster m : ppt.getSlideMasters()) {
                // We can't easily remove notes masters via POI high-level API, 
                // but we can ensure they are not used.
            }

            // Save once to apply slide removal before doing anything else
            try (FileOutputStream out = new FileOutputStream(file)) {
                ppt.write(out);
            }
            
            // Re-open fresh
            ppt.close();
            ppt = new XMLSlideShow(new java.io.FileInputStream(file));
            master = ppt.getSlideMasters().get(0);
            XSLFTheme theme = master.getTheme();
            if (theme != null) {
                CTOfficeStyleSheet styleSheet = theme.getXmlObject();
                CTBaseStyles themeElements = styleSheet.getThemeElements();
                CTFontScheme fontScheme = themeElements.getFontScheme();
                
                CTFontCollection major = fontScheme.getMajorFont();
                major.getLatin().setTypeface("Frutiger for ZKB Light");
                major.getEa().setTypeface("Frutiger for ZKB Light");
                major.getCs().setTypeface("Frutiger for ZKB Light");
                
                CTFontCollection minor = fontScheme.getMinorFont();
                minor.getLatin().setTypeface("Frutiger for ZKB Light");
                minor.getEa().setTypeface("Frutiger for ZKB Light");
                minor.getCs().setTypeface("Frutiger for ZKB Light");
            }

            // Create Layouts
            createTitleSlideLayout(master);
            createTitleAndContentLayout(master);
            createTwoContentLayout(master);

            // Add Footer and Slide Number to all layouts (optional, usually done in Master)
            for (XSLFSlideLayout layout : master.getSlideLayouts()) {
                addFooterAndSlideNumber(layout);
            }

            // Re-assign IDs globally before writing
            FinalResultFixer.validatePackageIntegrity(ppt);

            // Write to file
            try (FileOutputStream out = new FileOutputStream(file)) {
                ppt.write(out);
            }
            ppt.close();
            
            // Explicitly delete any residual slide files if possible or just use a fresh empty slideshow
            // if reference.pptx existed, it might have slides.
            
            // Re-open and remove all slides to ensure we have a clean reference-only template
            try (XMLSlideShow ppt2 = new XMLSlideShow(new java.io.FileInputStream(file))) {
                int count = ppt2.getSlides().size();
                System.out.println("Cleaning " + count + " slides from template...");
                if (count > 0) {
                    for (int i = count - 1; i >= 0; i--) {
                        ppt2.removeSlide(i);
                    }
                    try (FileOutputStream out = new FileOutputStream(file)) {
                        ppt2.write(out);
                    }
                }
            }
            
            // SECOND PASS: ensure NO slides exist
            try (XMLSlideShow ppt3 = new XMLSlideShow(new java.io.FileInputStream(file))) {
                if (ppt3.getSlides().size() > 0) {
                     System.err.println("CRITICAL: Template still contains " + ppt3.getSlides().size() + " slides after cleanup!");
                     while (ppt3.getSlides().size() > 0) {
                        ppt3.removeSlide(0);
                    }
                    try (FileOutputStream out = new FileOutputStream(file)) {
                        ppt3.write(out);
                    }
                }
            }
            
            // THIRD PASS: Final check
            try (XMLSlideShow ppt4 = new XMLSlideShow(new java.io.FileInputStream(file))) {
                System.out.println("Final slide count in template: " + ppt4.getSlides().size());
            }

            // Also repair reference.pptx if it exists and we used it
            if (refFile.exists()) {
                System.out.println("Repairing reference.pptx integrity...");
                try (XMLSlideShow refPpt = new XMLSlideShow(new java.io.FileInputStream(refFile))) {
                    FinalResultFixer.validatePackageIntegrity(refPpt);
                    try (FileOutputStream out = new FileOutputStream(refFile)) {
                        refPpt.write(out);
                    }
                }
            }

            System.out.println("Template created successfully at " + outputPath);
        } catch (Exception e) {
            System.err.println("Error creating template: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createTitleSlideLayout(XSLFSlideMaster master) {
        XSLFSlideLayout layout = null;
        for (XSLFSlideLayout l : master.getSlideLayouts()) {
            if (l.getType() == SlideLayout.TITLE) {
                layout = l;
                break;
            }
        }
        if (layout == null) return;
        
        System.out.println("Updating Title Slide layout...");
        
        // Background picture for Title Slide (referenced from template-company.pptx)
        try {
            File bgFile = new File("presentations/files/images/AiRace.png");
            if (!bgFile.exists()) {
                // Try relative to current dir if run from presentation dir
                bgFile = new File("files/images/AiRace.png");
            }
            if (!bgFile.exists()) {
                // Try relative to project root
                bgFile = new File("presentation/presentations/files/images/AiRace.png");
            }
            
            if (bgFile.exists()) {
                System.out.println("Adding background picture: " + bgFile.getAbsolutePath());
                byte[] pictureData = java.nio.file.Files.readAllBytes(bgFile.toPath());
                XSLFPictureData pd = layout.getSlideShow().addPicture(pictureData, XSLFPictureData.PictureType.PNG);
                XSLFPictureShape pic = layout.createPicture(pd);
                pic.setAnchor(new Rectangle2D.Double(0, 0, 720, 540));
                
                // For layouts, we can't easily set child order to back via high-level API
                // but usually the first created shape is at the bottom.
            } else {
                System.err.println("Background file not found: " + bgFile.getAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("Could not add background to Title Slide: " + e.getMessage());
            e.printStackTrace();
        }

        for (XSLFShape s : layout.getShapes()) {
            if (s instanceof XSLFTextShape) {
                XSLFTextShape ts = (XSLFTextShape) s;
                Placeholder ph = ts.getPlaceholder();
                if (ph == null) continue;
                
                if (ph == Placeholder.CENTERED_TITLE || ph == Placeholder.TITLE) {
                    ts.setAnchor(new Rectangle2D.Double(24, 148, 715, 374));
                    ts.clearText();
                    XSLFTextParagraph p = ts.addNewTextParagraph();
                    XSLFTextRun r = p.addNewTextRun();
                    r.setFontFamily("Frutiger for ZKB Light");
                    r.setFontSize(36.0);
                    r.setFontColor(ZKB_BLUE);
                    r.setBold(true);
                    r.setText(""); 
                } else if (ph == Placeholder.SUBTITLE) {
                    ts.setAnchor(new Rectangle2D.Double(24, 23, 715, 20));
                    ts.clearText();
                    XSLFTextParagraph p = ts.addNewTextParagraph();
                    XSLFTextRun r = p.addNewTextRun();
                    r.setFontFamily("Frutiger for ZKB Light");
                    r.setFontSize(14.0);
                    r.setFontColor(ZKB_BLUE);
                    r.setText(""); 
                }
            }
        }
    }

    private static void createTitleAndContentLayout(XSLFSlideMaster master) {
        XSLFSlideLayout layout = null;
        for (XSLFSlideLayout l : master.getSlideLayouts()) {
            if (l.getType() == SlideLayout.TITLE_AND_CONTENT) {
                layout = l;
                break;
            }
        }
        if (layout == null) return;

        System.out.println("Updating Title and Content layout...");
        for (XSLFShape s : layout.getShapes()) {
            if (s instanceof XSLFTextShape) {
                XSLFTextShape ts = (XSLFTextShape) s;
                Placeholder ph = ts.getPlaceholder();
                if (ph == null) continue;
                
                if (ph == Placeholder.TITLE) {
                    ts.setAnchor(new Rectangle2D.Double(24, 23, 802, 68));
                    ts.clearText();
                    XSLFTextParagraph p = ts.addNewTextParagraph();
                    XSLFTextRun r = p.addNewTextRun();
                    r.setFontFamily("Frutiger for ZKB Light");
                    r.setFontSize(24.0);
                    r.setFontColor(ZKB_BLUE);
                    r.setBold(true);
                    r.setText(""); 
                } else if (ph == Placeholder.BODY || ph == Placeholder.CONTENT) {
                    ts.setAnchor(new Rectangle2D.Double(24, 148, 802, 374));
                    ts.clearText();
                    XSLFTextParagraph p = ts.addNewTextParagraph();
                    XSLFTextRun r = p.addNewTextRun();
                    r.setFontFamily("Frutiger for ZKB Light");
                    r.setFontSize(18.0);
                    r.setFontColor(Color.BLACK);
                    r.setText(""); 
                } else if (ph == Placeholder.SLIDE_NUMBER) {
                    ts.setAnchor(new Rectangle2D.Double(24, 535, 100, 20));
                    applyStyle(ts, ZKB_BLUE, 9.0, false);
                }
            }
        }
    }

    private static void createTwoContentLayout(XSLFSlideMaster master) {
        XSLFSlideLayout layout = null;
        for (XSLFSlideLayout l : master.getSlideLayouts()) {
            if (l.getType() == SlideLayout.TWO_OBJ) {
                layout = l;
                break;
            }
        }
        if (layout == null) return;

        System.out.println("Updating Two Content layout...");
        int contentIdx = 0;
        for (XSLFShape s : layout.getShapes()) {
            if (s instanceof XSLFTextShape) {
                XSLFTextShape ts = (XSLFTextShape) s;
                Placeholder ph = ts.getPlaceholder();
                if (ph == null) continue;
                
                if (ph == Placeholder.TITLE) {
                    ts.setAnchor(new Rectangle2D.Double(24, 23, 802, 68));
                    ts.clearText();
                    XSLFTextParagraph p = ts.addNewTextParagraph();
                    XSLFTextRun r = p.addNewTextRun();
                    r.setFontFamily("Frutiger for ZKB Light");
                    r.setFontSize(24.0);
                    r.setFontColor(ZKB_BLUE);
                    r.setBold(true);
                    r.setText(""); 
                } else if (ph == Placeholder.BODY || ph == Placeholder.CONTENT) {
                    if (contentIdx == 0) {
                        ts.setAnchor(new Rectangle2D.Double(24, 148, 380, 374));
                        contentIdx++;
                    } else {
                        ts.setAnchor(new Rectangle2D.Double(446, 148, 380, 374));
                    }
                    ts.clearText();
                    XSLFTextParagraph p = ts.addNewTextParagraph();
                    XSLFTextRun r = p.addNewTextRun();
                    r.setFontFamily("Frutiger for ZKB Light");
                    r.setFontSize(18.0);
                    r.setFontColor(Color.BLACK);
                    r.setText(""); 
                } else if (ph == Placeholder.SLIDE_NUMBER) {
                    ts.setAnchor(new Rectangle2D.Double(24, 535, 100, 20));
                    applyStyle(ts, ZKB_BLUE, 9.0, false);
                }
            }
        }
    }

    private static void addFooterAndSlideNumber(XSLFSheet sheet) {
        // Slide Number
        XSLFTextShape sn = sheet.createAutoShape();
        sn.setPlaceholder(Placeholder.SLIDE_NUMBER);
        sn.setAnchor(new Rectangle2D.Double(24, 535, 100, 20));
        
        sn.clearText();
        XSLFTextParagraph p = sn.addNewTextParagraph();
        CTTextParagraph ctP = p.getXmlObject();
        CTTextField ctF = ctP.addNewFld();
        ctF.setType("slidenum");
        ctF.setT("<#>");
        CTTextCharacterProperties rPr = ctF.addNewRPr();
        rPr.setSz(900);
        rPr.addNewLatin().setTypeface("Frutiger for ZKB Light");
        CTSolidColorFillProperties fill = rPr.addNewSolidFill();
        fill.addNewSrgbClr().setVal(new byte[]{(byte)0, (byte)60, (byte)211});

        // Footer
        XSLFTextShape footer = sheet.createAutoShape();
        footer.setPlaceholder(Placeholder.FOOTER);
        footer.setAnchor(new Rectangle2D.Double(150, 535, 600, 20));
        footer.clearText(); // Ensure no dummy text
        XSLFTextParagraph fp = footer.addNewTextParagraph();
        XSLFTextRun fr = fp.addNewTextRun();
        fr.setFontFamily("Frutiger for ZKB Light");
        fr.setFontSize(9.0);
        fr.setFontColor(Color.GRAY);
        fr.setText(""); // Keep it empty for Pandoc/Generator to fill
    }

    private static void applyStyle(XSLFTextShape ts, Color color, double fontSize, boolean bold) {
        ts.clearText();
        XSLFTextParagraph p = ts.addNewTextParagraph();
        XSLFTextRun r = p.addNewTextRun();
        r.setFontColor(color);
        r.setFontSize(fontSize);
        r.setFontFamily("Frutiger for ZKB Light");
        r.setBold(bold);
    }
}
