package ch.goodone.angularai.presentation;

import org.apache.poi.sl.usermodel.Placeholder;
import org.apache.poi.xslf.usermodel.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.awt.geom.Rectangle2D;
import java.awt.Color;

public class CompanyStyleAligner {
    private static final Color ZKB_BLUE = new Color(0, 60, 211); // #003cd3

    public static void main(String[] args) throws Exception {
        String templatePath = "../doc/history/presentations/template.pptx";
        File file = new File(templatePath);
        if (!file.exists()) {
            System.out.println("File not found: " + templatePath);
            return;
        }

        try (XMLSlideShow ppt = new XMLSlideShow(new FileInputStream(file))) {
            for (XSLFSlideMaster master : ppt.getSlideMasters()) {
                for (XSLFSlideLayout layout : master.getSlideLayouts()) {
                    if (layout.getType() == SlideLayout.TITLE_AND_CONTENT || layout.getType() == SlideLayout.TITLE_ONLY) {
                        alignLayout(layout);
                    }
                }
            }

            try (FileOutputStream out = new FileOutputStream(file)) {
                ppt.write(out);
            }
            System.out.println("Template aligned with company style successfully.");
        }
    }

    private static void alignLayout(XSLFSlideLayout layout) {
        System.out.println("Aligning layout: " + layout.getName());
        for (XSLFShape shape : layout.getShapes()) {
            if (shape instanceof XSLFTextShape) {
                XSLFTextShape ts = (XSLFTextShape) shape;
                Placeholder ph = ts.getPlaceholder();
                if (ph == null) continue;

                switch (ph) {
                    case TITLE:
                        ts.setAnchor(new Rectangle2D.Double(24, 23, 802, 68));
                        applyTextStyle(ts, ZKB_BLUE, 24.0);
                        break;
                    case CONTENT:
                    case BODY:
                        ts.setAnchor(new Rectangle2D.Double(24, 148, 802, 374));
                        applyTextStyle(ts, Color.BLACK, 18.0);
                        break;
                    case SLIDE_NUMBER:
                        ts.setAnchor(new Rectangle2D.Double(24, 535, 100, 20));
                        applyTextStyle(ts, ZKB_BLUE, 9.0);
                        break;
                    case FOOTER:
                        ts.setAnchor(new Rectangle2D.Double(150, 535, 600, 20));
                        applyTextStyle(ts, Color.GRAY, 9.0);
                        break;
                }
            }
        }
    }

    private static void applyTextStyle(XSLFTextShape ts, Color color, double fontSize) {
        if (ts.getTextParagraphs().isEmpty()) {
            ts.addNewTextParagraph();
        }
        for (XSLFTextParagraph p : ts.getTextParagraphs()) {
            if (p.getTextRuns().isEmpty()) {
                p.addNewTextRun();
            }
            for (XSLFTextRun r : p.getTextRuns()) {
                r.setFontColor(color);
                r.setFontSize(fontSize);
                r.setFontFamily("Frutiger for ZKB Light");
            }
        }
    }
}
