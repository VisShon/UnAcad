package edu.univ.erp.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfWriter;
import edu.univ.erp.data.GradesDAO;

import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

public class PDFExporter {

    public static String exportTranscript(int studentId) throws Exception {
        String fileName = "transcript_" + studentId + ".pdf";
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(fileName));
        document.open();

        document.add(new Paragraph("Student Transcript"));
        document.add(new Paragraph("Student ID: " + studentId));
        document.add(new Paragraph(" ")); // Add some space before the table

        // Create a table with 4 columns: Course, Component, Score, Final Grade
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100); // Set table width to 100%
        table.setWidths(new float[]{2, 2, 1, 1}); // Set column widths proportionally

        // Add header row
        table.addCell(new PdfPCell(new Phrase("Course")));
        table.addCell(new PdfPCell(new Phrase("Component")));
        table.addCell(new PdfPCell(new Phrase("Score")));
        table.addCell(new PdfPCell(new Phrase("Final Grade")));

        // Add data rows
        List<Map<String, Object>> grades = GradesDAO.getGradesForStudent(studentId);
        for (Map<String, Object> g : grades) {
            table.addCell(new PdfPCell(new Phrase(String.valueOf(g.get("code")))));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(g.get("component")))));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(g.get("score")))));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(g.get("final_grade")))));
        }

        document.add(table);

        document.close();
        return fileName;
    }
}