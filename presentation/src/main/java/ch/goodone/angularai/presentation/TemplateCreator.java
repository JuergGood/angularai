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
     *   - Note: The footer text in the company template is usually empty or contains generic metadata.
     */

    public static void main(String[] args) throws Exception {
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
            
            // 1. Force Theme Fonts to Frutiger
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

            // Re-assign IDs globally before writing
            FinalResultFixer.validatePackageIntegrity(ppt);

            // Write to file
            try (FileOutputStream out = new FileOutputStream(file)) {
                ppt.write(out);
            }
            ppt.close();
            System.out.println("Template created successfully at " + outputPath);
        } catch (Exception e) {
            System.err.println("Error creating template: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createTitleSlideLayout(XSLFSlideMaster master) {
        XSLFSlideLayout layout = null;
        for (XSLFSlideLayout l : master.getSlideLayouts()) {
            if (l.getType() == SlideLayout.TITLE) { // "TITLE" is usually Layout 0 in reference.pptx
                layout = l;
                break;
            }
        }
        if (layout == null) return;
        
        System.out.println("Updating Title Slide layout...");
        for (XSLFShape s : layout.getShapes()) {
            if (s instanceof XSLFTextShape) {
                XSLFTextShape ts = (XSLFTextShape) s;
                Placeholder ph = ts.getPlaceholder();
                if (ph == null) continue;
                
                if (ph == Placeholder.CENTERED_TITLE || ph == Placeholder.TITLE) {
                    ts.setAnchor(new Rectangle2D.Double(24, 148, 715, 374));
                    applyStyle(ts, ZKB_BLUE, 36.0, true);
                } else if (ph == Placeholder.SUBTITLE) {
                    ts.setAnchor(new Rectangle2D.Double(24, 23, 715, 20));
                    applyStyle(ts, ZKB_BLUE, 14.0, false);
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
                    applyStyle(ts, ZKB_BLUE, 24.0, true);
                } else if (ph == Placeholder.BODY || ph == Placeholder.CONTENT) {
                    ts.setAnchor(new Rectangle2D.Double(24, 148, 802, 374));
                    applyStyle(ts, Color.BLACK, 18.0, false);
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
                    applyStyle(ts, ZKB_BLUE, 24.0, true);
                } else if (ph == Placeholder.BODY || ph == Placeholder.CONTENT) {
                    if (contentIdx == 0) {
                        ts.setAnchor(new Rectangle2D.Double(24, 148, 380, 374));
                        contentIdx++;
                    } else {
                        ts.setAnchor(new Rectangle2D.Double(446, 148, 380, 374));
                    }
                    applyStyle(ts, Color.BLACK, 18.0, false);
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
        footer.setText("AngularAI - Softwareentwicklung mit AI");
        applyStyle(footer, Color.GRAY, 9.0, false);
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
