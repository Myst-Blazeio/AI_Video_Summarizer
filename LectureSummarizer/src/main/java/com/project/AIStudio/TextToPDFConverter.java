package com.project.AIStudio;

import java.io.File;
import java.io.IOException;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;

public class TextToPDFConverter {

    public void createPdf(String heading, String body, String filePath) throws IOException {
        File file = new File(filePath);
        file.getParentFile().mkdirs();

        PdfWriter writer = new PdfWriter(filePath);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Using built-in fonts for compatibility
        PdfFont normalFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        // 🔹 Heading (Bold, Larger Font)
        Paragraph headingParagraph = new Paragraph(heading)
                .setFont(boldFont)
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER);

        // 🔹 Body Text (Regular Font, Smaller Size)
        Paragraph bodyParagraph = new Paragraph(body)
                .setFont(normalFont)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.JUSTIFIED);

        // Add elements to document
        document.add(headingParagraph);
        document.add(bodyParagraph);

        document.close();
    }
}
