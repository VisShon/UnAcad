package edu.univ.erp.api.reports;

import edu.univ.erp.api.common.APIResponse;
import edu.univ.erp.util.CSVExporter;
import edu.univ.erp.util.PDFExporter;

public class ReportAPI {

    public static APIResponse<String> transcriptPdf(int studentId) {
        try {
            String pdfPath = PDFExporter.exportTranscript(studentId);
            return APIResponse.<String>success("Transcript generated").withData(pdfPath);
        } catch (Exception e) {
            return APIResponse.error("Failed to generate transcript: " + e.getMessage());
        }
    }

    public static APIResponse<String> classListCsv(int sectionId) {
        try {
            String csvPath = CSVExporter.exportClassList(sectionId);
            return APIResponse.<String>success("Class list exported").withData(csvPath);
        } catch (Exception e) {
            return APIResponse.error("Failed to export class list: " + e.getMessage());
        }
    }
}